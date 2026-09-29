package br.com.arthurbaby.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

/**
 * Ajustes de esquema que o {@code ddl-auto=update} do Hibernate nao faz sozinho (ele so cria tabelas/colunas).
 * Roda depois do Hibernate e antes do seed; cada passo verifica o estado atual e e idempotente.
 */
@Component
@Order(1)
public class SchemaMigration implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public SchemaMigration(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        migrarCupomPercentualParaTipoValor();
        alinharFkPasswordResetTokenNoMysql();
    }

    /** Cupom antigo (so percentual_desconto NOT NULL) vira PERCENTUAL com valor = percentual; a coluna antiga sai. */
    private void migrarCupomPercentualParaTipoValor() throws SQLException {
        if (!colunaExiste("cupom", "percentual_desconto")) return;
        int migrados = jdbc.update("UPDATE cupom SET tipo = 'PERCENTUAL', valor = percentual_desconto "
                + "WHERE valor IS NULL AND percentual_desconto IS NOT NULL");
        jdbc.execute("ALTER TABLE cupom DROP COLUMN percentual_desconto");
        log.info("[MIGRACAO] cupom: {} cupom(ns) convertido(s) para tipo/valor; coluna percentual_desconto removida", migrados);
    }

    /**
     * Bancos criados pelo script arthurbaby_bd.sql usam usuario.id BIGINT UNSIGNED; o Hibernate cria
     * password_reset_token.usuario_id como BIGINT e o MySQL recusa a FK. Aqui a coluna e alinhada e a FK criada.
     */
    private void alinharFkPasswordResetTokenNoMysql() throws SQLException {
        if (!mysql() || !colunaExiste("password_reset_token", "usuario_id")) return;
        String tipoUsuarioId = tipoColuna("usuario", "id");
        String tipoFk = tipoColuna("password_reset_token", "usuario_id");
        if (tipoUsuarioId == null || tipoFk == null) return;
        if (tipoUsuarioId.contains("unsigned") && !tipoFk.contains("unsigned")) {
            jdbc.execute("ALTER TABLE password_reset_token MODIFY usuario_id BIGINT UNSIGNED NOT NULL");
            log.info("[MIGRACAO] password_reset_token.usuario_id alterado para BIGINT UNSIGNED");
        }
        Integer fks = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME = 'password_reset_token' AND COLUMN_NAME = 'usuario_id' AND REFERENCED_TABLE_NAME = 'usuario'", Integer.class);
        if (fks != null && fks == 0) {
            jdbc.execute("ALTER TABLE password_reset_token ADD CONSTRAINT fk_password_reset_token_usuario "
                    + "FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE");
            log.info("[MIGRACAO] FK fk_password_reset_token_usuario criada");
        }
    }

    private boolean mysql() throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            return c.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT).contains("mysql");
        }
    }

    private String tipoColuna(String tabela, String coluna) {
        List<String> tipos = jdbc.queryForList("SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME = ? AND COLUMN_NAME = ?", String.class, tabela, coluna);
        return tipos.isEmpty() ? null : tipos.getFirst().toLowerCase(Locale.ROOT);
    }

    /** Funciona em MySQL (nomes minusculos) e H2 (nomes maiusculos). */
    private boolean colunaExiste(String tabela, String coluna) throws SQLException {
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData meta = c.getMetaData();
            for (String t : List.of(tabela, tabela.toUpperCase(Locale.ROOT))) {
                for (String col : List.of(coluna, coluna.toUpperCase(Locale.ROOT))) {
                    try (ResultSet rs = meta.getColumns(c.getCatalog(), null, t, col)) {
                        if (rs.next()) return true;
                    }
                }
            }
            return false;
        }
    }
}
