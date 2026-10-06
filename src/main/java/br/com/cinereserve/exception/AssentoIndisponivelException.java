package br.com.cinereserve.exception;

/**
 * Lançada quando um ou mais assentos solicitados já estão ocupados ou não existem.
 */
public class AssentoIndisponivelException extends RegraDeNegocioException {
    public AssentoIndisponivelException(String message) {
        super(message);
    }
}
