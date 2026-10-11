package br.com.cinereserve.repository.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Utilitário para gravação atômica em disco de arquivos JSON,
 * prevenindo corrupção de dados em quedas abruptas da aplicação.
 */
public final class ArquivoJsonHelper {

    private ArquivoJsonHelper() {
    }

    public static void gravarAtomicamente(Path caminhoArquivo, Object dados, ObjectMapper mapper) throws IOException {
        Path pasta = caminhoArquivo.getParent();
        if (pasta != null && !Files.exists(pasta)) {
            Files.createDirectories(pasta);
        }

        String nomeTmp = caminhoArquivo.getFileName().toString() + ".tmp";
        Path arquivoTmp = (pasta != null) ? pasta.resolve(nomeTmp) : Path.of(nomeTmp);

        // 1. Grava no arquivo temporário
        mapper.writeValue(arquivoTmp.toFile(), dados);

        // 2. Substitui o arquivo final atomicamente
        try {
            Files.move(arquivoTmp, caminhoArquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(arquivoTmp, caminhoArquivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
