package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.FavoritoRequest;
import br.com.arthurbaby.dto.FavoritoResponse;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.FavoritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Favoritos do cliente. Existem duas formas de remover (corpo ou path) para compatibilidade com o Retrofit. */
@RestController
@RequestMapping(value = "/api/favoritos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Favoritos", description = "Lista de produtos favoritos do cliente. Exige token; o cliente só acessa os próprios favoritos.")
public class FavoritoController {
    private static final String OUTRO_CLIENTE = "Acesso negado: favoritos pertencem a outro cliente";

    private final FavoritoService service;
    public FavoritoController(FavoritoService service) {
        this.service = service;
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar favoritos do cliente", description = "Retorna os produtos favoritados pelo cliente identificado pelo id informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de favoritos do cliente (vazia se não houver)"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE)
    })
    public List<FavoritoResponse> listar(@Parameter(description = "Id do cliente", example = "3") @PathVariable Long clienteId, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(clienteId, logado);
        return service.listar(clienteId);
    }

    @PostMapping
    @Operation(summary = "Adicionar favorito", description = "Adiciona o produto aos favoritos do cliente. Se já estiver favoritado, retorna o favorito existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto favoritado (ou favorito já existente)"),
            @ApiResponse(responseCode = "400", description = "Cliente ou produto não encontrado, ou corpo mal formatado"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE)
    })
    public FavoritoResponse adicionar(@RequestBody FavoritoRequest request, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(request.clienteId(), logado);
        return service.adicionar(request.clienteId(), request.produtoId());
    }

    @DeleteMapping
    @Operation(summary = "Remover favorito (corpo)", description = "Remove o produto dos favoritos do cliente informados no corpo da requisição.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorito removido (sem erro se não existia)"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido ou mal formatado"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE)
    })
    public void remover(@RequestBody FavoritoRequest request, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(request.clienteId(), logado);
        service.remover(request.clienteId(), request.produtoId());
    }

    @DeleteMapping("/cliente/{clienteId}/produto/{produtoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover favorito", description = "Remove o produto dos favoritos do cliente. Mesma ação do DELETE com corpo, "
            + "para clientes HTTP que não enviam corpo em DELETE (ex.: Retrofit).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Favorito removido (sem erro se não existia)"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE)
    })
    public void removerPorPath(@Parameter(description = "Id do cliente", example = "3") @PathVariable Long clienteId, @Parameter(description = "Id do produto", example = "1") @PathVariable Long produtoId, @AuthenticationPrincipal Usuario logado) {
        verificarAcesso(clienteId, logado);
        service.remover(clienteId, produtoId);
    }

    /** Mesma regra de {@code ClienteController}: equipe da loja acessa tudo, cliente só os próprios favoritos. */
    private void verificarAcesso(Long clienteId, Usuario logado) {
        boolean equipeLoja = logado.getPerfil() == Perfil.ADMINISTRADOR || logado.getPerfil() == Perfil.VENDEDOR;
        if (!equipeLoja && !logado.getId().equals(clienteId)) {
            throw new AccessDeniedException("Acesso negado a favoritos de outro cliente");
        }
    }
}
