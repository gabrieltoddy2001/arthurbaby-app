# Contratos de API — ARTHURBABY App (Front)

Este documento define **todos os endpoints** que o aplicativo Android espera consumir do backend.
Os dados são **mockados** no front (em `repositories/` e `mock/`), então aqui estão os contratos
que o backend precisa implementar para substituir os mocks.

## Convenções gerais

- **Base URL:** configurável em `res/values/strings.xml` → `base_url`
  - Produção: `https://api.arthurbaby.com.br/api/`
  - Dev (emulador): `http://10.0.2.2:8080/api/`
- **Formato:** JSON, UTF-8
- **Autenticação:** Bearer JWT no header `Authorization: Bearer <token>`
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
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "usuario": {
    "id": 1,
    "nome": "Maria Silva",
    "email": "cliente@exemplo.com",
    "cpf": "123.456.789-09",
    "telefone": "(71) 99999-9999",
    "perfil": "CLIENTE"
  }
}
```

### POST /api/auth/cadastro

**Request:**
```json
{
  "nome": "Maria Silva",
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
```

**Response 201:**
```json
{
  "id": 2,
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### POST /api/auth/recuperar-senha

**Request:** `{ "email": "cliente@exemplo.com" }`
**Response 204:** sem corpo.

### Observações sobre o cadastro

- O front **valida o CPF** (dígitos verificadores) antes de enviar
- O front **busca o CEP automaticamente** via ViaCEP e envia os dados completos
- **Nota:** ViaCEP NÃO é endpoint do backend — é uma API pública externa (https://viacep.com.br/ws/{cep}/json/)

---

## 2. Categorias

### GET /api/categorias

Retorna lista em árvore (com subcategorias).

**Response 200:**
```json
[
  {
    "id": 1,
    "nome": "Enxoval",
    "icone": "enxoval",
    "ordemExibicao": 1,
    "subcategorias": [
      { "id": 11, "nome": "Kit Berço" },
      { "id": 12, "nome": "Mantas" }
    ]
  },
  {
    "id": 2,
    "nome": "Roupas para Bebês",
    "icone": "roupas_bebe",
    "ordemExibicao": 2,
    "subcategorias": [
      { "id": 21, "nome": "Bodies" },
      { "id": 22, "nome": "Macacões" },
      { "id": 23, "nome": "Pijamas" }
    ]
  }
]
```

> **Campo `icone`:** o front mapeia este campo para os ícones customizados (`ic_cat_enxoval`, `ic_cat_roupas_bebe`, etc.). Valores esperados:
> `enxoval`, `roupas_bebe`, `roupas_infantis`, `acessorios`, `higiene`, `alimentacao`, `quarto`, `presentes`, `kits`, `promocoes`.

### GET /api/categorias/{id}/produtos

**Query params:** `?page=0&size=20`
**Response:** mesma estrutura do endpoint 3.1.

---

## 3. Produtos

### GET /api/produtos

**Query params:**
- `categoriaId` (opcional)
- `busca` (opcional, filtra por nome/descrição)
- `promocao` (opcional, `true` para preço < R$ 50)
- `precoMax` (opcional, ex: `50`)
- `page`, `size`

**Response 200:**
```json
{
  "content": [
    {
      "id": 1,
      "nome": "Conjunto Bebê Azul",
      "descricao": "Conjunto completo RN em algodão macio",
      "preco": 89.90,
      "precoPromocional": 69.90,
      "precoAntigo": 89.90,
      "imagemPrincipal": "https://.../conjunto-azul.jpg",
      "avaliacao": 5.0,
      "quantidadeAvaliacoes": 5,
      "estoqueAtual": 5,
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
```

### GET /api/produtos/{id}

**Response 200:**
```json
{
  "id": 1,
  "nome": "Conjunto Bebê Azul",
  "descricao": "Conjunto completo RN em algodão macio",
  "preco": 89.90,
  "precoPromocional": null,
  "precoAntigo": null,
  "marca": "ArthurBaby",
  "avaliacao": 5.0,
  "quantidadeAvaliacoes": 5,
  "estoqueAtual": 5,
  "imagens": [
    { "url": "https://.../1.jpg", "principal": true },
    { "url": "https://.../2.jpg", "principal": false }
  ],
  "variacoes": [
    {
      "id": 10,
      "tamanho": "M",
      "cor": "Rosa",
      "modelo": "Padrão",
      "sku": "CONJ-AZ-M-PAD",
      "estoqueAtual": 5,
      "preco": 89.90
    }
  ],
  "categoria": { "id": 1, "nome": "Enxoval" }
}
```

### GET /api/produtos/busca

**Query params:** `?q=bebe&page=0&size=20`
**Response:** igual a `GET /api/produtos`.

---

## 4. Favoritos

### GET /api/favoritos

**Header:** `Authorization: Bearer <token>`
**Response 200:** lista de produtos (mesmo formato de `/api/produtos`).

### POST /api/favoritos/{produtoId}

**Header:** `Authorization`
**Response 201:** vazio.

### DELETE /api/favoritos/{produtoId}

**Response 204:** vazio.

---

## 5. Carrinho (opcional — pode continuar local)

> Observação: o app mantém o carrinho **em memória** (`CarrinhoRepository`). Se quiser persistir no servidor, defina os endpoints abaixo.

### POST /api/carrinhos/itens

**Request:**
```json
{
  "produtoId": 1,
  "variacaoId": 10,
  "quantidade": 2,
  "variacao": {
    "tamanho": "M",
    "cor": "Rosa",
    "modelo": "Padrão"
  }
}
```

**Response 201:** carrinho atualizado.

### DELETE /api/carrinhos/itens/{itemId}
**Response 204:** vazio.

---

## 6. Pedidos

### POST /api/pedidos

**Header:** `Authorization`
**Request:**
```json
{
  "formaRecebimento": "ENTREGA",
  "observacao": "Entregar após as 14h",
  "cupom": "ARTHUR10",
  "desconto": 17.98,
  "frete": 15.00,
  "itens": [
    { "produtoId": 1, "variacaoId": 10, "quantidade": 2 },
    { "produtoId": 3, "variacaoId": null, "quantidade": 1 }
  ]
}
```

**Cupons válidos (mock):** `ARTHUR10` (10% de desconto)
> Back-end deve gerenciar a lista de cupons válidos.

**Response 201:**
```json
{
  "numero": "1001",
  "status": "PEDIDO_GERADO",
  "data": "2025-09-15T15:30:00",
  "cliente": { "id": 1, "nome": "Maria Silva" },
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
  "desconto": 17.98,
  "frete": 15.00,
  "total": 176.82,
  "formaRecebimento": "ENTREGA",
  "observacao": "Entregar após as 14h"
}
```

### GET /api/pedidos/cliente/{clienteId}

**Query params:** `?status=EM_ANDAMENTO` ou `?status=FINALIZADO` (opcional)

**Response 200:**
```json
[
  {
    "numero": "1001",
    "data": "2025-09-12T15:30:00",
    "status": "ENTREGUE",
    "statusColor": "#10B981",
    "total": 225.30
  }
]
```

> O front divide automaticamente em duas abas:
> - **Em andamento:** status ≠ `ENTREGUE` e ≠ `CANCELADO`
> - **Finalizado:** status = `ENTREGUE` ou `CANCELADO`

### GET /api/pedidos/{numero}

**Response 200:** pedido completo com histórico.

### PUT /api/pedidos/{numero}/status

**Request:** `{ "status": "CONFIRMADO", "observacao": "Aprovado pelo vendedor" }`
**Response 200:** pedido atualizado.

### PUT /api/pedidos/{numero}/cancelar

**Request:** `{ "motivo": "Cliente desistiu" }`
**Response 200:** pedido cancelado.

---

## 7. Endereços

### GET /api/clientes/{clienteId}/enderecos
**Response 200:** lista de endereços.

### POST /api/clientes/{clienteId}/enderecos
**Request:**
```json
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
```
**Response 201:** endereço criado.

### DELETE /api/clientes/{clienteId}/enderecos/{enderecoId}
**Response 204:** vazio.

---

## 8. Configurações da loja

### GET /api/configuracoes

**Response 200:**
```json
{
  "nomeFantasia": "ARTHURBABY",
  "whatsapp": "(71) 99131-1944",
  "email": "pedidoababy@gmail.com",
  "instagram": "@lojao_arthur_baby",
  "shopee": "https://shopee.com.br/arthurbabylojao",
  "facebook": "https://facebook.com/...",
  "corPrimaria": "#37B6B0",
  "corDestaque": "#ED83A4"
}
```

---

## 9. Enums de status de pedido

| Valor | Rótulo no app | Cor no app |
|---|---|---|
| `PEDIDO_GERADO` | Gerado | Cinza (#94A3B8) |
| `EM_ANALISE` | Em análise | Amarelo (#FFC107) |
| `AGUARDANDO_CONFIRMACAO` | Aguardando | Amarelo (#FFC107) |
| `CONFIRMADO` | Confirmado | Verde claro (#37B6B0) |
| `SEPARANDO_PRODUTOS` | Separando | Azul (#3B82F6) |
| `PRONTO_PARA_RETIRADA` | Pronto | Verde claro (#37B6B0) |
| `EM_TRANSPORTE` | A caminho | Azul (#3B82F6) |
| `ENTREGUE` | Entregue | Verde (#10B981) |
| `CANCELADO` | Cancelado | Vermelho (#EF4444) |

---

## 10. Regras de negócio (backend)

- **RN01** — Produto com `status=INATIVO` não aparece para novos pedidos.
- **RN02** — Variação com `estoqueAtual=0` deve ser sinalizada como indisponível.
- **RN03** — Quantidade pedida não pode exceder `estoqueAtual`.
- **RN04** — Pedido deve ter pelo menos 1 item.
- **RN05** — Número de pedido único (gerado no backend).
- **RN06** — `valorUnitario` gravado no pedido para preservar histórico.
- **RN07** — Variações obrigatórias devem ser selecionadas antes do carrinho.
- **RN08** — Cancelamento exige motivo + usuário + data.
- **RN09** — Toda alteração de status deve gerar entrada em `pedido_status_historico`.
- **RN10** — Produtos usados em pedidos devem ser inativados, não excluídos.

---

## 11. Cálculo de frete (front)

O app calcula o frete localmente com a regra:

- **Retirada na loja:** R$ 0,00 (grátis)
- **Entrega:** R$ 15,00
- **Entrega com subtotal ≥ R$ 200,00:** R$ 0,00 (frete grátis)

> Se o back quiser aplicar regras diferentes (por CEP, transportadora, etc.), pode enviar `frete` na response do `POST /api/pedidos` e o front passará a usar o valor do back.

---

## 12. Cupons de desconto

O app aceita (no mock) o cupom `ARTHUR10` (10% de desconto no subtotal).

**Back-end deve:**
- Validar o cupom
- Calcular o desconto correto
- Retornar o valor do desconto na response do pedido

---

## 13. Observações para integração

O front está com **dados mockados** em:

- `mock/MockData.java` — produtos, categorias, pedidos
- `repositories/ProdutoRepository.java` — catálogo
- `repositories/CarrinhoRepository.java` — carrinho (com frete)
- `repositories/FavoritoRepository.java` — favoritos
- `repositories/EnderecoRepository.java` — endereços
- `utils/ViaCepService.java` — autopreenchimento de CEP (já usa API real)

**Para integrar com o backend real:**

1. Criar `network/ApiService.java` (interface Retrofit com os endpoints deste documento)
2. Criar `network/RetrofitClient.java`
3. Criar `network/AuthInterceptor.java` (injeta `Authorization: Bearer`)
4. Adicionar dependências Retrofit/Gson no `app/build.gradle.kts`
5. Trocar `base_url` em `res/values/strings.xml`
6. Substituir chamadas aos repositórios por chamadas Retrofit
7. Trocar placeholders de imagem (`img.setBackgroundColor(...)`) por Glide:
   ```java
   Glide.with(context).load(produto.getImagemUrl()).into(imageView);
   ```

---

*Documento atualizado em 15/09/2025 — versão pós-redesign visual.*