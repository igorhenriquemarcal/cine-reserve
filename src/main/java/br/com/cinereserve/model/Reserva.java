package br.com.cinereserve.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa a reserva realizada por um Cliente para um ou mais Assentos em uma Sessao.
 */
public class Reserva {

    private final String id;
    private final Cliente cliente;
    private final Sessao sessao;
    private final List<Assento> assentos;
    private final LocalDateTime dataHoraCriacao;
    private StatusReserva status;
    private final BigDecimal valorTotal;

    public Reserva(String id, Cliente cliente, Sessao sessao, List<Assento> assentos, LocalDateTime dataHoraCriacao, StatusReserva status) {
        this.cliente = Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
        this.sessao = Objects.requireNonNull(sessao, "Sessão não pode ser nula.");
        if (assentos == null || assentos.isEmpty()) {
            throw new IllegalArgumentException("A reserva deve conter pelo menos um assento.");
        }
        this.assentos = Collections.unmodifiableList(new ArrayList<>(assentos));
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        this.dataHoraCriacao = (dataHoraCriacao != null) ? dataHoraCriacao : LocalDateTime.now();
        this.status = (status != null) ? status : StatusReserva.CRIADA;
        this.valorTotal = calcularTotal();
    }

    public Reserva(Cliente cliente, Sessao sessao, List<Assento> assentos) {
        this(UUID.randomUUID().toString(), cliente, sessao, assentos, LocalDateTime.now(), StatusReserva.CRIADA);
    }

    private BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Assento a : this.assentos) {
            total = total.add(this.sessao.calcularPrecoAssento(a));
        }
        return total;
    }

    public void confirmar() {
        if (this.status == StatusReserva.CANCELADA) {
            throw new IllegalStateException("Não é possível confirmar uma reserva já cancelada.");
        }
        this.status = StatusReserva.CONFIRMADA;
    }

    public void cancelar() {
        if (this.status == StatusReserva.CANCELADA) {
            throw new IllegalStateException("A reserva já se encontra cancelada.");
        }
        this.status = StatusReserva.CANCELADA;
    }

    public String getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Sessao getSessao() {
        return sessao;
    }

    public List<Assento> getAssentos() {
        return assentos;
    }

    public LocalDateTime getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(id, reserva.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id='" + id + '\'' +
                ", cliente=" + cliente.getNome() +
                ", filme=" + sessao.getTituloFilme() +
                ", assentos=" + assentos.size() +
                ", status=" + status +
                ", valorTotal=" + valorTotal +
                '}';
    }
}
