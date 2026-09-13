markdown
# ArthurBabyApp — Front-end Android (2026)

Aplicativo Android (cliente) do catálogo digital e geração de pedidos da **ARTHURBABY**.

Desenvolvido em **Java + Android Studio**. Consome (futuramente) uma API REST em Spring Boot.

> **Nota para a equipe de desenvolvimento:**
> Este é o **front-end** (app Android) do projeto ARTHURBABY.
> Os dados estão **mockados** no momento; os contratos de API que o backend
> precisa implementar estão documentados em [`CONTRATOS_API.md`](./CONTRATOS_API.md).
> O checklist de integração passo a passo está em [`CHECKLIST_INTEGRACAO.md`](./CHECKLIST_INTEGRACAO.md).

---

## 📱 Sobre o projeto

Este repositório contém **apenas o front-end** (aplicativo Android) com dados mockados.
A equipe de back-end deve usar o documento [`CONTRATOS_API.md`](./CONTRATOS_API.md) para
implementar os endpoints e substituir os mocks.

## ✅ Status do front

- ✅ Splash com logo institucional e animação
- ✅ Login (e-mail/CPF + senha)
- ✅ Recuperar senha
- ✅ Cadastro (nome, CPF, e-mail, telefone, senha, endereço, LGPD)
- ✅ Validação de CPF + máscaras (CPF, telefone, CEP)
- ✅ ViaCEP (autopreenchimento de endereço)
- ✅ Home com banner, categorias (com subcategorias), destaques, promoções e atalho de busca
- ✅ Categorias (lista + subcategorias + produtos por categoria em grade)
- ✅ Detalhe do produto com variações (tamanho, cor, modelo), marca e controle de estoque
- ✅ Carrinho com subtotal, frete, total, forma de recebimento (retirada/entrega) e observação
- ✅ Confirmação de pedido com resumo, número único e forma de recebimento
- ✅ Perfil com Meus Pedidos, Detalhe do Pedido, Endereços, Favoritos, Ajuda, Sobre e Sair
- ✅ Busca com filtros (promoção, faixa de preço) e filtro em tempo real
- ✅ Favoritos com coração nos cards
- ✅ Loading visual em ações assíncronas
- ✅ Ícones vetoriais (Material Design)
- ✅ Animações de transição entre telas
- ❌ Modo escuro (não implementado nesta versão)

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 11 |
| IDE | Android Studio (versão 2024+) |
| SDK mínimo | API 24 (Android 7.0) |
| SDK alvo | API 34 (Android 14) |
| Build | Gradle (Kotlin DSL) |
| Bibliotecas | AndroidX, Material Components, RecyclerView, CardView, Glide |

## 📂 Estrutura de pastas
app/src/main/java/br/com/arthurbaby/
├── activities/ → Splash, Login, Cadastro, RecuperarSenha, MainActivity
├── fragments/ → Home, Categorias, Produtos, Detalhe, Carrinho, Confirmação,
│ Perfil, MeusPedidos, DetalhePedido, Enderecos, Favoritos,
│ Busca, Promocoes, Ajuda, Sobre
├── adapters/ → ProdutoAdapter, CategoriaAdapter, ItemCarrinhoAdapter,
│ PedidoAdapter, EnderecoAdapter
├── models/ → Produto, Categoria, ItemCarrinho, Pedido, PedidoStatus,
│ Endereco, Variacao
├── repositories/ → ProdutoRepository, CarrinhoRepository, FavoritoRepository,
│ EnderecoRepository
├── mock/ → MockData (dados fictícios)
├── network/ → (a criar) Retrofit
└── utils/ → MaskUtils, LoadingUtils, NavUtils, ViaCepService

text

## 🚀 Como rodar

1. Abra o projeto no **Android Studio** (versão 2024 ou superior)
2. Aguarde o **Gradle Sync**
3. Conecte um dispositivo ou inicie um **emulador** (Pixel 5, API 33+)
4. Clique em **Run ▶** (Shift+F10)

## 🔌 Como integrar com o backend

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
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
Substituir os mocks pelos repositórios Retrofit, um por um, seguindo o
CHECKLIST_INTEGRACAO.md.

👤 Autor / Contato
Cliente: ARTHURBABY — Enxovais Ltda ME

CNPJ: 35.671.222/0001-10

Endereço: Av. Sete de Setembro, 548 — Salvador/BA

WhatsApp: (71) 99131-1944

E-mail: pedidoababy@gmail.com

Instagram: @lojao_arthur_baby

📄 Licença
Projeto proprietário — todos os direitos reservados à ARTHURBABY.
© 2026 ARTHURBABY.

text

---

# 📄 ARQUIVO 2 — `CONTRATOS_API.md`

