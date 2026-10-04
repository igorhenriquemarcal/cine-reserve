package br.com.cinereserve.model;

import java.math.BigDecimal;

/**
 * Representa as categorias físicas de assentos disponíveis no cinema
 * e seus respectivos multiplicadores sobre o preço base do ingresso.
 */
public enum TipoAssento {
    COMUM(new BigDecimal("1.0"), "Assento Comum"),
    VIP(new BigDecimal("1.5"), "Assento VIP"),
    PNE(new BigDecimal("1.0"), "Assento Acessível / PNE");

    private final BigDecimal multiplicadorPreco;
    private final String descricao;

    TipoAssento(BigDecimal multiplicadorPreco, String descricao) {
        this.multiplicadorPreco = multiplicadorPreco;
        this.descricao = descricao;
    }

    public BigDecimal getMultiplicadorPreco() {
        return multiplicadorPreco;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao + " (x" + multiplicadorPreco + ")";
    }
}
