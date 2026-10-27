package com.careidoso.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Armazena fotos de perfil em disco. O nome do arquivo é gerado no servidor
 * (UUID + extensão derivada do conteúdo real), nunca do nome enviado pelo cliente.
 */
@Service
public class FotoStorageService {

    private static final long TAMANHO_MAXIMO = 2L * 1024 * 1024;

    private final Path diretorio;

    public FotoStorageService(@Value("${careidoso.upload.dir}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize().resolve("fotos");
    }

    /** Valida e grava a foto; devolve o nome do arquivo gerado. */
    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione uma foto para enviar.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("A foto excede o tamanho máximo permitido de 2 MB.");
        }

        String extensao = detectarExtensao(arquivo);
        String nome = UUID.randomUUID() + "." + extensao;

        try {
            Files.createDirectories(diretorio);
            try (InputStream in = arquivo.getInputStream()) {
                Files.copy(in, diretorio.resolve(nome), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar a foto. Tente novamente mais tarde.");
        }
        return nome;
    }

    public Resource carregar(String nome) {
        Path arquivo = resolver(nome);
        return arquivo != null && Files.isReadable(arquivo) ? new FileSystemResource(arquivo) : null;
    }

    public void remover(String nome) {
        Path arquivo = resolver(nome);
        if (arquivo == null) {
            return;
        }
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException ignorado) {
            // foto antiga órfã não deve impedir a troca
        }
    }

    public static String tipoMidia(String nome) {
        return nome.endsWith(".png") ? "image/png" : "image/jpeg";
    }

    /** Confere os "magic bytes" para não confiar no Content-Type informado pelo cliente. */
    private String detectarExtensao(MultipartFile arquivo) {
        byte[] cabecalho;
        try (InputStream in = arquivo.getInputStream()) {
            cabecalho = in.readNBytes(8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler a foto enviada.");
        }

        boolean png = cabecalho.length >= 8
                && (cabecalho[0] & 0xFF) == 0x89 && cabecalho[1] == 'P' && cabecalho[2] == 'N' && cabecalho[3] == 'G'
                && cabecalho[4] == 0x0D && cabecalho[5] == 0x0A && cabecalho[6] == 0x1A && cabecalho[7] == 0x0A;
        boolean jpg = cabecalho.length >= 3
                && (cabecalho[0] & 0xFF) == 0xFF && (cabecalho[1] & 0xFF) == 0xD8 && (cabecalho[2] & 0xFF) == 0xFF;

        if (png) {
            return "png";
        }
        if (jpg) {
            return "jpg";
        }
        throw new IllegalArgumentException("Formato de foto inválido. Envie uma imagem JPG ou PNG.");
    }

    /** Resolve o nome dentro do diretório de fotos, bloqueando path traversal. */
    private Path resolver(String nome) {
        if (nome == null || nome.isBlank()) {
            return null;
        }
        Path arquivo = diretorio.resolve(nome).normalize();
        return arquivo.startsWith(diretorio) ? arquivo : null;
    }
}
