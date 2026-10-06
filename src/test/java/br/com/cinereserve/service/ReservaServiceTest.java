package br.com.cinereserve.service;

import br.com.cinereserve.exception.AssentoIndisponivelException;
import br.com.cinereserve.exception.RegraDeNegocioException;
import br.com.cinereserve.model.*;
import br.com.cinereserve.repository.json.ClienteJsonRepository;
import br.com.cinereserve.repository.json.ReservaJsonRepository;
import br.com.cinereserve.repository.json.SessaoJsonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Negócio e Concorrência do ReservaService")
class ReservaServiceTest {

    @TempDir
    Path tempDir;

    private ClienteJsonRepository clienteRepository;
    private SessaoJsonRepository sessaoRepository;
    private ReservaJsonRepository reservaRepository;
    private ReservaService reservaService;

    private Cliente cliente1;
    private Cliente cliente2;
    private Sessao sessao;

    @BeforeEach
    void setUp() {
        clienteRepository = new ClienteJsonRepository(tempDir.resolve("clientes.json"));
        sessaoRepository = new SessaoJsonRepository(tempDir.resolve("sessoes.json"));
        reservaRepository = new ReservaJsonRepository(tempDir.resolve("reservas.json"), clienteRepository, sessaoRepository);
        reservaService = new ReservaService(clienteRepository, sessaoRepository, reservaRepository);

        cliente1 = new Cliente("Ana Lima", "ana@email.com", "111.222.333-44");
        cliente2 = new Cliente("Bruno Costa", "bruno@email.com", "555.666.777-88");
        clienteRepository.salvar(cliente1);
        clienteRepository.salvar(cliente2);

        Assento a1 = new Assento('A', 1, TipoAssento.COMUM);
        Assento a2 = new Assento('A', 2, TipoAssento.VIP);
        sessao = new Sessao("Inception", LocalDateTime.now().plusDays(2), new BigDecimal("25.00"), List.of(a1, a2));
        sessaoRepository.salvar(sessao);
    }

    @Test
    @DisplayName("Deve realizar reserva com sucesso e atualizar disponibilidade")
    void deveRealizarReservaComSucesso() {
        Reserva reserva = reservaService.realizarReserva(cliente1.getId(), sessao.getId(), List.of("A1"));

        assertNotNull(reserva.getId());
        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());
        assertEquals(new BigDecimal("25.00"), reserva.getValorTotal());

        Sessao sessaoAtualizada = sessaoRepository.buscarPorId(sessao.getId()).orElseThrow();
        assertFalse(sessaoAtualizada.estaDisponivel("A1"));
        assertTrue(sessaoAtualizada.estaDisponivel("A2"));
    }

    @Test
    @DisplayName("Deve cancelar reserva e liberar assento de volta para a sessão")
    void deveCancelarReservaELiberarAssento() {
        Reserva reserva = reservaService.realizarReserva(cliente1.getId(), sessao.getId(), List.of("A1"));
        assertFalse(sessaoRepository.buscarPorId(sessao.getId()).orElseThrow().estaDisponivel("A1"));

        reservaService.cancelarReserva(reserva.getId());

        Reserva reservaCancelada = reservaRepository.buscarPorId(reserva.getId()).orElseThrow();
        assertEquals(StatusReserva.CANCELADA, reservaCancelada.getStatus());

        Sessao sessaoAtualizada = sessaoRepository.buscarPorId(sessao.getId()).orElseThrow();
        assertTrue(sessaoAtualizada.estaDisponivel("A1"));
    }

    @Test
    @DisplayName("Deve falhar ao tentar reservar assento já ocupado")
    void deveFalharAssentoOcupado() {
        reservaService.realizarReserva(cliente1.getId(), sessao.getId(), List.of("A1"));

        assertThrows(AssentoIndisponivelException.class, () ->
                reservaService.realizarReserva(cliente2.getId(), sessao.getId(), List.of("A1"))
        );
    }

    @Test
    @DisplayName("Deve garantir concorrência segura com múltiplas threads disputando o mesmo assento")
    void deveGarantirConcorrenciaEmDisputaDeAssento() throws InterruptedException {
        int numeroThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numeroThreads);
        CountDownLatch latchInicio = new CountDownLatch(1);
        CountDownLatch latchFim = new CountDownLatch(numeroThreads);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        for (int i = 0; i < numeroThreads; i++) {
            Cliente cliente = new Cliente("Cliente " + i, "cliente" + i + "@email.com", "000.000.00" + i + "-00");
            clienteRepository.salvar(cliente);

            executor.submit(() -> {
                try {
                    latchInicio.await(); // Aguarda todas as threads estarem prontas para disparo simultâneo
                    reservaService.realizarReserva(cliente.getId(), sessao.getId(), List.of("A1"));
                    sucessos.incrementAndGet();
                } catch (AssentoIndisponivelException e) {
                    falhas.incrementAndGet();
                } catch (Exception e) {
                    fail("Erro inesperado durante teste de concorrência: " + e.getMessage());
                } finally {
                    latchFim.countDown();
                }
            });
        }

        latchInicio.countDown(); // Dispara todas ao mesmo tempo
        assertTrue(latchFim.await(5, TimeUnit.SECONDS));
        executor.shutdown();

        // EXATAMENTE 1 conseguiu a reserva e as outras 9 falharam
        assertEquals(1, sucessos.get());
        assertEquals(9, falhas.get());

        // Confirma integridade no repositório
        Sessao sessaoPersistida = sessaoRepository.buscarPorId(sessao.getId()).orElseThrow();
        assertFalse(sessaoPersistida.estaDisponivel("A1"));
        assertEquals(1, reservaRepository.buscarPorSessaoId(sessao.getId()).size());
    }
}
