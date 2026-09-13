package br.com.arthurbaby.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.com.arthurbaby.models.Produto;

public class MockData {

    public static List<Produto> getProdutosDestaque() {
        List<Produto> lista = new ArrayList<>();
        lista.add(new Produto(1L, "Conjunto Bebê Azul",
                "Conjunto completo RN em algodão macio",
                new BigDecimal("89.90"), ""));
        lista.add(new Produto(2L, "Manta Rosa",
                "Manta de algodão super macia",
                new BigDecimal("45.50"), ""));
        lista.add(new Produto(3L, "Carrinho de Bebê",
                "Carrinho modelo X com bolsa",
                new BigDecimal("350.00"), ""));
        lista.add(new Produto(4L, "Kit Higiene",
                "Kit completo de higiene infantil",
                new BigDecimal("120.00"), ""));
        lista.add(new Produto(5L, "Body Bebê",
                "Body manga curta 100% algodão",
                new BigDecimal("29.90"), ""));
        return lista;
    }

    public static java.util.List<br.com.arthurbaby.models.Categoria> getCategorias() {
        java.util.List<br.com.arthurbaby.models.Categoria> lista = new java.util.ArrayList<>();

        br.com.arthurbaby.models.Categoria enxoval = new br.com.arthurbaby.models.Categoria(1L, "Enxoval");
        enxoval.addSubcategoria(new br.com.arthurbaby.models.Categoria(11L, "Kit Berço"));
        enxoval.addSubcategoria(new br.com.arthurbaby.models.Categoria(12L, "Mantas"));
        lista.add(enxoval);

        br.com.arthurbaby.models.Categoria roupasBebe = new br.com.arthurbaby.models.Categoria(2L, "Roupas para Bebês");
        roupasBebe.addSubcategoria(new br.com.arthurbaby.models.Categoria(21L, "Bodies"));
        roupasBebe.addSubcategoria(new br.com.arthurbaby.models.Categoria(22L, "Macacões"));
        roupasBebe.addSubcategoria(new br.com.arthurbaby.models.Categoria(23L, "Pijamas"));
        lista.add(roupasBebe);

        br.com.arthurbaby.models.Categoria roupasInfantis = new br.com.arthurbaby.models.Categoria(3L, "Roupas Infantis");
        lista.add(roupasInfantis);

        br.com.arthurbaby.models.Categoria acessorios = new br.com.arthurbaby.models.Categoria(4L, "Acessórios");
        acessorios.addSubcategoria(new br.com.arthurbaby.models.Categoria(41L, "Babadores"));
        acessorios.addSubcategoria(new br.com.arthurbaby.models.Categoria(42L, "Toucas"));
        lista.add(acessorios);

        lista.add(new br.com.arthurbaby.models.Categoria(5L, "Higiene"));
        lista.add(new br.com.arthurbaby.models.Categoria(6L, "Alimentação"));
        lista.add(new br.com.arthurbaby.models.Categoria(7L, "Quarto"));
        lista.add(new br.com.arthurbaby.models.Categoria(8L, "Presentes"));

        return lista;
    }

