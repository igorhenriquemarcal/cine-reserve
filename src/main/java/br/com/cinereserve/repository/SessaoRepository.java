package br.com.cinereserve.repository;

import br.com.cinereserve.model.Sessao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato para operações de persistência e consulta de Sessões de cinema.
 */
public interface SessaoRepository {

    void salvar(Sessao sessao);

    Optional<Sessao> buscarPorId(String id);

    List<Sessao> buscarTodas();

    List<Sessao> buscarPorFilme(String tituloFilme);

    void remover(String id);
}
