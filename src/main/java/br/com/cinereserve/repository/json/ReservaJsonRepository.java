package br.com.cinereserve.repository.json;

import br.com.cinereserve.model.Assento;
import br.com.cinereserve.model.Cliente;
import br.com.cinereserve.model.Reserva;
import br.com.cinereserve.model.Sessao;
import br.com.cinereserve.repository.ClienteRepository;
import br.com.cinereserve.repository.ReservaRepository;
import br.com.cinereserve.repository.SessaoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Implementação de ReservaRepository com persistência em arquivo JSON.
 * Utiliza ClienteRepository e SessaoRepository para reconstruir as associações de domínio.
 */
public class ReservaJsonRepository implements ReservaRepository {

    private final Path caminhoArquivo;
    private final ClienteRepository clienteRepository;
    private final SessaoRepository sessaoRepository;
    private final ObjectMapper mapper;
    private final Map<String, Reserva> cacheReservas;

    public ReservaJsonRepository(Path caminhoArquivo, ClienteRepository clienteRepository, SessaoRepository sessaoRepository) {
        this.caminhoArquivo = Objects.requireNonNull(caminhoArquivo, "Caminho do arquivo não pode ser nulo.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository não pode ser nulo.");
        this.sessaoRepository = Objects.requireNonNull(sessaoRepository, "SessaoRepository não pode ser nulo.");
        this.mapper = JsonMapperConfig.getMapper();
        this.cacheReservas = new LinkedHashMap<>();
        inicializarArquivoECarregar();
    }

    private synchronized void inicializarArquivoECarregar() {
        try {
            if (caminhoArquivo.getParent() != null && !Files.exists(caminhoArquivo.getParent())) {
                Files.createDirectories(caminhoArquivo.getParent());
            }

            File arquivo = caminhoArquivo.toFile();
            if (!arquivo.exists() || arquivo.length() == 0) {
                mapper.writeValue(arquivo, Collections.emptyList());
                return;
            }

            List<ReservaJsonDto> dtos = mapper.readValue(arquivo, new TypeReference<List<ReservaJsonDto>>() {});
            for (ReservaJsonDto dto : dtos) {
                Optional<Cliente> clienteOpt = clienteRepository.buscarPorId(dto.getClienteId());
                Optional<Sessao> sessaoOpt = sessaoRepository.buscarPorId(dto.getSessaoId());

                if (clienteOpt.isPresent() && sessaoOpt.isPresent()) {
                    Cliente cliente = clienteOpt.get();
                    Sessao sessao = sessaoOpt.get();

                    List<Assento> assentos = dto.getCodigosAssentos().stream()
                            .map(cod -> sessao.getAssentos().get(cod))
                            .filter(Objects::nonNull)
                            .toList();

                    if (!assentos.isEmpty()) {
                        Reserva reserva = new Reserva(
                                dto.getId(),
                                cliente,
                                sessao,
                                assentos,
                                dto.getDataHoraCriacao(),
                                dto.getStatus()
                        );
                        cacheReservas.put(reserva.getId(), reserva);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar reservas do arquivo JSON: " + caminhoArquivo, e);
        }
    }

    private synchronized void persistir() {
        try {
            List<ReservaJsonDto> dtos = cacheReservas.values().stream()
                    .map(ReservaJsonDto::new)
                    .toList();
            mapper.writeValue(caminhoArquivo.toFile(), dtos);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao persistir reservas no arquivo JSON: " + caminhoArquivo, e);
        }
    }

    @Override
    public synchronized void salvar(Reserva reserva) {
        Objects.requireNonNull(reserva, "Reserva não pode ser nula.");
        cacheReservas.put(reserva.getId(), reserva);
        persistir();
    }

    @Override
    public synchronized Optional<Reserva> buscarPorId(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(cacheReservas.get(id));
    }

    @Override
    public synchronized List<Reserva> buscarPorClienteId(String clienteId) {
        if (clienteId == null) return Collections.emptyList();
        return cacheReservas.values().stream()
                .filter(r -> r.getCliente().getId().equals(clienteId))
                .toList();
    }

    @Override
    public synchronized List<Reserva> buscarPorSessaoId(String sessaoId) {
        if (sessaoId == null) return Collections.emptyList();
        return cacheReservas.values().stream()
                .filter(r -> r.getSessao().getId().equals(sessaoId))
                .toList();
    }

    @Override
    public synchronized List<Reserva> buscarTodas() {
        return Collections.unmodifiableList(new ArrayList<>(cacheReservas.values()));
    }

    @Override
    public synchronized void remover(String id) {
        if (id != null && cacheReservas.remove(id) != null) {
            persistir();
        }
    }
}
