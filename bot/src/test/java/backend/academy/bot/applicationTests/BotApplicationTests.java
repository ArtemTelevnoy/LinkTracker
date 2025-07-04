package backend.academy.bot.applicationTests;

import backend.academy.bot.botCommands.Command;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.controller.KafkaController;
import backend.academy.bot.service.Bot;
import backend.academy.bot.service.ProcessingUpdates;
import backend.academy.bot.stateMachine.State;
import backend.academy.bot.stateMachine.StateMachine;
import backend.academy.dto.links.AddLinkRequest;
import backend.academy.dto.links.LinkResponse;
import backend.academy.dto.links.LinkUpdate;
import backend.academy.dto.links.ListLinksResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import java.util.List;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(
        properties = {
            "app.dlq-topic-name=dlq-test",
            "app.updates-topic-name=updates-test",
            "spring.kafka.consumer.group-id=testGroup",
            "app.message-transport=Kafka"
        })
@SpringBootTest
class BotApplicationTests extends TestcontainersConfiguration {

    @MockitoBean
    @SuppressWarnings("unused")
    private Bot bot;

    @MockitoBean
    @SuppressWarnings("unused")
    private ScrapperClient scrapperClient;

    @Autowired
    private StateMachine stateMachine;

    @Autowired
    private List<Command> commands;

    @Value("${app.dlq-topic-name}")
    private String dlqTopicName;

    @MockitoBean
    @SuppressWarnings("unused")
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean
    @SuppressWarnings("unused")
    private ProcessingUpdates processingUpdates;

    @Autowired
    private KafkaController kafkaController;

    @BeforeAll
    static void before() {
        redis.start();
        kafka.start();
    }

    @AfterAll
    static void after() {
        redis.stop();
        kafka.stop();
    }

    @Test
    @DisplayName("bot: unknown command")
    void test1() {
        // Arrange

        // Act
        final String answer1 = Bot.work("/some bad or unknown command", 123, stateMachine, scrapperClient, commands);
        final String answer2 = Bot.work("/invalidName", 123, stateMachine, scrapperClient, commands);
        final String answer3 = Bot.work("/tag", 123, stateMachine, scrapperClient, commands);

        // Assert
        Assertions.assertEquals("Unknown command", answer1);
        Assertions.assertEquals("Unknown command", answer2);
        Assertions.assertEquals("Unknown command", answer3);
    }

    @Test
    @DisplayName("list format: no any links")
    void test2() {
        // Arrange
        stateMachine.put(1, State.REGISTERED);

        Mockito.when(scrapperClient.getListResponse(1)).thenReturn(new ListLinksResponse(new LinkResponse[0], 0));

        // Act
        final String answer = Bot.work("/list", 1, stateMachine, scrapperClient, commands);

        // Assert
        Assertions.assertEquals("You don't track any links", answer);
    }

    @Test
    @DisplayName("list format: some links exist")
    void test3() {
        // Arrange
        stateMachine.put(1, State.REGISTERED);

        final String url = "https://github.com/user/repo";
        final LinkResponse linkResponse =
                new LinkResponse(1, url, new String[] {"tag1", "tag2", "tag3"}, new String[0]);

        Mockito.when(scrapperClient.getListResponse(1))
                .thenReturn(new ListLinksResponse(new LinkResponse[] {linkResponse}, 1));

        Mockito.when(scrapperClient.getTrackResponse(1, url, new String[] {"tag1", "tag2", "tag3"}, new String[0]))
                .thenReturn(linkResponse);

        // Act
        Bot.work("/track", 1, stateMachine, scrapperClient, commands);
        Bot.work(url, 1, stateMachine, scrapperClient, commands);
        Bot.work("tag1 tag2   tag3", 1, stateMachine, scrapperClient, commands);
        Bot.work("-", 1, stateMachine, scrapperClient, commands);

        final String answer = Bot.work("/list", 1, stateMachine, scrapperClient, commands);

        // Assert
        Assertions.assertEquals(
                String.format("Your links:%nlink=https://github.com/user/repo, "
                        + "tags=[tag1, tag2, tag3], filters=No any items"),
                answer);
    }

