package br.com.arthurbaby.controller;

import br.com.arthurbaby.service.AdminCrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD genérico do painel administrativo: uma única rota {@code /api/admin/{tipo}} atende todas as tabelas
 * da loja. O acesso é restrito aos perfis ADMINISTRADOR e VENDEDOR em {@code SecurityConfig}.
 * Como o corpo é livre ({@code Object}), os exemplos abaixo mostram o formato esperado para os tipos mais usados.
 */
@RestController
@RequestMapping(value = "/api/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Administração", description = "CRUD genérico das tabelas da loja. Exige perfil ADMINISTRADOR ou VENDEDOR.")
public class AdminCrudController {
    private static final String TIPOS = " Tipos aceitos: usuarios, enderecos, categorias, marcas, produtos, produto-imagens, "
            + "tamanhos, cores, modelos, produto-variacoes, movimentacoes-estoque, pedidos, pedido-itens, "
            + "pedido-status-historicos, configuracoes-loja, favoritos.";
    private static final String SEM_PERMISSAO = "Acesso negado: apenas perfis ADMINISTRADOR ou VENDEDOR";

    // Exemplos de corpo exibidos no Swagger (o mesmo JSON serve para criar e atualizar)
    private static final String EXEMPLO_CATEGORIA = """
            { "nome": "Bodies", "descricao": "Bodies de manga curta e longa", "icone": "bodies", "ordemExibicao": 11 }""";
    private static final String EXEMPLO_COR = """
            { "nome": "Lilás", "codigoHex": "#C8A2C8", "ordemExibicao": 6 }""";
    private static final String EXEMPLO_TAMANHO = """
            { "nome": "16", "ordemExibicao": 13 }""";
    private static final String EXEMPLO_MARCA = """
            { "nome": "ArthurBaby Premium" }""";

    private static final String CORPO = "Campos da entidade do tipo informado (os nomes seguem as colunas da tabela). "
            + "Use o seletor Examples para ver o formato de cada tipo.";

    private final AdminCrudService adminCrudService;

    public AdminCrudController(AdminCrudService adminCrudService) {
        this.adminCrudService = adminCrudService;
    }

    @GetMapping("/{tipo}")
    @Operation(summary = "Listar registros", description = "Retorna todos os registros do tipo informado." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de registros do tipo informado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO)
    })
    public List<?> listar(@Parameter(description = "Tipo de recurso", example = "categorias") @PathVariable String tipo) {
        return adminCrudService.listar(tipo);
    }

    @GetMapping("/{tipo}/{id}")
    @Operation(summary = "Buscar registro por id", description = "Retorna o registro do tipo informado identificado pelo id." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido ou registro não encontrado"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO)
    })
    public Object buscar(@Parameter(description = "Tipo de recurso", example = "categorias") @PathVariable String tipo,
                         @Parameter(description = "Id do registro", example = "1") @PathVariable Long id) {
        return adminCrudService.buscar(tipo, id);
    }

    @PostMapping(value = "/{tipo}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Criar registro", description = "Cria um novo registro do tipo informado com os dados fornecidos no corpo da requisição." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro criado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido ou dados do corpo inválidos"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "409", description = "Registro duplicado (ex.: nome já cadastrado)")
    })
    public Object criar(@Parameter(description = "Tipo de recurso", example = "categorias") @PathVariable String tipo,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = CORPO,
                                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                                        @ExampleObject(name = "categorias", summary = "Categoria", value = EXEMPLO_CATEGORIA),
                                        @ExampleObject(name = "cores", summary = "Cor", value = EXEMPLO_COR),
                                        @ExampleObject(name = "tamanhos", summary = "Tamanho", value = EXEMPLO_TAMANHO),
                                        @ExampleObject(name = "marcas", summary = "Marca", value = EXEMPLO_MARCA)}))
                        @RequestBody Object body) {
        return adminCrudService.criar(tipo, body);
    }

    @PutMapping(value = "/{tipo}/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualizar registro", description = "Atualiza o registro do tipo informado identificado pelo id com os dados do corpo da requisição." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro atualizado"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido, registro não encontrado ou dados do corpo inválidos"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "409", description = "Registro duplicado (ex.: nome já cadastrado)")
    })
    public Object atualizar(@Parameter(description = "Tipo de recurso", example = "categorias") @PathVariable String tipo,
                            @Parameter(description = "Id do registro", example = "1") @PathVariable Long id,
                            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = CORPO,
                                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                                        @ExampleObject(name = "categorias", summary = "Categoria", value = EXEMPLO_CATEGORIA),
                                        @ExampleObject(name = "cores", summary = "Cor", value = EXEMPLO_COR),
                                        @ExampleObject(name = "tamanhos", summary = "Tamanho", value = EXEMPLO_TAMANHO),
                                        @ExampleObject(name = "marcas", summary = "Marca", value = EXEMPLO_MARCA)}))
                        @RequestBody Object body) {
        return adminCrudService.atualizar(tipo, id, body);
    }

    @DeleteMapping("/{tipo}/{id}")
    @Operation(summary = "Excluir registro", description = "Exclui o registro do tipo informado identificado pelo id." + TIPOS)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro excluído"),
            @ApiResponse(responseCode = "400", description = "Tipo de recurso inválido ou registro não encontrado"),
            @ApiResponse(responseCode = "403", description = SEM_PERMISSAO),
            @ApiResponse(responseCode = "409", description = "Registro em uso por outra tabela (ex.: categoria com produtos)")
    })
    public void excluir(@Parameter(description = "Tipo de recurso", example = "categorias") @PathVariable String tipo,
                        @Parameter(description = "Id do registro", example = "1") @PathVariable Long id) {
        adminCrudService.excluir(tipo, id);
    }
}
