package br.com.arthurbaby.dto;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
public record StatusPedidoRequest(PedidoStatus status, String observacao, String motivoCancelamento, Long usuarioId) {}
