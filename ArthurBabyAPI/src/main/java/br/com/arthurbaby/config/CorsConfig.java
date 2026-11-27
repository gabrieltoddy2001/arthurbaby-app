package br.com.arthurbaby.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Origens que podem chamar a API pelo navegador (o painel web).
 * Além das locais de desenvolvimento, aceita as da variável CORS_ORIGENS, separadas por vírgula
 * (ex.: CORS_ORIGENS=https://meu-painel.vercel.app). Assim o domínio publicado não fica fixo no código.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final List<String> ORIGENS_LOCAIS = List.of(
            "http://127.0.0.1:5500", "http://localhost:5500",
            "http://127.0.0.1:5501", "http://localhost:5501",
            "http://127.0.0.1:8080", "http://localhost:8080");

    private final List<String> origens = new ArrayList<>(ORIGENS_LOCAIS);

    public CorsConfig(@Value("${CORS_ORIGENS:}") String extras) {
        Arrays.stream(extras.split(","))
                .map(String::trim)
                .map(o -> o.replaceAll("/+$", ""))
                .filter(o -> !o.isEmpty())
                .forEach(origens::add);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origens.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
