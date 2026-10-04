package br.com.cinereserve.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Representa uma sessão de exibição de filme em um determinado horário.
 * Gerencia a lista de assentos físicos da sala e o estado de ocupação de cada um nesta sessão.
 */
public class Sessao {

    private final String id;
    private final String tituloFilme;
    private final LocalDateTime horarioInicio;
    private final BigDecimal precoBase;
    private final Map<String, Assento> assentos;
    private final Set<String> assentosOcupados;

    public Sessao(String id, String tituloFilme, LocalDateTime horarioInicio, BigDecimal precoBase, List<Assento> assentosDaSala) {
        if (tituloFilme == null || tituloFilme.trim().isEmpty()) {
            throw new IllegalArgumentException("O título do filme não pode ser vazio.");
        }
        if (horarioInicio == null) {
            throw new IllegalArgumentException("O horário de início da sessão não pode ser nulo.");
        }
        if (precoBase == null || precoBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço base da sessão deve ser maior que zero.");
        }
        if (assentosDaSala == null || assentosDaSala.isEmpty()) {
            throw new IllegalArgumentException("A sala da sessão deve conter pelo menos um assento.");
        }

        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        this.tituloFilme = tituloFilme.trim();
        this.horarioInicio = horarioInicio;
        this.precoBase = precoBase.setScale(2, RoundingMode.HALF_UP);

        Map<String, Assento> mapaTemp = new LinkedHashMap<>();
        for (Assento a : assentosDaSala) {
            mapaTemp.put(a.getCodigo(), a);
        }
        this.assentos = Collections.unmodifiableMap(mapaTemp);
        this.assentosOcupados = new HashSet<>();
    }

    public Sessao(String tituloFilme, LocalDateTime horarioInicio, BigDecimal precoBase, List<Assento> assentosDaSala) {
        this(UUID.randomUUID().toString(), tituloFilme, horarioInicio, precoBase, assentosDaSala);
    }

    public String getId() {
        return id;
    }

    public String getTituloFilme() {
        return tituloFilme;
    }

    public LocalDateTime getHorarioInicio() {
        return horarioInicio;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public Map<String, Assento> getAssentos() {
        return assentos;
    }

    public Set<String> getAssentosOcupados() {
        return Collections.unmodifiableSet(assentosOcupados);
    }

    public boolean existeAssento(String codigoAssento) {
        return assentos.containsKey(codigoAssento);
    }

    public boolean estaDisponivel(String codigoAssento) {
        return existeAssento(codigoAssento) && !assentosOcupados.contains(codigoAssento);
    }

    public void ocuparAssento(String codigoAssento) {
        if (!existeAssento(codigoAssento)) {
            throw new IllegalArgumentException("Assento " + codigoAssento + " não existe nesta sessão.");
        }
        if (assentosOcupados.contains(codigoAssento)) {
            throw new IllegalStateException("Assento " + codigoAssento + " já está ocupado.");
        }
        assentosOcupados.add(codigoAssento);
    }

    public void liberarAssento(String codigoAssento) {
        if (!existeAssento(codigoAssento)) {
            throw new IllegalArgumentException("Assento " + codigoAssento + " não existe nesta sessão.");
        }
        assentosOcupados.remove(codigoAssento);
    }

    public BigDecimal calcularPrecoAssento(Assento assento) {
        Objects.requireNonNull(assento, "Assento não pode ser nulo para cálculo de preço.");
        return precoBase.multiply(assento.getTipo().getMultiplicadorPreco()).setScale(2, RoundingMode.HALF_UP);
    }

    public int getTotalAssentos() {
        return assentos.size();
    }

    public int getAssentosDisponiveisCount() {
        return assentos.size() - assentosOcupados.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sessao sessao = (Sessao) o;
        return Objects.equals(id, sessao.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sessao{" +
                "id='" + id + '\'' +
                ", tituloFilme='" + tituloFilme + '\'' +
                ", horarioInicio=" + horarioInicio +
                ", precoBase=" + precoBase +
                ", ocupados=" + assentosOcupados.size() + "/" + assentos.size() +
                '}';
    }
}
