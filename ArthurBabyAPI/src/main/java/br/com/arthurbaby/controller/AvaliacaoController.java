package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.AvaliacaoRequest;
import br.com.arthurbaby.dto.AvaliacaoResponse;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Avaliações de produto. A listagem (GET) é pública, porque o SecurityConfig libera GET em /api/produtos/**;
 * criar avaliação (POST) exige token.
 */
@RestController
@RequestMapping(value = "/api/produtos/{produtoId}/avaliacoes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Avaliações", description = "Notas (1 a 5) e comentários dos clientes sobre os produtos. Listar é público; avaliar exige token.")
public class AvaliacaoController {
    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar avaliações do produto", description = "Retorna as avaliações do produto, da mais recente para a mais antiga.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de avaliações (vazia se ainda não houver)"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    public List<AvaliacaoResponse> listar(@Parameter(description = "Id do produto", example = "1") @PathVariable Long produtoId) {
        return service.listar(produtoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Avaliar produto", description = "Registra a nota (1 a 5) e o comentário do cliente logado. "
            + "O cliente precisa ter um pedido ENTREGUE com o produto e só pode avaliar cada produto uma vez. "
            + "A média exibida em GET /api/produtos/{id} é atualizada na hora.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação criada"),
            @ApiResponse(responseCode = "400", description = "Nota fora de 1 a 5, produto já avaliado pelo cliente ou nenhum pedido entregue com o produto"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "clienteId do corpo diferente do dono do token"),
            @ApiResponse(responseCode = "404", description = "Produto ou cliente não encontrado")
    })
    public AvaliacaoResponse avaliar(@Parameter(description = "Id do produto", example = "1") @PathVariable Long produtoId,
                                     @RequestBody AvaliacaoRequest request, @AuthenticationPrincipal Usuario logado) {
        return service.avaliar(produtoId, request, logado);
    }
}
