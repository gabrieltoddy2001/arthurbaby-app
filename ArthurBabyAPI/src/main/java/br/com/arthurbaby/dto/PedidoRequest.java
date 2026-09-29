package br.com.arthurbaby.dto;
import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import java.math.BigDecimal;
import java.util.List;
public record PedidoRequest(Long clienteId, FormaRecebimento formaRecebimento, String cupom, BigDecimal desconto,
                            BigDecimal frete, String observacao, List<PedidoItemRequest> itens) {}
