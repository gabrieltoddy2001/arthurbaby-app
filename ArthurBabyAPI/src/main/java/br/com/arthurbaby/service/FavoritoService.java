package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.FavoritoResponse;
import br.com.arthurbaby.dto.ProdutoResumoResponse;
import br.com.arthurbaby.entity.Favorito;
import br.com.arthurbaby.repository.FavoritoRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/** Favoritos sempre devolvidos como DTO, montado dentro da transacao (evita LazyInitializationException). */
@Service
public class FavoritoService {
    private final FavoritoRepository favoritos;
    private final UsuarioRepository usuarios;
    private final ProdutoRepository produtos;

    public FavoritoService(FavoritoRepository favoritos, UsuarioRepository usuarios, ProdutoRepository produtos) {
        this.favoritos = favoritos;
        this.usuarios = usuarios;
        this.produtos = produtos;
    }

    @Transactional(readOnly = true)
    public List<FavoritoResponse> listar(Long clienteId) {
        return favoritos.findByClienteId(clienteId).stream().map(FavoritoService::toResponse).toList();
    }

    /** Idempotente: se o produto ja estiver favoritado, devolve o favorito existente. */
    @Transactional
    public FavoritoResponse adicionar(Long clienteId, Long produtoId) {
        if (clienteId == null || produtoId == null) throw new IllegalArgumentException("clienteId e produtoId sao obrigatorios");
        Favorito favorito = favoritos.findByClienteIdAndProdutoId(clienteId, produtoId).orElseGet(() -> {
            Favorito novo = new Favorito();
            novo.setCliente(usuarios.findById(clienteId).orElseThrow(() -> new IllegalArgumentException("Cliente nao encontrado")));
            novo.setProduto(produtos.findById(produtoId).orElseThrow(() -> new IllegalArgumentException("Produto nao encontrado")));
            return favoritos.save(novo);
        });
        return toResponse(favorito);
    }

    @Transactional
    public void remover(Long clienteId, Long produtoId) {
        favoritos.findByClienteIdAndProdutoId(clienteId, produtoId).ifPresent(favoritos::delete);
    }

    private static FavoritoResponse toResponse(Favorito f) {
        return new FavoritoResponse(f.getId(), ProdutoResumoResponse.de(f.getProduto()), f.getCriadoEm());
    }
}
