package br.org.assandef.assandefsystem;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@SpringBootApplication
public class AssandefsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssandefsystemApplication.class, args);
	}

	@Bean
	ApplicationRunner ajustarSchemaSolicitacoesAluguelSalao(JdbcTemplate jdbcTemplate) {
		return args -> {
			String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
			if (database == null || database.isBlank()) {
				return;
			}

			Integer tabelaExiste = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'solicitacoes_aluguel_salao'",
					Integer.class,
					database
			);
			if (tabelaExiste == null || tabelaExiste == 0) {
				return;
			}

			Integer colunaLegadaExiste = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'solicitacoes_aluguel_salao' AND COLUMN_NAME = 'id_disponibilidade'",
					Integer.class,
					database
			);
			if (colunaLegadaExiste != null && colunaLegadaExiste > 0) {
				List<String> foreignKeys = jdbcTemplate.queryForList(
						"SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'solicitacoes_aluguel_salao' AND COLUMN_NAME = 'id_disponibilidade' AND REFERENCED_TABLE_NAME IS NOT NULL",
						String.class,
						database
				);
				for (String foreignKey : foreignKeys) {
					jdbcTemplate.execute("ALTER TABLE solicitacoes_aluguel_salao DROP FOREIGN KEY `" + foreignKey + "`");
				}

				List<String> indexes = jdbcTemplate.queryForList(
						"SELECT DISTINCT INDEX_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'solicitacoes_aluguel_salao' AND COLUMN_NAME = 'id_disponibilidade' AND INDEX_NAME <> 'PRIMARY'",
						String.class,
						database
				);
				for (String index : indexes) {
					jdbcTemplate.execute("ALTER TABLE solicitacoes_aluguel_salao DROP INDEX `" + index + "`");
				}

				jdbcTemplate.execute("ALTER TABLE solicitacoes_aluguel_salao DROP COLUMN id_disponibilidade");
			}

			Integer statusInvalido = jdbcTemplate.queryForObject(
					"SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'solicitacoes_aluguel_salao' AND COLUMN_NAME = 'status' AND COLUMN_TYPE = \"enum('APROVADA','CANCELADA','EM_CONTATO','PENDENTE','RECUSADA')\"",
					Integer.class,
					database
			);
			if (statusInvalido != null && statusInvalido > 0) {
				jdbcTemplate.execute("ALTER TABLE solicitacoes_aluguel_salao MODIFY COLUMN status ENUM('PENDENTE','EM_CONTATO','ALUGADO','RECUSADA','CANCELADA') NOT NULL DEFAULT 'PENDENTE'");
			}
		};
	}
}
