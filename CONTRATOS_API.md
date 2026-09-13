# Contratos de API — ARTHURBABY App (Front) — 2026

Especificação dos endpoints que o aplicativo Android espera consumir do backend.
Os dados estão mockados no front (`mock/` e `repositories/`); estes contratos
substituem os mocks quando o backend estiver pronto.

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
{ "login": "email ou CPF", "senha": "123456" }
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
Response 401:

json
{ "erro": "Credenciais inválidas", "codigo": 401 }
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
{ "id": 2, "token": "eyJhbGciOiJIUzI1NiJ9..." }
Response 400:

json
{ "erro": "CPF já cadastrado", "codigo": 400 }
POST /api/auth/recuperar-senha
Request: { "email": "cliente@exemplo.com" }
Response 204: sem corpo.

2. Categorias
GET /api/categorias
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
Response: mesma estrutura de GET /api/produtos.

3. Produtos
GET /api/produtos
Query params: categoriaId, busca, promocao, precoMax, page, size

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
    { "url": "https://.../1.jpg", "principal": true }
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
GET /api/favoritos → lista de produtos (formato de /api/produtos)

POST /api/favoritos/{produtoId} → 201 vazio

DELETE /api/favoritos/{produtoId} → 204 vazio

Todos exigem Authorization: Bearer <token>.

5. Carrinho (opcional — atualmente em memória)
POST /api/carrinhos/itens — { "produtoId": 1, "variacaoId": 10, "quantidade": 2 }

DELETE /api/carrinhos/itens/{itemId} → 204

6. Pedidos
POST /api/pedidos
Request:

json
{
  "formaRecebimento": "ENTREGA",
  "observacao": "Entregar após as 14h",
  "itens": [
    { "produtoId": 1, "variacaoId": 10, "quantidade": 2 }
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
  "frete": 0,
  "total": 179.80,
  "formaRecebimento": "ENTREGA",
  "observacao": "Entregar após as 14h"
}
GET /api/pedidos/cliente/{clienteId}
Response 200: lista com número, data, status e total.

GET /api/pedidos/{numero}
Response 200: pedido completo com histórico:

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
Request: { "status": "CONFIRMADO", "observacao": "Aprovado" }

PUT /api/pedidos/{numero}/cancelar
Request: { "motivo": "Cliente desistiu" }

7. Endereços
GET /api/clientes/{clienteId}/enderecos → lista

POST /api/clientes/{clienteId}/enderecos → 201 com o endereço criado

DELETE /api/clientes/{clienteId}/enderecos/{enderecoId} → 204

Body de POST:

json
{
  "cep": "40020-455",
  "logradouro": "Avenida Sete de Setembro",
  "numero": "548",
  "complemento": "Ed. Fatima Loja",
  "bairro": "Centro",
  "cidade": "Salvador",
  "uf": "BA",
  "principal": true
}
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
  "corPrimaria": "#37B6B0",
  "corDestaque": "#ED83A4"
}
9. Enums
Status do pedido
RASCUNHO, PEDIDO_GERADO, EM_ANALISE, AGUARDANDO_CONFIRMACAO,
CONFIRMADO, SEPARANDO_PRODUTOS, PRONTO_PARA_RETIRADA,
EM_TRANSPORTE, ENTREGUE, CANCELADO

Forma de recebimento
RETIRADA_LOJA, ENTREGA

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

11. Frete
Cálculo mock atual do front:

Retirada na loja: R$ 0,00

Entrega: R$ 15,00

Entrega gratuita quando subtotal ≥ R$ 200,00

O backend deve confirmar/refazer esse cálculo no POST /api/pedidos.

12. ViaCEP
O front consulta diretamente https://viacep.com.br/ws/{cep}/json/ para
autopreencher o endereço no cadastro. O backend não precisa implementar isso.

13. Observação sobre os mocks
Os seguintes arquivos contêm dados mockados no front e devem ser substituídos:

mock/MockData.java

repositories/ProdutoRepository.java

repositories/CarrinhoRepository.java

repositories/FavoritoRepository.java

repositories/EnderecoRepository.java