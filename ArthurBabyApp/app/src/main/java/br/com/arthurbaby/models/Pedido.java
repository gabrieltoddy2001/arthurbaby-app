package br.com.arthurbaby.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Pedido implements Serializable {
    private String numeroPedido;
    private Date data;
    private String statusAtual;
    private double subtotal;
    private double desconto;
    private double frete;
    private double total;
    private String cupom;
    private String formaRecebimento;
    private String observacao;
    private List<ItemCarrinho> itens;
    private List<PedidoStatus> historico;

    public Pedido(String numeroPedido, Date data, String statusAtual,
                  double subtotal, double desconto, double frete, double total,
                  String cupom, String formaRecebimento, String observacao,
                  List<ItemCarrinho> itens, List<PedidoStatus> historico) {
        this.numeroPedido = numeroPedido;
        this.data = data;
        this.statusAtual = statusAtual;
        this.subtotal = subtotal;
        this.desconto = desconto;
        this.frete = frete;
        this.total = total;
        this.cupom = cupom;
        this.formaRecebimento = formaRecebimento;
        this.observacao = observacao;
        this.itens = itens;
        this.historico = historico;
    }

    public String getNumeroPedido() { return numeroPedido; }
    public Date getData() { return data; }
    public String getStatusAtual() { return statusAtual; }
    public double getSubtotal() { return subtotal; }
    public double getDesconto() { return desconto; }
    public double getFrete() { return frete; }
    public double getTotal() { return total; }
    public String getCupom() { return cupom; }
    public String getFormaRecebimento() { return formaRecebimento; }
    public String getObservacao() { return observacao; }
    public List<ItemCarrinho> getItens() { return itens; }
    public List<PedidoStatus> getHistorico() { return historico; }
}