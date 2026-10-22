package br.com.arthurbaby.service;

import br.com.arthurbaby.dto.AvaliacaoRequest;
import br.com.arthurbaby.dto.AvaliacaoResponse;
import br.com.arthurbaby.entity.Avaliacao;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.AvaliacaoRepository;
import br.com.arthurbaby.repository.PedidoRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Avaliações de produto (nota de 1 a 5 + comentário).
 * Regras: só quem recebeu o produto (pedido ENTREGUE) avalia, e uma única vez por produto.
 */
@Service
public class AvaliacaoService {
    static final int NOTA_MINIMA = 1;
    static final int NOTA_MAXIMA = 5;

    private final AvaliacaoRepository avaliacoes;
    private final ProdutoRepository produtos;
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;

    public AvaliacaoService(AvaliacaoRepository avaliacoes, ProdutoRepository produtos, UsuarioRepository usuarios,
                            PedidoRepository pedidos) {
        this.avaliacoes = avaliacoes;
        this.produtos = produtos;
        this.usuarios = usuarios;
        this.pedidos = pedidos;
    }

    /** Lista pública das avaliações do produto, mais recentes primeiro (404 se o produto não existir). */
    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listar(Long produtoId) {
        if (!produtos.existsById(produtoId)) throw new NoSuchElementException("Produto nao encontrado");
        return avaliacoes.findByProdutoIdOrderByCriadoEmDesc(produtoId).stream().map(AvaliacaoResponse::de).toList();
    }

    /**
     * Cria a avaliação do cliente logado. A ordem das checagens segue o que o app mostra ao usuário:
     * dados inválidos (400) → produto/cliente inexistente (404) → já avaliou (400) → não comprou (400).
     */
    @Transactional
    public AvaliacaoResponse avaliar(Long produtoId, AvaliacaoRequest request, Usuario logado) {
        if (request == null) throw new IllegalArgumentException("Informe a nota da avaliacao");
        // O cliente sempre avalia em nome próprio: clienteId do corpo, se informado, tem que ser o do token
        Long clienteId = request.clienteId() != null ? request.clienteId() : logado.getId();
        if (!clienteId.equals(logado.getId())) {
            throw new AccessDeniedException("Avaliacao em nome de outro cliente");
        }
        Integer nota = request.nota();
        if (nota == null || nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new IllegalArgumentException("A nota deve estar entre " + NOTA_MINIMA + " e " + NOTA_MAXIMA);
        }

        Produto produto = produtos.findById(produtoId).orElseThrow(() -> new NoSuchElementException("Produto nao encontrado"));
        Usuario cliente = usuarios.findById(clienteId).orElseThrow(() -> new NoSuchElementException("Cliente nao encontrado"));

        // Checagem amigável antes de bater na UNIQUE (cliente_id, produto_id) do banco
        if (avaliacoes.existsByClienteIdAndProdutoId(clienteId, produtoId)) {
            throw new IllegalArgumentException("Voce ja avaliou este produto");
        }
        if (!pedidos.existsByClienteIdAndStatusAndItensProdutoId(clienteId, PedidoStatus.ENTREGUE, produtoId)) {
            throw new IllegalArgumentException("So e possivel avaliar produtos de pedidos entregues");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setProduto(produto);
        avaliacao.setCliente(cliente);
        avaliacao.setNota(nota);
        avaliacao.setComentario(request.comentario() == null || request.comentario().isBlank() ? null : request.comentario().trim());
        avaliacao = avaliacoes.save(avaliacao);

        // Guarda a média também na coluna produto.avaliacao, usada pela listagem de produtos (vitrine);
        // o detalhe do produto sempre recalcula a média direto da tabela avaliacao.
        produto.setAvaliacao(media(produtoId));
        produtos.save(produto);

        return AvaliacaoResponse.de(avaliacao);
    }

    /** Média das notas com 1 casa decimal (ex.: 4.5); 0.0 se o produto ainda não tem avaliações. */
    @Transactional(readOnly = true)
    public BigDecimal media(Long produtoId) {
        return arredondarMedia(avaliacoes.calcularMediaAvaliacoesByProdutoId(produtoId));
    }

    static BigDecimal arredondarMedia(Double media) {
        return BigDecimal.valueOf(media == null ? 0.0 : media).setScale(1, RoundingMode.HALF_UP);
    }
}
