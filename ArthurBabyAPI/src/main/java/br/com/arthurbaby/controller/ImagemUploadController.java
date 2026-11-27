package br.com.arthurbaby.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import br.com.arthurbaby.entity.ImagemArquivo;
import br.com.arthurbaby.repository.ImagemArquivoRepository;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Upload administrativo e leitura pública das imagens de produtos.
 * As fotos são gravadas no banco (tabela imagem_arquivo), porque o disco do Render é apagado a cada deploy.
 * A pasta uploads/ só é lida como reserva, para fotos antigas enviadas antes dessa mudança num servidor local.
 */
@RestController
@Tag(name = "Administração - Imagens", description = "Upload de arquivos de imagem para os produtos e leitura pública das imagens salvas.")
public class ImagemUploadController {

    private final Path diretorio;

    private final String baseUrl;

    private final boolean importarCaminhoHabilitado;

    private final ImagemArquivoRepository imagens;

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    public ImagemUploadController(
            @Value("${arthurbaby.upload.dir:uploads/produtos}") String caminho,
            @Value("${arthurbaby.imagem.base-url:}") String baseUrl,
            @Value("${IMAGEM_IMPORTAR_CAMINHO:false}") boolean importarCaminhoHabilitado,
            ImagemArquivoRepository imagens) {
        this.diretorio = Paths.get(caminho).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        this.importarCaminhoHabilitado = importarCaminhoHabilitado;
        this.imagens = imagens;
    }

    @PostMapping(value = "/api/admin/imagens/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Fazer upload de imagem", description = "Recebe o arquivo (JPG, PNG, WEBP ou GIF até 5MB), valida o formato real do arquivo e retorna a URL pública.")
    public Map<String, String> upload(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty() || arquivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem vazia ou maior que 5 MB");
        }
        return Map.of("url", salvar(arquivo.getBytes()));
    }

    /**
     * Importa uma imagem a partir de um caminho no disco DO SERVIDOR (ex.: C:\Fotos\body.jpg), para quando a API roda
     * no mesmo computador das fotos. Desligado por padrão: ative com IMAGEM_IMPORTAR_CAMINHO=true (variável de ambiente
     * ou application-local.properties) só em ambiente local. Aceita apenas arquivos de imagem reais de até 5 MB.
     */
    @PostMapping(value = "/api/admin/imagens/importar-caminho", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Importar imagem por caminho no servidor", description = "Lê o arquivo do caminho informado (no computador onde a API roda), "
            + "valida o formato real (JPG, PNG, WEBP ou GIF até 5MB), grava no banco e retorna a URL pública. "
            + "Desativado por padrão (IMAGEM_IMPORTAR_CAMINHO=true para ativar).")
    public Map<String, String> importarCaminho(@RequestBody Map<String, String> corpo) throws IOException {
        if (!importarCaminhoHabilitado) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Importacao por caminho desativada neste servidor. Use o botao Escolher fotos.");
        }
        String informado = corpo == null ? null : corpo.get("caminho");
        if (informado == null || informado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o caminho do arquivo");
        }
        Path arquivo;
        try {
            String limpo = informado.strip().replaceAll("^\"|\"$", "");
            arquivo = limpo.regionMatches(true, 0, "file:", 0, 5) ? Paths.get(java.net.URI.create(limpo.replace(" ", "%20")))
                    : Paths.get(limpo);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Caminho invalido: " + informado);
        }
        // Mensagem única para "não existe" e "sem permissão": não revela detalhes do disco do servidor
        String nomeArquivo = arquivo.getFileName() == null ? "" : arquivo.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!arquivo.isAbsolute() || !nomeArquivo.matches(".+\\.(jpe?g|png|webp|gif)")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe o caminho completo de um arquivo .jpg, .png, .webp ou .gif (ex.: C:\\Fotos\\produto.jpg)");
        }
        if (!Files.isRegularFile(arquivo) || !Files.isReadable(arquivo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Arquivo nao encontrado no computador onde a API esta rodando: " + informado);
        }
        long tamanho = Files.size(arquivo);
        if (tamanho == 0 || tamanho > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Imagem vazia ou maior que 5 MB. Para fotos grandes use Escolher fotos (reduz automaticamente).");
        }
        return Map.of("url", salvar(Files.readAllBytes(arquivo)));
    }

    /** Confere o formato pelo conteúdo (não pela extensão), grava com nome aleatório e devolve a URL pública. */
    private String salvar(byte[] bytes) throws IOException {
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
        String nome = UUID.randomUUID() + extensao;
        ImagemArquivo imagem = new ImagemArquivo();
        imagem.setNome(nome);
        imagem.setTipo(tipo.toString());
        imagem.setTamanho(bytes.length);
        imagem.setDados(bytes);
        imagens.save(imagem);
        // Sem base-url configurada, usa o endereço pelo qual a API foi chamada (no Render, o domínio público).
        String base = baseUrl.isBlank()
                ? ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
                : baseUrl.replaceAll("/+$", "");
        return base + "/api/produtos/imagens/" + nome;
    }

    @GetMapping("/api/produtos/imagens/{nome}")
    @Operation(summary = "Obter imagem", description = "Retorna a imagem salva (no banco, ou na pasta local para fotos antigas) para exibição no app ou no painel.")
    public ResponseEntity<org.springframework.core.io.Resource> obter(@PathVariable String nome) throws IOException {
        if (!nome.matches("[a-fA-F0-9-]{36}\\.(jpg|png|webp|gif)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        CacheControl cache = CacheControl.maxAge(java.time.Duration.ofDays(7)).cachePublic();
        Optional<ImagemArquivo> salva = imagens.findById(nome);
        if (salva.isPresent()) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(salva.get().getTipo())).cacheControl(cache)
                    .body(new org.springframework.core.io.ByteArrayResource(salva.get().getDados()));
        }
        // Reserva: fotos antigas gravadas na pasta uploads/ antes de irem para o banco
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
        return ResponseEntity.ok().contentType(tipo).cacheControl(cache)
                .body(new org.springframework.core.io.UrlResource(arquivo.toUri()));
    }
}