    // NOVO: Retorna produtos fictícios para uma categoria específica
    public static List<Produto> getProdutosPorCategoria(String categoria) {
        List<Produto> lista = new ArrayList<>();

        // Gera produtos fictícios com base no nome da categoria
        lista.add(new Produto(100L, categoria + " - Modelo A",
                "Produto de " + categoria + " em algodão premium",
                new BigDecimal("59.90"), ""));
        lista.add(new Produto(101L, categoria + " - Modelo B",
                "Produto de " + categoria + " com estampa",
                new BigDecimal("79.90"), ""));
        lista.add(new Produto(102L, categoria + " - Modelo C",
                "Produto de " + categoria + " edição especial",
                new BigDecimal("99.90"), ""));
        lista.add(new Produto(103L, categoria + " - Modelo D",
                "Produto de " + categoria + " super macio",
                new BigDecimal("129.90"), ""));
        lista.add(new Produto(104L, categoria + " - Modelo E",
                "Produto de " + categoria + " importado",
                new BigDecimal("189.90"), ""));

        return lista;
    }
    public static java.util.List<br.com.arthurbaby.models.Pedido> getPedidos() {
        java.util.List<br.com.arthurbaby.models.Pedido> lista = new java.util.ArrayList<>();
        java.util.Date agora = new java.util.Date();

        // Pedido 1 — Entregue
        java.util.List<br.com.arthurbaby.models.ItemCarrinho> itens1 = new java.util.ArrayList<>();
        itens1.add(new br.com.arthurbaby.models.ItemCarrinho(
                new Produto(1L, "Conjunto Bebê Azul", "Conjunto RN",
                        new BigDecimal("89.90"), ""), 2));
        itens1.add(new br.com.arthurbaby.models.ItemCarrinho(
                new Produto(2L, "Manta Rosa", "Manta macia",
                        new BigDecimal("45.50"), ""), 1));

        java.util.List<br.com.arthurbaby.models.PedidoStatus> hist1 = new java.util.ArrayList<>();
        hist1.add(new br.com.arthurbaby.models.PedidoStatus("PEDIDO_GERADO", new java.util.Date(agora.getTime() - 10L*24*60*60*1000)));
        hist1.add(new br.com.arthurbaby.models.PedidoStatus("CONFIRMADO", new java.util.Date(agora.getTime() - 9L*24*60*60*1000)));
        hist1.add(new br.com.arthurbaby.models.PedidoStatus("EM_TRANSPORTE", new java.util.Date(agora.getTime() - 5L*24*60*60*1000)));
        hist1.add(new br.com.arthurbaby.models.PedidoStatus("ENTREGUE", new java.util.Date(agora.getTime() - 3L*24*60*60*1000)));

        lista.add(new br.com.arthurbaby.models.Pedido("1001", new java.util.Date(agora.getTime() - 10L*24*60*60*1000),
                "ENTREGUE", 225.30, itens1, hist1));

        // Pedido 2 — Em transporte
        java.util.List<br.com.arthurbaby.models.ItemCarrinho> itens2 = new java.util.ArrayList<>();
        itens2.add(new br.com.arthurbaby.models.ItemCarrinho(
                new Produto(3L, "Carrinho de Bebê", "Carrinho modelo X",
                        new BigDecimal("350.00"), ""), 1));

        java.util.List<br.com.arthurbaby.models.PedidoStatus> hist2 = new java.util.ArrayList<>();
        hist2.add(new br.com.arthurbaby.models.PedidoStatus("PEDIDO_GERADO", new java.util.Date(agora.getTime() - 5L*24*60*60*1000)));
        hist2.add(new br.com.arthurbaby.models.PedidoStatus("CONFIRMADO", new java.util.Date(agora.getTime() - 4L*24*60*60*1000)));
        hist2.add(new br.com.arthurbaby.models.PedidoStatus("EM_TRANSPORTE", new java.util.Date(agora.getTime() - 1L*24*60*60*1000)));

        lista.add(new br.com.arthurbaby.models.Pedido("1002", new java.util.Date(agora.getTime() - 5L*24*60*60*1000),
                "EM_TRANSPORTE", 350.00, itens2, hist2));

        // Pedido 3 — Aguardando confirmação
        java.util.List<br.com.arthurbaby.models.ItemCarrinho> itens3 = new java.util.ArrayList<>();
        itens3.add(new br.com.arthurbaby.models.ItemCarrinho(
                new Produto(4L, "Kit Higiene", "Kit completo",
                        new BigDecimal("120.00"), ""), 1));

        java.util.List<br.com.arthurbaby.models.PedidoStatus> hist3 = new java.util.ArrayList<>();
        hist3.add(new br.com.arthurbaby.models.PedidoStatus("PEDIDO_GERADO", new java.util.Date(agora.getTime() - 2L*60*60*1000)));
        hist3.add(new br.com.arthurbaby.models.PedidoStatus("AGUARDANDO_CONFIRMACAO", new java.util.Date(agora.getTime() - 1L*60*60*1000)));

        lista.add(new br.com.arthurbaby.models.Pedido("1003", new java.util.Date(agora.getTime() - 2L*60*60*1000),
                "AGUARDANDO_CONFIRMACAO", 120.00, itens3, hist3));

        return lista;
    }
}