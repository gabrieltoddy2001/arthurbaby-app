package br.com.arthurbaby.config;

import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(UsuarioRepository usuarios, CategoriaRepository categorias, MarcaRepository marcas,
                           TamanhoRepository tamanhos, CorRepository cores, ProdutoRepository produtos,
                           ConfiguracaoLojaRepository configs, PasswordEncoder encoder) {
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
                Usuario admin = usuario("Administrador ArthurBaby", "admin@arthurbaby.com.br", "admin123", Perfil.ADMINISTRADOR, encoder);
                Usuario cliente = usuario("Cliente Teste", "cliente@teste.com", "cliente123", Perfil.CLIENTE, encoder);
                usuarios.saveAll(List.of(admin, cliente));
            }
            if (categorias.count() == 0) {
                int i = 1;
                for (String nome : List.of("Enxoval", "Roupas para Bebes", "Roupas Infantis", "Acessorios", "Higiene e Cuidados",
                        "Alimentacao", "Quarto do Bebe", "Presentes", "Kits", "Promocoes")) {
                    Categoria c = new Categoria();
                    c.setNome(nome); c.setDescricao("Categoria " + nome); c.setOrdemExibicao(i++);
                    categorias.save(c);
                }
            }
            if (tamanhos.count() == 0) {
                int i = 1; for (String nome : List.of("RN", "P", "M", "G", "GG", "2", "4", "6", "8", "10", "12", "14")) {
                    Tamanho t = new Tamanho(); t.setNome(nome); t.setOrdemExibicao(i++); tamanhos.save(t);
                }
            }
            if (cores.count() == 0) {
                String[][] cs = {{"Branco","#FFFFFF"},{"Azul","#3B82F6"},{"Rosa","#EC4899"},{"Amarelo","#FFC000"},{"Verde","#4CB896"}};
                for (int i = 0; i < cs.length; i++) { Cor cor = new Cor(); cor.setNome(cs[i][0]); cor.setCodigoHex(cs[i][1]); cor.setOrdemExibicao(i + 1); cores.save(cor); }
            }
            if (marcas.count() == 0) { Marca marca = new Marca(); marca.setNome("ArthurBaby"); marcas.save(marca); }
            if (produtos.count() == 0) {
                Produto p = new Produto();
                p.setCategoria(categorias.findAll().get(0)); p.setMarca(marcas.findAll().get(0));
                p.setCodigo("AB-001"); p.setSku("AB-001"); p.setNome("Kit Enxoval Bebe");
                p.setDescricao("Produto inicial para testes da API"); p.setPreco(new BigDecimal("129.90"));
                p.setDestaque(true); p.setPromocao(false);
                ProdutoVariacao v = new ProdutoVariacao();
                v.setProduto(p); v.setSku("AB-001-RN-BR"); v.setTamanho(tamanhos.findAll().get(0)); v.setCor(cores.findAll().get(0)); v.setEstoqueAtual(10);
                p.getVariacoes().add(v);
                produtos.save(p);
            }
        };
    }
    private Usuario usuario(String nome, String email, String senha, Perfil perfil, PasswordEncoder encoder) {
        Usuario u = new Usuario();
        u.setNomeCompleto(nome); u.setEmail(email); u.setSenha(encoder.encode(senha)); u.setPerfil(perfil);
        u.setAceiteLgpd(true); u.setAceiteTermoUso(true);
        return u;
    }
}
