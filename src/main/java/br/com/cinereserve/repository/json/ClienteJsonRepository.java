package br.com.cinereserve.repository.json;

import br.com.cinereserve.model.Cliente;
import br.com.cinereserve.repository.ClienteRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Implementação de ClienteRepository com persistência em arquivo JSON.
 */
public class ClienteJsonRepository implements ClienteRepository {

    private final Path caminhoArquivo;
    private final ObjectMapper mapper;
    private final Map<String, Cliente> cacheClientes;

    public ClienteJsonRepository(Path caminhoArquivo) {
        this.caminhoArquivo = Objects.requireNonNull(caminhoArquivo, "Caminho do arquivo não pode ser nulo.");
        this.mapper = JsonMapperConfig.getMapper();
        this.cacheClientes = new LinkedHashMap<>();
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

            List<ClienteJsonDto> dtos = mapper.readValue(arquivo, new TypeReference<List<ClienteJsonDto>>() {});
            for (ClienteJsonDto dto : dtos) {
                Cliente cliente = dto.paraDominio();
                cacheClientes.put(cliente.getId(), cliente);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar clientes do arquivo JSON: " + caminhoArquivo, e);
        }
    }

    private synchronized void persistir() {
        try {
            List<ClienteJsonDto> dtos = cacheClientes.values().stream()
                    .map(ClienteJsonDto::new)
                    .toList();
            mapper.writeValue(caminhoArquivo.toFile(), dtos);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao persistir clientes no arquivo JSON: " + caminhoArquivo, e);
        }
    }

    @Override
    public synchronized void salvar(Cliente cliente) {
        Objects.requireNonNull(cliente, "Cliente não pode ser nulo.");
        cacheClientes.put(cliente.getId(), cliente);
        persistir();
    }

    @Override
    public synchronized Optional<Cliente> buscarPorId(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(cacheClientes.get(id));
    }

    @Override
    public synchronized Optional<Cliente> buscarPorCpf(String cpf) {
        if (cpf == null) return Optional.empty();
        return cacheClientes.values().stream()
                .filter(c -> c.getCpf().equalsIgnoreCase(cpf.trim()))
                .findFirst();
    }

    @Override
    public synchronized Optional<Cliente> buscarPorEmail(String email) {
        if (email == null) return Optional.empty();
        return cacheClientes.values().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst();
    }

    @Override
    public synchronized List<Cliente> buscarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(cacheClientes.values()));
    }

    @Override
    public synchronized void remover(String id) {
        if (id != null && cacheClientes.remove(id) != null) {
            persistir();
        }
    }
}
