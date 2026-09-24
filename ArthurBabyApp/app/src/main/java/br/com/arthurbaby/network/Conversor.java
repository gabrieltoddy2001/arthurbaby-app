package br.com.arthurbaby.network;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.arthurbaby.R;
import br.com.arthurbaby.models.Categoria;
import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.models.Pedido;
import br.com.arthurbaby.models.PedidoStatus;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.network.dto.CategoriaResponse;
import br.com.arthurbaby.network.dto.PedidoItemResponse;
import br.com.arthurbaby.network.dto.PedidoResponse;
import br.com.arthurbaby.network.dto.PedidoStatusHistoricoResponse;
import br.com.arthurbaby.network.dto.ProdutoResponse;

public class Conversor {

    // ---------- CATEGORIA ----------
    public static Categoria paraCategoria(CategoriaResponse c) {
        if (c == null) return null;
        Categoria cat = new Categoria(c.id, c.nome, iconeResPorNome(c.icone));
        if (c.subcategorias != null) {
            for (CategoriaResponse sub : c.subcategorias) {
                Categoria subCat = new Categoria(sub.id, sub.nome, iconeResPorNome(sub.icone));
                cat.addSubcategoria(subCat);
            }
        }
        return cat;
    }

    public static List<Categoria> paraCategorias(List<CategoriaResponse> lista) {
        List<Categoria> out = new ArrayList<>();
        if (lista == null) return out;
        for (CategoriaResponse c : lista) out.add(paraCategoria(c));
        return out;
    }

    private static int iconeResPorNome(String icone) {
        if (icone == null || icone.isEmpty()) return 0;
        switch (icone) {
            case "enxoval": return R.drawable.ic_cat_enxoval;
            case "roupas_para_bebes": return R.drawable.ic_cat_roupas_bebe;
            case "roupas_infantis": return R.drawable.ic_cat_roupas_infantis;
            case "acessorios": return R.drawable.ic_cat_acessorios;
            case "higiene_e_cuidados": return R.drawable.ic_cat_higiene;
            case "alimentacao": return R.drawable.ic_cat_alimentacao;
            case "quarto_do_bebe": return R.drawable.ic_cat_quarto;
            case "presentes": return R.drawable.ic_cat_presentes;
            case "kits": return R.drawable.ic_cat_kits;
            case "promocoes": return R.drawable.ic_cat_promocoes;
            default: return 0;
        }
    }

    // ---------- PRODUTO ----------
    public static Produto paraProduto(ProdutoResponse p) {
        if (p == null) return null;
        String marca = (p.marca != null && !p.marca.isEmpty()) ? p.marca : "ArthurBaby";
        Produto produto = new Produto(
                p.id,
                p.nome,
                p.descricao,
                p.precoPromocional != null ? p.precoPromocional : p.preco,
                null,
                10,
                marca
        );

        // Avaliação
        produto.setAvaliacao(p.avaliacao);

        // Imagens
        if (p.imagens != null) {
            List<String> urls = new ArrayList<>();
            for (ProdutoResponse.ImagemResponse img : p.imagens) {
                if (img.url != null) urls.add(img.url);
            }
            produto.setImagens(urls);
        }

        // Variações
        if (p.variacoes != null) {
            List<Produto.VariacaoReal> vars = new ArrayList<>();
            for (ProdutoResponse.VariacaoResponse v : p.variacoes) {
                vars.add(new Produto.VariacaoReal(
                        v.id, v.sku, v.tamanho, v.cor, v.modelo, v.preco, v.estoqueAtual));
            }
            produto.setVariacoes(vars);

            // Se tem variações, usa a primeira como estoque do produto
            if (!vars.isEmpty()) {
                int totalEstoque = 0;
                for (Produto.VariacaoReal v : vars) totalEstoque += v.estoqueAtual;
                // como não temos setter para estoque, o produto assume 10 por padrão.
                // Se quiser, adicione setEstoque() no Produto.
            }
        }

        return produto;
    }

    public static List<Produto> paraProdutos(List<ProdutoResponse> lista) {
        List<Produto> out = new ArrayList<>();
        if (lista == null) return out;
        for (ProdutoResponse p : lista) out.add(paraProduto(p));
        return out;
    }

    // ---------- PEDIDO ----------
    public static Pedido paraPedido(PedidoResponse r) {
        if (r == null) return null;

        List<ItemCarrinho> itens = new ArrayList<>();
        if (r.itens != null) {
            for (PedidoItemResponse item : r.itens) {
                Produto p = new Produto(
                        item.produtoId != null ? item.produtoId : 0L,
                        item.nomeProduto != null ? item.nomeProduto : "Produto",
                        "",
                        item.valorUnitario != null ? item.valorUnitario : BigDecimal.ZERO,
                        null,
                        10,
                        "ArthurBaby"
                );
                itens.add(new ItemCarrinho(p, item.quantidade));
            }
        }

        List<PedidoStatus> hist = new ArrayList<>();
        if (r.historico != null) {
            for (PedidoStatusHistoricoResponse h : r.historico) {
                hist.add(new PedidoStatus(h.statusNovo, parseData(h.data)));
            }
        }

        return new Pedido(
                r.numero,
                parseData(r.data),
                r.status,
                r.total != null ? r.total.doubleValue() : 0,
                itens,
                hist
        );
    }

    public static List<Pedido> paraPedidos(List<PedidoResponse> lista) {
        List<Pedido> out = new ArrayList<>();
        if (lista == null) return out;
        for (PedidoResponse r : lista) out.add(paraPedido(r));
        return out;
    }

    private static Date parseData(String texto) {
        if (texto == null || texto.isEmpty()) return new Date();
        try {
            if (texto.contains(".")) {
                int idx = texto.indexOf(".");
                String base = texto.substring(0, idx);
                String frac = texto.substring(idx + 1);
                if (frac.length() > 3) frac = frac.substring(0, 3);
                texto = base + "." + frac;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
            return sdf.parse(texto);
        } catch (Exception e) {
            return new Date();
        }
    }
    // ---------- ENDEREÇO ----------
    public static br.com.arthurbaby.models.Endereco paraEndereco(
            br.com.arthurbaby.network.dto.EnderecoResponse e) {
        if (e == null) return null;
        return new br.com.arthurbaby.models.Endereco(
                e.id, e.cep, e.logradouro, e.numero, e.complemento,
                e.bairro, e.cidade, e.uf, e.referencia, e.principal
        );
    }

    public static List<br.com.arthurbaby.models.Endereco> paraEnderecos(
            List<br.com.arthurbaby.network.dto.EnderecoResponse> lista) {
        List<br.com.arthurbaby.models.Endereco> out = new ArrayList<>();
        if (lista == null) return out;
        for (br.com.arthurbaby.network.dto.EnderecoResponse e : lista)
            out.add(paraEndereco(e));
        return out;
    }
}