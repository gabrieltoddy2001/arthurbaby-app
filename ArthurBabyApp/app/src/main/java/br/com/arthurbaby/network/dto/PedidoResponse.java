package br.com.arthurbaby.network.dto;

import java.math.BigDecimal;
import java.util.List;

public class PedidoResponse {
    public Long id;
    public String numero;
    public String status;
    public String data;                       // ← era Date
    public ClienteResponse cliente;
    public List<PedidoItemResponse> itens;
    public BigDecimal subtotal;
    public String cupom;
    public BigDecimal desconto;
    public BigDecimal frete;
    public BigDecimal total;
    public String formaRecebimento;
    public String observacao;
    public String motivoCancelamento;
    public String confirmadoEm;              // ← era Date
    public String canceladoEm;               // ← era Date
    public List<PedidoStatusHistoricoResponse> historico;

    public static class ClienteResponse {
        public Long id;
        public String nome;
    }
}