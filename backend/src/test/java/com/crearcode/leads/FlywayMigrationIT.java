package com.crearcode.leads;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FlywayMigrationIT {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void aplicaLaMigracionBaselineAlArrancarContraPostgresReal() {
		Integer migracionesExitosas = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where success = true",
				Integer.class);

		assertThat(migracionesExitosas).isGreaterThanOrEqualTo(1);
	}

}
