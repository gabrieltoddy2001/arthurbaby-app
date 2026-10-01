Pendências do Backend — Documento Consolidado Final

Documento único com todas as pendências do backend identificadas após a integração completa do app Android e do painel admin web.

Status: o time de backend já resolveu a maioria das pendências anteriores (cupom, recuperar senha, endereços, favoritos e editar perfil).
Este documento lista apenas o que ainda falta.

📊 Resumo executivo
#	Pendência	Prioridade	Impacto
1	LazyInitializationException definitivo (DTOs)	🔴 Crítica	App + Painel Admin
2	Campo icone nas categorias	🔴 Crítica	Ícones coloridos no app
3	Formatação de datas no JSON	🟡 Importante	Parse de datas no app
4	Seed incompleto (modelos, imagens, avaliação)	🟡 Importante	Detalhe do produto vazio
5	Normalização de e-mail no login/cadastro	🟢 Desejável	Duplicidade de contas
🔴 1. LazyInitializationException — solução definitiva
Problema

Vários endpoints retornam HTTP 500 com o erro:

org.springframework.http.converter.HttpMessageNotWritableException:
Could not write JSON: failed to lazily initialize a collection of role:
br.com.arthurbaby.entity.Produto.imagens:
could not initialize proxy - no Session

Endpoints afetados

❌ GET /api/admin/produtos — listar produtos (painel admin)

❌ GET /api/admin/{tipo} — qualquer listagem de entidade com relacionamentos

❌ POST /api/admin/produtos/completo — pode dar erro ao retornar

⚠️ GET /api/favoritos/cliente/{id} — resolvido com URL nova? Precisa confirmar

⚠️ GET /api/pedidos/{id} — provavelmente apresenta o mesmo problema

Causa

O AdminCrudController retorna entidades JPA diretamente, e essas entidades possuem relacionamentos LAZY (@OneToMany).

Quando o Jackson tenta serializar essas entidades para JSON, a sessão do JPA já foi encerrada e as coleções lazy não podem mais ser inicializadas.

Solução definitiva

Criar DTOs para todos os endpoints do AdminCrudController, evitando retornar entidades JPA diretamente.

Exemplo de DTO para produto
public record ProdutoAdminResponse(
Long id,
String codigo,
String sku,
String nome,
String descricao,
BigDecimal preco,
BigDecimal precoPromocional,
boolean promocao,
boolean destaque,
String status,
Long categoriaId,
String categoriaNome,
Long marcaId,
String marcaNome,
int avaliacao,
List<ImagemResponse> imagens,
List<VariacaoResponse> variacoes
) {}

DTO de imagem
public record ImagemResponse(
Long id,
String url,
String descricao,
boolean principal
) {}

DTO de variação
public record VariacaoResponse(
Long id,
String sku,
String tamanho,
String cor,
String modelo,
BigDecimal preco,
int estoqueAtual
) {}

Mapeamento no controller

Exemplo:

@GetMapping("/{tipo}")
public List<?> listar(@PathVariable String tipo) {
if ("produtos".equals(tipo)) {
return produtoRepository.findAll().stream()
.map(this::toProdutoResponse)
.toList();
}

    // ... demais entidades
}


O ideal é que o mapeamento seja feito em métodos específicos ou em uma camada de mapper/service, evitando deixar a lógica de transformação excessivamente concentrada no controller.

Solução temporária

Caso ainda esteja em uso:

spring.jpa.open-in-view=true


Essa configuração mantém o contexto de persistência aberto durante a renderização da resposta.

⚠️ Não utilizar como solução definitiva em produção.

A correção recomendada é trabalhar com DTOs + consultas adequadas, garantindo que todos os dados necessários sejam carregados antes da serialização.

🔴 2. Campo icone nas categorias
Problema

O app mobile mostra ícones coloridos por categoria, como:

urso

chupeta

mamadeira

etc.

Atualmente, o app identifica o ícone pelo nome da categoria, o que é frágil.

Exemplo:

"Enxoval" → ícone enxoval


Se o administrador alterar:

"Enxoval"


para:

"Enxoval Completo"


o mapeamento do app pode deixar de funcionar.

Além disso:

categorias novas não possuem ícone;

alterações de nome podem quebrar o mapeamento;

o nome da categoria passa a ter uma responsabilidade que não deveria ter.

Solução

Adicionar o campo icone à entidade Categoria:

@Entity
public class Categoria {

    // ... campos existentes

    private String icone;
}


O endpoint:

GET /api/categorias


deve retornar:

{
"id": 1,
"nome": "Enxoval",
"icone": "enxoval",
"subcategorias": []
}

Valores esperados para icone

Os valores sugeridos são:

enxoval
roupas_para_bebes
roupas_infantis
acessorios
higiene_e_cuidados
alimentacao
quarto_do_bebe
presentes
kits
promocoes

Seed sugerido
UPDATE categoria SET icone = 'enxoval'
WHERE nome = 'Enxoval';

UPDATE categoria SET icone = 'roupas_para_bebes'
WHERE nome = 'Roupas para Bebes';

UPDATE categoria SET icone = 'roupas_infantis'
WHERE nome = 'Roupas Infantis';

UPDATE categoria SET icone = 'acessorios'
WHERE nome = 'Acessorios';

UPDATE categoria SET icone = 'higiene_e_cuidados'
WHERE nome = 'Higiene e Cuidados';

UPDATE categoria SET icone = 'alimentacao'
WHERE nome = 'Alimentacao';

UPDATE categoria SET icone = 'quarto_do_bebe'
WHERE nome = 'Quarto do Bebe';

UPDATE categoria SET icone = 'presentes'
WHERE nome = 'Presentes';

UPDATE categoria SET icone = 'kits'
WHERE nome = 'Kits';

UPDATE categoria SET icone = 'promocoes'
WHERE nome = 'Promocoes';

🟡 3. Formatação de datas no JSON
Problema

O backend atualmente pode retornar datas com frações de segundo contendo até 7 dígitos:

{
"data": "2026-09-23T11:02:00.9942499"
}


O app Android, utilizando Java + Gson, espera trabalhar com precisão de milissegundos, com até 3 dígitos na fração de segundo.

Por isso, o app precisou implementar tratamento manual para evitar erros de parsing.

Solução

Configurar o Jackson para utilizar o formato:

yyyy-MM-dd'T'HH:mm:ss.SSS


No application.properties:

spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss.SSS
spring.jackson.serialization.write-dates-as-timestamps=false

Alternativa: @JsonFormat

Também é possível configurar diretamente nos DTOs:

@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
private LocalDateTime data;

Formato esperado

O backend deve retornar:

{
"data": "2026-09-23T11:02:00.994"
}


em vez de:

{
"data": "2026-09-23T11:02:00.9942499"
}

🟡 4. Seed incompleto

O DataInitializer atualmente cadastra cor e tamanho para os produtos, porém alguns dados necessários para o funcionamento completo do detalhe do produto ainda não estão sendo populados.

4.1 — Modelos
Problema

A tabela modelo existe, mas está vazia.

Consequentemente, as variações dos produtos não possuem modelo associado.

Impacto

O app mobile não consegue exibir corretamente a seção:

Modelo


no detalhe do produto.

Solução

Adicionar modelos no DataInitializer:

if (modelos.count() == 0) {
for (String nome : List.of("Padrão", "Premium", "Deluxe")) {
Modelo m = new Modelo();
m.setNome(nome);
modelos.save(m);
}
}


Depois, associar um modelo às variações:

v.setModelo(modelos.findAll().get(0));


Idealmente, a implementação deve evitar chamadas repetidas a findAll() dentro de loops e manter os modelos carregados em memória durante a inicialização.

4.2 — Imagens do produto
Problema

A tabela:

produto_imagem


está vazia.

Impacto

O app mobile exibe o placeholder rosa em vez da foto do produto.

Solução

Adicionar imagens no DataInitializer:

if (imagens.count() == 0) {
ProdutoImagem img = new ProdutoImagem();

    img.setProduto(p);
    img.setUrl(
        "https://placehold.co/400x400/ED83A4/FFFFFF?text=Produto"
    );
    img.setPrincipal(true);
    img.setOrdemExibicao(1);

    imagens.save(img);
}


Para ambiente de desenvolvimento, o placeholder é suficiente para validar o fluxo. Em produção, as URLs devem apontar para as imagens reais dos produtos.

4.3 — Avaliação do produto
Problema

A coluna:

avaliacao


existe na tabela produto, porém os produtos do seed não estão recebendo um valor.

Impacto

O detalhe do produto pode exibir avaliação zerada ou não apresentar a informação corretamente.

Solução

Ao criar o produto no DataInitializer:

p.setAvaliacao(5);

🟢 5. Normalização de e-mail no login/cadastro
Problema

Atualmente:

TESTE@x.com


e:

teste@x.com


