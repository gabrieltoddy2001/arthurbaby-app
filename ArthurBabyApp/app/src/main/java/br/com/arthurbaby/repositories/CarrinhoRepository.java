package br.com.arthurbaby.repositories;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.models.Variacao;

public class CarrinhoRepository {

    private static final String PREF_NAME = "arthurbaby_carrinho";
    private static final String KEY_ITENS = "itens";
    private static final String KEY_FORMA = "forma_recebimento";

    private static final double FRETE_PADRAO = 15.00;
    private static final double FRETE_GRATIS_ACIMA_DE = 200.00;

    private static CarrinhoRepository instance;
    private final List<ItemCarrinho> itens = new ArrayList<>();
    private String formaRecebimento = "RETIRADA_LOJA";

    private Context appContext;
    private boolean carregado = false;

    /** Quem quer saber quando a quantidade de itens muda (ex.: contador no ícone do carrinho). */
    public interface OnCarrinhoAlterado { void onAlterado(int totalItens); }
    private final List<OnCarrinhoAlterado> observadores = new ArrayList<>();
    private final android.os.Handler mainHandler =
            new android.os.Handler(android.os.Looper.getMainLooper());

    private CarrinhoRepository() {}

    public static CarrinhoRepository getInstance() {
        if (instance == null) instance = new CarrinhoRepository();
        return instance;
    }

    /**
     * Carrega o carrinho salvo em SharedPreferences.
     * Chame isto no MainActivity.onCreate().
     */
    public void carregar(Context ctx) {
        this.appContext = ctx.getApplicationContext();
        if (carregado) return;
        carregado = true;

        try {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_ITENS, null);
            if (json != null && !json.isEmpty()) {
                Type tipo = new TypeToken<List<ItemCarrinho>>(){}.getType();
                List<ItemCarrinho> salvos = new Gson().fromJson(json, tipo);
                if (salvos != null) {
                    itens.clear();
                    itens.addAll(salvos);
                }
            }
            String forma = prefs.getString(KEY_FORMA, "RETIRADA_LOJA");
            if (forma != null) formaRecebimento = forma;
        } catch (Exception e) {
            android.util.Log.e("CARRINHO", "Erro ao carregar", e);
        }
        notificar();
    }

    /** Registra um observador e já informa a quantidade atual. */
    public void observar(OnCarrinhoAlterado o) {
        if (!observadores.contains(o)) observadores.add(o);
        o.onAlterado(getTotalItens());
    }

    public void pararDeObservar(OnCarrinhoAlterado o) {
        observadores.remove(o);
    }

    private void notificar() {
        mainHandler.post(() -> {
            int total = getTotalItens();
            for (OnCarrinhoAlterado o : new ArrayList<>(observadores)) o.onAlterado(total);
        });
    }

    /**
     * Para alterações feitas direto na lista (ex.: botões +/- da tela do carrinho):
     * salva em disco e atualiza o contador.
     */
    public void salvarAlteracoes() {
        persistir();
    }

    /**
     * Persiste o carrinho em SharedPreferences.
     * Chamado automaticamente em cada mutação.
     */
    private void persistir() {
        notificar();
        if (appContext == null) return;
        try {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = new Gson().toJson(itens);
            prefs.edit()
                    .putString(KEY_ITENS, json)
                    .putString(KEY_FORMA, formaRecebimento)
                    .apply();
        } catch (Exception e) {
            android.util.Log.e("CARRINHO", "Erro ao salvar", e);
        }
    }

    public void adicionar(Produto produto, int quantidade) {
        adicionar(produto, quantidade, null, null);
    }

    public void adicionar(Produto produto, int quantidade, Variacao variacao) {
        adicionar(produto, quantidade, variacao, null);
    }

    public void adicionar(Produto produto, int quantidade, Variacao variacao, Long variacaoId) {
        adicionar(produto, quantidade, variacao, variacaoId, null);
    }

    /**
     * Junta a quantidade só quando é o mesmo produto E a mesma variação.
     * Antes juntava apenas pelo produto: escolher o tamanho M depois do P somava no item P.
     */
    public void adicionar(Produto produto, int quantidade, Variacao variacao, Long variacaoId,
                          java.math.BigDecimal precoVariacao) {
        for (ItemCarrinho item : itens) {
            if (item.mesmoItem(produto.getId(), variacaoId)) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                persistir();
                return;
            }
        }
        ItemCarrinho novo = new ItemCarrinho(produto, quantidade, variacao, variacaoId);
        novo.setPrecoVariacao(precoVariacao);
        itens.add(novo);
        persistir();
    }

    /** Quantidade já no carrinho para o produto/variação (para respeitar o estoque). */
    public int quantidadeNoCarrinho(Long produtoId, Long variacaoId) {
        for (ItemCarrinho item : itens) {
            if (item.mesmoItem(produtoId, variacaoId)) return item.getQuantidade();
        }
        return 0;
    }

    public void remover(ItemCarrinho item) {
        itens.remove(item);
        persistir();
    }

    public void alterarQuantidade(ItemCarrinho item, int novaQtd) {
        if (novaQtd <= 0) itens.remove(item);
        else item.setQuantidade(novaQtd);
        persistir();
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

    public void setFormaRecebimento(String forma) {
        this.formaRecebimento = forma;
        persistir();
    }

    public void limpar() {
        itens.clear();
        formaRecebimento = "RETIRADA_LOJA";
        persistir();
    }

    /**
     * Limpa o carrinho E o que está salvo em disco.
     * Use no logout (se quiser começar do zero).
     */
    public void limparTudo() {
        itens.clear();
        formaRecebimento = "RETIRADA_LOJA";
        if (appContext != null) {
            SharedPreferences prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
        notificar();
    }
}