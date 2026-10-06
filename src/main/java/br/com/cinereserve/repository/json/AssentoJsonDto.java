package br.com.cinereserve.repository.json;

import br.com.cinereserve.model.Assento;
import br.com.cinereserve.model.TipoAssento;

/**
 * DTO para serialização JSON de Assento.
 */
public class AssentoJsonDto {

    private char fila;
    private int numero;
    private TipoAssento tipo;

    public AssentoJsonDto() {
    }

    public AssentoJsonDto(Assento assento) {
        this.fila = assento.getFila();
        this.numero = assento.getNumero();
        this.tipo = assento.getTipo();
    }

    public Assento paraDominio() {
        return new Assento(this.fila, this.numero, this.tipo);
    }

    public char getFila() {
        return fila;
    }

    public void setFila(char fila) {
        this.fila = fila;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public TipoAssento getTipo() {
        return tipo;
    }

    public void setTipo(TipoAssento tipo) {
        this.tipo = tipo;
    }
}
