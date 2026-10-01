package br.com.arthurbaby.network.dto;

import java.math.BigDecimal;
import java.util.List;

public class PedidoRequest {
    public Long clienteId;
    public String formaRecebimento;
    public String cupom;
    public BigDecimal desconto;
    public BigDecimal frete;
    public String observacao;
    public List<PedidoItemRequest> itens;

    public PedidoRequest(Long clienteId, String formaRecebimento,
                         String cupom, BigDecimal desconto, BigDecimal frete,
                         String observacao, List<PedidoItemRequest> itens) {
        this.clienteId = clienteId;
        this.formaRecebimento = formaRecebimento;
        this.cupom = cupom;
        this.desconto = desconto;
        this.frete = frete;
        this.observacao = observacao;
        this.itens = itens;
    }
}