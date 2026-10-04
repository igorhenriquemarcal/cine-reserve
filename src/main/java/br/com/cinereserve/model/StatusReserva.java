package br.com.cinereserve.model;

/**
 * Representa os estados possíveis de uma reserva no seu ciclo de vida.
 */
public enum StatusReserva {
    CRIADA("Criada / Pendente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusReserva(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
