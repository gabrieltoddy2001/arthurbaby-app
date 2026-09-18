package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.FavoritoRequest;
import br.com.arthurbaby.entity.Favorito;
import br.com.arthurbaby.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {
    private final FavoritoRepository favoritos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    public FavoritoController(FavoritoRepository favoritos, UsuarioRepository usuarios, ProdutoRepository produtos) {
        this.favoritos = favoritos;
        this.usuarios = usuarios;
        this.produtos = produtos;
    }
    @GetMapping("/cliente/{clienteId}") public List<Favorito> listar(@PathVariable Long clienteId) {
        return favoritos.findByClienteId(clienteId);
    }
    @PostMapping public Favorito adicionar(@RequestBody FavoritoRequest request) {
        return favoritos.findByClienteIdAndProdutoId(request.clienteId(), request.produtoId()).orElseGet(() -> {
            Favorito favorito = new Favorito();
            favorito.setCliente(usuarios.findById(request.clienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado")));
            favorito.setProduto(produtos.findById(request.produtoId()).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado")));
            return favoritos.save(favorito);
        });
    }
    @DeleteMapping public void remover(@RequestBody FavoritoRequest request) {
        favoritos.findByClienteIdAndProdutoId(request.clienteId(), request.produtoId()).ifPresent(favoritos::delete);
    }
}
