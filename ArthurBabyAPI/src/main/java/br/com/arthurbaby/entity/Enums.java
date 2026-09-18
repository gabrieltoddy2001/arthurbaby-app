package br.com.arthurbaby.entity;

public final class Enums {
    private Enums() {
    }

    public enum Perfil { ADMINISTRADOR, VENDEDOR, CLIENTE }
    public enum UsuarioStatus { ATIVO, INATIVO, BLOQUEADO }
    public enum CategoriaStatus { ATIVA, INATIVA }
    public enum AtivoStatus { ATIVO, INATIVO }
    public enum ProdutoStatus { ATIVO, INATIVO, ESGOTADO }
    public enum VariacaoStatus { ATIVA, INATIVA }
    public enum CorStatus { ATIVA, INATIVA }
    public enum MovimentoEstoqueTipo { ENTRADA, SAIDA, AJUSTE, RESERVA, ESTORNO }
    public enum PedidoStatus {
        RASCUNHO, PEDIDO_GERADO, EM_ANALISE, AGUARDANDO_CONFIRMACAO, CONFIRMADO,
        SEPARANDO_PRODUTOS, PRONTO_PARA_RETIRADA, EM_TRANSPORTE, ENTREGUE, CANCELADO
    }
    public enum FormaRecebimento { RETIRADA_LOJA, ENTREGA }
}
