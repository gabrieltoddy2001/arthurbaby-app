package br.com.arthurbaby.config;

import br.com.arthurbaby.entity.*;
import br.com.arthurbaby.entity.Enums.FormaRecebimento;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.entity.Enums.PedidoStatus;
import br.com.arthurbaby.entity.Enums.Perfil;
import br.com.arthurbaby.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Carga de dados de EXEMPLO para desenvolvimento e testes (Swagger, Postman, app e site).
 * <p>
 * Complementa o {@link DataInitializer} (que cria só o mínimo para a API funcionar) com um catálogo mais
 * cheio e dados de movimento: produtos com imagens e variações, clientes com endereço, pedidos em vários
 * status (com itens e histórico), favoritos, avaliações, movimentações de estoque e auditoria.
 * <p>
 * Roda depois do {@link DataInitializer} (ordem 3) e é <b>idempotente</b>: cada registro é procurado pela sua
 * chave natural (código do produto, e-mail do cliente, número do pedido...) e só é criado se ainda não existir.
 * Assim, reiniciar a API não duplica nada, e quem apagar um exemplo o terá de volta no próximo start.
 * Os registros criados aqui usam o prefixo {@code EX-} no código do produto e {@code PED-EX-} no pedido,
 * para ficarem fáceis de identificar.
 */
