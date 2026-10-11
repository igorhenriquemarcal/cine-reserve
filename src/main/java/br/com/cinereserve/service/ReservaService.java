package br.com.cinereserve.service;

import br.com.cinereserve.exception.AssentoIndisponivelException;
import br.com.cinereserve.exception.EntidadeNaoEncontradaException;
import br.com.cinereserve.exception.RegraDeNegocioException;
import br.com.cinereserve.model.Assento;
import br.com.cinereserve.model.Cliente;
import br.com.cinereserve.model.Reserva;
import br.com.cinereserve.model.Sessao;
import br.com.cinereserve.repository.ClienteRepository;
import br.com.cinereserve.repository.ReservaRepository;
import br.com.cinereserve.repository.SessaoRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Serviço responsável pelas operações de negócio de reservas do cinema.
 * Implementa controle transacional e de concorrência com travas por sessão.
 */
public class ReservaService {

    private final ClienteRepository clienteRepository;
    private final SessaoRepository sessaoRepository;
    private final ReservaRepository reservaRepository;

    // Locks por sessão para garantir exclusão mútua fina e alta concorrência entre salas diferentes
    private final ConcurrentHashMap<String, ReentrantLock> locksPorSessao = new ConcurrentHashMap<>();

    public ReservaService(ClienteRepository clienteRepository,
                          SessaoRepository sessaoRepository,
                          ReservaRepository reservaRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository não pode ser nulo.");
        this.sessaoRepository = Objects.requireNonNull(sessaoRepository, "SessaoRepository não pode ser nulo.");
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository não pode ser nulo.");
    }

    private ReentrantLock obterLockSessao(String sessaoId) {
        return locksPorSessao.computeIfAbsent(sessaoId, id -> new ReentrantLock());
    }

    /**
     * Realiza uma nova reserva garantindo operação atômica e prevenção de reservas duplicadas em multithread.
     */
    public Reserva realizarReserva(String clienteId, String sessaoId, List<String> codigosAssentos) {
        if (codigosAssentos == null || codigosAssentos.isEmpty()) {
            throw new RegraDeNegocioException("É necessário informar ao menos um assento para a reserva.");
        }

        Cliente cliente = clienteRepository.buscarPorId(clienteId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Cliente com ID " + clienteId + " não foi encontrado."));

        ReentrantLock lock = obterLockSessao(sessaoId);
        lock.lock();
        try {
            Sessao sessao = sessaoRepository.buscarPorId(sessaoId)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Sessão com ID " + sessaoId + " não foi encontrada."));

            if (sessao.getHorarioInicio().isBefore(LocalDateTime.now())) {
                throw new RegraDeNegocioException("Não é possível realizar reservas para uma sessão que já começou ou encerrou.");
            }

            // 1. Normalização e validação de duplicidade na lista de assentos
            Set<String> assentosUnicos = new LinkedHashSet<>();
            for (String codigo : codigosAssentos) {
                if (codigo == null || codigo.trim().isEmpty()) {
                    throw new RegraDeNegocioException("Código de assento inválido ou vazio informado.");
                }
                String codigoFormatado = codigo.trim().toUpperCase();
                if (!assentosUnicos.add(codigoFormatado)) {
                    throw new RegraDeNegocioException("Assento duplicado na solicitação: " + codigoFormatado);
                }
            }

            // 2. Validação prévia de existência e disponibilidade de todos os assentos (tudo ou nada)
            List<Assento> assentosParaReservar = new ArrayList<>();
            for (String codigoFormatado : assentosUnicos) {
                if (!sessao.existeAssento(codigoFormatado)) {
                    throw new AssentoIndisponivelException("O assento " + codigoFormatado + " não existe na sala desta sessão.");
                }
                if (!sessao.estaDisponivel(codigoFormatado)) {
                    throw new AssentoIndisponivelException("O assento " + codigoFormatado + " já está ocupado.");
                }
                assentosParaReservar.add(sessao.getAssentos().get(codigoFormatado));
            }

            // 3. Ocupação atômica dos assentos
            for (Assento a : assentosParaReservar) {
                sessao.ocuparAssento(a.getCodigo());
            }

            // 4. Criação e confirmação da reserva
            Reserva reserva = new Reserva(cliente, sessao, assentosParaReservar);
            reserva.confirmar();

            // 5. Persistência
            sessaoRepository.salvar(sessao);
            reservaRepository.salvar(reserva);

            return reserva;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cancela uma reserva existente e libera os assentos de volta para a sessão.
     */
    public void cancelarReserva(String reservaId) {
        Reserva reserva = reservaRepository.buscarPorId(reservaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Reserva com ID " + reservaId + " não foi encontrada."));

        if (reserva.getStatus() == br.com.cinereserve.model.StatusReserva.CANCELADA) {
            throw new RegraDeNegocioException("A reserva " + reservaId + " já se encontra cancelada.");
        }

        String sessaoId = reserva.getSessao().getId();
        ReentrantLock lock = obterLockSessao(sessaoId);
        lock.lock();
        try {
            Sessao sessao = sessaoRepository.buscarPorId(sessaoId)
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Sessão associada à reserva não foi encontrada."));

            if (sessao.getHorarioInicio().isBefore(LocalDateTime.now())) {
                throw new RegraDeNegocioException("Não é permitido cancelar reservas de sessões que já iniciaram.");
            }

            // Cancela a reserva
            reserva.cancelar();

            // Libera os assentos na sessão
            for (Assento a : reserva.getAssentos()) {
                sessao.liberarAssento(a.getCodigo());
            }

            // Persiste as atualizações
            sessaoRepository.salvar(sessao);
            reservaRepository.salvar(reserva);
        } finally {
            lock.unlock();
        }
    }

    public List<Reserva> buscarReservasPorCliente(String clienteId) {
        return reservaRepository.buscarPorClienteId(clienteId);
    }

    public List<Reserva> buscarReservasPorSessao(String sessaoId) {
        return reservaRepository.buscarPorSessaoId(sessaoId);
    }
}
