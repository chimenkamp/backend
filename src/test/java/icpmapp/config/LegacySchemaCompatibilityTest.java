package icpmapp.config;

import icpmapp.ConferiaApplication;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacySchemaCompatibilityTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void baselinesCompatibleLegacyDatabaseWithoutFlywayHistory() throws Exception {
        String url = databaseUrl("compatible");
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             InputStreamReader migration = new InputStreamReader(
                 new ClassPathResource("db/migration/V1__create_conferia_schema.sql").getInputStream(),
                 StandardCharsets.UTF_8
             )) {
            RunScript.execute(connection, migration);
        }

        try (ConfigurableApplicationContext context = runApplication(url)) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
            assertEquals(
                1,
                jdbc.queryForObject(
                    "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"type\" = 'BASELINE'",
                    Integer.class
                )
            );
        }
    }

    @Test
    void rejectsIncompatibleLegacyDatabaseWithSchemaDiagnostic() throws Exception {
        String url = databaseUrl("incompatible");
        try (Connection connection = DriverManager.getConnection(url, "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE incompatible_schema (id INTEGER PRIMARY KEY)");
        }

        RuntimeException exception = assertThrows(RuntimeException.class, () -> runApplication(url));

        String diagnostic = completeMessage(exception);
        String normalizedDiagnostic = diagnostic.toLowerCase(Locale.ROOT);
        assertTrue(
            diagnostic.contains("Schema-validation")
                || normalizedDiagnostic.contains("missing table")
                || (normalizedDiagnostic.contains("table \"users\" not found")
                    && normalizedDiagnostic.contains(
                        "migration v3__add_agenda_push_reminders.sql failed")),
            diagnostic
        );
    }

    private ConfigurableApplicationContext runApplication(String url) {
        SpringApplication application = new SpringApplication(ConferiaApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        return application.run(
            "--spring.datasource.url=" + url,
            "--spring.datasource.username=sa",
            "--spring.datasource.password=",
            "--conferia.setup-token=test-setup-token",
            "--conferia.jwt-secret=8HMAnIuSaJewPZpV2ah13uLOmySq7wE+kpmRWffu0rg=",
            "--server.port=0",
            "--spring.jmx.enabled=false",
            "--spring.main.banner-mode=off",
            "--debug=false",
            "--logging.level.root=OFF"
        );
    }

    private String databaseUrl(String name) {
        return "jdbc:h2:file:"
            + temporaryDirectory.resolve(name);
    }

    private String completeMessage(Throwable throwable) {
        StringBuilder message = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null) {
                message.append(current.getMessage()).append('\n');
            }
            current = current.getCause();
        }
        return message.toString();
    }
}
