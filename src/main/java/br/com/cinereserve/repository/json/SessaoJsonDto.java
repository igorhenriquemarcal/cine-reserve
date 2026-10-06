package br.com.cinereserve.repository.json;

import br.com.cinereserve.model.Assento;
import br.com.cinereserve.model.Sessao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DTO para persistência JSON de Sessões de cinema.
 * Preserva o estado de assentos cadastrados e assentos já ocupados.
 */
public class SessaoJsonDto {

    private String id;
    private String tituloFilme;
    private LocalDateTime horarioInicio;
    private BigDecimal precoBase;
    private List<AssentoJsonDto> assentos;
    private Set<String> assentosOcupados;

    public SessaoJsonDto() {
        this.assentos = new ArrayList<>();
        this.assentosOcupados = new HashSet<>();
    }

    public SessaoJsonDto(Sessao sessao) {
        this.id = sessao.getId();
        this.tituloFilme = sessao.getTituloFilme();
        this.horarioInicio = sessao.getHorarioInicio();
        this.precoBase = sessao.getPrecoBase();
        this.assentos = sessao.getAssentos().values().stream()
                .map(AssentoJsonDto::new)
                .toList();
        this.assentosOcupados = new HashSet<>(sessao.getAssentosOcupados());
    }

    public Sessao paraDominio() {
        List<Assento> assentosDominio = this.assentos.stream()
                .map(AssentoJsonDto::paraDominio)
                .toList();

        Sessao sessao = new Sessao(this.id, this.tituloFilme, this.horarioInicio, this.precoBase, assentosDominio);
        for (String codOcupado : this.assentosOcupados) {
            sessao.ocuparAssento(codOcupado);
        }
        return sessao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTituloFilme() {
        return tituloFilme;
    }

    public void setTituloFilme(String tituloFilme) {
        this.tituloFilme = tituloFilme;
    }

    public LocalDateTime getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(LocalDateTime horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = precoBase;
    }

    public List<AssentoJsonDto> getAssentos() {
        return assentos;
    }

    public void setAssentos(List<AssentoJsonDto> assentos) {
        this.assentos = assentos;
    }

    public Set<String> getAssentosOcupados() {
        return assentosOcupados;
    }

    public void setAssentosOcupados(Set<String> assentosOcupados) {
        this.assentosOcupados = assentosOcupados;
    }
}
