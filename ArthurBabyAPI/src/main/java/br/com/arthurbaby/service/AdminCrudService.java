package br.com.arthurbaby.service;

import br.com.arthurbaby.entity.Auditoria;
import br.com.arthurbaby.entity.Categoria;
import br.com.arthurbaby.entity.ConfiguracaoLoja;
import br.com.arthurbaby.entity.Cor;
import br.com.arthurbaby.entity.Cupom; // NOVO IMPORT
import br.com.arthurbaby.entity.Endereco;
import br.com.arthurbaby.entity.Favorito;
import br.com.arthurbaby.entity.Marca;
import br.com.arthurbaby.entity.Modelo;
import br.com.arthurbaby.entity.MovimentacaoEstoque;
import br.com.arthurbaby.entity.Pedido;
import br.com.arthurbaby.entity.PedidoItem;
import br.com.arthurbaby.entity.PedidoStatusHistorico;
import br.com.arthurbaby.entity.Produto;
import br.com.arthurbaby.entity.ProdutoImagem;
import br.com.arthurbaby.entity.ProdutoVariacao;
import br.com.arthurbaby.entity.Tamanho;
import br.com.arthurbaby.entity.Usuario;
import br.com.arthurbaby.repository.AuditoriaRepository;
import br.com.arthurbaby.repository.CategoriaRepository;
import br.com.arthurbaby.repository.ConfiguracaoLojaRepository;
import br.com.arthurbaby.repository.CorRepository;
import br.com.arthurbaby.repository.CupomRepository; // NOVO IMPORT
import br.com.arthurbaby.repository.EnderecoRepository;
import br.com.arthurbaby.repository.FavoritoRepository;
import br.com.arthurbaby.repository.MarcaRepository;
import br.com.arthurbaby.repository.ModeloRepository;
import br.com.arthurbaby.repository.MovimentacaoEstoqueRepository;
import br.com.arthurbaby.repository.PedidoItemRepository;
import br.com.arthurbaby.repository.PedidoRepository;
import br.com.arthurbaby.repository.PedidoStatusHistoricoRepository;
import br.com.arthurbaby.repository.ProdutoImagemRepository;
import br.com.arthurbaby.repository.ProdutoRepository;
import br.com.arthurbaby.repository.ProdutoVariacaoRepository;
import br.com.arthurbaby.repository.TamanhoRepository;
import br.com.arthurbaby.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanWrapperImpl;
import br.com.arthurbaby.entity.Enums.MovimentoEstoqueTipo;
import br.com.arthurbaby.entity.Enums.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CRUD generico do painel admin. As entidades nunca saem do servico: sao convertidas em mapas JSON
 * dentro da transacao, entao colecoes lazy sao lidas enquanto a sessao do JPA esta aberta.
 *
 * <p>Matriz de permissoes (ver {@link Regra}); a mesma separacao por perfil tambem esta no SecurityConfig:</p>
 * <ul>
 *   <li>Catalogo (categorias, marcas, produtos, imagens, tamanhos, cores, modelos): ADMINISTRADOR e VENDEDOR
 *       criam/editam; excluir so ADMINISTRADOR.</li>
 *   <li>Usuarios e configuracoes da loja: somente ADMINISTRADOR, sem exclusao (desative pelo status).</li>
 *   <li>Pedidos, itens, historicos, variacoes e movimentacoes de estoque: somente leitura aqui. Alteracoes
 *       passam pelos endpoints dedicados, que aplicam as regras de negocio (estoque atomico, historico...).</li>
 *   <li>Auditoria, enderecos e favoritos: somente leitura e somente ADMINISTRADOR. A auditoria e gravada pelo
 *       servidor ({@link AuditoriaService}), nunca pelo navegador.</li>
 * </ul>
 */
@Service
public class AdminCrudService {
    private static final TypeReference<Map<String, Object>> JSON = new TypeReference<>() {};
    /** Palavras so com letras (qualquer idioma), separadas por espaco, hifen, apostrofo ou ". ". */
    private static final Pattern NOME_PESSOA = Pattern.compile("^\\p{L}+(?:(?:\\. ?| |[-'’])\\p{L}+)*\\.?$");

