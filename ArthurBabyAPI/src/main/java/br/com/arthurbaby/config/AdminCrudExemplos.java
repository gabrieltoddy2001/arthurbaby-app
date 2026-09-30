package br.com.arthurbaby.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Exemplos de corpo JSON do CRUD genérico {@code /api/admin/{tipo}}, um para cada tipo aceito.
 * Aparecem no seletor "Examples" do Swagger nas rotas de criar (POST) e atualizar (PUT); quem os
 * injeta na documentação é {@link OpenApiConfig#exemplosAdminCrud()}.
 * <p>
 * Os nomes dos campos seguem as entidades JPA (pacote {@code entity}). Relacionamentos são
 * informados só pelo id, no formato {@code "produto": { "id": 1 }}. Os ids usados aqui existem
 * na carga inicial do banco (dump / DataInitializer): usuário 1 = admin, 2 = vendedor, 3 = cliente Ana.
 */
public final class AdminCrudExemplos {
    private AdminCrudExemplos() {
    }

    /** Exemplo de um tipo: rótulo do combobox, texto explicativo e o JSON do corpo. */
    public record Exemplo(String resumo, String descricao, String json) {
    }

    /** Ordem de inserção = ordem em que os tipos aparecem no combobox do Swagger. */
    public static final Map<String, Exemplo> EXEMPLOS = new LinkedHashMap<>();

    static {
        add("usuarios", "Usuário",
                "Usuário do sistema. perfil: ADMINISTRADOR, VENDEDOR ou CLIENTE; status: ATIVO, INATIVO ou BLOQUEADO. "
                        + "A senha pode ir em texto puro (a API grava o hash BCrypt). No PUT, omita a senha para manter a atual.",
                """
                {
                  "nomeCompleto": "Maria Oliveira",
                  "email": "maria.oliveira@email.com",
                  "senha": "123456",
                  "telefone": "(71) 98888-7777",
                  "cpf": "123.456.789-09",
                  "perfil": "CLIENTE",
                  "status": "ATIVO",
                  "aceiteTermoUso": true,
                  "aceiteLgpd": true
                }""");
        add("enderecos", "Endereço",
                "Endereço de entrega de um cliente. \"usuario.id\" é o dono do endereço.",
                """
                {
                  "usuario": { "id": 3 },
                  "cep": "40020-455",
                  "logradouro": "Avenida Sete de Setembro",
                  "numero": "1200",
                  "complemento": "Apto 301",
                  "bairro": "Centro",
                  "cidade": "Salvador",
                  "uf": "BA",
                  "referencia": "Próximo ao Relógio de São Pedro",
                  "principal": true
                }""");
        add("categorias", "Categoria",
                "Categoria do catálogo. status: ATIVA ou INATIVA. Para subcategoria, informe \"categoriaPai\": { \"id\": 1 }.",
                """
                {
                  "nome": "Bodies",
                  "descricao": "Bodies de manga curta e longa",
                  "icone": "bodies",
                  "ordemExibicao": 11,
                  "status": "ATIVA"
                }""");
        add("marcas", "Marca",
                "Marca dos produtos. status: ATIVA ou INATIVA.",
                """
                {
                  "nome": "ArthurBaby Premium",
                  "status": "ATIVA"
                }""");
        add("produtos", "Produto",
                "Produto do catálogo (sem imagens/variações; cadastre-as em produto-imagens e produto-variacoes, "
                        + "ou use POST /api/admin/produtos/completo). status: ATIVO, INATIVO ou ESGOTADO.",
                """
                {
                  "categoria": { "id": 1 },
                  "marca": { "id": 1 },
                  "codigo": "AB-100",
                  "sku": "AB-100-BODY",
                  "nome": "Body Manga Longa Ursinho",
                  "descricao": "Body 100% algodão com estampa de ursinho.",
                  "preco": 59.90,
                  "precoPromocional": 49.90,
                  "custo": 25.00,
                  "peso": 0.15,
                  "altura": 2.00,
                  "largura": 20.00,
                  "comprimento": 25.00,
                  "estoqueMinimo": 5,
                  "status": "ATIVO",
                  "destaque": true,
                  "promocao": true,
                  "avaliacao": 4.8
                }""");
        add("produto-imagens", "Imagem de produto",
                "Imagem vinculada a um produto. \"principal\": true marca a foto de capa.",
                """
                {
                  "produto": { "id": 1 },
                  "url": "https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto",
                  "descricao": "Foto frontal",
                  "ordemExibicao": 2,
                  "principal": false
                }""");
        add("tamanhos", "Tamanho",
                "Tamanho de roupa. status: ATIVO ou INATIVO.",
                """
                {
                  "nome": "16",
                  "ordemExibicao": 13,
                  "status": "ATIVO"
                }""");
        add("cores", "Cor",
                "Cor de produto com o código hexadecimal usado no app. status: ATIVA ou INATIVA.",
                """
                {
                  "nome": "Lilás",
                  "codigoHex": "#C8A2C8",
                  "ordemExibicao": 9,
                  "status": "ATIVA"
                }""");
        add("modelos", "Modelo",
                "Modelo/estampa de produto. status: ATIVO ou INATIVO.",
                """
                {
                  "nome": "Estampado",
                  "status": "ATIVO"
                }""");
        add("produto-variacoes", "Variação de produto",
                "Combinação tamanho/cor/modelo de um produto, com SKU e estoque próprios. "
                        + "\"preco\" nulo usa o preço do produto. status: ATIVA ou INATIVA.",
                """
                {
                  "produto": { "id": 1 },
                  "tamanho": { "id": 6 },
                  "cor": { "id": 3 },
                  "modelo": { "id": 3 },
                  "sku": "KIDS-001-2-ROSA-FLORAL",
                  "preco": 89.90,
                  "estoqueAtual": 10,
                  "estoqueMinimo": 2,
                  "status": "ATIVA"
                }""");
        add("movimentacoes-estoque", "Movimentação de estoque",
                "Registro no histórico de estoque. tipo: ENTRADA, SAIDA, AJUSTE, RESERVA ou ESTORNO. "
                        + "Este cadastro NÃO altera o estoque da variação; para isso use /api/admin/estoque/variacoes/{variacaoId}.",
                """
                {
                  "produto": { "id": 2 },
                  "variacao": { "id": 1 },
                  "usuario": { "id": 1 },
                  "tipo": "ENTRADA",
                  "quantidade": 5,
                  "estoqueAnterior": 10,
                  "estoquePosterior": 15,
                  "observacao": "Reposição do fornecedor"
                }""");
        add("pedidos", "Pedido",
                "Cabeçalho do pedido (os itens vão em pedido-itens). Pedidos de clientes normalmente são criados por "
                        + "POST /api/pedidos. status: RASCUNHO, PEDIDO_GERADO, EM_ANALISE, AGUARDANDO_CONFIRMACAO, CONFIRMADO, "
                        + "SEPARANDO_PRODUTOS, PRONTO_PARA_RETIRADA, EM_TRANSPORTE, ENTREGUE ou CANCELADO; "
                        + "formaRecebimento: RETIRADA_LOJA ou ENTREGA.",
                """
                {
                  "numeroPedido": "PED-ADMIN-0001",
                  "cliente": { "id": 3 },
                  "vendedor": { "id": 2 },
                  "status": "PEDIDO_GERADO",
                  "formaRecebimento": "RETIRADA_LOJA",
                  "subtotal": 129.90,
                  "desconto": 0.00,
                  "frete": 0.00,
                  "total": 129.90,
                  "observacao": "Pedido lançado pelo balcão"
                }""");
        add("pedido-itens", "Item de pedido",
                "Item de um pedido existente. Os dados do produto (código, nome, valores) ficam gravados no item "
                        + "para o histórico não mudar se o produto for alterado depois.",
                """
                {
                  "pedido": { "id": 1 },
                  "produto": { "id": 2 },
                  "variacao": { "id": 1 },
                  "codigoProduto": "AB-001",
                  "nomeProduto": "Kit Enxoval Bebe",
                  "variacaoDescricao": "RN / Branco / Padrão",
                  "quantidade": 1,
                  "valorUnitario": 129.90,
                  "desconto": 0.00,
                  "valorTotal": 129.90
                }""");
        add("pedido-status-historicos", "Histórico de status do pedido",
                "Linha do histórico de status de um pedido. Não muda o status do pedido em si; "
                        + "para isso use PUT /api/pedidos/{id}/status.",
                """
                {
                  "pedido": { "id": 1 },
                  "usuario": { "id": 2 },
                  "statusAnterior": "PEDIDO_GERADO",
                  "statusNovo": "EM_ANALISE",
                  "observacao": "Pedido conferido pelo vendedor"
                }""");
        add("configuracoes-loja", "Configuração da loja",
                "Dados institucionais e cores da loja exibidos no app. Normalmente existe um único registro (id 1): use PUT /api/admin/configuracoes-loja/1.",
                """
                {
                  "nomeFantasia": "ARTHURBABY",
                  "razaoSocial": "ARTHUR BABY ENXOVAIS LTDA - ME",
                  "cnpj": "35.671.222/0001-10",
                  "telefone": "(71) 99339-9816",
                  "whatsapp": "(71) 99131-1944",
                  "email": "arthuradorno13@gmail.com",
                  "emailPedidos": "pedidoababy@gmail.com",
                  "instagram": "@lojao_arthur_baby",
                  "shopeeUrl": "https://shopee.com.br/arthurbabylojao",
                  "facebookUrl": null,
                  "logradouro": "Avenida Sete de Setembro",
                  "numero": "548",
                  "complemento": "Edf. Fatima Loja Lj",
                  "bairro": "Centro - Dois de Julho",
                  "cidade": "Salvador",
                  "uf": "BA",
                  "cep": "40020-455",
                  "logoUrl": null,
                  "corPrimaria": "#37B6B0",
                  "corDestaque": "#ED83A4",
                  "corAcento": null,
                  "corFundo": "#FFFFFF",
                  "corSuperficie": "#F0F2F5",
                  "corTextoPrimario": null,
                  "corTextoSecundario": null
                }""");
        add("favoritos", "Favorito",
                "Produto favoritado por um cliente. O par cliente/produto é único (repetir gera 409).",
                """
                {
                  "cliente": { "id": 3 },
                  "produto": { "id": 1 }
                }""");
    }

    private static void add(String tipo, String resumo, String descricao, String json) {
        EXEMPLOS.put(tipo, new Exemplo(resumo, descricao, json));
    }
}
