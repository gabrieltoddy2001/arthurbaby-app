package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.MovimentacaoEstoqueAdminResponse;
import br.com.arthurbaby.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Histórico de movimentações de estoque usado pelo painel (dashboard, estoque e relatórios).
 * A rota literal {@code /api/admin/movimentacoes} tem precedência sobre o CRUD genérico {@code /api/admin/{tipo}}.
 */
@RestController
@RequestMapping(value = "/api/admin/movimentacoes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administração - Estoque")
public class MovimentacaoAdminController {
    private final EstoqueService estoque;

    public MovimentacaoAdminController(EstoqueService estoque) {
        this.estoque = estoque;
    }

    @GetMapping
    @Operation(summary = "Listar movimentações", description = "Todas as movimentações de estoque, da mais recente para a mais antiga, "
            + "com nome do produto e do usuário que registrou.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de movimentações"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: apenas perfis ADMINISTRADOR ou VENDEDOR")
    })
    public List<MovimentacaoEstoqueAdminResponse> listar() {
        return estoque.listarMovimentacoes();
    }
}
