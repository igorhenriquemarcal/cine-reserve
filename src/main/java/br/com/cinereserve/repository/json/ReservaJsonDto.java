package br.com.cinereserve.repository.json;

import br.com.cinereserve.model.Assento;
import br.com.cinereserve.model.Reserva;
import br.com.cinereserve.model.StatusReserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de transferência de dados (DTO) para persistência JSON de Reservas.
 * Evita duplicação dos dados completos de Cliente e Sessao no arquivo reservas.json.
 */
public class ReservaJsonDto {

    private String id;
    private String clienteId;
    private String sessaoId;
    private List<String> codigosAssentos;
    private LocalDateTime dataHoraCriacao;
    private StatusReserva status;
    private BigDecimal valorTotal;

    public ReservaJsonDto() {
        this.codigosAssentos = new ArrayList<>();
    }

    public ReservaJsonDto(Reserva reserva) {
        this.id = reserva.getId();
        this.clienteId = reserva.getCliente().getId();
        this.sessaoId = reserva.getSessao().getId();
        this.codigosAssentos = reserva.getAssentos().stream()
                .map(Assento::getCodigo)
                .toList();
        this.dataHoraCriacao = reserva.getDataHoraCriacao();
        this.status = reserva.getStatus();
        this.valorTotal = reserva.getValorTotal();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getSessaoId() {
        return sessaoId;
    }

    public void setSessaoId(String sessaoId) {
        this.sessaoId = sessaoId;
    }

    public List<String> getCodigosAssentos() {
        return codigosAssentos;
    }

    public void setCodigosAssentos(List<String> codigosAssentos) {
        this.codigosAssentos = codigosAssentos;
    }

    public LocalDateTime getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public void setDataHoraCriacao(LocalDateTime dataHoraCriacao) {
        this.dataHoraCriacao = dataHoraCriacao;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public void setStatus(StatusReserva status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
