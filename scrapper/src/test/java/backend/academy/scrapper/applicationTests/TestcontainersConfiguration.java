package backend.academy.scrapper.applicationTests;

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
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

// isolated from the "bot" module's containers!
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    private static final int REDIS_DOCKER_PORT = 6379;

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(REDIS_DOCKER_PORT);

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withExposedPorts(5432)
            .withDatabaseName("local")
            .withUsername("postgres")
            .withPassword("test");

    @DynamicPropertySource
    private static void registerRedisProperties(@NotNull DynamicPropertyRegistry registry) {
        // Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.access-type", () -> "ORM");

        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(REDIS_DOCKER_PORT));

        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.properties.acks", () -> "1");
        registry.add("spring.kafka.producer.properties.batch.size", () -> "1024");
        registry.add("spring.kafka.producer.properties.linger.ms", () -> "10000");
        registry.add("schema.registry.url", () -> "http://localhost:8083");
    }

    @PostConstruct
    private void init() {
        final Path changelogPath =
                new File(".").toPath().toAbsolutePath().getParent().getParent().resolve("migrations");

        migrate(changelogPath);
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