    @Test
    @DisplayName("link validation")
    void test4() {
        // Arrange
        final AddLinkRequest request1 = new AddLinkRequest("https://github.com/user/repo", null, null);
        final AddLinkRequest request2 =
                new AddLinkRequest("https://stackoverflow.com/questions/123456/question", null, null);
        final AddLinkRequest request3 = new AddLinkRequest("some invalid url", null, null);

        // Act
        final Set<ConstraintViolation<AddLinkRequest>> violations1;
        final Set<ConstraintViolation<AddLinkRequest>> violations2;
        final Set<ConstraintViolation<AddLinkRequest>> violations3;
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            violations1 = factory.getValidator().validate(request1);
            violations2 = factory.getValidator().validate(request2);
            violations3 = factory.getValidator().validate(request3);
        }

        // Assert
        Assertions.assertTrue(violations1.isEmpty());
        Assertions.assertTrue(violations2.isEmpty());
        Assertions.assertFalse(violations3.isEmpty());
    }

    @Test
    @SneakyThrows
    @DisplayName("objectMapper mapped correct json to dto")
    void test5() {
        // Arrange
        final ObjectMapper mapper = new ObjectMapper();

        final String goodJson1 = "{\"description\":\"desc1\",\"userId\":1}";
        final String goodJson2 = "{\"description\":\"desc2\",\"userId\":2}";
        final String goodJson3 = "{\"description\":\"desc3\",\"userId\":1}";

        final LinkUpdate linkUpdate1 = new LinkUpdate("desc1", 1);
        final LinkUpdate linkUpdate2 = new LinkUpdate("desc2", 2);
        final LinkUpdate linkUpdate3 = new LinkUpdate("desc3", 1);

        // Act
        final LinkUpdate mappedLinkUpdate1 = mapper.readValue(goodJson1, LinkUpdate.class);
        final LinkUpdate mappedLinkUpdate2 = mapper.readValue(goodJson2, LinkUpdate.class);
        final LinkUpdate mappedLinkUpdate3 = mapper.readValue(goodJson3, LinkUpdate.class);

        // Assert
        Assertions.assertEquals(linkUpdate1, mappedLinkUpdate1);
        Assertions.assertEquals(linkUpdate2, mappedLinkUpdate2);
        Assertions.assertEquals(linkUpdate3, mappedLinkUpdate3);
    }

    @Test
    @SneakyThrows
    @DisplayName("kafka: invalid messages sending to dead letter queue")
    void test6() {
        // Arrange
        final String[] invalidMessages = new String[] {
            "invalid message", "{\"description\":\"desc1\",\"userId\":}", "{\"dn\":\"desc1\",\"userId\":1}"
        };

        // Act
        for (String message : invalidMessages) {
            kafkaController.consume(message);
        }

        // Assert
        Mockito.verify(kafkaTemplate, Mockito.times(invalidMessages.length))
                .send(Mockito.eq(dlqTopicName), Mockito.anyString());

        Mockito.verify(processingUpdates, Mockito.never()).updates(Mockito.any());
    }

    @Test
    @SneakyThrows
    @DisplayName("kafka: good messages send to processingUpdates")
    void test7() {
        // Arrange
        final ObjectMapper mapper = new ObjectMapper();
        final LinkUpdate linkUpdate1 = new LinkUpdate("desc1", 1);
        final LinkUpdate linkUpdate2 = new LinkUpdate("desc2", 2);
        final LinkUpdate linkUpdate3 = new LinkUpdate("desc3", 1);

        // Act
        kafkaController.consume(mapper.writeValueAsString(linkUpdate1));
        kafkaController.consume(mapper.writeValueAsString(linkUpdate2));
        kafkaController.consume(mapper.writeValueAsString(linkUpdate3));

        // Assert
        Mockito.verify(processingUpdates, Mockito.times(1)).updates(linkUpdate1);
        Mockito.verify(processingUpdates, Mockito.times(1)).updates(linkUpdate2);
        Mockito.verify(processingUpdates, Mockito.times(1)).updates(linkUpdate3);

        Mockito.verify(kafkaTemplate, Mockito.never()).send(Mockito.eq(dlqTopicName), Mockito.anyString());
    }
}
