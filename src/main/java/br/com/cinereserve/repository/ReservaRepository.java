package br.com.cinereserve.repository;

import br.com.cinereserve.model.Reserva;

import java.util.List;
import java.util.Optional;

/**
 * Contrato para operações de persistência e consulta de Reservas.
 */
public interface ReservaRepository {

    void salvar(Reserva reserva);

    Optional<Reserva> buscarPorId(String id);

    List<Reserva> buscarPorClienteId(String clienteId);

    List<Reserva> buscarPorSessaoId(String sessaoId);

    List<Reserva> buscarTodas();

    void remover(String id);
}