podem ser tratados como e-mails diferentes.

Isso pode resultar em duplicidade de contas.

Solução

Normalizar o e-mail antes de consultar ou salvar:

String email = request.email()
.toLowerCase()
.trim();

Login

No AuthService:

public AuthResponse login(LoginRequest request) {

    String login = request.login()
        .toLowerCase()
        .trim();

    Usuario usuario = usuarios.findByEmail(login)
        .or(() -> usuarios.findByCpf(login))
        .orElseThrow(() ->
            new IllegalArgumentException("Usuário não encontrado")
        );

    // ...
}

Cadastro
public AuthResponse cadastrar(CadastroRequest request) {

    String email = request.email()
        .toLowerCase()
        .trim();

    if (usuarios.existsByEmail(email)) {
        throw new IllegalArgumentException(
            "E-mail já cadastrado"
        );
    }

    usuario.setEmail(email);

    // ...
}

Recomendação adicional

Além da normalização no service, é recomendável garantir a unicidade no banco de dados através de uma constraint/index UNIQUE no campo de e-mail.

Assim, mesmo que alguma chamada futura não passe pelo mesmo fluxo de normalização, o banco continuará protegendo contra duplicidade.

🎯 Resumo de prioridades
#	Pendência	Prioridade	Tempo estimado
1	LazyInit definitivo (DTOs)	🔴 Crítica	1–2h
2	Campo icone nas categorias	🔴 Crítica	~15 min
3	Formatação de datas	🟡 Importante	~10 min
4	Seed (modelos, imagens, avaliação)	🟡 Importante	~30 min
5	Normalização de e-mail	🟢 Desejável	~10 min
Total estimado		~2–3h

Os tempos são estimativas e podem variar conforme a estrutura atual dos repositories, services, entidades e DTOs.

📌 Como testar cada pendência
Após #1 — DTOs

Listar produtos do painel admin:

curl -X GET http://localhost:8080/api/admin/produtos \
-H "Authorization: Bearer SEU_TOKEN"

Resultado esperado
HTTP 200


com uma lista de produtos serializada corretamente, incluindo imagens e variações quando aplicável.

Não deve ocorrer:

LazyInitializationException


nem:

HttpMessageNotWritableException

Após #2 — icone

Consultar categorias:

curl -X GET http://localhost:8080/api/categorias

Resultado esperado

Cada categoria deve possuir o campo:

{
"id": 1,
"nome": "Enxoval",
"icone": "enxoval"
}

Após #3 — datas

Consultar pedidos:

curl -X GET http://localhost:8080/api/pedidos/cliente/8 \
-H "Authorization: Bearer SEU_TOKEN"

Resultado esperado

As datas devem seguir o padrão:

2026-09-23T11:02:00.994


sem 4 ou mais casas decimais na fração de segundo.

Após #4 — seed

Verificar no MySQL:

Modelos
SELECT * FROM modelo;


Resultado esperado:

Padrão
Premium
Deluxe

Imagens
SELECT * FROM produto_imagem;


Resultado esperado:

pelo menos 1 registro;

produto associado;

URL preenchida;

uma imagem marcada como principal.

Avaliação
SELECT id, nome, avaliacao
FROM produto;


Resultado esperado:

produtos com avaliacao preenchida;

nenhum produto do seed com avaliação 0, caso essa seja a regra definida para os dados iniciais.

Após #5 — e-mail

Cadastrar utilizando:

TESTE@x.com


Depois tentar realizar login utilizando:

teste@x.com

Resultado esperado

O login deve localizar a mesma conta.

Também deve ser rejeitado um novo cadastro utilizando:

teste@x.com


caso:

TESTE@x.com


já esteja cadastrado.

✅ Checklist final

Corrigir LazyInitializationException utilizando DTOs

Revisar todos os endpoints do AdminCrudController

Confirmar /api/favoritos/cliente/{id}

Confirmar /api/pedidos/{id}

Adicionar campo icone em Categoria

Atualizar seed das categorias

Padronizar formato das datas no JSON

Popular tabela modelo

Associar modelos às variações

Popular produto_imagem

Definir imagens principais

Preencher avaliacao dos produtos

Normalizar e-mails no cadastro

Normalizar e-mails no login

Garantir unicidade do e-mail no banco

Executar os testes dos endpoints

Validar integração no app Android

Validar integração no painel admin web

📅 Controle do documento

Documento: Pendências do Backend — Documento Consolidado Final
Status: Pendências finais após integração completa
Data: 01/10/2026