package br.com.cinereserve.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do Modelo de Domínio - CineReserve")
class DominioTest {

    @Test
    @DisplayName("Deve criar assento com código formatado corretamente")
    void deveCriarAssentoCorretamente() {
        Assento assento = new Assento('a', 5, TipoAssento.VIP);

        assertEquals("A5", assento.getCodigo());
        assertEquals('A', assento.getFila());
        assertEquals(5, assento.getNumero());
        assertEquals(TipoAssento.VIP, assento.getTipo());
    }

    @Test
    @DisplayName("Deve falhar ao criar assento com fila ou número inválidos")
    void deveFalharAssentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Assento('1', 5, TipoAssento.COMUM));
        assertThrows(IllegalArgumentException.class, () -> new Assento('A', 0, TipoAssento.COMUM));
        assertThrows(NullPointerException.class, () -> new Assento('A', 1, null));
    }

    @Test
    @DisplayName("Deve gerenciar ocupação e disponibilidade de assentos na Sessao")
    void deveGerenciarOcupacaoDeAssentosNaSessao() {
        Assento a1 = new Assento('A', 1, TipoAssento.COMUM);
        Assento a2 = new Assento('A', 2, TipoAssento.VIP);
        Sessao sessao = new Sessao("Interestelar", LocalDateTime.now().plusDays(1), new BigDecimal("30.00"), List.of(a1, a2));

        assertTrue(sessao.estaDisponivel("A1"));
        assertTrue(sessao.estaDisponivel("A2"));
        assertEquals(2, sessao.getAssentosDisponiveisCount());

        sessao.ocuparAssento("A1");
        assertFalse(sessao.estaDisponivel("A1"));
        assertEquals(1, sessao.getAssentosDisponiveisCount());

        // Tentar ocupar o mesmo assento novamente deve lançar exceção
        assertThrows(IllegalStateException.class, () -> sessao.ocuparAssento("A1"));

        // Liberar assento
        sessao.liberarAssento("A1");
        assertTrue(sessao.estaDisponivel("A1"));
        assertEquals(2, sessao.getAssentosDisponiveisCount());
    }

    @Test
    @DisplayName("Deve calcular preço do assento de acordo com o tipo")
    void deveCalcularPrecoPorTipoAssento() {
        Assento comum = new Assento('A', 1, TipoAssento.COMUM);
        Assento vip = new Assento('A', 2, TipoAssento.VIP);
        Sessao sessao = new Sessao("Oppenheimer", LocalDateTime.now().plusDays(2), new BigDecimal("40.00"), List.of(comum, vip));

        // COMUM: 40.00 * 1.0 = 40.00
        assertEquals(new BigDecimal("40.00"), sessao.calcularPrecoAssento(comum));

        // VIP: 40.00 * 1.5 = 60.00
        assertEquals(new BigDecimal("60.00"), sessao.calcularPrecoAssento(vip));
    }

    @Test
    @DisplayName("Deve criar reserva com valor total correto e alterar status")
    void deveCriarECancelarReserva() {
        Cliente cliente = new Cliente("Igor", "igor@exemplo.com", "123.456.789-00");
        Assento a1 = new Assento('B', 1, TipoAssento.COMUM);
        Assento a2 = new Assento('B', 2, TipoAssento.VIP);
        Sessao sessao = new Sessao("Duna: Parte 2", LocalDateTime.now().plusDays(3), new BigDecimal("20.00"), List.of(a1, a2));

        Reserva reserva = new Reserva(cliente, sessao, List.of(a1, a2));

        // 20.00 (comum) + 30.00 (vip) = 50.00
        assertEquals(new BigDecimal("50.00"), reserva.getValorTotal());
        assertEquals(StatusReserva.CRIADA, reserva.getStatus());

        reserva.confirmar();
        assertEquals(StatusReserva.CONFIRMADA, reserva.getStatus());

        reserva.cancelar();
        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());

        assertThrows(IllegalStateException.class, reserva::confirmar);
    }
}
