package backend.academy.bot.applicationTests;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

// isolated from the "scrapper" module's containers!
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    private static final int REDIS_DOCKER_PORT = 6379;

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(REDIS_DOCKER_PORT);

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @DynamicPropertySource
    private static void registerRedisProperties(@NotNull DynamicPropertyRegistry registry) {
        // Redis
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(REDIS_DOCKER_PORT));

        // Kafka
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.producer.properties.acks", () -> "1");
        registry.add("spring.kafka.producer.properties.batch.size", () -> "1024");
        registry.add("spring.kafka.producer.properties.linger.ms", () -> "10000");
        registry.add("schema.registry.url", () -> "http://localhost:8083");

        registry.add("spring.kafka.consumer.properties.enable.auto.commit", () -> "false");
        registry.add("spring.kafka.consumer.properties.auto.offset.reset", () -> "earliest");
        registry.add("spring.kafka.consumer.properties.isolation.level", () -> "read_committed");
        registry.add("spring.kafka.consumer.properties.fetch.min.bytes", () -> "1024");
        registry.add("spring.kafka.consumer.properties.fetch.max.bytes", () -> "1048576");
        registry.add("spring.kafka.consumer.properties.fetch.max.wait.ms", () -> "10000");
        registry.add("spring.kafka.consumer.properties.max.poll.interval.ms", () -> "10000");
        registry.add("spring.kafka.consumer.properties.max.poll.records", () -> "10");
        registry.add("spring.kafka.consumer.properties.schema.registry.url", () -> "http://localhost:8083");
        registry.add("spring.kafka.consumer.properties.group.id", () -> "consumerGroupId");
    }
}
