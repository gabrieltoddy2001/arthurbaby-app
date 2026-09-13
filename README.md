# ArthurBabyApp — Front-end Android (2026)

Aplicativo Android (cliente) do catálogo digital e geração de pedidos da **ARTHURBABY**.

Desenvolvido em **Java + Android Studio**. Consome (futuramente) uma API REST em Spring Boot.

> **Nota para a equipe:**
> Este é o **front-end** (app Android) do projeto ARTHURBABY.
> Os dados estão **mockados** no momento.
> - Especificação dos endpoints que o backend precisa implementar: [`CONTRATOS_API.md`](./CONTRATOS_API.md)
> - Passo a passo de integração: [`CHECKLIST_INTEGRACAO.md`](./CHECKLIST_INTEGRACAO.md)

---

## 📱 Sobre

Este repositório contém **apenas o front-end** (aplicativo Android) com dados mockados.
A equipe de back-end deve usar o `CONTRATOS_API.md` para implementar os endpoints e
substituir os mocks.

## ✅ Status

- ✅ Splash com logo e animação
- ✅ Login (e-mail/CPF + senha)
- ✅ Recuperar senha
- ✅ Cadastro (nome, CPF, e-mail, telefone, senha, endereço, LGPD)
- ✅ Validação de CPF + máscaras (CPF, telefone, CEP)
- ✅ ViaCEP (autopreenchimento de endereço)
- ✅ Home com banner, categorias, subcategorias, destaques e promoções
- ✅ Categorias (lista + subcategorias + produtos em grade)
- ✅ Detalhe do produto (variações, marca, controle de estoque)
- ✅ Carrinho (subtotal, frete, total, forma de recebimento, observação)
- ✅ Confirmação de pedido com número único
- ✅ Perfil (Meus Pedidos, Detalhe do Pedido, Endereços, Favoritos, Ajuda, Sobre, Sair)
- ✅ Busca com filtros (promoção, faixa de preço)
- ✅ Favoritos com coração nos cards
- ✅ Loading visual, ícones vetoriais e animações de transição
- ❌ Modo escuro (não implementado nesta versão)

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 11 |
| IDE | Android Studio (2024+) |
| SDK mínimo | API 24 (Android 7.0) |
| SDK alvo | API 34 (Android 14) |
| Build | Gradle (Kotlin DSL) |
| Bibliotecas | AndroidX, Material Components, RecyclerView, CardView, Glide |

## 📂 Estrutura de pastas
app/src/main/java/br/com/arthurbaby/
├── activities/ → Splash, Login, Cadastro, RecuperarSenha, MainActivity
├── fragments/ → Home, Categorias, Produtos, Detalhe, Carrinho,
│ Confirmação, Perfil, MeusPedidos, DetalhePedido,
│ Enderecos, Favoritos, Busca, Promocoes, Ajuda, Sobre
├── adapters/ → ProdutoAdapter, CategoriaAdapter, ItemCarrinhoAdapter,
│ PedidoAdapter, EnderecoAdapter
├── models/ → Produto, Categoria, ItemCarrinho, Pedido, PedidoStatus,
│ Endereco, Variacao
├── repositories/ → ProdutoRepository, CarrinhoRepository,
│ FavoritoRepository, EnderecoRepository
├── mock/ → MockData (dados fictícios)
├── network/ → (a criar) Retrofit
└── utils/ → MaskUtils, LoadingUtils, NavUtils, ViaCepService

text

## 🚀 Como rodar

1. Abra o projeto no **Android Studio** (versão 2024 ou superior)
2. Aguarde o **Gradle Sync**
3. Inicie um **emulador** (Pixel 5, API 33+) ou conecte um dispositivo
4. Clique em **Run ▶** (Shift+F10)

## 🔌 Integração com o backend (resumo)

1. Alterar `base_url` em `res/values/strings.xml`
2. Criar `network/ApiService.java`, `network/RetrofitClient.java` e `network/AuthInterceptor.java`
3. Adicionar dependências Retrofit/Gson no `app/build.gradle.kts`
4. Substituir os mocks (`mock/` e `repositories/`) por chamadas Retrofit

**Detalhes completos:** veja `CONTRATOS_API.md` e `CHECKLIST_INTEGRACAO.md`.

## 👤 Contato

- Cliente: **ARTHURBABY — Enxovais Ltda ME**
- CNPJ: 35.671.222/0001-10
- Endereço: Av. Sete de Setembro, 548 — Salvador/BA
- WhatsApp: (71) 99131-1944
- E-mail: pedidoababy@gmail.com
- Instagram: @lojao_arthur_baby

## 📄 Licença

Projeto proprietário — todos os direitos reservados à ARTHURBABY.
© 2026 ARTHURBABY.