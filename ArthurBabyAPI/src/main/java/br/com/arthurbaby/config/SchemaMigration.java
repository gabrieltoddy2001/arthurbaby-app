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
        alinharFkNoMysql("password_reset_token", "usuario_id", "usuario", "fk_password_reset_token_usuario");
        alinharFkNoMysql("avaliacao", "produto_id", "produto", "fk_avaliacao_produto");
        alinharFkNoMysql("avaliacao", "cliente_id", "usuario", "fk_avaliacao_cliente");
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
     * Bancos criados pelo script arthurbaby_bd.sql usam ids BIGINT UNSIGNED (usuario, produto...); o Hibernate cria
     * as colunas de FK das tabelas novas (password_reset_token, avaliacao) como BIGINT e o MySQL recusa a FK.
     * Aqui a coluna e alinhada ao tipo do id referenciado e a FK (ON DELETE CASCADE) e criada se ainda faltar.
     * Os nomes de tabela/coluna sao constantes do proprio codigo (nunca vem de fora), por isso podem ir no SQL.
     */
    private void alinharFkNoMysql(String tabela, String coluna, String tabelaRef, String nomeFk) throws SQLException {
        if (!mysql() || !colunaExiste(tabela, coluna)) return;
        String tipoRefId = tipoColuna(tabelaRef, "id");
        String tipoFk = tipoColuna(tabela, coluna);
        if (tipoRefId == null || tipoFk == null) return;
        if (tipoRefId.contains("unsigned") && !tipoFk.contains("unsigned")) {
            jdbc.execute("ALTER TABLE " + tabela + " MODIFY " + coluna + " BIGINT UNSIGNED NOT NULL");
            log.info("[MIGRACAO] {}.{} alterado para BIGINT UNSIGNED", tabela, coluna);
        }
        Integer fks = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = DATABASE() "
                + "AND TABLE_NAME = ? AND COLUMN_NAME = ? AND REFERENCED_TABLE_NAME = ?", Integer.class, tabela, coluna, tabelaRef);
        if (fks != null && fks == 0) {
            jdbc.execute("ALTER TABLE " + tabela + " ADD CONSTRAINT " + nomeFk
                    + " FOREIGN KEY (" + coluna + ") REFERENCES " + tabelaRef + " (id) ON DELETE CASCADE");
            log.info("[MIGRACAO] FK {} criada", nomeFk);
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