**Como criar:** botão direito em `ArthurBabyApp` (raiz) → **New → File** → nome `CONTRATOS_API.md` → cole o conteúdo.

```markdown
# Contratos de API — ARTHURBABY App (Front) — 2026

Este documento define **todos os endpoints** que o aplicativo Android espera consumir do backend.
Os dados são **mockados** no front (em `repositories/` e `mock/`), então aqui estão os contratos
que o backend precisa implementar para substituir os mocks.

## Convenções gerais

- **Base URL:** configurável em `res/values/strings.xml` → `base_url`
  - Produção sugerida: `https://api.arthurbaby.com.br/api/`
  - Dev (emulador): `http://10.0.2.2:8080/api/`
- **Formato:** JSON, UTF-8
- **Autenticação:** Bearer JWT no header `Authorization`
- **Paginação:** `?page=0&size=20` (padrão Spring Data)
- **Erros:** `{ "erro": "mensagem", "codigo": 400 }`

---

## 1. Autenticação

### POST /api/auth/login

**Request:**
```json
{
  "login": "email@exemplo.com ou CPF",
  "senha": "123456"
}
Response 200:

json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": {
    "id": 1,
    "nome": "Adeilma Silva",
    "email": "cliente@exemplo.com",
    "cpf": "123.456.789-09",
    "telefone": "(71) 99999-9999",
    "perfil": "CLIENTE"
  }
}
POST /api/auth/cadastro
Request:

json
{
  "nome": "Adeilma Silva",
  "cpf": "123.456.789-09",
  "email": "cliente@exemplo.com",
  "telefone": "(71) 99999-9999",
  "senha": "123456",
  "aceiteTermoUso": true,
  "aceiteLgpd": true,
  "endereco": {
    "cep": "40020-455",
    "logradouro": "Avenida Sete de Setembro",
    "numero": "548",
    "complemento": "Ed. Fatima Loja",
    "bairro": "Centro",
    "cidade": "Salvador",
    "uf": "BA"
  }
}
Response 201:

json
{
  "id": 2,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
POST /api/auth/recuperar-senha
Request: { "email": "cliente@exemplo.com" }
Response 204: sem corpo.

2. Categorias
GET /api/categorias
Retorna lista em árvore (com subcategorias).

Response 200:

json
[
  {
    "id": 1,
    "nome": "Enxoval",
    "imagemUrl": "https://.../enxoval.png",
    "ordemExibicao": 1,
    "subcategorias": [
      { "id": 11, "nome": "Kit Berço" },
      { "id": 12, "nome": "Mantas" }
    ]
  }
]
GET /api/categorias/{id}/produtos
Query params: ?page=0&size=20
Response: mesma estrutura do endpoint 3.1.

3. Produtos
GET /api/produtos
Query params:

categoriaId (opcional)

busca (opcional, filtra por nome/descrição)

promocao (opcional, true para preço < R$ 50)

precoMax (opcional)

page, size

Response 200:

json
{
  "content": [
    {
      "id": 1,
      "nome": "Conjunto Bebê Azul",
      "descricao": "Conjunto completo RN em algodão macio",
      "preco": 89.90,
      "precoPromocional": null,
      "imagemPrincipal": "https://.../conjunto-azul.jpg",
      "estoque": 5,
      "status": "ATIVO",
      "destaque": true,
      "promocao": false,
      "marca": "ArthurBaby",
      "categoria": { "id": 1, "nome": "Enxoval" }
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "number": 0
}
GET /api/produtos/{id}
Response 200:

json
{
  "id": 1,
  "nome": "Conjunto Bebê Azul",
  "descricao": "Conjunto completo RN em algodão macio",
  "preco": 89.90,
  "precoPromocional": null,
  "marca": "ArthurBaby",
  "estoque": 5,
  "imagens": [
    { "url": "https://.../1.jpg", "principal": true },
    { "url": "https://.../2.jpg", "principal": false }
  ],
  "variacoes": [
    {
      "id": 10,
      "tamanho": "M",
      "cor": "Azul",
      "modelo": "Padrão",
      "sku": "CONJ-AZ-M-PAD",
      "estoqueAtual": 5,
      "preco": 89.90
    }
  ],
  "categoria": { "id": 1, "nome": "Enxoval" }
}
GET /api/produtos/busca
Query params: ?q=bebe&page=0&size=20
Response: igual a GET /api/produtos.

4. Favoritos
GET /api/favoritos
Header: Authorization: Bearer <token>
Response 200: lista de produtos.

POST /api/favoritos/{produtoId}
Response 201: vazio.

DELETE /api/favoritos/{produtoId}
Response 204: vazio.

5. Carrinho (opcional — pode ser local)
POST /api/carrinhos/itens
Request:

json
{
  "produtoId": 1,
  "variacaoId": 10,
  "quantidade": 2
}
Response 201: carrinho atualizado.

DELETE /api/carrinhos/itens/{itemId}
Response 204: vazio.

6. Pedidos
POST /api/pedidos
Header: Authorization
Request:

json
{
  "formaRecebimento": "ENTREGA",
  "observacao": "Entregar após as 14h",
  "itens": [
    { "produtoId": 1, "variacaoId": 10, "quantidade": 2 },
    { "produtoId": 3, "variacaoId": null, "quantidade": 1 }
  ]
}
Response 201:

json
{
  "numero": "1001",
  "status": "PEDIDO_GERADO",
  "data": "2026-01-15T15:30:00",
  "cliente": { "id": 1, "nome": "Adeilma Silva" },
  "itens": [
    {
      "produtoId": 1,
      "nomeProduto": "Conjunto Bebê Azul",
      "variacaoDescricao": "M | Azul | Padrão",
      "quantidade": 2,
      "valorUnitario": 89.90,
      "valorTotal": 179.80
    }
  ],
  "subtotal": 179.80,
  "desconto": 0,
  "frete": 15.00,
  "total": 194.80,
  "formaRecebimento": "ENTREGA",
  "observacao": "Entregar após as 14h"
}
GET /api/pedidos/cliente/{clienteId}
Response 200: lista de pedidos.

GET /api/pedidos/{numero}
Response 200: pedido completo com histórico.

json
{
  "numero": "1001",
  "status": "ENTREGUE",
  "historico": [
    { "status": "PEDIDO_GERADO", "data": "2026-01-05T10:00:00" },
    { "status": "CONFIRMADO", "data": "2026-01-06T11:00:00" },
    { "status": "EM_TRANSPORTE", "data": "2026-01-10T09:00:00" },
    { "status": "ENTREGUE", "data": "2026-01-12T14:00:00" }
  ]
}
PUT /api/pedidos/{numero}/status
Request: { "status": "CONFIRMADO", "observacao": "Aprovado pelo vendedor" }

PUT /api/pedidos/{numero}/cancelar
Request: { "motivo": "Cliente desistiu" }

7. Endereços
GET /api/clientes/{clienteId}/enderecos
Response 200: lista de endereços.

POST /api/clientes/{clienteId}/enderecos
Response 201: endereço criado.

DELETE /api/clientes/{clienteId}/enderecos/{enderecoId}
Response 204: vazio.

8. Configurações da loja
GET /api/configuracoes
Response 200:

json
{
  "nomeFantasia": "ARTHURBABY",
  "whatsapp": "(71) 99131-1944",
  "email": "pedidoababy@gmail.com",
  "instagram": "@lojao_arthur_baby",
  "shopee": "https://shopee.com.br/arthurbabylojao",
  "facebook": "https://www.facebook.com/p/loj%C3%A3o-Arthur-baby-61590691140950/",
  "corPrimaria": "#37B6B0",
  "corDestaque": "#ED83A4"
}
9. Status de pedido (enums)
Valor	Descrição
RASCUNHO	Não finalizado
PEDIDO_GERADO	Pedido criado
EM_ANALISE	Em análise pela loja
AGUARDANDO_CONFIRMACAO	Aguardando confirmação
CONFIRMADO	Confirmado pela loja
SEPARANDO_PRODUTOS	Em separação
PRONTO_PARA_RETIRADA	Pronto para retirada
EM_TRANSPORTE	Em transporte
ENTREGUE	Entregue
CANCELADO	Cancelado
10. Regras de negócio
RN01 — Produto INATIVO não aparece para novos pedidos.

RN02 — Variação com estoqueAtual=0 é sinalizada como indisponível.

RN03 — Quantidade pedida não pode exceder estoqueAtual.

RN04 — Pedido deve ter pelo menos 1 item.

RN05 — Número de pedido único (gerado no backend).

RN06 — valorUnitario gravado no pedido para preservar histórico.

RN07 — Variações obrigatórias selecionadas antes do carrinho.

RN08 — Cancelamento exige motivo + usuário + data.

RN09 — Toda alteração de status gera entrada em pedido_status_historico.

RN10 — Produtos usados em pedidos são inativados, não excluídos.

11. Observações para integração
Mocks no front:

mock/MockData.java

repositories/ProdutoRepository.java

repositories/CarrinhoRepository.java

repositories/FavoritoRepository.java

repositories/EnderecoRepository.java

Para integrar:

Criar Retrofit em network/ApiService.java

Substituir chamadas diretas aos repositórios

Adicionar AuthInterceptor

Alterar base_url em strings.xml

Tratar erros com LoadingUtils + Toast

Frete (mock): retirada = R
0
;
e
n
t
r
e
g
a
=
R
0;entrega=R 15 (grátis acima de R$ 200).
ViaCEP: o front consulta https://viacep.com.br/ws/{cep}/json/ diretamente.

Documento gerado em 2026. Ajustar conforme a implementação do backend evoluir.

text

---

# 📄 ARQUIVO 3 — `CHECKLIST_INTEGRACAO.md`

**Como criar:** botão direito em `ArthurBabyApp` (raiz) → **New → File** → nome `CHECKLIST_INTEGRACAO.md` → cole o conteúdo.

```markdown
# Checklist de Integração — Front + Back (2026)

