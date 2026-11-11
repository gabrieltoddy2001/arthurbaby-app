package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.PedidoAdminResponse;
import br.com.arthurbaby.dto.PedidoRequest;
import br.com.arthurbaby.dto.StatusPedidoRequest;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pedidos no painel web. As rotas literais {@code /api/admin/pedidos} têm precedência sobre o CRUD genérico
 * {@code /api/admin/{tipo}}; o acesso já é restrito a ADMINISTRADOR/VENDEDOR em {@code SecurityConfig}.
 * Alterações passam pelo {@link PedidoService} para manter estoque, histórico e totais consistentes.
 */
@RestController
@RequestMapping(value = "/api/admin/pedidos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administração - Pedidos", description = "Listagem, lançamento e alteração de status de pedidos pelo painel.")
public class PedidoAdminController {
    private final PedidoService service;

    public PedidoAdminController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Todos os pedidos, do mais recente para o mais antigo, com dados do cliente e do vendedor.")
    public List<PedidoAdminResponse> listar() {
        return service.listarParaPainel();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar pedido")
    @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404", description = "Pedido não encontrado")})
    public PedidoAdminResponse buscar(@Parameter(example = "1") @PathVariable Long id) {
        return service.buscarParaPainel(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lançar pedido para um cliente", description = "Cria o pedido em nome do cliente informado em clienteId (perfil CLIENTE e ativo). "
            + "O usuário logado é registrado como vendedor. Preços, frete e desconto são calculados no servidor e o estoque é baixado.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Cliente ausente/inválido, sem itens, estoque insuficiente ou cupom inválido")})
    public PedidoAdminResponse criar(@RequestBody PedidoRequest request, @AuthenticationPrincipal Usuario logado) {
        return service.criarPeloPainel(request, logado);
    }

    @PutMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Alterar status", description = "Para CANCELADO informe motivoCancelamento (o estoque é estornado). "
            + "Pedidos cancelados ou entregues não mudam mais de status.")
    @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "400", description = "Transição inválida ou motivo ausente")})
    public PedidoAdminResponse alterarStatus(@PathVariable Long id, @RequestBody StatusPedidoRequest request,
                                             @AuthenticationPrincipal Usuario logado) {
        return service.alterarStatusPeloPainel(id, request, logado);
    }
}