    /** Quem pode ler e quem pode gravar cada tipo do CRUD generico. */
    private enum Regra {
        /** Leitura e escrita para a equipe (ADMINISTRADOR e VENDEDOR); excluir so o ADMINISTRADOR. */
        EQUIPE_EDITA,
        /** Leitura e escrita somente para o ADMINISTRADOR, sem exclusao. */
        ADMIN_EDITA,
        /** Equipe le; gravacao somente pelos endpoints dedicados. */
        EQUIPE_LE,
        /** Somente o ADMINISTRADOR le; ninguem grava pelo CRUD generico. */
        ADMIN_LE
    }

    private static final Map<String, Regra> REGRAS = Map.ofEntries(
            Map.entry("categorias", Regra.EQUIPE_EDITA),
            Map.entry("marcas", Regra.EQUIPE_EDITA),
            Map.entry("produtos", Regra.EQUIPE_EDITA),
            Map.entry("produto-imagens", Regra.EQUIPE_EDITA),
            Map.entry("tamanhos", Regra.EQUIPE_EDITA),
            Map.entry("cores", Regra.EQUIPE_EDITA),
            Map.entry("modelos", Regra.EQUIPE_EDITA),
            Map.entry("cupons", Regra.EQUIPE_EDITA), // NOVO
            Map.entry("usuarios", Regra.ADMIN_EDITA),
            Map.entry("configuracoes-loja", Regra.ADMIN_EDITA),
            Map.entry("produto-variacoes", Regra.EQUIPE_LE),
            Map.entry("movimentacoes-estoque", Regra.EQUIPE_LE),
            Map.entry("pedidos", Regra.EQUIPE_LE),
            Map.entry("pedido-itens", Regra.EQUIPE_LE),
            Map.entry("pedido-status-historicos", Regra.EQUIPE_LE),
            Map.entry("auditoria", Regra.ADMIN_LE),
            Map.entry("enderecos", Regra.ADMIN_LE),
            Map.entry("favoritos", Regra.ADMIN_LE)
    );

    private final Map<String, RecursoAdmin> recursos;
    private final ObjectMapper mapper;
    private final PasswordEncoder encoder;
    private final AuditoriaService auditoriaService;
    private final EstoqueService estoqueService;