Use esta lista para acompanhar o progresso da integração do app Android
com o backend Spring Boot.

---

## 1. Rede

- [ ] Trocar `base_url` em `res/values/strings.xml` para a URL do backend
- [ ] Criar `network/RetrofitClient.java`
- [ ] Criar `network/ApiService.java` (interface com todos os endpoints)
- [ ] Criar `network/AuthInterceptor.java` (injeta `Authorization: Bearer`)
- [ ] Adicionar dependências Retrofit/Gson no `app/build.gradle.kts`
- [ ] Confirmar permissão `INTERNET` no `AndroidManifest.xml` (já existe)

## 2. Autenticação

- [ ] Implementar `POST /api/auth/login` no backend
- [ ] Implementar `POST /api/auth/cadastro` no backend
- [ ] Implementar `POST /api/auth/recuperar-senha` no backend
- [ ] Substituir mock do Login por chamada Retrofit
- [ ] Substituir mock do Cadastro por chamada Retrofit
- [ ] Substituir mock do Recuperar Senha por chamada Retrofit
- [ ] Salvar token em `EncryptedSharedPreferences`
- [ ] Interceptor para expirar sessão em 401

## 3. Catálogo

- [ ] Implementar `GET /api/categorias`
- [ ] Implementar `GET /api/categorias/{id}/produtos`
- [ ] Implementar `GET /api/produtos` com filtros e paginação
- [ ] Implementar `GET /api/produtos/{id}`
- [ ] Implementar `GET /api/produtos/busca?q=`
- [ ] Substituir `MockData` por chamadas Retrofit
- [ ] Trocar `ImageView` verde por `Glide.with(...).load(...)`

