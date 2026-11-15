package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.entity.Pedido;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static br.com.arthurbaby.entity.Enums.PedidoStatus.*;

/**
 * Máquina de estados do pedido (regras únicas para app, painel e API).
 *
 * <pre>
 * RASCUNHO → PEDIDO_GERADO → [EM_ANALISE] → [AGUARDANDO_CONFIRMACAO] → CONFIRMADO → SEPARANDO_PRODUTOS
 *          → EM_TRANSPORTE (entrega) ou PRONTO_PARA_RETIRADA (retirada na loja) → ENTREGUE
 * </pre>
 * <ul>
 *   <li>Avança uma etapa por vez; as etapas entre colchetes são opcionais e podem ser puladas.</li>
 *   <li>Pode voltar uma etapa (correção de engano), registrada no histórico.</li>
 *   <li>EM_TRANSPORTE só para pedidos com entrega; PRONTO_PARA_RETIRADA só para retirada na loja.</li>
 *   <li>ENTREGUE e CANCELADO são finais.</li>
 *   <li>Cancelamento pela equipe: em qualquer etapa antes de ENTREGUE (com motivo; o estoque é estornado).</li>
 *   <li>Cancelamento pelo cliente: só até CONFIRMADO, antes de a loja começar a separar os produtos.</li>
 * </ul>
 */
public final class FluxoPedido {
    private FluxoPedido() {}

    /** Etapas que podem ser puladas ao avançar. */
    private static final Set<PedidoStatus> OPCIONAIS = EnumSet.of(EM_ANALISE, AGUARDANDO_CONFIRMACAO);

    /** Até onde o cliente pode cancelar pelo app. */
    public static final Set<PedidoStatus> CLIENTE_PODE_CANCELAR =
            EnumSet.of(RASCUNHO, PEDIDO_GERADO, EM_ANALISE, AGUARDANDO_CONFIRMACAO, CONFIRMADO);

    public static boolean finalizado(PedidoStatus status) {
        return status == ENTREGUE || status == CANCELADO;
    }

    /** Sequência de etapas do pedido conforme a forma de recebimento. */
    public static List<PedidoStatus> etapas(FormaRecebimento forma) {
        List<PedidoStatus> etapas = new ArrayList<>(List.of(
                RASCUNHO, PEDIDO_GERADO, EM_ANALISE, AGUARDANDO_CONFIRMACAO, CONFIRMADO, SEPARANDO_PRODUTOS));
        etapas.add(forma == FormaRecebimento.ENTREGA ? EM_TRANSPORTE : PRONTO_PARA_RETIRADA);
        etapas.add(ENTREGUE);
        return etapas;
    }

    /** Status para os quais a equipe pode mudar o pedido agora (sem contar CANCELADO). */
    public static List<PedidoStatus> proximos(Pedido pedido) {
        PedidoStatus atual = pedido.getStatus();
        List<PedidoStatus> resultado = new ArrayList<>();
        if (atual == null || finalizado(atual)) return resultado;
        List<PedidoStatus> etapas = etapas(pedido.getFormaRecebimento());
        int idx = etapas.indexOf(atual);
        if (idx < 0) {
            // Pedido antigo em etapa que não combina com a forma de recebimento: só permite concluir
            resultado.add(ENTREGUE);
            return resultado;
        }
        // Voltar uma etapa (correção); RASCUNHO não é destino de volta
        if (idx > 1) resultado.add(etapas.get(idx - 1));
        // Avançar: a próxima etapa e, enquanto ela for opcional, também a seguinte
        for (int j = idx + 1; j < etapas.size(); j++) {
            resultado.add(etapas.get(j));
            if (!OPCIONAIS.contains(etapas.get(j))) break;
        }
        return resultado;
    }

    /** Próxima etapa "normal" (botão Avançar do painel), ou null se finalizado. */
    public static PedidoStatus proximaEtapa(Pedido pedido) {
        if (finalizado(pedido.getStatus())) return null;
        List<PedidoStatus> etapas = etapas(pedido.getFormaRecebimento());
        int idx = etapas.indexOf(pedido.getStatus());
        return idx >= 0 && idx < etapas.size() - 1 ? etapas.get(idx + 1) : null;
    }

    /** Lança IllegalArgumentException (400) se a mudança não for permitida. */
    public static void validarTransicao(Pedido pedido, PedidoStatus novo) {
        PedidoStatus atual = pedido.getStatus();
        if (atual == CANCELADO) throw new IllegalArgumentException("Pedido cancelado não pode mudar de status.");
        if (atual == ENTREGUE) throw new IllegalArgumentException("Pedido entregue não pode mudar de status.");
        if (atual == novo) throw new IllegalArgumentException("O pedido já está com o status " + novo + ".");
        if (novo == EM_TRANSPORTE && pedido.getFormaRecebimento() != FormaRecebimento.ENTREGA) {
            throw new IllegalArgumentException("Pedido para retirada na loja não passa por \"Em transporte\": use \"Pronto para retirada\".");
        }
        if (novo == PRONTO_PARA_RETIRADA && pedido.getFormaRecebimento() == FormaRecebimento.ENTREGA) {
            throw new IllegalArgumentException("Pedido com entrega não passa por \"Pronto para retirada\": use \"Em transporte\".");
        }
        List<PedidoStatus> permitidos = proximos(pedido);
        if (!permitidos.contains(novo)) {
            throw new IllegalArgumentException("Não é possível mudar de " + atual + " para " + novo + ". Permitido agora: "
                    + permitidos.stream().map(Enum::name).collect(Collectors.joining(", ")) + " ou CANCELADO.");
        }
    }

    /** Regra de cancelamento: equipe até antes de ENTREGUE; cliente só até CONFIRMADO. */
    public static void validarCancelamento(Pedido pedido, boolean equipeLoja) {
        PedidoStatus atual = pedido.getStatus();
        if (atual == CANCELADO) throw new IllegalArgumentException("Este pedido já está cancelado.");
        if (atual == ENTREGUE) throw new IllegalArgumentException("Pedido entregue não pode ser cancelado.");
        if (!equipeLoja && !CLIENTE_PODE_CANCELAR.contains(atual)) {
            throw new IllegalArgumentException("Este pedido já está sendo preparado pela loja e não pode mais ser cancelado pelo app. "
                    + "Fale com a loja pelo WhatsApp.");
        }
    }
}
