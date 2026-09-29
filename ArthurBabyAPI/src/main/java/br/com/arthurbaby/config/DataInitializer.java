package br.com.arthurbaby.config;

import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.entity.Enums.TipoCupom;
import br.com.arthurbaby.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {
    static final String IMAGEM_PLACEHOLDER = "https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto";

    /** Roda depois da {@link SchemaMigration} (ordem 1). Cada bloco so insere o que ainda falta. */
    @Bean
    @Order(2)
    CommandLineRunner seed(UsuarioRepository usuarios, CategoriaRepository categorias, MarcaRepository marcas,
                           TamanhoRepository tamanhos, CorRepository cores, ModeloRepository modelos,
                           ProdutoRepository produtos, ProdutoImagemRepository imagens, ProdutoVariacaoRepository variacoes,
                           ConfiguracaoLojaRepository configs, CupomRepository cupons, PasswordEncoder encoder) {
        return args -> {
            if (configs.count() == 0) {
                ConfiguracaoLoja c = new ConfiguracaoLoja();
                c.setNomeFantasia("ARTHURBABY"); c.setRazaoSocial("ARTHUR BABY ENXOVAIS LTDA - ME");
                c.setCnpj("35.671.222/0001-10"); c.setTelefone("(71) 99339-9816"); c.setWhatsapp("(71) 99131-1944");
                c.setEmail("arthuradorno13@gmail.com"); c.setEmailPedidos("pedidoababy@gmail.com");
                c.setInstagram("@lojao_arthur_baby"); c.setShopeeUrl("https://shopee.com.br/arthurbabylojao");
                c.setLogradouro("Avenida Sete de Setembro"); c.setNumero("548"); c.setComplemento("Edf. Fatima Loja Lj");
                c.setBairro("Centro - Dois de Julho"); c.setCidade("Salvador"); c.setUf("BA"); c.setCep("40020-455");
                c.setCorPrimaria("#37B6B0"); c.setCorDestaque("#ED83A4"); c.setCorFundo("#FFFFFF"); c.setCorSuperficie("#F0F2F5");
                configs.save(c);
            }
            if (usuarios.count() == 0) {
                Usuario admin = usuario("Administrador ArthurBaby", "admin@arthurbaby.com.br", "123456", Perfil.ADMINISTRADOR, encoder);
                Usuario vendedor = usuario("Carlos Almeida", "vendedor@arthurbaby.com.br", "123456", Perfil.VENDEDOR, encoder);
                Usuario ana = usuario("Ana Souza", "ana.souza@email.com", "123456", Perfil.CLIENTE, encoder);
                Usuario mariana = usuario("Mariana Santos", "mariana.santos@email.com", "123456", Perfil.CLIENTE, encoder);
                Usuario mariaTeste = usuario("Maria Teste Silva Atualizada", "maria.teste@exemplo.com", "123456", Perfil.CLIENTE, encoder);
                usuarios.saveAll(List.of(admin, vendedor, ana, mariana, mariaTeste));
            }
            if (categorias.count() == 0) {
                int i = 1;
                for (String nome : List.of("Enxoval", "Roupas para Bebes", "Roupas Infantis", "Acessorios", "Higiene e Cuidados",
                        "Alimentacao", "Quarto do Bebe", "Presentes", "Kits", "Promocoes")) {
                    Categoria c = new Categoria();
                    c.setNome(nome); c.setDescricao("Categoria " + nome); c.setOrdemExibicao(i++);
                    c.setIcone(iconeDe(nome));
                    categorias.save(c);
                }
                Categoria enxoval = categorias.findAll().get(0);
                int j = 1;
                for (String nome : List.of("Kit Berco", "Mantas e Cobertores", "Toalhas")) {
                    Categoria sub = new Categoria();
                    sub.setNome(nome); sub.setDescricao("Subcategoria de Enxoval"); sub.setOrdemExibicao(j++);
                    sub.setCategoriaPai(enxoval);
                    categorias.save(sub);
                }
            }
            garantirCupom(cupons, "ARTHUR10", TipoCupom.PERCENTUAL, "10.00", "10% de desconto");
            garantirCupom(cupons, "FRETEGRATIS", TipoCupom.FRETE_GRATIS, null, "Frete gratis");
            garantirCupom(cupons, "BEMVINDO", TipoCupom.VALOR_FIXO, "15.00", "R$ 15 de desconto");
            if (tamanhos.count() == 0) {
                int i = 1; for (String nome : List.of("RN", "P", "M", "G", "GG", "2", "4", "6", "8", "10", "12", "14")) {
                    Tamanho t = new Tamanho(); t.setNome(nome); t.setOrdemExibicao(i++); tamanhos.save(t);
                }
            }
            if (cores.count() == 0) {
                String[][] cs = {{"Branco","#FFFFFF"},{"Azul","#3B82F6"},{"Rosa","#EC4899"},{"Amarelo","#FFC000"},{"Verde","#4CB896"}};
                for (int i = 0; i < cs.length; i++) { Cor cor = new Cor(); cor.setNome(cs[i][0]); cor.setCodigoHex(cs[i][1]); cor.setOrdemExibicao(i + 1); cores.save(cor); }
            }
            for (String nome : List.of("Padrão", "Premium", "Deluxe")) {
                if (modelos.findByNome(nome).isEmpty()) { Modelo m = new Modelo(); m.setNome(nome); modelos.save(m); }
            }
            Modelo modeloPadrao = modelos.findByNome("Padrão").orElseThrow();
            if (marcas.count() == 0) { Marca marca = new Marca(); marca.setNome("ArthurBaby"); marcas.save(marca); }
            if (imagens.count() == 0) completarProdutosDeExemplo(produtos, imagens, variacoes, modeloPadrao);
            // Produto de exemplo completo (variacao com tamanho/cor/modelo, imagem e avaliacao) para o detalhe do app
            if (produtos.findByCodigo("AB-001").isEmpty()) {
                Produto p = new Produto();
                p.setCategoria(categorias.findAll().get(0)); p.setMarca(marcas.findAll().get(0));
                p.setCodigo("AB-001"); p.setSku("AB-001"); p.setNome("Kit Enxoval Bebe");
                p.setDescricao("Produto inicial para testes da API"); p.setPreco(new BigDecimal("129.90"));
                p.setDestaque(true); p.setPromocao(false); p.setAvaliacao(new BigDecimal("5.0"));
                ProdutoVariacao v = new ProdutoVariacao();
                v.setProduto(p); v.setSku("AB-001-RN-BR"); v.setTamanho(tamanhos.findAll().get(0)); v.setCor(cores.findAll().get(0));
                v.setModelo(modeloPadrao); v.setEstoqueAtual(10);
                p.getVariacoes().add(v);
                ProdutoImagem img = new ProdutoImagem();
                img.setProduto(p); img.setUrl(IMAGEM_PLACEHOLDER); img.setDescricao("Imagem de exemplo");
                img.setPrincipal(true); img.setOrdemExibicao(1);
                p.getImagens().add(img);
                produtos.save(p);
            }
        };
    }

    /**
     * Chamado so enquanto o catalogo nao tem nenhuma imagem (banco de desenvolvimento recem-criado ou importado
     * sem imagens): completa os produtos existentes com imagem placeholder, avaliacao 5.0 e modelo padrao nas
     * variacoes sem modelo. Depois da primeira imagem cadastrada, nao mexe mais nos dados.
     */
    private static void completarProdutosDeExemplo(ProdutoRepository produtos, ProdutoImagemRepository imagens,
                                                   ProdutoVariacaoRepository variacoes, Modelo modeloPadrao) {
        for (ProdutoVariacao v : variacoes.findAll()) {
            if (v.getModelo() == null) { v.setModelo(modeloPadrao); variacoes.save(v); }
        }
        for (Produto p : produtos.findAll()) {
            if (imagens.findByProdutoIdOrderByOrdemExibicaoAsc(p.getId()).isEmpty()) {
                ProdutoImagem img = new ProdutoImagem();
                img.setProduto(p); img.setUrl(IMAGEM_PLACEHOLDER); img.setDescricao("Imagem de exemplo");
                img.setPrincipal(true); img.setOrdemExibicao(1);
                imagens.save(img);
            }
            if (p.getAvaliacao() == null) { p.setAvaliacao(new BigDecimal("5.0")); produtos.save(p); }
        }
    }

    private static void garantirCupom(CupomRepository cupons, String codigo, TipoCupom tipo, String valor, String descricao) {
        var existente = cupons.findByCodigoIgnoreCase(codigo);
        if (existente.isPresent()) {
            // Cupons migrados do esquema antigo nao tinham descricao
            if (existente.get().getDescricao() == null) { existente.get().setDescricao(descricao); cupons.save(existente.get()); }
            return;
        }
        Cupom cupom = new Cupom();
        cupom.setCodigo(codigo); cupom.setTipo(tipo); cupom.setDescricao(descricao);
        cupom.setValor(valor == null ? null : new BigDecimal(valor));
        cupons.save(cupom);
    }
    /** Identificador de icone consumido pelo app: nome da categoria em minusculas, sem acentos, com "_" no lugar dos espacos. */
    private static String iconeDe(String nome) {
        return java.text.Normalizer.normalize(nome, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(java.util.Locale.ROOT).replace(' ', '_');
    }
    private Usuario usuario(String nome, String email, String senha, Perfil perfil, PasswordEncoder encoder) {
        Usuario u = new Usuario();
        u.setNomeCompleto(nome); u.setEmail(email); u.setSenha(encoder.encode(senha)); u.setPerfil(perfil);
        u.setAceiteLgpd(true); u.setAceiteTermoUso(true);
        return u;
    }
}