## 4. Favoritos

- [ ] Implementar `GET/POST/DELETE /api/favoritos`
- [ ] Substituir `FavoritoRepository` por chamadas Retrofit

## 5. Endereços

- [ ] Implementar `GET/POST/DELETE /api/clientes/{id}/enderecos`
- [ ] Substituir `EnderecoRepository` por chamadas Retrofit
- [ ] Implementar tela de cadastro de endereço (front)

## 6. Pedidos

- [ ] Implementar `POST /api/pedidos`
- [ ] Implementar `GET /api/pedidos/cliente/{id}`
- [ ] Implementar `GET /api/pedidos/{numero}`
- [ ] Implementar `PUT /api/pedidos/{numero}/status`
- [ ] Implementar `PUT /api/pedidos/{numero}/cancelar`
- [ ] Substituir `ConfirmaPedidoFragment` por chamada real
- [ ] Substituir `MeusPedidosFragment` por chamada real
- [ ] Substituir `DetalhePedidoFragment` por chamada real

## 7. Tratamento de erros

- [ ] Criar `ErrorHandler` central
- [ ] Tratar 400, 401, 403, 404, 500
- [ ] Tratar timeout e sem conexão

## 8. Segurança

- [ ] HTTPS em produção
- [ ] Token criptografado
- [ ] Senha nunca em logs
- [ ] Aceite de LGPD registrado no cadastro
- [ ] CORS configurado (se houver frontend Web)

## 9. Publicação

- [ ] Configurar keystore de release
- [ ] Gerar APK/AAB assinado
- [ ] Publicar na Play Store (se aplicável)

## 10. Validação final

- [ ] Fluxo completo: Splash → Login → Home → Produto → Carrinho → Pedido → Meus Pedidos
- [ ] Testar em 3 tamanhos de tela
- [ ] Testar com internet lenta
- [ ] Testar com backend desligado
- [ ] Testar cadastro com CPF inválido
- [ ] Testar ViaCEP com CEP inexistente

---

## Observações

- **Ajustes visuais:** o layout atual é uma aproximação funcional do protótipo.
- **Modo escuro:** não implementado nesta versão.
- **Menu lateral (drawer):** não implementado nesta versão (BottomNavigation cobre os fluxos).

---

*Documento gerado em 2026.*
