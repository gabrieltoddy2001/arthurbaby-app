package br.com.arthurbaby.repositories;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.models.Produto;

public class ProdutoRepository {

    private static ProdutoRepository instance;
    private final List<Produto> produtos = new ArrayList<>();

    private ProdutoRepository() {
        produtos.add(new Produto(1L, "Conjunto Bebê Azul", "Conjunto completo RN em algodão macio", new BigDecimal("89.90"), "", 5));
        produtos.add(new Produto(2L, "Manta Rosa", "Manta de algodão super macia", new BigDecimal("45.50"), "", 8));
        produtos.add(new Produto(3L, "Carrinho de Bebê", "Carrinho modelo X com bolsa", new BigDecimal("350.00"), "", 3));
        produtos.add(new Produto(4L, "Kit Higiene", "Kit completo de higiene infantil", new BigDecimal("120.00"), "", 0)); // ESGOTADO
        produtos.add(new Produto(5L, "Body Bebê", "Body manga curta 100% algodão", new BigDecimal("29.90"), "", 15));
        produtos.add(new Produto(6L, "Babador Estampado", "Babador impermeável", new BigDecimal("19.90"), "", 0)); // ESGOTADO
        produtos.add(new Produto(7L, "Macacão Bebê", "Macacão de plush", new BigDecimal("79.90"), "", 6));
        produtos.add(new Produto(8L, "Kit Berço Completo", "Kit berço com 9 peças", new BigDecimal("450.00"), "", 2));
        produtos.add(new Produto(9L, "Fralda de Pano", "Pacote com 5 fraldas", new BigDecimal("39.90"), "", 20));
        produtos.add(new Produto(10L, "Mamadeira Anticólica", "Mamadeira 260ml", new BigDecimal("55.00"), "", 4));
    }

    /**
     * Produtos em promoção = preço abaixo de R$ 50
     */
    public List<Produto> getPromocoes() {
        List<Produto> promo = new ArrayList<>();
        for (Produto p : produtos) {
            if (p.getPreco().doubleValue() < 50) promo.add(p);
        }
        return promo;
    }

    public static ProdutoRepository getInstance() {
        if (instance == null) instance = new ProdutoRepository();
        return instance;
    }

    public List<Produto> getTodos() {
        return produtos;
    }

    public List<Produto> buscar(String termo) {
        if (termo == null || termo.trim().isEmpty()) return produtos;
        String t = termo.toLowerCase();
        List<Produto> resultado = new ArrayList<>();
        for (Produto p : produtos) {
            if (p.getNome().toLowerCase().contains(t)
                    || p.getDescricao().toLowerCase().contains(t)) {
                resultado.add(p);
            }
        }
        return resultado;
    }
}