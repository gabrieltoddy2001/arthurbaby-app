package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pedidos. A verificação de dono do pedido fica no {@link PedidoService} (que recebe o usuário logado),
 * exceto na criação, validada aqui antes de chamar o serviço.
 */
@RestController
@RequestMapping(value = "/api/pedidos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pedidos", description = "Criação, consulta, alteração de status e cancelamento de pedidos. Exige token; clientes só acessam os próprios pedidos.")
public class PedidoController {
    private static final String OUTRO_CLIENTE = "Acesso negado: pedido pertence a outro cliente";

    private final PedidoService service;
    public PedidoController(PedidoService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido com os itens e dados fornecidos no corpo da requisição. "
            + "Subtotal, desconto (somente via cupom), frete e total são recalculados no servidor: os campos \"desconto\" e \"frete\" enviados são ignorados. "
            + "Frete: retirada na loja = 0; entrega = R$ 15,00, grátis a partir de R$ 200,00 de subtotal ou com cupom FRETE_GRATIS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido criado"),
            @ApiResponse(responseCode = "400", description = "Pedido sem itens, quantidade inválida, cliente/produto/variação não encontrado, estoque insuficiente, cupom inválido ou não aplicável"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: cliente só cria pedidos para si mesmo")
    })
    public PedidoResponse criar(@RequestBody PedidoRequest request, @AuthenticationPrincipal Usuario logado) {
        // Cliente comum só pode criar pedido em nome próprio; equipe da loja pode criar para qualquer cliente
        boolean equipeLoja = logado.getPerfil() == Perfil.ADMINISTRADOR || logado.getPerfil() == Perfil.VENDEDOR;
        if (!equipeLoja && !logado.getId().equals(request.clienteId())) {
            throw new AccessDeniedException("Cliente so pode criar pedidos para si mesmo");
        }
        return service.criar(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por id", description = "Retorna o pedido identificado pelo id informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public PedidoResponse buscar(@Parameter(description = "Id do pedido", example = "15") @PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.buscarPorId(id, logado);
    }

    @GetMapping("/numero/{numeroPedido}")
    @Operation(summary = "Buscar pedido por número", description = "Retorna o pedido correspondente ao número informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public PedidoResponse buscarPorNumero(@Parameter(description = "Número do pedido", example = "AB20260928143512345") @PathVariable String numeroPedido, @AuthenticationPrincipal Usuario logado) {
        return service.buscarPorNumero(numeroPedido, logado);
    }

    @GetMapping("/cliente/{id}")
    @Operation(summary = "Listar pedidos do cliente", description = "Retorna todos os pedidos do cliente identificado pelo id informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos do cliente, do mais recente para o mais antigo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: pedidos de outro cliente")
    })
    public List<PedidoResponse> porCliente(@Parameter(description = "Id do cliente", example = "3") @PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.listarPorCliente(id, logado);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Alterar status do pedido", description = "Atualiza o status do pedido identificado pelo id informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado; retorna o pedido atualizado"),
            @ApiResponse(responseCode = "400", description = "Pedido não encontrado, status inválido ou cancelamento sem motivo")
    })
    public PedidoResponse status(@Parameter(description = "Id do pedido", example = "15") @PathVariable Long id, @RequestBody StatusPedidoRequest request) {
        return service.alterarStatus(id, request);
    }

    @PutMapping("/{numeroPedido}/cancelar")
    @Operation(summary = "Cancelar pedido", description = "Cancela o pedido correspondente ao número informado, registrando o motivo do cancelamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido cancelado; retorna o pedido atualizado"),
            @ApiResponse(responseCode = "400", description = "Motivo não informado, pedido já cancelado ou já entregue"),
            @ApiResponse(responseCode = "403", description = OUTRO_CLIENTE),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    })
    public PedidoResponse cancelar(@Parameter(description = "Número do pedido", example = "AB20260928143512345") @PathVariable String numeroPedido, @RequestBody CancelarPedidoRequest request,
                                   @AuthenticationPrincipal Usuario logado) {
        return service.cancelarPorNumero(numeroPedido, request.motivoEfetivo(), logado);
    }
}