@Component
@Order(3)
public class DadosExemploInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DadosExemploInitializer.class);

    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final MarcaRepository marcas;
    private final TamanhoRepository tamanhos;
    private final CorRepository cores;
    private final ModeloRepository modelos;
    private final ProdutoRepository produtos;
    private final PedidoRepository pedidos;
    private final FavoritoRepository favoritos;
    private final AvaliacaoRepository avaliacoes;
    private final MovimentacaoEstoqueRepository movimentacoes;
    private final AuditoriaRepository auditorias;
    private final PasswordEncoder encoder;

    public DadosExemploInitializer(UsuarioRepository usuarios, CategoriaRepository categorias, MarcaRepository marcas,
                                   TamanhoRepository tamanhos, CorRepository cores, ModeloRepository modelos,
                                   ProdutoRepository produtos, PedidoRepository pedidos, FavoritoRepository favoritos,
                                   AvaliacaoRepository avaliacoes, MovimentacaoEstoqueRepository movimentacoes,
                                   AuditoriaRepository auditorias, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.categorias = categorias;
        this.marcas = marcas;
        this.tamanhos = tamanhos;
        this.cores = cores;
        this.modelos = modelos;
        this.produtos = produtos;
        this.pedidos = pedidos;
        this.favoritos = favoritos;
        this.avaliacoes = avaliacoes;
        this.movimentacoes = movimentacoes;
        this.auditorias = auditorias;
        this.encoder = encoder;
    }

    /** Uma transação só: ou a carga de exemplo entra inteira, ou nada entra. */
    @Override
    @Transactional
    public void run(String... args) {
        Usuario admin = usuarios.findByEmail("admin@arthurbaby.com.br").orElse(null);
        Usuario vendedor = usuarios.findByEmail("vendedor@arthurbaby.com.br").orElse(admin);
        if (admin == null) {
            log.warn("Carga de exemplo ignorada: usuário admin@arthurbaby.com.br não encontrado");
            return;
        }

        // ---------- Clientes (senha 123456), cada um com um endereço principal ----------
        Usuario beatriz = cliente("Beatriz Lima", "beatriz.lima@email.com", "(71) 98111-2233",
                "41820-020", "Avenida Tancredo Neves", "1632", "Sala 1001", "Caminho das Árvores", "Próximo ao Shopping da Bahia");
        Usuario fernanda = cliente("Fernanda Rocha", "fernanda.rocha@email.com", "(71) 98222-3344",
                "40140-110", "Rua Marques de Caravelas", "85", "Casa", "Barra", "Perto do Farol da Barra");
        Usuario juliana = cliente("Juliana Costa", "juliana.costa@email.com", "(71) 98333-4455",
                "41940-030", "Rua das Hortênsias", "210", "Apto 502", "Pituba", null);
        Usuario rafael = cliente("Rafael Mendes", "rafael.mendes@email.com", "(71) 98444-5566",
                "40301-155", "Rua do Imperador", "12", null, "Liberdade", "Em frente à praça");
        // Clientes criados pelo DataInitializer: recebem pedidos de retirada para os pedidos não ficarem concentrados
        Usuario ana = usuarios.findByEmail("ana.souza@email.com").orElse(juliana);
        Usuario mariana = usuarios.findByEmail("mariana.santos@email.com").orElse(fernanda);

        // ---------- Catálogo ----------
        Marca arthurBaby = marca("ArthurBaby");
        Marca babyConfort = marca("Baby Confort");
        Marca kidsFashion = marca("Kids Fashion");

        // Cada produto: código, nome, categoria, marca, preço, preço promocional, destaque, tamanhos e cores das variações
        Produto body = produto("EX-001", "Body Manga Curta Ursinho", "Body 100% algodão com estampa de ursinho e abertura com botões de pressão.",
                "Roupas para Bebês", babyConfort, "39.90", "34.90", true, "Ursinho", List.of("RN", "P", "M"), List.of("Branco", "Azul"), admin);
        Produto macacao = produto("EX-002", "Macacão Plush Estrelinhas", "Macacão em plush macio, com pezinho, ideal para dias frios.",
                "Roupas para Bebês", babyConfort, "79.90", null, false, "Padrão", List.of("P", "M", "G"), List.of("Rosa", "Azul"), admin);
        Produto manta = produto("EX-003", "Manta Soft Bebê", "Manta antialérgica de microfibra soft, 90 x 110 cm.",
                "Enxoval", arthurBaby, "69.90", null, true, "Padrão", List.of("RN"), List.of("Branco", "Rosa", "Azul"), admin);
        Produto toalha = produto("EX-004", "Toalha de Banho com Capuz", "Toalha felpuda com capuz bordado, 100% algodão.",
                "Enxoval", arthurBaby, "49.90", "44.90", false, "Padrão", List.of("RN"), List.of("Branco", "Amarelo"), admin);
        Produto vestido = produto("EX-005", "Vestido Infantil Floral", "Vestido de tricoline com estampa floral e laço na cintura.",
                "Roupas Infantis", kidsFashion, "89.90", null, true, "Floral", List.of("2", "4", "6"), List.of("Rosa"), admin);
        Produto conjunto = produto("EX-006", "Conjunto Moletom Infantil", "Blusa e calça em moletom flanelado, com punhos canelados.",
                "Roupas Infantis", kidsFashion, "109.90", "99.90", false, "Padrão", List.of("2", "4", "6", "8"), List.of("Azul", "Verde"), admin);
        Produto kitHigiene = produto("EX-007", "Kit Higiene Bebê 4 Peças", "Escova, pente, cortador de unhas e tesourinha de pontas arredondadas.",
                "Higiene e Cuidados", arthurBaby, "59.90", null, false, "Padrão", List.of(), List.of("Branco"), admin);
        Produto babador = produto("EX-008", "Kit 3 Babadores Impermeáveis", "Babadores com forro impermeável e fecho de velcro.",
                "Alimentação", babyConfort, "29.90", "24.90", false, "Padrão", List.of(), List.of("Branco", "Verde"), admin);
        Produto mosquiteiro = produto("EX-009", "Mosquiteiro para Berço", "Mosquiteiro de tule com elástico, serve em berços padrão.",
                "Quarto do Bebê", arthurBaby, "89.90", null, false, "Padrão", List.of(), List.of("Branco"), admin);
        Produto kitPresente = produto("EX-010", "Kit Presente Maternidade", "Caixa presente com body, manta, toalha e naninha.",
                "Presentes", arthurBaby, "199.90", "179.90", true, "Premium", List.of("RN"), List.of("Branco", "Azul", "Rosa"), admin);

        // ---------- Pedidos em vários status (itens: produto e quantidade; usa a 1ª variação do produto) ----------
        pedido("PED-EX-0001", beatriz, vendedor, PedidoStatus.ENTREGUE, FormaRecebimento.ENTREGA, "15.00",
                "Entregar em horário comercial", List.of(item(body, 2), item(manta, 1)));
        pedido("PED-EX-0002", fernanda, vendedor, PedidoStatus.ENTREGUE, FormaRecebimento.RETIRADA_LOJA, "0.00",
                null, List.of(item(kitPresente, 1), item(body, 1)));
        pedido("PED-EX-0003", juliana, vendedor, PedidoStatus.ENTREGUE, FormaRecebimento.ENTREGA, "12.00",
                "Presente: embalar separado", List.of(item(vestido, 1), item(kitHigiene, 1), item(babador, 2)));
        pedido("PED-EX-0004", beatriz, vendedor, PedidoStatus.EM_TRANSPORTE, FormaRecebimento.ENTREGA, "15.00",
                null, List.of(item(conjunto, 1)));
        pedido("PED-EX-0005", ana, vendedor, PedidoStatus.SEPARANDO_PRODUTOS, FormaRecebimento.RETIRADA_LOJA, "0.00",
                "Cliente retira no sábado", List.of(item(macacao, 2), item(toalha, 1)));
        pedido("PED-EX-0006", rafael, vendedor, PedidoStatus.CONFIRMADO, FormaRecebimento.ENTREGA, "18.00",
                null, List.of(item(mosquiteiro, 1), item(manta, 1)));
        pedido("PED-EX-0007", mariana, null, PedidoStatus.PEDIDO_GERADO, FormaRecebimento.RETIRADA_LOJA, "0.00",
                null, List.of(item(vestido, 2)));
        pedido("PED-EX-0008", rafael, vendedor, PedidoStatus.CANCELADO, FormaRecebimento.ENTREGA, "18.00",
                null, List.of(item(kitPresente, 1)));

        // ---------- Favoritos ----------
        favorito(beatriz, kitPresente);
        favorito(beatriz, vestido);
        favorito(fernanda, macacao);
        favorito(juliana, manta);
        favorito(juliana, conjunto);
        favorito(rafael, body);

        // ---------- Avaliações (só de produtos que o cliente recebeu, como exige a regra do app) ----------
        avaliacao(beatriz, body, 5, "Tecido muito macio, meu filho ficou lindo!");
        avaliacao(beatriz, manta, 4, "Quentinha e bonita, mas veio um pouco menor do que eu esperava.");
        avaliacao(fernanda, body, 4, "Boa qualidade, o tamanho P ficou certinho.");
        avaliacao(fernanda, kitPresente, 5, "Presente perfeito, a caixa é linda.");
        avaliacao(juliana, vestido, 5, "Vestido maravilhoso, a estampa é ainda mais bonita pessoalmente.");
        avaliacao(juliana, kitHigiene, 4, null);
        avaliacao(juliana, babador, 3, "Cumprem o papel, mas o velcro solta fácil.");

        // Recalcula a média gravada em produto.avaliacao (usada na vitrine), como faz o AvaliacaoService
        for (Produto p : List.of(body, macacao, manta, toalha, vestido, conjunto, kitHigiene, babador, mosquiteiro, kitPresente)) {
            Double media = avaliacoes.calcularMediaAvaliacoesByProdutoId(p.getId());
            p.setAvaliacao(BigDecimal.valueOf(media == null ? 0.0 : media).setScale(1, RoundingMode.HALF_UP));
            produtos.save(p);
        }

        // ---------- Auditoria (só se a tabela estiver vazia, para não repetir a cada start) ----------
        if (auditorias.count() == 0) {
            auditoria(admin, "CRIAR", "Produto", kitPresente.getId(), "Cadastrou o produto Kit Presente Maternidade");
            auditoria(admin, "ATUALIZAR", "Produto", body.getId(), "Alterou o preço promocional do Body Manga Curta Ursinho");
            auditoria(vendedor, "ALTERAR_STATUS", "Pedido", pedidos.findByNumeroPedido("PED-EX-0004").map(Pedido::getId).orElse(null),
                    "Pedido PED-EX-0004 enviado para transporte");
            auditoria(vendedor, "CANCELAR", "Pedido", pedidos.findByNumeroPedido("PED-EX-0008").map(Pedido::getId).orElse(null),
                    "Pedido PED-EX-0008 cancelado a pedido do cliente");
            auditoria(admin, "RESETAR_SENHA", "Usuario", juliana.getId(), "Resetou a senha da cliente Juliana Costa");
        }
        log.info("Carga de dados de exemplo conferida/aplicada");
    }

    // ===================== Auxiliares =====================

    /** Busca o cliente pelo e-mail; se não existir, cria com senha 123456 e um endereço principal. */
    private Usuario cliente(String nome, String email, String telefone, String cep, String logradouro, String numero,
                           String complemento, String bairro, String referencia) {
        Optional<Usuario> existente = usuarios.findByEmail(email);
        if (existente.isPresent()) return existente.get();

        Usuario u = new Usuario();
        u.setNomeCompleto(nome); u.setEmail(email); u.setSenha(encoder.encode("123456")); u.setTelefone(telefone);
        u.setPerfil(Perfil.CLIENTE); u.setAceiteTermoUso(true); u.setAceiteLgpd(true);
        Endereco e = new Endereco();
        e.setUsuario(u); e.setCep(cep); e.setLogradouro(logradouro); e.setNumero(numero); e.setComplemento(complemento);
        e.setBairro(bairro); e.setCidade("Salvador"); e.setUf("BA"); e.setReferencia(referencia); e.setPrincipal(true);
        u.getEnderecos().add(e); // salvo junto com o usuário (cascade)
        return usuarios.save(u);
    }

    /** Marca pelo nome (sem diferenciar acento/maiúscula); cria se não existir. */
    private Marca marca(String nome) {
        return marcas.findAll().stream().filter(m -> mesmoNome(m.getNome(), nome)).findFirst().orElseGet(() -> {
            Marca m = new Marca();
            m.setNome(nome);
            return marcas.save(m);
        });
    }

    /**
     * Busca o produto pelo código; se não existir, cria com 2 imagens e uma variação para cada combinação
     * tamanho x cor (lista de tamanhos vazia = produto de tamanho único). Cada variação nasce com estoque e
     * gera a movimentação de ENTRADA correspondente, igual ao cadastro pelo painel.
     */
    private Produto produto(String codigo, String nome, String descricao, String nomeCategoria, Marca marca,
                            String preco, String precoPromocional, boolean destaque, String nomeModelo,
                            List<String> nomesTamanhos, List<String> nomesCores, Usuario responsavel) {
        Optional<Produto> existente = produtos.findByCodigo(codigo);
        if (existente.isPresent()) return existente.get();

        Produto p = new Produto();
        p.setCodigo(codigo); p.setSku(codigo); p.setNome(nome); p.setDescricao(descricao);
        p.setCategoria(categoria(nomeCategoria)); p.setMarca(marca);
        p.setPreco(new BigDecimal(preco));
        p.setPrecoPromocional(precoPromocional == null ? null : new BigDecimal(precoPromocional));
        p.setPromocao(precoPromocional != null);
        p.setCusto(new BigDecimal(preco).multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP));
        p.setDestaque(destaque); p.setEstoqueMinimo(3);
        p.setPeso(new BigDecimal("0.30")); p.setAltura(new BigDecimal("5.00"));
        p.setLargura(new BigDecimal("25.00")); p.setComprimento(new BigDecimal("30.00"));

        // Imagens placeholder com o nome do produto escrito (rosa = capa, verde = 2ª foto)
        p.getImagens().add(imagem(p, "ED83A4", nome, "Foto principal", 1, true));
        p.getImagens().add(imagem(p, "37B6B0", nome, "Foto detalhe", 2, false));

        Modelo modelo = modelos.findByNome(nomeModelo).or(() -> modelos.findByNome("Padrão")).orElse(null);
        // Lista com um "null" = uma variação sem tamanho (tamanho único)
        List<String> listaTamanhos = nomesTamanhos.isEmpty() ? java.util.Collections.singletonList(null) : nomesTamanhos;
        int estoque = 8;
        for (String nomeTamanho : listaTamanhos) {
            for (String nomeCor : nomesCores) {
                ProdutoVariacao v = new ProdutoVariacao();
                v.setProduto(p);
                v.setTamanho(nomeTamanho == null ? null : tamanho(nomeTamanho));
                v.setCor(cor(nomeCor));
                v.setModelo(modelo);
                v.setSku(codigo + (nomeTamanho == null ? "-UN" : "-" + nomeTamanho) + "-" + sigla(nomeCor));
                v.setEstoqueAtual(estoque); v.setEstoqueMinimo(2);
                estoque = estoque >= 20 ? 6 : estoque + 4; // varia o estoque entre as variações
                p.getVariacoes().add(v);
            }
        }
        Produto salvo = produtos.save(p);

        for (ProdutoVariacao v : salvo.getVariacoes()) {
            MovimentacaoEstoque m = new MovimentacaoEstoque();
            m.setProduto(salvo); m.setVariacao(v); m.setUsuario(responsavel);
            m.setTipo(MovimentoEstoqueTipo.ENTRADA); m.setQuantidade(v.getEstoqueAtual());
            m.setEstoqueAnterior(0); m.setEstoquePosterior(v.getEstoqueAtual());
            m.setObservacao("Estoque inicial (carga de exemplo)");
            movimentacoes.save(m);
        }
        return salvo;
    }

    private static ProdutoImagem imagem(Produto p, String corFundo, String texto, String descricao, int ordem, boolean principal) {
        ProdutoImagem img = new ProdutoImagem();
        img.setProduto(p);
        img.setUrl("https://placehold.co/400x400/" + corFundo + "/FFFFFF?text=" + URLEncoder.encode(texto, StandardCharsets.UTF_8));
        img.setDescricao(descricao); img.setOrdemExibicao(ordem); img.setPrincipal(principal);
        return img;
    }

    /** Item ainda não gravado: só guarda produto e quantidade até o pedido ser montado. */
    private record ItemExemplo(Produto produto, int quantidade) {
    }

    private static ItemExemplo item(Produto produto, int quantidade) {
        return new ItemExemplo(produto, quantidade);
    }

    /**
     * Cria o pedido (se o número ainda não existir) com itens, totais calculados e o histórico de status
     * percorrendo o fluxo normal até o status final (ex.: PEDIDO_GERADO → EM_ANALISE → CONFIRMADO → ...).
     */
    private void pedido(String numero, Usuario cliente, Usuario vendedor, PedidoStatus statusFinal, FormaRecebimento forma,
                        String frete, String observacao, List<ItemExemplo> itens) {
        if (pedidos.findByNumeroPedido(numero).isPresent()) return;

        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(numero); pedido.setCliente(cliente); pedido.setVendedor(vendedor);
        pedido.setFormaRecebimento(forma); pedido.setObservacao(observacao);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemExemplo ie : itens) {
            Produto p = ie.produto();
            ProdutoVariacao v = p.getVariacoes().isEmpty() ? null : p.getVariacoes().get(0);
            // Preço praticado: promocional quando houver; a variação pode ter preço próprio
            BigDecimal unitario = v != null && v.getPreco() != null ? v.getPreco()
                    : p.getPrecoPromocional() != null ? p.getPrecoPromocional() : p.getPreco();
            BigDecimal total = unitario.multiply(BigDecimal.valueOf(ie.quantidade()));

            PedidoItem pi = new PedidoItem();
            pi.setPedido(pedido); pi.setProduto(p); pi.setVariacao(v);
            // Dados copiados do produto para o histórico não mudar se o produto for alterado depois
            pi.setCodigoProduto(p.getCodigo()); pi.setNomeProduto(p.getNome()); pi.setVariacaoDescricao(descricaoVariacao(v));
            pi.setQuantidade(ie.quantidade()); pi.setValorUnitario(unitario); pi.setValorTotal(total);
            pedido.getItens().add(pi);
            subtotal = subtotal.add(total);
        }
        BigDecimal valorFrete = forma == FormaRecebimento.ENTREGA ? new BigDecimal(frete) : BigDecimal.ZERO;
        pedido.setSubtotal(subtotal); pedido.setFrete(valorFrete); pedido.setTotal(subtotal.add(valorFrete));
        pedido.setStatus(statusFinal);

        // Monta o histórico seguindo o fluxo; pedido cancelado é cancelado logo após a análise
        List<PedidoStatus> fluxo = new ArrayList<>(List.of(PedidoStatus.PEDIDO_GERADO, PedidoStatus.EM_ANALISE, PedidoStatus.CONFIRMADO,
                PedidoStatus.SEPARANDO_PRODUTOS,
                forma == FormaRecebimento.ENTREGA ? PedidoStatus.EM_TRANSPORTE : PedidoStatus.PRONTO_PARA_RETIRADA,
                PedidoStatus.ENTREGUE));
        if (statusFinal == PedidoStatus.CANCELADO) fluxo = new ArrayList<>(List.of(PedidoStatus.PEDIDO_GERADO, PedidoStatus.EM_ANALISE, PedidoStatus.CANCELADO));
        String anterior = null;
        for (PedidoStatus s : fluxo) {
            PedidoStatusHistorico h = new PedidoStatusHistorico();
            h.setPedido(pedido);
            h.setUsuario(s == PedidoStatus.PEDIDO_GERADO ? cliente : vendedor);
            h.setStatusAnterior(anterior); h.setStatusNovo(s.name());
            h.setObservacao(s == PedidoStatus.PEDIDO_GERADO ? "Pedido criado pelo cliente" : "Status alterado (carga de exemplo)");
            pedido.getHistorico().add(h);
            anterior = s.name();
            if (s == statusFinal) break;
        }
        if (statusFinal == PedidoStatus.CANCELADO) {
            pedido.setMotivoCancelamento("Cliente desistiu da compra");
            pedido.setCanceladoEm(java.time.LocalDateTime.now());
        } else if (statusFinal.ordinal() >= PedidoStatus.CONFIRMADO.ordinal()) {
            pedido.setConfirmadoEm(java.time.LocalDateTime.now());
        }
        pedidos.save(pedido);
    }

    private void favorito(Usuario cliente, Produto produto) {
        if (favoritos.findByClienteIdAndProdutoId(cliente.getId(), produto.getId()).isPresent()) return;
        Favorito f = new Favorito();
        f.setCliente(cliente); f.setProduto(produto);
        favoritos.save(f);
    }

    private void avaliacao(Usuario cliente, Produto produto, int nota, String comentario) {
        if (avaliacoes.existsByClienteIdAndProdutoId(cliente.getId(), produto.getId())) return;
        Avaliacao a = new Avaliacao();
        a.setCliente(cliente); a.setProduto(produto); a.setNota(nota); a.setComentario(comentario);
        avaliacoes.save(a);
    }

    private void auditoria(Usuario usuario, String acao, String entidade, Long entidadeId, String descricao) {
        Auditoria a = new Auditoria();
        a.setUsuario(usuario.getNomeCompleto()); a.setAcao(acao); a.setEntidade(entidade);
        a.setEntidadeId(entidadeId); a.setDescricao(descricao);
        auditorias.save(a);
    }

    /** Categoria pelo nome, ignorando acentos (o banco MySQL tem "Bebês" e o H2 "Bebes"); cai na 1ª se não achar. */
    private Categoria categoria(String nome) {
        List<Categoria> todas = categorias.findAll();
        return todas.stream().filter(c -> c.getCategoriaPai() == null && mesmoNome(c.getNome(), nome)).findFirst()
                .orElse(todas.get(0));
    }

    private Tamanho tamanho(String nome) {
        return tamanhos.findAll().stream().filter(t -> mesmoNome(t.getNome(), nome)).findFirst().orElse(null);
    }

    private Cor cor(String nome) {
        return cores.findAll().stream().filter(c -> mesmoNome(c.getNome(), nome)).findFirst().orElse(null);
    }

    /** Texto exibido no item do pedido, no formato "Tamanho / Cor / Modelo" (ex.: "P / Rosa / Padrão"). */
    private static String descricaoVariacao(ProdutoVariacao v) {
        if (v == null) return null;
        List<String> partes = new ArrayList<>();
        partes.add(v.getTamanho() == null ? "Único" : v.getTamanho().getNome());
        if (v.getCor() != null) partes.add(v.getCor().getNome());
        if (v.getModelo() != null) partes.add(v.getModelo().getNome());
        return String.join(" / ", partes);
    }

    /** Três primeiras letras da cor, sem acento, para compor o SKU (ex.: "Branco" → "BRA"). */
    private static String sigla(String cor) {
        String s = normalizar(cor).toUpperCase(Locale.ROOT).replace(" ", "");
        return s.substring(0, Math.min(3, s.length()));
    }

    private static boolean mesmoNome(String a, String b) {
        return a != null && b != null && normalizar(a).equals(normalizar(b));
    }

    /** Minúsculas e sem acentos, para comparar nomes vindos de bancos com ou sem acentuação. */
    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