    public AdminCrudService(UsuarioRepository usuarios, EnderecoRepository enderecos, CategoriaRepository categorias,
                            MarcaRepository marcas, ProdutoRepository produtos, ProdutoImagemRepository imagens,
                            TamanhoRepository tamanhos, CorRepository cores, ModeloRepository modelos,
                            ProdutoVariacaoRepository variacoes, MovimentacaoEstoqueRepository movimentos,
                            PedidoRepository pedidos, PedidoItemRepository itens,
                            PedidoStatusHistoricoRepository historicos, ConfiguracaoLojaRepository configs,
                            FavoritoRepository favoritos, AuditoriaRepository auditoria, CupomRepository cupons, // NOVO PARAMETRO
                            ObjectMapper mapper, PasswordEncoder encoder,
                            AuditoriaService auditoriaService, EstoqueService estoqueService) {
        this.mapper = mapper;
        this.encoder = encoder;
        this.auditoriaService = auditoriaService;
        this.estoqueService = estoqueService;
        this.recursos = Map.ofEntries(
                recurso("usuarios", usuarios, Usuario.class),
                recurso("enderecos", enderecos, Endereco.class),
                recurso("categorias", categorias, Categoria.class),
                recurso("marcas", marcas, Marca.class),
                recurso("produtos", produtos, Produto.class),
                recurso("produto-imagens", imagens, ProdutoImagem.class),
                recurso("tamanhos", tamanhos, Tamanho.class),
                recurso("cores", cores, Cor.class),
                recurso("modelos", modelos, Modelo.class),
                recurso("cupons", cupons, Cupom.class), // NOVO RECURSO
                recurso("produto-variacoes", variacoes, ProdutoVariacao.class),
                recurso("movimentacoes-estoque", movimentos, MovimentacaoEstoque.class),
                recurso("pedidos", pedidos, Pedido.class),
                recurso("pedido-itens", itens, PedidoItem.class),
                recurso("pedido-status-historicos", historicos, PedidoStatusHistorico.class),
                recurso("configuracoes-loja", configs, ConfiguracaoLoja.class),
                recurso("favoritos", favoritos, Favorito.class),
                recurso("auditoria", auditoria, Auditoria.class)
        );
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(String tipo) {
        RecursoAdmin recurso = recurso(tipo);
        exigirLeitura(tipo);
        return recurso.repository().findAll().stream().map(this::json).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> buscar(String tipo, Long id) {
        RecursoAdmin recurso = recurso(tipo);
        exigirLeitura(tipo);
        return json(recurso.repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado")));
    }

    @Transactional
    public Map<String, Object> criar(String tipo, Object body) {
        RecursoAdmin recurso = recurso(tipo);
        exigirEscrita(tipo);
        Object entity = mapper.convertValue(body, recurso.entityClass());
        new BeanWrapperImpl(entity).setPropertyValue("id", null); // POST sempre cria: nunca sobrescreve um id existente
        prepararUsuario(entity, null);
        java.util.Set<String> skusNovos = prepararProduto(entity, null);
        Object salvo = recurso.repository().save(entity);
        registrarEstoqueInicial(salvo, skusNovos);
        auditoriaService.registrar("CRIAR", recurso.entityClass().getSimpleName(), idDe(salvo),
                "Criou " + descrever(recurso, salvo));
        return json(salvo);
    }

    @Transactional
    public Map<String, Object> atualizar(String tipo, Long id, Object body) {
        RecursoAdmin recurso = recurso(tipo);
        exigirEscrita(tipo);
        Object existente = recurso.repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado"));

        Object entity = mapper.convertValue(body, recurso.entityClass());
        new BeanWrapperImpl(entity).setPropertyValue("id", id);
        boolean trocouSenha = prepararUsuario(entity, existente);
        java.util.Set<String> skusNovos = prepararProduto(entity, existente);
        Object salvo = recurso.repository().save(entity);
        registrarEstoqueInicial(salvo, skusNovos);
        auditoriaService.registrar(trocouSenha ? "RESETAR_SENHA" : "ATUALIZAR", recurso.entityClass().getSimpleName(), id,
                (trocouSenha ? "Redefiniu a senha de " : "Atualizou ") + descrever(recurso, salvo));
        return json(salvo);
    }

    @Transactional
    public void excluir(String tipo, Long id) {
        RecursoAdmin recurso = recurso(tipo);
        exigirAdmin();
        if (REGRAS.get(tipo) != Regra.EQUIPE_EDITA) {
            throw new AccessDeniedException("Exclusao nao permitida para " + tipo + ": desative o registro pelo status");
        }
        Object existente = recurso.repository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro nao encontrado"));
        String descricao = "Excluiu " + descrever(recurso, existente);
        recurso.repository().deleteById(id);
        auditoriaService.registrar("DELETAR", recurso.entityClass().getSimpleName(), id, descricao);
    }

    /* ---------------- Permissoes por perfil ---------------- */

    private void exigirLeitura(String tipo) {
        Regra regra = REGRAS.get(tipo);
        if (regra == Regra.ADMIN_EDITA || regra == Regra.ADMIN_LE) exigirAdmin();
        else exigirEquipe();
    }

    private void exigirEscrita(String tipo) {
        Regra regra = REGRAS.get(tipo);
        if (regra == Regra.EQUIPE_LE || regra == Regra.ADMIN_LE) {
            throw new AccessDeniedException("O tipo " + tipo + " e somente leitura no CRUD generico: use o endpoint dedicado");
        }
        if (regra == Regra.ADMIN_EDITA) exigirAdmin();
        else exigirEquipe();
    }

    private static Usuario exigirEquipe() {
        Usuario logado = AuditoriaService.usuarioLogado();
        if (logado == null || (logado.getPerfil() != Perfil.ADMINISTRADOR && logado.getPerfil() != Perfil.VENDEDOR)) {
            throw new AccessDeniedException("Acesso restrito a ADMINISTRADOR ou VENDEDOR");
        }
        return logado;
    }

    private static Usuario exigirAdmin() {
        Usuario logado = AuditoriaService.usuarioLogado();
        if (logado == null || logado.getPerfil() != Perfil.ADMINISTRADOR) {
            throw new AccessDeniedException("Acesso restrito ao ADMINISTRADOR");
        }
        return logado;
    }

    /* ---------------- Auditoria ---------------- */

    private static Long idDe(Object entity) {
        Object id = new BeanWrapperImpl(entity).getPropertyValue("id");
        return id instanceof Long l ? l : null;
    }

    /** Ex.: "Produto #5 (Body Manga Curta)". Usa o primeiro campo de nome que a entidade tiver. */
    private static String descrever(RecursoAdmin recurso, Object entity) {
        BeanWrapperImpl bean = new BeanWrapperImpl(entity);
        String rotulo = null;
        for (String campo : List.of("nomeCompleto", "nome", "nomeFantasia", "sku", "url")) {
            if (bean.isReadableProperty(campo) && bean.getPropertyValue(campo) != null) {
                rotulo = String.valueOf(bean.getPropertyValue(campo));
                break;
            }
        }
        return recurso.entityClass().getSimpleName() + " #" + idDe(entity) + (rotulo == null ? "" : " (" + rotulo + ")");
    }

    private Map<String, Object> json(Object entity) {
        return mapper.convertValue(entity, JSON);
    }

    /**
     * Usuario criado/alterado pelo ADMINISTRADOR: senha em texto vira hash BCrypt; sem senha no corpo, mantem a atual.
     * Devolve true quando a senha foi trocada (para a auditoria registrar RESETAR_SENHA).
     */
    private boolean prepararUsuario(Object entity, Object existente) {
        if (!(entity instanceof Usuario usuario)) return false;
        if (usuario.getPerfil() == null) throw new IllegalArgumentException("Informe o perfil do usuario");
        Usuario logado = AuditoriaService.usuarioLogado();
        if (existente instanceof Usuario atual && logado != null && logado.getId().equals(atual.getId())
                && (usuario.getPerfil() != atual.getPerfil() || usuario.getStatus() != atual.getStatus())) {
            // Evita que o administrador perca o proprio acesso (ou que a loja fique sem administrador ativo)
            throw new IllegalArgumentException("Voce nao pode alterar o proprio perfil nem o proprio status");
        }
        usuario.setEmail(AuthService.normalizarEmail(usuario.getEmail()));
        usuario.setNomeCompleto(validarNome(usuario.getNomeCompleto()));
        if (existente instanceof Usuario atual) {
            // O painel nao edita enderecos nem aceites: preserva os dados do cadastro em vez de apaga-los
            usuario.setEnderecos(new java.util.ArrayList<>(atual.getEnderecos()));
            usuario.setCriadoEm(atual.getCriadoEm());
            usuario.setAceiteTermoUso(atual.isAceiteTermoUso());
            usuario.setDataAceiteTermoUso(atual.getDataAceiteTermoUso());
            usuario.setAceiteLgpd(atual.isAceiteLgpd());
            usuario.setDataAceiteLgpd(atual.getDataAceiteLgpd());
            usuario.setCredenciaisAlteradasEm(atual.getCredenciaisAlteradasEm()); // campo fora do JSON: preserva
        } else {
            usuario.getEnderecos().forEach(endereco -> endereco.setUsuario(usuario));
        }
        String senha = usuario.getSenha();
        if (senha == null || senha.isBlank()) {
            if (existente instanceof Usuario atual) usuario.setSenha(atual.getSenha());
            else throw new IllegalArgumentException("Senha inicial e obrigatoria");
            return false;
        }
        // Sempre texto puro: um hash pronto no corpo nao e aceito (impede gravar um hash conhecido)
        PoliticaSenha.validar(senha, usuario.getEmail(), usuario.getCpf());
        usuario.setSenha(encoder.encode(senha));
        if (existente != null) usuario.invalidarSessoes(); // senha redefinida: derruba as sessões antigas
        return existente != null;
    }

    /**
     * Nome de pessoa: so letras (com acento), separadas por espaco, hifen, apostrofo ou ponto de abreviacao
     * (ex.: "Ana Clara", "Maria D'Avila", "Joao P. Souza"). Recusa numeros, simbolos e emojis.
     * Devolve o nome sem espacos sobrando.
     */
    static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do usuário é obrigatório.");
        }
        String limpo = Normalizer.normalize(nome, Normalizer.Form.NFC).strip().replaceAll("\\s+", " ");
        if (limpo.length() > 150) {
            throw new IllegalArgumentException("O nome deve ter no máximo 150 caracteres.");
        }
        if (!NOME_PESSOA.matcher(limpo).matches() || limpo.codePoints().filter(Character::isLetter).count() < 2) {
            throw new IllegalArgumentException("Nome inválido: use apenas letras e espaços (hífen, apóstrofo e ponto de abreviação são aceitos).");
        }
        return limpo;
    }

    /**
     * Produto com imagens/variacoes aninhadas: o JSON nao traz a referencia de volta (campo WRITE_ONLY),
     * entao cada filho e ligado ao produto aqui. Na edicao, mantem campos que o painel nao envia.
     */
    private java.util.Set<String> prepararProduto(Object entity, Object existente) {
        if (!(entity instanceof Produto produto)) return java.util.Set.of();
        produto.getImagens().forEach(imagem -> imagem.setProduto(produto));
        produto.getVariacoes().forEach(variacao -> variacao.setProduto(produto));

        // Saldo de variacao existente so muda pela tela Estoque (gera movimentacao). Aqui o valor enviado e ignorado.
        Map<Long, ProdutoVariacao> atuais = new java.util.HashMap<>();
        if (existente instanceof Produto atual) atual.getVariacoes().forEach(v -> atuais.put(v.getId(), v));
        java.util.Set<String> skusNovos = new java.util.HashSet<>();
        for (var variacao : produto.getVariacoes()) {
            if (variacao.getId() == null) {
                if (variacao.getSku() != null) skusNovos.add(variacao.getSku().trim().toUpperCase());
                continue;
            }
            ProdutoVariacao atual = atuais.get(variacao.getId());
            if (atual == null) throw new IllegalArgumentException("Variacao " + variacao.getId() + " nao pertence a este produto.");
            variacao.setEstoqueAtual(atual.getEstoqueAtual());
        }

        // O estoque é controlado por variação: produto sem variação venderia sem baixa nem conferência de estoque
        if (produto.getVariacoes().isEmpty()) {
            throw new IllegalArgumentException("Cadastre pelo menos uma variação (tamanho/cor/modelo): o estoque é controlado por variação.");
        }
        java.util.Set<String> skus = new java.util.HashSet<>();
        for (var variacao : produto.getVariacoes()) {
            String sku = variacao.getSku() == null ? "" : variacao.getSku().trim().toUpperCase();
            if (sku.isEmpty()) throw new IllegalArgumentException("Informe o SKU de cada variação.");
            if (!skus.add(sku)) throw new IllegalArgumentException("SKU repetido nas variações: " + variacao.getSku().trim() + ".");
            if (variacao.getEstoqueAtual() < 0) throw new IllegalArgumentException("O estoque não pode ficar negativo (SKU " + variacao.getSku().trim() + ").");
        }
        // Status automático: ATIVO sem saldo vira ESGOTADO; ESGOTADO com saldo volta a ATIVO. INATIVO não muda.
        int saldo = produto.getVariacoes().stream()
                .filter(v -> v.getStatus() != br.com.arthurbaby.entity.Enums.VariacaoStatus.INATIVA)
                .mapToInt(br.com.arthurbaby.entity.ProdutoVariacao::getEstoqueAtual).sum();
        if (produto.getStatus() == br.com.arthurbaby.entity.Enums.ProdutoStatus.ATIVO && saldo <= 0) {
            produto.setStatus(br.com.arthurbaby.entity.Enums.ProdutoStatus.ESGOTADO);
        } else if (produto.getStatus() == br.com.arthurbaby.entity.Enums.ProdutoStatus.ESGOTADO && saldo > 0) {
            produto.setStatus(br.com.arthurbaby.entity.Enums.ProdutoStatus.ATIVO);
        }
        if (existente instanceof Produto atual) {
            produto.setCriadoEm(atual.getCriadoEm());
            if (produto.getAvaliacao() == null) produto.setAvaliacao(atual.getAvaliacao());
        }
        return skusNovos;
    }

    /** Estoque inicial de variacao nova entra no historico como ENTRADA, igual ao cadastro completo. */
    private void registrarEstoqueInicial(Object salvo, java.util.Set<String> skusNovos) {
        if (!(salvo instanceof Produto produto) || skusNovos.isEmpty()) return;
        Usuario logado = AuditoriaService.usuarioLogado();
        for (ProdutoVariacao variacao : produto.getVariacoes()) {
            int inicial = variacao.getEstoqueAtual();
            if (inicial > 0 && variacao.getSku() != null && skusNovos.contains(variacao.getSku().trim().toUpperCase())) {
                estoqueService.registrar(produto, variacao, logado, MovimentoEstoqueTipo.ENTRADA, inicial, 0, inicial,
                        "Estoque inicial do cadastro");
            }
        }
    }

    private RecursoAdmin recurso(String tipo) {
        RecursoAdmin recurso = recursos.get(tipo);

        if (recurso == null) {
            throw new IllegalArgumentException("Tipo de recurso invalido: " + tipo);
        }

        return recurso;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map.Entry<String, RecursoAdmin> recurso(String tipo, JpaRepository repository, Class<?> entityClass) {
        return Map.entry(tipo, new RecursoAdmin((JpaRepository<Object, Long>) repository, entityClass));
    }

    private record RecursoAdmin(JpaRepository<Object, Long> repository, Class<?> entityClass) {
    }
}