package backend.academy.scrapper.postgresTests;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Path;
import java.sql.DriverManager;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresContainer {
    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withExposedPorts(5432)
            .withDatabaseName("local")
            .withUsername("postgres")
            .withPassword("test");

    @PostConstruct
    private void init() {
        final Path changelogPath =
                new File(".").toPath().toAbsolutePath().getParent().getParent().resolve("migrations");

        migrate(changelogPath);
    }

    @DynamicPropertySource
    private static void configureDatabase(@NotNull DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private void migrate(Path changelogPath) {
        try (var connection =
                DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            final var database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            final var liquibase = new Liquibase("master.xml", new DirectoryResourceAccessor(changelogPath), database);

            liquibase.update(new Contexts(), new LabelExpression());
        } catch (Exception e) {
            System.err.printf("Something was bad: %s%n", e.getMessage());
        }
    }
}
