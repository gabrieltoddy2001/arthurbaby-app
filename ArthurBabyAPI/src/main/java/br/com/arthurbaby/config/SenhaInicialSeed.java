package br.com.arthurbaby.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Senha das contas criadas pelos seeds (DataInitializer e DadosExemploInitializer).
 * Vem de SEED_SENHA_INICIAL (variável de ambiente ou application-local.properties, que não vai para o Git).
 * Sem configuração, gera uma senha aleatória a cada inicialização e mostra no log: nenhuma senha fixa fica no código.
 */
@Component
public class SenhaInicialSeed {
    private static final Logger log = LoggerFactory.getLogger(SenhaInicialSeed.class);
    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    private final String senha;
    private final String emailAdmin;
    private boolean avisado;

    public SenhaInicialSeed(@Value("${SEED_SENHA_INICIAL:}") String configurada,
                            @Value("${ADMIN_EMAIL:admin@arthurbaby.com}") String emailAdmin) {
        this.emailAdmin = emailAdmin.trim().toLowerCase(java.util.Locale.ROOT);
        if (configurada != null && !configurada.isBlank()) {
            this.senha = configurada;
            this.avisado = true;
        } else {
            SecureRandom random = new SecureRandom();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 14; i++) sb.append(LETRAS.charAt(random.nextInt(LETRAS.length())));
            this.senha = sb.toString();
        }
    }

    /** E-mail do único administrador criado num banco vazio (variável ADMIN_EMAIL). */
    public String emailAdmin() { return emailAdmin; }

    /**
     * Senha aleatória e descartável para as clientes de exemplo: elas existem só para ter pedidos, avaliações
     * e favoritos de teste, e ninguém deve conseguir entrar no app com elas.
     */
    public static String senhaAleatoria() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) sb.append(LETRAS.charAt(random.nextInt(LETRAS.length())));
        return sb.toString();
    }

    /** Chamado só quando um seed realmente cria uma conta; avisa no log uma única vez se a senha foi gerada. */
    public String senha() {
        if (!avisado) {
            avisado = true;
            log.warn("SEED_SENHA_INICIAL nao configurada: contas de exemplo criadas com a senha gerada '{}'. "
                    + "Defina SEED_SENHA_INICIAL (variavel de ambiente ou application-local.properties) para fixar uma senha.", senha);
        }
        return senha;
    }
}
