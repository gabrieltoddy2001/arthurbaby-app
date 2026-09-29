package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.EstoqueOperacaoRequest;
import br.com.arthurbaby.dto.MovimentacaoEstoqueResponse;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/estoque/variacoes/{variacaoId}")
@Tag(name = "Administração - Estoque", description = "Entrada, ajuste e estorno manuais de estoque por variação. Exige perfil ADMINISTRADOR ou VENDEDOR. "
        + "Cada operação gera uma movimentação registrada com o usuário logado.")
public class EstoqueAdminController {
    private static final String SEM_PERMISSAO = "Acesso negado: apenas perfis ADMINISTRADOR ou VENDEDOR";

    private final EstoqueService estoque;
    public EstoqueAdminController(EstoqueService estoque) {
        this.estoque = estoque;
    }

    @PostMapping("/entrada")
    @Operation(summary = "Entrada de estoque", description = "Soma \"quantidade\" unidades ao estoque da variação (ex.: recebimento de mercadoria).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentação de ENTRADA registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade ausente ou menor/igual a zero"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "404", description = "Variação não encontrada")
    })
    public MovimentacaoEstoqueResponse entrada(@PathVariable Long variacaoId, @RequestBody EstoqueOperacaoRequest request,
                                               @AuthenticationPrincipal Usuario logado) {
        return estoque.movimentarVariacao(variacaoId, MovimentoEstoqueTipo.ENTRADA, request.quantidade(), logado, request.observacao());
    }

    @PostMapping("/ajuste")
    @Operation(summary = "Ajuste de estoque", description = "Define o estoque da variação para \"quantidade\" (correção de inventário).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentação de AJUSTE registrada (quantidade = diferença absoluta)"),
            @ApiResponse(responseCode = "400", description = "Quantidade ausente ou negativa"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "404", description = "Variação não encontrada")
    })
    public MovimentacaoEstoqueResponse ajuste(@PathVariable Long variacaoId, @RequestBody EstoqueOperacaoRequest request,
                                              @AuthenticationPrincipal Usuario logado) {
        return estoque.movimentarVariacao(variacaoId, MovimentoEstoqueTipo.AJUSTE, request.quantidade(), logado, request.observacao());
    }

    @PostMapping("/estorno")
    @Operation(summary = "Estorno de estoque", description = "Devolve \"quantidade\" unidades ao estoque da variação (ex.: devolução). "
            + "O cancelamento de pedidos já estorna automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentação de ESTORNO registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade ausente ou menor/igual a zero"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "404", description = "Variação não encontrada")
    })
    public MovimentacaoEstoqueResponse estorno(@PathVariable Long variacaoId, @RequestBody EstoqueOperacaoRequest request,
                                               @AuthenticationPrincipal Usuario logado) {
        return estoque.movimentarVariacao(variacaoId, MovimentoEstoqueTipo.ESTORNO, request.quantidade(), logado, request.observacao());
    }
}
