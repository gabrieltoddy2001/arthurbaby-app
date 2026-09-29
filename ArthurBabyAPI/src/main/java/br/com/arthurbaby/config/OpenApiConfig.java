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

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String ERRO_SCHEMA = "ErroResponse";

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
                                + "**Erros:** todas as respostas de erro seguem o formato "
                                + "`{ \"erro\": \"mensagem\", \"codigo\": 400 }`.")
                        .contact(new Contact().name("Equipe ArthurBaby")))
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
     * protegidos e 500 em todos), associa o corpo de erro padrão às respostas 4xx/5xx
     * e ordena as respostas por código.
     */
    @Bean
    OpenApiCustomizer respostasDeErroPadrao() {
        return openApi -> {
            Schema<?> erroSchema = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ErroResponse.class)).schema;
            openApi.getComponents().addSchemas(ERRO_SCHEMA, erroSchema);
            Content erroContent = new Content().addMediaType("application/json",
                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + ERRO_SCHEMA)));

            openApi.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((metodo, operacao) -> {
                ApiResponses respostas = operacao.getResponses();
                if (!publico(path, metodo)) {
                    respostas.putIfAbsent("401", new ApiResponse().description(
                            "Não autenticado: token JWT ausente, inválido ou expirado. Faça login e clique em Authorize."));
                } else {
                    operacao.setSecurity(List.of());
                }
                respostas.putIfAbsent("500", new ApiResponse().description("Erro interno do servidor"));

                respostas.forEach((codigo, resposta) -> {
                    if ((codigo.startsWith("4") || codigo.startsWith("5")) && resposta.getContent() == null) {
                        resposta.setContent(erroContent);
                    }
                });

                Map<String, ApiResponse> ordenadas = new LinkedHashMap<>();
                respostas.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                        .forEach(e -> ordenadas.put(e.getKey(), e.getValue()));
                respostas.clear();
                respostas.putAll(ordenadas);
            }));
        };
    }

    /** Espelha as rotas liberadas em SecurityConfig. */
    private static boolean publico(String path, PathItem.HttpMethod metodo) {
        if (path.equals("/api/auth/me")) return false;
        if (path.startsWith("/api/auth/")) return true;
        if (path.equals("/api/cupons/validar") && metodo == PathItem.HttpMethod.POST) return true;
        return metodo == PathItem.HttpMethod.GET
                && (path.equals("/api/categorias") || path.startsWith("/api/produtos") || path.startsWith("/api/estoque/"));
    }
}
