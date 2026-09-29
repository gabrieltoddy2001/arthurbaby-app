package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.service.PedidoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;
    public PedidoController(PedidoService service) { this.service = service; }

    @PostMapping public PedidoResponse criar(@RequestBody PedidoRequest request) { return service.criar(request); }
    @GetMapping("/{id}") public PedidoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.buscarPorId(id, logado);
    }
    @GetMapping("/numero/{numeroPedido}")
    public PedidoResponse buscarPorNumero(@PathVariable String numeroPedido, @AuthenticationPrincipal Usuario logado) {
        return service.buscarPorNumero(numeroPedido, logado);
    }
    @GetMapping("/cliente/{id}")
    public List<PedidoResponse> porCliente(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        return service.listarPorCliente(id, logado);
    }
    @PutMapping("/{id}/status") public PedidoResponse status(@PathVariable Long id, @RequestBody StatusPedidoRequest request) {
        return service.alterarStatus(id, request);
    }
    @PutMapping("/{numeroPedido}/cancelar")
    public PedidoResponse cancelar(@PathVariable String numeroPedido, @RequestBody CancelarPedidoRequest request,
                                   @AuthenticationPrincipal Usuario logado) {
        return service.cancelarPorNumero(numeroPedido, request.motivoEfetivo(), logado);
    }
}
