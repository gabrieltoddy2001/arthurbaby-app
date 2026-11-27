package br.com.arthurbaby.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Ambiente de demonstração: todo dia, no horário RESET_HORA (padrão 4h, horário de Brasília), apaga todos os dados
 * e recria a carga inicial (administrador, catálogo, pedidos de exemplo). Assim o que os testadores alteram, ou o que
 * alguém mal-intencionado bagunçar, dura no máximo um dia.
 * <p>
 * Só existe com RESET_DIARIO=true. No plano gratuito do Render o servidor "dorme" e nenhum agendamento roda; por isso,
 * além de conferir a cada 15 minutos, confere também ao subir: se o horário de reinício passou desde o último, reinicia.
 * A data do último reinício fica na tabela reinicio_controle (que não é apagada).
 */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "RESET_DIARIO", havingValue = "true")
public class ReinicioDiario {
    private static final Logger log = LoggerFactory.getLogger(ReinicioDiario.class);
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final String TABELA_CONTROLE = "reinicio_controle";

    private final JdbcTemplate jdbc;
    private final ApplicationContext contexto;
    private final int hora;

    public ReinicioDiario(JdbcTemplate jdbc, ApplicationContext contexto,
                          @org.springframework.beans.factory.annotation.Value("${RESET_HORA:4}") int hora) {
        this.jdbc = jdbc;
        this.contexto = contexto;
        this.hora = Math.max(0, Math.min(23, hora));
    }

    /** Depois que a API subiu (e os seeds já rodaram). */
    @EventListener(ApplicationReadyEvent.class)
    public void aoSubir() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS " + TABELA_CONTROLE + " (id INT PRIMARY KEY, ultimo DATETIME NOT NULL)");
        List<Timestamp> ultimo = jdbc.queryForList("SELECT ultimo FROM " + TABELA_CONTROLE + " WHERE id = 1", Timestamp.class);
        if (ultimo.isEmpty()) {
            // Banco novo: a carga inicial acabou de rodar, então conta como o reinício de hoje
            jdbc.update("INSERT INTO " + TABELA_CONTROLE + " (id, ultimo) VALUES (1, ?)", Timestamp.valueOf(agora()));
            log.info("[REINICIO] Controle criado; próximo reinício às {}h", hora);
            return;
        }
        conferir();
    }

    @Scheduled(cron = "0 */15 * * * *", zone = "America/Sao_Paulo")
    public synchronized void conferir() {
        List<Timestamp> ultimo = jdbc.queryForList("SELECT ultimo FROM " + TABELA_CONTROLE + " WHERE id = 1", Timestamp.class);
        LocalDateTime corte = ultimoHorarioDeReinicio();
        if (!ultimo.isEmpty() && !ultimo.get(0).toLocalDateTime().isBefore(corte)) return;
        reiniciar();
    }

    /** Apaga todas as tabelas (menos a de controle) e roda de novo a carga inicial, na mesma ordem da inicialização. */
    private void reiniciar() {
        log.warn("[REINICIO] Reiniciando o banco de demonstração");
        List<String> tabelas = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE() "
                        + "AND table_type = 'BASE TABLE' AND table_name <> ?", String.class, TABELA_CONTROLE);
        // Tudo na mesma conexão: FOREIGN_KEY_CHECKS vale só para a conexão em que foi definido
        jdbc.execute((org.springframework.jdbc.core.ConnectionCallback<Void>) con -> {
            try (java.sql.Statement st = con.createStatement()) {
                st.execute("SET FOREIGN_KEY_CHECKS = 0");
                try {
                    for (String tabela : tabelas) st.execute("TRUNCATE TABLE `" + tabela + "`");
                } finally {
                    st.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
            }
            return null;
        });
        contexto.getBeanProvider(CommandLineRunner.class).orderedStream().forEach(runner -> {
            try {
                runner.run();
            } catch (Exception e) {
                log.error("[REINICIO] Falha ao recriar dados em {}", runner.getClass().getSimpleName(), e);
            }
        });
        jdbc.update("UPDATE " + TABELA_CONTROLE + " SET ultimo = ? WHERE id = 1", Timestamp.valueOf(agora()));
        log.warn("[REINICIO] Banco de demonstração recriado ({} tabelas)", tabelas.size());
    }

    /** Horário de reinício mais recente que já passou (hoje às RESET_HORA, ou ontem se ainda não chegou). */
    private LocalDateTime ultimoHorarioDeReinicio() {
        ZonedDateTime agora = ZonedDateTime.now(FUSO);
        ZonedDateTime hoje = agora.toLocalDate().atTime(hora, 0).atZone(FUSO);
        return (agora.isBefore(hoje) ? hoje.minusDays(1) : hoje).toLocalDateTime();
    }

    private static LocalDateTime agora() {
        return LocalDateTime.now(FUSO);
    }
}
