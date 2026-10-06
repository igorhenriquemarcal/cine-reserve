package br.com.cinereserve.exception;

/**
 * Exceção base para violações de regras de negócio do CineReserve.
 */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String message) {
        super(message);
    }
}
