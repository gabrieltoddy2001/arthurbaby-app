package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.EstoqueOperacaoRequest;
import br.com.arthurbaby.dto.MovimentacaoEstoqueResponse;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Operações manuais de estoque por variação. Todas delegam para {@link EstoqueService#movimentarVariacao},
 * que atualiza o saldo e grava a movimentação com o usuário logado.
 */
@RestController
@RequestMapping(value = "/api/admin/estoque/variacoes/{variacaoId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administração - Estoque", description = "Entrada, saída, ajuste e estorno manuais de estoque por variação. Exige perfil ADMINISTRADOR ou VENDEDOR. "
        + "Cada operação gera uma movimentação registrada com o usuário logado.")
public class EstoqueAdminController {
    private static final String SEM_PERMISSAO = "Acesso negado: apenas perfis ADMINISTRADOR ou VENDEDOR";

    private final EstoqueService estoque;
    private final br.com.arthurbaby.service.AuditoriaService auditoria;
    public EstoqueAdminController(EstoqueService estoque, br.com.arthurbaby.service.AuditoriaService auditoria) {
        this.estoque = estoque;
        this.auditoria = auditoria;
    }

    /** Executa a operação e registra a auditoria no servidor (o autor vem do token). */
    private MovimentacaoEstoqueResponse movimentar(Long variacaoId, MovimentoEstoqueTipo tipo, EstoqueOperacaoRequest request, Usuario logado) {
        if (request == null) throw new IllegalArgumentException("Informe a quantidade.");
        MovimentacaoEstoqueResponse mov = estoque.movimentarVariacao(variacaoId, tipo, request.quantidade(), logado, request.observacao());
        auditoria.registrar("MOVIMENTAR_ESTOQUE", "MovimentacaoEstoque", mov.id(),
                tipo + " de " + mov.quantidade() + " un. no SKU " + mov.sku() + " (saldo " + mov.estoqueAnterior() + " -> " + mov.estoquePosterior() + ")"
                        + (request.observacao() == null || request.observacao().isBlank() ? "" : ": " + request.observacao()));
        return mov;
    }

    @PostMapping("/entrada")
    @Operation(summary = "Entrada de estoque", description = "Soma \"quantidade\" unidades ao estoque da variação (ex.: recebimento de mercadoria).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentação de ENTRADA registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade ausente ou menor/igual a zero"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "404", description = "Variação não encontrada")
    })
    public MovimentacaoEstoqueResponse entrada(@Parameter(description = "Id da variação do produto", example = "1") @PathVariable Long variacaoId, @RequestBody EstoqueOperacaoRequest request,
                                               @AuthenticationPrincipal Usuario logado) {
        return movimentar(variacaoId, MovimentoEstoqueTipo.ENTRADA, request, logado);
    }

    @PostMapping("/saida")
    @Operation(summary = "Saída de estoque", description = "Subtrai \"quantidade\" unidades do estoque da variação (ex.: perda, avaria, venda fora do site).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentação de SAIDA registrada"),
            @ApiResponse(responseCode = "400", description = "Quantidade ausente, menor/igual a zero ou maior que o estoque atual"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "404", description = "Variação não encontrada")
    })
    public MovimentacaoEstoqueResponse saida(@Parameter(description = "Id da variação do produto", example = "1") @PathVariable Long variacaoId, @RequestBody EstoqueOperacaoRequest request,
                                             @AuthenticationPrincipal Usuario logado) {
        return movimentar(variacaoId, MovimentoEstoqueTipo.SAIDA, request, logado);
    }

    @PostMapping("/ajuste")
    @Operation(summary = "Ajuste de estoque", description = "Define o estoque da variação para \"quantidade\" (correção de inventário).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimentação de AJUSTE registrada (quantidade = diferença absoluta)"),
            @ApiResponse(responseCode = "400", description = "Quantidade ausente ou negativa"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "404", description = "Variação não encontrada")
    })
    public MovimentacaoEstoqueResponse ajuste(@Parameter(description = "Id da variação do produto", example = "1") @PathVariable Long variacaoId, @RequestBody EstoqueOperacaoRequest request,
                                              @AuthenticationPrincipal Usuario logado) {
        return movimentar(variacaoId, MovimentoEstoqueTipo.AJUSTE, request, logado);
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
    public MovimentacaoEstoqueResponse estorno(@Parameter(description = "Id da variação do produto", example = "1") @PathVariable Long variacaoId, @RequestBody EstoqueOperacaoRequest request,
                                               @AuthenticationPrincipal Usuario logado) {
        return movimentar(variacaoId, MovimentoEstoqueTipo.ESTORNO, request, logado);
    }
}
