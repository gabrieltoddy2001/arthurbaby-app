package br.com.arthurbaby.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Upload administrativo e leitura pública das imagens de produtos. */
@RestController
public class ImagemUploadController {
    private final Path diretorio;
    public ImagemUploadController(@Value("${arthurbaby.upload.dir:uploads/produtos}") String caminho) {
        this.diretorio = Paths.get(caminho).toAbsolutePath().normalize();
    }

    @PostMapping(value = "/api/admin/imagens/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty() || arquivo.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem vazia ou maior que 5 MB");
        }
        byte[] bytes = arquivo.getBytes();
        String extensao;
        MediaType tipo;
        if (bytes.length >= 3 && (bytes[0] & 255) == 0xff && (bytes[1] & 255) == 0xd8 && (bytes[2] & 255) == 0xff) {
            extensao = ".jpg"; tipo = MediaType.IMAGE_JPEG;
        } else if (bytes.length >= 8 && (bytes[0] & 255) == 137 && bytes[1] == 80 && bytes[2] == 78 && bytes[3] == 71 && bytes[4] == 13 && bytes[5] == 10 && bytes[6] == 26 && bytes[7] == 10) {
            extensao = ".png"; tipo = MediaType.IMAGE_PNG;
        } else if (bytes.length >= 12 && new String(bytes, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(bytes, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP")) {
            extensao = ".webp"; tipo = MediaType.parseMediaType("image/webp");
        } else if (bytes.length >= 6 && (new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).equals("GIF87a") || new String(bytes, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).equals("GIF89a"))) {
            extensao = ".gif"; tipo = MediaType.IMAGE_GIF;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de imagem nao suportado");
        }
        Files.createDirectories(diretorio);
        String nome = UUID.randomUUID() + extensao;
        Files.write(diretorio.resolve(nome), bytes, StandardOpenOption.CREATE_NEW);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/produtos/imagens/").path(nome).toUriString();
        return Map.of("url", url);
    }

    @GetMapping("/api/produtos/imagens/{nome}")
    public ResponseEntity<org.springframework.core.io.Resource> obter(@PathVariable String nome) throws IOException {
        if (!nome.matches("[a-fA-F0-9-]{36}\\.(jpg|png|webp|gif)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path arquivo = diretorio.resolve(nome).normalize();
        if (!arquivo.startsWith(diretorio) || !Files.isRegularFile(arquivo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String extensao = nome.substring(nome.lastIndexOf('.') + 1);
        MediaType tipo = switch (extensao) {
            case "jpg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.parseMediaType("image/webp");
        };
        return ResponseEntity.ok().contentType(tipo).cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7)))
                .body(new org.springframework.core.io.UrlResource(arquivo.toUri()));
    }
}
