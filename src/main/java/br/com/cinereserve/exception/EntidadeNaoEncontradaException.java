package br.com.cinereserve.exception;

/**
 * Lançada quando uma entidade requisitada não é localizada nos repositórios.
 */
public class EntidadeNaoEncontradaException extends RegraDeNegocioException {
    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }
}
