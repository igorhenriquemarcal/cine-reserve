package br.com.cinereserve.repository;

import br.com.cinereserve.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Contrato para operações de persistência e consulta de Clientes.
 */
public interface ClienteRepository {

    void salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(String id);

    Optional<Cliente> buscarPorCpf(String cpf);

    Optional<Cliente> buscarPorEmail(String email);

    List<Cliente> buscarTodos();

    void remover(String id);
}
