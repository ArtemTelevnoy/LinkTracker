package backend.academy.scrapper.safeTests;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.ConfluentKafkaContainer;

// isolated from the "bot" module's containers!
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

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
        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.properties.acks", () -> "1");
        registry.add("spring.kafka.producer.properties.batch.size", () -> "1024");
        registry.add("spring.kafka.producer.properties.linger.ms", () -> "10000");
        registry.add("schema.registry.url", () -> "http://localhost:8083");
    }
}
