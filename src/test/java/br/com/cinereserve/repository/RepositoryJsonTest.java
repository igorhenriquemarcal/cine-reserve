package br.com.cinereserve.repository;

import br.com.cinereserve.model.*;
import br.com.cinereserve.repository.json.ClienteJsonRepository;
import br.com.cinereserve.repository.json.ReservaJsonRepository;
import br.com.cinereserve.repository.json.SessaoJsonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Persistência JSON dos Repositórios")
class RepositoryJsonTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deve salvar, buscar e recarregar clientes via JSON")
    void devePersistirClientesEmJson() {
        Path arquivoClientes = tempDir.resolve("clientes.json");
        ClienteJsonRepository repo1 = new ClienteJsonRepository(arquivoClientes);

        Cliente cliente = new Cliente("Igor Marcal", "igor@email.com", "123.456.789-00");
        repo1.salvar(cliente);

        assertEquals(1, repo1.buscarTodos().size());
        assertTrue(repo1.buscarPorCpf("123.456.789-00").isPresent());
        assertTrue(repo1.buscarPorEmail("igor@email.com").isPresent());

        // Nova instância lendo do mesmo arquivo para validar a persistência em disco
        ClienteJsonRepository repo2 = new ClienteJsonRepository(arquivoClientes);
        Optional<Cliente> carregado = repo2.buscarPorId(cliente.getId());
        assertTrue(carregado.isPresent());
        assertEquals("Igor Marcal", carregado.get().getNome());
        assertEquals("123.456.789-00", carregado.get().getCpf());
    }

    @Test
    @DisplayName("Deve salvar e recarregar sessões preservando assentos ocupados")
    void devePersistirSessoesComAssentosOcupados() {
        Path arquivoSessoes = tempDir.resolve("sessoes.json");
        SessaoJsonRepository repo1 = new SessaoJsonRepository(arquivoSessoes);

        Assento a1 = new Assento('A', 1, TipoAssento.COMUM);
        Assento a2 = new Assento('A', 2, TipoAssento.VIP);
        Sessao sessao = new Sessao("Matrix", LocalDateTime.of(2026, 10, 10, 20, 0), new BigDecimal("35.00"), List.of(a1, a2));
        sessao.ocuparAssento("A1");

        repo1.salvar(sessao);

        // Recarrega em uma nova instância
        SessaoJsonRepository repo2 = new SessaoJsonRepository(arquivoSessoes);
        Optional<Sessao> sessaoCarregada = repo2.buscarPorId(sessao.getId());
        assertTrue(sessaoCarregada.isPresent());
        assertEquals("Matrix", sessaoCarregada.get().getTituloFilme());
        assertFalse(sessaoCarregada.get().estaDisponivel("A1"));
        assertTrue(sessaoCarregada.get().estaDisponivel("A2"));
    }

    @Test
    @DisplayName("Deve salvar e recarregar reservas reconstruindo as referências a Cliente e Sessão")
    void devePersistirEReconstruirReservas() {
        Path arqClientes = tempDir.resolve("clientes.json");
        Path arqSessoes = tempDir.resolve("sessoes.json");
        Path arqReservas = tempDir.resolve("reservas.json");

        ClienteJsonRepository clienteRepo = new ClienteJsonRepository(arqClientes);
        SessaoJsonRepository sessaoRepo = new SessaoJsonRepository(arqSessoes);

        Cliente cliente = new Cliente("Maria Silva", "maria@email.com", "987.654.321-99");
        clienteRepo.salvar(cliente);

        Assento a1 = new Assento('B', 1, TipoAssento.COMUM);
        Assento a2 = new Assento('B', 2, TipoAssento.VIP);
        Sessao sessao = new Sessao("Interestelar", LocalDateTime.of(2026, 10, 15, 19, 30), new BigDecimal("30.00"), List.of(a1, a2));
        sessao.ocuparAssento("B1");
        sessaoRepo.salvar(sessao);

        ReservaJsonRepository reservaRepo1 = new ReservaJsonRepository(arqReservas, clienteRepo, sessaoRepo);
        Reserva reserva = new Reserva(cliente, sessao, List.of(a1));
        reserva.confirmar();
        reservaRepo1.salvar(reserva);

        // Recarrega a reserva a partir do disco
        ReservaJsonRepository reservaRepo2 = new ReservaJsonRepository(arqReservas, clienteRepo, sessaoRepo);
        Optional<Reserva> reservaRecarregada = reservaRepo2.buscarPorId(reserva.getId());

        assertTrue(reservaRecarregada.isPresent());
        assertEquals(StatusReserva.CONFIRMADA, reservaRecarregada.get().getStatus());
        assertEquals(cliente.getId(), reservaRecarregada.get().getCliente().getId());
        assertEquals("Maria Silva", reservaRecarregada.get().getCliente().getNome());
        assertEquals("Interestelar", reservaRecarregada.get().getSessao().getTituloFilme());
        assertEquals(1, reservaRecarregada.get().getAssentos().size());
        assertEquals("B1", reservaRecarregada.get().getAssentos().get(0).getCodigo());
        assertEquals(new BigDecimal("30.00"), reservaRecarregada.get().getValorTotal());
    }
}
