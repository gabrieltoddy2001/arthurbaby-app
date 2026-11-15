package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.entity.Pedido;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static br.com.arthurbaby.entity.Enums.PedidoStatus.*;
import static org.junit.jupiter.api.Assertions.*;

/** Regras da auditoria de segurança: política de senha, limite de tentativas de login e fluxo de status do pedido. */
class RegrasSegurancaTest {

    // ---------------- Política de senha ----------------

    @Test
    void politicaAceitaSenhaComLetraENumeroEntre8e72() {
        assertDoesNotThrow(() -> PoliticaSenha.validar("bebe2026"));
        assertDoesNotThrow(() -> PoliticaSenha.validar("Maternidade 2026!"));
    }

    @Test
    void politicaRecusaCurtaSemLetraSemNumeroOuLongaDemais() {
        for (String fraca : new String[]{null, "", "abc12", "12345678", "somenteletras", "a1".repeat(37)}) {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> PoliticaSenha.validar(fraca));
            assertEquals(PoliticaSenha.DESCRICAO, e.getMessage());
        }
    }

    @Test
    void politicaRecusaSenhaIgualAoEmail() {
        assertThrows(IllegalArgumentException.class, () -> PoliticaSenha.validar("paula2026", "paula2026@teste.com", null));
        assertThrows(IllegalArgumentException.class, () -> PoliticaSenha.validar("PAULA2026@TESTE.COM", "paula2026@teste.com", null));
        assertDoesNotThrow(() -> PoliticaSenha.validar("bebe2026", "paula2026@teste.com", null));
    }

    // ---------------- Limite de tentativas de login ----------------

    @Test
    void cincoErrosNaMesmaContaBloqueiamComHttp429() {
        LimiteTentativasLogin limite = new LimiteTentativasLogin();
        for (int i = 0; i < 4; i++) limite.registrarFalha("ana@email.com", "10.0.0.1");
        assertDoesNotThrow(() -> limite.verificar("ana@email.com", "10.0.0.1"));
        limite.registrarFalha("ana@email.com", "10.0.0.1");
        ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> limite.verificar("ANA@email.com", "10.0.0.9"));
        assertEquals(429, e.getStatusCode().value());
        // Outra conta, de outro IP, continua liberada
        assertDoesNotThrow(() -> limite.verificar("maria@email.com", "10.0.0.2"));
    }

    @Test
    void acertoZeraContadorDaConta() {
        LimiteTentativasLogin limite = new LimiteTentativasLogin();
        for (int i = 0; i < 4; i++) limite.registrarFalha("123.456.789-09", "10.0.0.1");
        limite.registrarSucesso("12345678909"); // CPF com ou sem pontuação é a mesma conta
        for (int i = 0; i < 4; i++) limite.registrarFalha("12345678909", "10.0.0.1");
        assertDoesNotThrow(() -> limite.verificar("123.456.789-09", "10.0.0.3"));
    }

    @Test
    void vinteErrosDoMesmoIpBloqueiamQualquerConta() {
        LimiteTentativasLogin limite = new LimiteTentativasLogin();
        for (int i = 0; i < 20; i++) limite.registrarFalha("spray" + i + "@x.com", "10.9.9.9");
        assertThrows(ResponseStatusException.class, () -> limite.verificar("admin@arthurbaby.com.br", "10.9.9.9"));
        assertDoesNotThrow(() -> limite.verificar("admin@arthurbaby.com.br", "10.0.0.1"));
    }

    // ---------------- Fluxo de status do pedido ----------------

    private static Pedido pedido(PedidoStatus status, FormaRecebimento forma) {
        Pedido p = new Pedido();
        p.setStatus(status);
        p.setFormaRecebimento(forma);
        return p;
    }

    @Test
    void avancaPulandoSoAsEtapasOpcionais() {
        Pedido p = pedido(PEDIDO_GERADO, FormaRecebimento.RETIRADA_LOJA);
        assertEquals(List.of(EM_ANALISE, AGUARDANDO_CONFIRMACAO, CONFIRMADO), FluxoPedido.proximos(p));
        assertDoesNotThrow(() -> FluxoPedido.validarTransicao(p, CONFIRMADO));
        assertThrows(IllegalArgumentException.class, () -> FluxoPedido.validarTransicao(p, SEPARANDO_PRODUTOS));
        assertThrows(IllegalArgumentException.class, () -> FluxoPedido.validarTransicao(p, ENTREGUE));
    }

    @Test
    void voltaSoUmaEtapaERespeitaFormaDeRecebimento() {
        Pedido retirada = pedido(SEPARANDO_PRODUTOS, FormaRecebimento.RETIRADA_LOJA);
        assertEquals(List.of(CONFIRMADO, PRONTO_PARA_RETIRADA), FluxoPedido.proximos(retirada));
        assertThrows(IllegalArgumentException.class, () -> FluxoPedido.validarTransicao(retirada, EM_TRANSPORTE));
        assertThrows(IllegalArgumentException.class, () -> FluxoPedido.validarTransicao(retirada, AGUARDANDO_CONFIRMACAO));

        Pedido entrega = pedido(SEPARANDO_PRODUTOS, FormaRecebimento.ENTREGA);
        assertEquals(List.of(CONFIRMADO, EM_TRANSPORTE), FluxoPedido.proximos(entrega));
        assertThrows(IllegalArgumentException.class, () -> FluxoPedido.validarTransicao(entrega, PRONTO_PARA_RETIRADA));
    }

    @Test
    void entregueECanceladoSaoFinais() {
        for (PedidoStatus fim : new PedidoStatus[]{ENTREGUE, CANCELADO}) {
            Pedido p = pedido(fim, FormaRecebimento.ENTREGA);
            assertTrue(FluxoPedido.proximos(p).isEmpty());
            assertThrows(IllegalArgumentException.class, () -> FluxoPedido.validarTransicao(p, CONFIRMADO));
            assertThrows(IllegalArgumentException.class, () -> FluxoPedido.validarCancelamento(p, true));
        }
    }

    @Test
    void clienteCancelaSoAteConfirmadoEquipeAteAntesDaEntrega() {
        assertDoesNotThrow(() -> FluxoPedido.validarCancelamento(pedido(CONFIRMADO, FormaRecebimento.ENTREGA), false));
        assertThrows(IllegalArgumentException.class,
                () -> FluxoPedido.validarCancelamento(pedido(SEPARANDO_PRODUTOS, FormaRecebimento.ENTREGA), false));
        assertDoesNotThrow(() -> FluxoPedido.validarCancelamento(pedido(EM_TRANSPORTE, FormaRecebimento.ENTREGA), true));
    }
}
