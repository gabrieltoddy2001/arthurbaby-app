package br.com.arthurbaby.repositories;

import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.models.Variacao;

public class CarrinhoRepository {

    private static CarrinhoRepository instance;
    private final List<ItemCarrinho> itens = new ArrayList<>();

    private static final double FRETE_PADRAO = 15.00;
    private static final double FRETE_GRATIS_ACIMA_DE = 200.00;

    private String formaRecebimento = "RETIRADA_LOJA";

    private CarrinhoRepository() {}

    public static CarrinhoRepository getInstance() {
        if (instance == null) instance = new CarrinhoRepository();
        return instance;
    }

    public void adicionar(Produto produto, int quantidade) {
        adicionar(produto, quantidade, null, null);
    }

    public void adicionar(Produto produto, int quantidade, Variacao variacao) {
        adicionar(produto, quantidade, variacao, null);
    }

    public void adicionar(Produto produto, int quantidade, Variacao variacao, Long variacaoId) {
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId().equals(produto.getId())) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                return;
            }
        }
        itens.add(new ItemCarrinho(produto, quantidade, variacao, variacaoId));
    }

    public void remover(ItemCarrinho item) { itens.remove(item); }

    public void alterarQuantidade(ItemCarrinho item, int novaQtd) {
        if (novaQtd <= 0) itens.remove(item);
        else item.setQuantidade(novaQtd);
    }

    public List<ItemCarrinho> getItens() { return itens; }

    public int getTotalItens() {
        int total = 0;
        for (ItemCarrinho i : itens) total += i.getQuantidade();
        return total;
    }

    public double getSubtotal() {
        double total = 0;
        for (ItemCarrinho i : itens) total += i.getSubtotal();
        return total;
    }

    public double getFrete() {
        if ("RETIRADA_LOJA".equals(formaRecebimento)) return 0;
        if (getSubtotal() >= FRETE_GRATIS_ACIMA_DE) return 0;
        return FRETE_PADRAO;
    }

    public double getTotal() {
        return getSubtotal() + getFrete();
    }

    public String getFormaRecebimento() { return formaRecebimento; }
    public void setFormaRecebimento(String forma) { this.formaRecebimento = forma; }

    public void limpar() {
        itens.clear();
        formaRecebimento = "RETIRADA_LOJA";
    }
}