package br.com.cinereserve.model;

import java.util.Objects;

/**
 * Representa um assento físico em uma sala de cinema.
 * O assento físico é imutável: sua identificação física (fila, número) e tipo não mudam.
 * O estado de ocupação pertence à Sessao, não ao assento isolado.
 */
public class Assento {

    private final String codigo;
    private final char fila;
    private final int numero;
    private final TipoAssento tipo;

    public Assento(char fila, int numero, TipoAssento tipo) {
        char filaNormalizada = Character.toUpperCase(fila);
        if (filaNormalizada < 'A' || filaNormalizada > 'Z') {
            throw new IllegalArgumentException("A fila do assento deve ser uma letra entre A e Z.");
        }
        if (numero <= 0) {
            throw new IllegalArgumentException("O número do assento deve ser maior que zero.");
        }
        this.fila = filaNormalizada;
        this.numero = numero;
        this.tipo = Objects.requireNonNull(tipo, "O tipo do assento não pode ser nulo.");
        this.codigo = String.format("%c%d", this.fila, this.numero);
    }

    public String getCodigo() {
        return codigo;
    }

    public char getFila() {
        return fila;
    }

    public int getNumero() {
        return numero;
    }

    public TipoAssento getTipo() {
        return tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assento assento = (Assento) o;
        return Objects.equals(codigo, assento.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return "Assento{" +
                "codigo='" + codigo + '\'' +
                ", tipo=" + tipo +
                '}';
    }
}
