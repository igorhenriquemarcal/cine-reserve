package br.com.cinereserve.repository.json;

import br.com.cinereserve.model.Sessao;
import br.com.cinereserve.repository.SessaoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Implementação de SessaoRepository com persistência em arquivo JSON.
 */
public class SessaoJsonRepository implements SessaoRepository {

    private final Path caminhoArquivo;
    private final ObjectMapper mapper;
    private final Map<String, Sessao> cacheSessoes;

    public SessaoJsonRepository(Path caminhoArquivo) {
        this.caminhoArquivo = Objects.requireNonNull(caminhoArquivo, "Caminho do arquivo não pode ser nulo.");
        this.mapper = JsonMapperConfig.getMapper();
        this.cacheSessoes = new LinkedHashMap<>();
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

            List<SessaoJsonDto> dtos = mapper.readValue(arquivo, new TypeReference<List<SessaoJsonDto>>() {});
            for (SessaoJsonDto dto : dtos) {
                Sessao sessao = dto.paraDominio();
                cacheSessoes.put(sessao.getId(), sessao);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar sessões do arquivo JSON: " + caminhoArquivo, e);
        }
    }

    private synchronized void persistir() {
        try {
            List<SessaoJsonDto> dtos = cacheSessoes.values().stream()
                    .map(SessaoJsonDto::new)
                    .toList();
            mapper.writeValue(caminhoArquivo.toFile(), dtos);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao persistir sessões no arquivo JSON: " + caminhoArquivo, e);
        }
    }

    @Override
    public synchronized void salvar(Sessao sessao) {
        Objects.requireNonNull(sessao, "Sessão não pode ser nula.");
        cacheSessoes.put(sessao.getId(), sessao);
        persistir();
    }

    @Override
    public synchronized Optional<Sessao> buscarPorId(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(cacheSessoes.get(id));
    }

    @Override
    public synchronized List<Sessao> buscarTodas() {
        return Collections.unmodifiableList(new ArrayList<>(cacheSessoes.values()));
    }

    @Override
    public synchronized List<Sessao> buscarPorFilme(String tituloFilme) {
        if (tituloFilme == null) return Collections.emptyList();
        String filtro = tituloFilme.trim().toLowerCase();
        return cacheSessoes.values().stream()
                .filter(s -> s.getTituloFilme().toLowerCase().contains(filtro))
                .toList();
    }

    @Override
    public synchronized void remover(String id) {
        if (id != null && cacheSessoes.remove(id) != null) {
            persistir();
        }
    }
}
