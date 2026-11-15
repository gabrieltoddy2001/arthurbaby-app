package br.com.arthurbaby.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import br.com.arthurbaby.models.ItemCarrinho;
import br.com.arthurbaby.models.Produto;
import br.com.arthurbaby.network.dto.ProdutoResponse;
import br.com.arthurbaby.utils.AvaliacaoUtils;

/** Garante que o app continua compatível com o JSON e as regras da ArthurBabyAPI. */
public class ContratoApiTest {

    private static final String PRODUTO_JSON = "{"
            + "\"id\":1,\"nome\":\"Kit Enxoval\",\"preco\":129.90,\"precoPromocional\":99.90,"
            + "\"promocao\":true,\"status\":\"ATIVO\",\"avaliacao\":4.5,"
            + "\"imagens\":[{\"id\":1,\"url\":\"http://x/a.png\",\"principal\":true}]}";

    @Test
    public void produtoComMediaFracionariaNaoQuebraOGson() {
        // Era a causa do "Erro de conexão: java.lang.NumberFormatException" na busca
        ProdutoResponse r = new Gson().fromJson(PRODUTO_JSON, ProdutoResponse.class);
        assertEquals(0, new BigDecimal("4.5").compareTo(r.avaliacao));

        Produto p = Conversor.paraProduto(r);
        assertEquals(4.5, p.getAvaliacao(), 0.0001);
        assertEquals(0, new BigDecimal("99.90").compareTo(p.getPreco()));
        assertEquals(0, new BigDecimal("129.90").compareTo(p.getPrecoOriginal()));
        assertTrue(p.isPromocao());
        assertTrue(p.temDesconto());
        assertFalse(p.isEsgotado());
    }

    @Test
    public void produtoSemPromocaoNaoTemPrecoRiscado() {
        ProdutoResponse r = new Gson().fromJson(
                "{\"id\":2,\"nome\":\"Body\",\"preco\":39.90,\"promocao\":false,\"status\":\"ESGOTADO\",\"avaliacao\":null}",
                ProdutoResponse.class);
        Produto p = Conversor.paraProduto(r);
        assertNull(p.getPrecoOriginal());
        assertFalse(p.isPromocao());
        assertTrue(p.isEsgotado());
        assertEquals(0.0, p.getAvaliacao(), 0.0001);
    }

    @Test
    public void rotasPublicasEspelhamOSecurityConfig() {
        assertTrue(AuthInterceptor.ehPublica("GET", "/api/produtos"));
        assertTrue(AuthInterceptor.ehPublica("GET", "/api/produtos/5"));
        assertTrue(AuthInterceptor.ehPublica("POST", "/api/auth/login"));
        assertTrue(AuthInterceptor.ehPublica("POST", "/api/cupons/validar"));
        // Avaliar exige login: antes ia sem token, recebia 401 e deslogava o usuário
        assertFalse(AuthInterceptor.ehPublica("POST", "/api/produtos/5/avaliacoes"));
        assertFalse(AuthInterceptor.ehPublica("GET", "/api/auth/me"));
        assertFalse(AuthInterceptor.ehPublica("GET", "/api/pedidos/cliente/3"));
    }

    @Test
    public void estrelasRefletemAMedia() {
        assertEquals("★★★★★", AvaliacaoUtils.estrelas(4.5));
        assertEquals("★★★☆☆", AvaliacaoUtils.estrelas(3.2));
        assertEquals("☆☆☆☆☆", AvaliacaoUtils.estrelas(0));
        assertEquals("(4,5)", AvaliacaoUtils.nota(4.5, ""));
        assertEquals("sem", AvaliacaoUtils.nota(0, "sem"));
    }

    @Test
    public void dataDoBackComESemMilissegundos() {
        Calendar c = Calendar.getInstance();
        c.setTime(Conversor.parseData("2026-11-10T14:35:12.123"));
        assertEquals(2026, c.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, c.get(Calendar.MONTH));
        assertEquals(14, c.get(Calendar.HOUR_OF_DAY));

        c.setTime(Conversor.parseData("2026-11-10T14:35:12"));
        assertEquals(35, c.get(Calendar.MINUTE));
    }

    @Test
    public void carrinhoSeparaVariacoesEUsaPrecoDaVariacao() {
        Produto p = new Produto(1L, "Body", "", new BigDecimal("40.00"), null);
        ItemCarrinho tamanhoP = new ItemCarrinho(p, 1, null, 10L);
        tamanhoP.setPrecoVariacao(new BigDecimal("45.00"));

        assertTrue(tamanhoP.mesmoItem(1L, 10L));
        assertFalse(tamanhoP.mesmoItem(1L, 11L));
        assertEquals(45.00, tamanhoP.getSubtotal(), 0.001);

        ItemCarrinho semVariacao = new ItemCarrinho(p, 2);
        assertEquals(80.00, semVariacao.getSubtotal(), 0.001);
        assertTrue(new Date().getTime() > 0);
    }
}
