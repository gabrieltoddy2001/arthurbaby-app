package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.*;
import br.com.arthurbaby.entity.Pedido;
import br.com.arthurbaby.repository.PedidoRepository;
import br.com.arthurbaby.service.PedidoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;
    private final PedidoRepository pedidos;
    public PedidoController(PedidoService service, PedidoRepository pedidos) {
        this.service = service;
        this.pedidos = pedidos;
    }
    @PostMapping public Pedido criar(@RequestBody PedidoRequest request) { return service.criar(request); }
    @GetMapping("/{id}") public Pedido buscar(@PathVariable Long id) {
        return pedidos.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido nao encontrado"));
    }
    @GetMapping("/cliente/{id}") public List<Pedido> porCliente(@PathVariable Long id) {
        return pedidos.findByClienteIdOrderByCriadoEmDesc(id);
    }
    @PutMapping("/{id}/status") public Pedido status(@PathVariable Long id, @RequestBody StatusPedidoRequest request) {
        return service.alterarStatus(id, request);
    }
}
