# ArthurBabyApp — Front-end Android

Aplicativo Android (cliente) do catálogo digital e geração de pedidos da **ARTHURBABY**.

Desenvolvido em **Java + Android Studio**. Consome (futuramente) uma API REST em Spring Boot.

## Sobre o projeto

Este repositório contém **apenas o front-end** (aplicativo Android) com **dados mockados**.
A equipe de back-end deve usar o documento [`CONTRATOS_API.md`](./CONTRATOS_API.md) para
implementar os endpoints e substituir os mocks.

## Status do front

### Funcionalidades

- ✅ Splash com curva rosa/verde, logo e slogan "Tudo para o seu bebê em um só lugar!"
- ✅ Login (e-mail/CPF + senha) com campos com ícones
- ✅ Recuperar senha
- ✅ Cadastro completo (nome, CPF, e-mail, telefone, senha, endereço, LGPD)
    - ✅ Máscaras de CPF, telefone e CEP
    - ✅ Validação de CPF com dígitos verificadores
    - ✅ **Autopreenchimento de endereço via ViaCEP**
- ✅ Home com cabeçalho, busca, banner, **categorias em grid horizontal com ícones coloridos**, produtos em destaque (2 colunas) com estrelas
- ✅ Categorias com **ícones customizados por categoria** e subcategorias em chips
- ✅ Produtos por categoria em grid
- ✅ Detalhe do produto com:
    - ✅ Coração na imagem
    - ✅ Estrelas de avaliação
    - ✅ **Círculos coloridos para cor**
    - ✅ **Botões redondos para tamanho**
    - ✅ Seletor de modelo e quantidade
    - ✅ Botão "ADICIONAR AO CARRINHO" fixo no rodapé, arredondado
- ✅ Carrinho com:
    - ✅ Cards com imagem, nome, variação, preço
    - ✅ **Botão vermelho de remover**
    - ✅ Contador +/-
    - ✅ **Cupom de desconto** (ARTHUR10 = 10%)
    - ✅ Forma de recebimento (Retirada / Entrega)
    - ✅ Observação
    - ✅ Cálculo de frete (R$ 0 retirada; R$ 15 entrega; grátis acima de R$ 200)
- ✅ Confirmação de pedido com ícone verde e "O que acontece agora?"
- ✅ Branding Final com redes sociais (Instagram, WhatsApp, Shopee)
- ✅ Perfil com **avatar circular rosa** e menu com ícones
- ✅ **Meus Pedidos com abas** "Em andamento" / "Finalizado" e status colorido
- ✅ Detalhe do Pedido com itens e histórico
- ✅ Endereços (lista + remoção)
- ✅ Favoritos com coração rosa preenchido
- ✅ **Busca com chips de filtro** (Todas / Promoção / Até R$ 50)
- ✅ Promoções com selo
- ✅ Ajuda em cards com ícones coloridos + botão WhatsApp
- ✅ Sobre com contato e redes sociais
- ✅ Loading visual em ações assíncronas
- ✅ Ícones vetoriais customizados
- ✅ Animações de transição

### Visual

- Paleta institucional: **verde (#37B6B0)** + **rosa (#ED83A4)** + branco
- Cards com cantos arredondados
- Cabeçalhos brancos com texto escuro
- Ícones coloridos por categoria (urso, body, chupeta, mamadeira, etc.)

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 11 |
| IDE | Android Studio |
| SDK mínimo | API 24 (Android 7.0) |
| SDK alvo | API 34 |
| Build | Gradle (Kotlin DSL) |
| Bibliotecas | AndroidX, Material Components, RecyclerView, CardView, ViewPager2, Glide |

## Estrutura de pastas
app/src/main/java/br/com/arthurbaby/
├── activities/ → Splash, Login, Cadastro, RecuperarSenha, MainActivity
├── fragments/ → Home, Categorias, ProdutosCategoria, DetalheProduto,
│ Carrinho, ConfirmaPedido, BrandingFinal, Perfil,
│ MeusPedidos, ListaPedidos, DetalhePedido, Enderecos,
│ Favoritos, Pesquisa, Promocoes, Ajuda, Sobre
├── adapters/ → ProdutoAdapter, CategoriaAdapter, CategoriaIconeAdapter,
│ ItemCarrinhoAdapter, PedidoAdapter, EnderecoAdapter,
│ PedidosPagerAdapter
├── models/ → Produto, Categoria, ItemCarrinho, Variacao, Pedido,
│ PedidoStatus, Endereco
├── repositories/ → ProdutoRepository, CarrinhoRepository,
│ FavoritoRepository, EnderecoRepository
├── mock/ → MockData (dados fictícios)
├── network/ → (a criar) Retrofit
└── utils/ → MaskUtils, LoadingUtils, NavUtils, ViaCepService

text

## Recursos gráficos

### Drawables customizados (ícones de categoria)

- `ic_cat_enxoval` — urso (verde)
- `ic_cat_roupas_bebe` — body (rosa)
- `ic_cat_roupas_infantis` — camiseta (amarelo)
- `ic_cat_acessorios` — chupeta (roxo)
- `ic_cat_higiene` — frasco (verde)
- `ic_cat_alimentacao` — mamadeira (laranja)
- `ic_cat_quarto` — berço (azul)
- `ic_cat_presentes` — presente (rosa choque)
- `ic_cat_promocoes` — fogo (vermelho/amarelo)
- `ic_cat_kits` — caixa (cinza)

### Ícones da interface

- `ic_home`, `ic_categorias`, `ic_busca`, `ic_carrinho`, `ic_perfil`
- `ic_pedidos`, `ic_localizacao`, `ic_favorito`, `ic_favorito_preenchido`
- `ic_ajuda`, `ic_info`, `ic_sair`, `ic_promocao`
- `ic_whatsapp`, `ic_email`, `ic_instagram`, `ic_shopee`, `ic_check_circle`

## Como rodar

1. Abra o projeto no **Android Studio** (versão 2023+)
2. Aguarde o **Gradle Sync**
3. Conecte um dispositivo ou inicie um **emulador** (Pixel 5, API 33+)
4. Clique em **Run ▶** (Shift+F10)

## Como integrar com o backend

1. **Alterar a URL** em `res/values/strings.xml`:
   ```xml
   <string name="base_url">https://api.arthurbaby.com.br/api/</string>
Para emular com o backend rodando na sua máquina:

xml
<string name="base_url">http://10.0.2.2:8080/api/</string>
Criar as classes de rede:

network/ApiService.java (interface Retrofit com os endpoints do CONTRATOS_API.md)

network/RetrofitClient.java (instância do Retrofit)

network/AuthInterceptor.java (adiciona o header Authorization: Bearer <token>)

Adicionar dependências no app/build.gradle.kts:

kotlin
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
Substituir os mocks pelos repositórios Retrofit, um por um

Trocar placeholders de imagem (img.setBackgroundColor(...)) por Glide

Fluxo completo testado
text
Splash → Login → Cadastro → Home
→ Categoria → Produto → Carrinho
→ Cupom → Frete → Confirmação → Branding Final
→ Home
Autor / Contato
Cliente: ARTHURBABY — Enxovais Ltda ME

CNPJ: 35.671.222/0001-10

Endereço: Av. Sete de Setembro, 548 — Salvador/BA

WhatsApp: (71) 99131-1944

E-mail: pedidoababy@gmail.com

Licença
Projeto proprietário — todos os direitos reservados à ARTHURBABY.