package br.com.arthurbaby.controller;

import br.com.arthurbaby.dto.FavoritoRequest;
import br.com.arthurbaby.entity.Favorito;
import br.com.arthurbaby.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Lista de produtos favoritos do cliente. Exige token.")
public class FavoritoController {
    private final FavoritoRepository favoritos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;
    public FavoritoController(FavoritoRepository favoritos, UsuarioRepository usuarios, ProdutoRepository produtos) {
        this.favoritos = favoritos;
        this.usuarios = usuarios;
        this.produtos = produtos;
    }
    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar favoritos do cliente", description = "Retorna os produtos favoritados pelo cliente identificado pelo id informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de favoritos do cliente (vazia se não houver)")
    })
    public List<Favorito> listar(@PathVariable Long clienteId) {
        return favoritos.findByClienteId(clienteId);
    }
    @PostMapping
    @Operation(summary = "Adicionar favorito", description = "Adiciona o produto aos favoritos do cliente. Se já estiver favoritado, retorna o favorito existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto favoritado (ou favorito já existente)"),
            @ApiResponse(responseCode = "400", description = "Cliente ou produto não encontrado, ou corpo mal formatado")
    })
    public Favorito adicionar(@RequestBody FavoritoRequest request) {
        return favoritos.findByClienteIdAndProdutoId(request.clienteId(), request.produtoId()).orElseGet(() -> {
            Favorito favorito = new Favorito();
            favorito.setCliente(usuarios.findById(request.clienteId()).orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado")));
            favorito.setProduto(produtos.findById(request.produtoId()).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado")));
            return favoritos.save(favorito);
        });
    }
    @DeleteMapping
    @Operation(summary = "Remover favorito", description = "Remove o produto dos favoritos do cliente informados no corpo da requisição.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorito removido (sem erro se não existia)"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido ou mal formatado")
    })
    public void remover(@RequestBody FavoritoRequest request) {
        favoritos.findByClienteIdAndProdutoId(request.clienteId(), request.produtoId()).ifPresent(favoritos::delete);
    }
}
