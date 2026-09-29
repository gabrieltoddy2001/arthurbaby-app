package br.com.arthurbaby.dto;
/** O app envia {@code motivo}; {@code motivoCancelamento} e o nome usado no endpoint generico de status. Ambos sao aceitos. */
public record CancelarPedidoRequest(String motivo, String motivoCancelamento) {
    public String motivoEfetivo() {
        if (motivoCancelamento != null && !motivoCancelamento.isBlank()) return motivoCancelamento.trim();
        return motivo == null ? null : motivo.trim();
    }
}
