package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoVariacao;

/**
 * Mensagens de erro exibidas ao cliente (app) e à equipe (painel), num só lugar.
 *
 * <p>Padrão: frase completa em português, com acentos, terminada em ponto, citando o produto pelo nome
 * (e não pelo SKU) e dizendo o que fazer quando possível. Não usar aspas duplas no texto: o app lê o
 * campo {@code erro} do JSON até a próxima aspa.</p>
 */
public final class MensagensLoja {
    private MensagensLoja() {
    }

    public static String nomeItem(Produto produto, ProdutoVariacao variacao) {
        String nome = produto != null && produto.getNome() != null ? produto.getNome() : "Produto";
        String variacaoTexto = EstoqueService.descreverVariacao(variacao);
        return variacaoTexto == null || variacaoTexto.isBlank() ? nome : nome + " (" + variacaoTexto + ")";
    }

    public static String estoqueInsuficiente(Produto produto, ProdutoVariacao variacao, int disponivel, int solicitado) {
        if (disponivel <= 0) {
            return "Produto sem estoque: " + nomeItem(produto, variacao) + ". Remova o item ou escolha outra opção.";
        }
        return "Estoque insuficiente para " + nomeItem(produto, variacao) + ": disponível " + disponivel
                + (disponivel == 1 ? " unidade" : " unidades") + ", solicitado " + solicitado + ".";
    }

    public static String selecioneVariacao(Produto produto) {
        return "Selecione tamanho, cor ou modelo de " + nomeItem(produto, null) + " antes de finalizar.";
    }

    public static String produtoSemVariacao(Produto produto) {
        return "O produto " + nomeItem(produto, null) + " está indisponível para venda: não possui tamanho/cor com estoque cadastrado.";
    }

    public static String produtoIndisponivel(Produto produto) {
        return "O produto " + nomeItem(produto, null) + " não está disponível para venda no momento.";
    }

    public static String variacaoDeOutroProduto(Produto produto) {
        return "A opção escolhida não pertence ao produto " + nomeItem(produto, null) + ".";
    }

    public static String variacaoInativa(Produto produto, ProdutoVariacao variacao) {
        return "A opção " + nomeItem(produto, variacao) + " não está mais disponível.";
    }
}
