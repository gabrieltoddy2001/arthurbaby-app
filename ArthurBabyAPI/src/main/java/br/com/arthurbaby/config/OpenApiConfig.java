package br.com.arthurbaby.config;

import br.com.arthurbaby.dto.ErroResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuração da documentação OpenAPI/Swagger (disponível em {@code /swagger-ui.html}).
 * Define os dados gerais da API, o esquema de autenticação JWT e completa automaticamente
 * as respostas de erro de todos os endpoints.
 */
@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String ERRO_SCHEMA = "ErroResponse";

    /**
     * Mensagem de exemplo por código de erro. São as mesmas mensagens que o {@code ApiExceptionHandler}
     * devolve de verdade, para o exemplo do Swagger corresponder ao que o cliente vai receber.
     */
    private static final Map<String, String> MENSAGENS_ERRO = Map.of(
            "400", "Corpo da requisicao invalido ou mal formatado",
            "401", "Nao autenticado: token ausente, invalido ou expirado",
            "403", "Acesso negado",
            "404", "Registro nao encontrado",
            "409", "Operacao viola uma restricao do banco (registro duplicado ou em uso)",
            "500", "Erro interno do servidor");

    /** Informações gerais exibidas no topo do Swagger UI e o botão "Authorize" (token JWT). */
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ArthurBaby API - Loja de Moda Infantil")
                        .version("1.0.0")
                        .description("API REST da loja ArthurBaby: autenticação de clientes, perfil e endereços, "
                                + "catálogo de produtos e estoque, pedidos, favoritos e administração.\n\n"
                                + "**Autenticação:** faça login em `POST /api/auth/login`, copie o `token` da resposta "
                                + "e informe-o no botão **Authorize** (sem o prefixo `Bearer`).\n\n"
                                + "**Formato:** todas as requisições e respostas usam `application/json`.\n\n"
                                + "**Erros:** todas as respostas de erro seguem o formato "
                                + "`{ \"erro\": \"mensagem\", \"codigo\": 400 }`.")
                        .contact(new Contact().name("Equipe ArthurBaby")))
                // Por padrão todos os endpoints exigem token; os públicos são liberados em respostasDeErroPadrao()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /**
     * Completa a documentação de todos os endpoints: adiciona os erros comuns (401 nos endpoints
     * protegidos e 500 em todos), define o corpo de erro padrão com um exemplo real nas respostas 4xx/5xx
     * e ordena as respostas por código.
     */
    @Bean
    OpenApiCustomizer respostasDeErroPadrao() {
        return openApi -> {
            // Registra o schema ErroResponse para ser referenciado por todas as respostas de erro
            Schema<?> erroSchema = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ErroResponse.class)).schema;
            openApi.getComponents().addSchemas(ERRO_SCHEMA, erroSchema);

            openApi.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((metodo, operacao) -> {
                ApiResponses respostas = operacao.getResponses();
                if (!publico(path, metodo)) {
                    respostas.putIfAbsent("401", new ApiResponse().description(
                            "Não autenticado: token JWT ausente, inválido ou expirado. Faça login e clique em Authorize."));
                } else {
                    // Lista de segurança vazia = endpoint público (Swagger não envia o token nem mostra o cadeado)
                    operacao.setSecurity(List.of());
                }
                respostas.putIfAbsent("500", new ApiResponse().description("Erro interno do servidor"));

                // Toda resposta 4xx/5xx recebe o ErroResponse com exemplo do código. Sobrescreve sempre porque,
                // com "produces" no controller, o springdoc preenche o erro com o schema de sucesso do método.
                respostas.forEach((codigo, resposta) -> {
                    if (codigo.startsWith("4") || codigo.startsWith("5")) {
                        resposta.setContent(conteudoErro(codigo));
                    }
                });

                // Ordena por código (200, 201, 400, 401...) para leitura mais fácil no Swagger UI
                Map<String, ApiResponse> ordenadas = new LinkedHashMap<>();
                respostas.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                        .forEach(e -> ordenadas.put(e.getKey(), e.getValue()));
                respostas.clear();
                respostas.putAll(ordenadas);
            }));
        };
    }

    /** Corpo {@code application/json} de erro com o exemplo correspondente ao código HTTP. */
    private static Content conteudoErro(String codigo) {
        Map<String, Object> exemplo = new LinkedHashMap<>();
        exemplo.put("erro", MENSAGENS_ERRO.getOrDefault(codigo, "Requisicao invalida"));
        exemplo.put("codigo", Integer.parseInt(codigo));
        return new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/" + ERRO_SCHEMA))
                .example(exemplo));
    }

    /** Espelha as rotas liberadas em SecurityConfig. Se mudar lá, mude aqui também. */
    private static boolean publico(String path, PathItem.HttpMethod metodo) {
        if (path.equals("/api/auth/me")) return false;
        if (path.startsWith("/api/auth/")) return true;
        if (path.equals("/api/cupons/validar") && metodo == PathItem.HttpMethod.POST) return true;
        return metodo == PathItem.HttpMethod.GET
                && (path.equals("/api/categorias") || path.startsWith("/api/produtos") || path.startsWith("/api/estoque/"));
    }
}
