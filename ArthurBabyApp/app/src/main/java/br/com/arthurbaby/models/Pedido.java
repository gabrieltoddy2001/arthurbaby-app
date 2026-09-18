package br.com.arthurbaby.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Pedido implements Serializable {
    private String numero;
    private Date data;
    private String statusAtual;
    private double total;
    private List<ItemCarrinho> itens;
    private List<PedidoStatus> historico;

    public Pedido(String numero, Date data, String statusAtual, double total,
                  List<ItemCarrinho> itens, List<PedidoStatus> historico) {
        this.numero = numero;
        this.data = data;
        this.statusAtual = statusAtual;
        this.total = total;
        this.itens = itens;
        this.historico = historico;
    }

    public String getNumero() { return numero; }
    public Date getData() { return data; }
    public String getStatusAtual() { return statusAtual; }
    public double getTotal() { return total; }
    public List<ItemCarrinho> getItens() { return itens; }
    public List<PedidoStatus> getHistorico() { return historico; }
}