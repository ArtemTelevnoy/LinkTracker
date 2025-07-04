package backend.academy.bot.applicationTests;

import backend.academy.bot.botCommands.Command;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.controller.KafkaController;
import backend.academy.bot.redis.RedisService;
import backend.academy.bot.service.Bot;
import backend.academy.bot.stateMachine.State;
import backend.academy.bot.stateMachine.StateMachine;
import backend.academy.dto.links.LinkResponse;
import backend.academy.dto.links.ListLinksResponse;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RedisCacheTests extends TestcontainersConfiguration {

    @MockitoBean
    @SuppressWarnings("unused")
    private Bot bot;

    @MockitoBean
    @SuppressWarnings("unused")
    private KafkaController kafkaController;

    @Mock
    private ScrapperClient scrapperClient;

    @Autowired
    private StateMachine stateMachine;

    @Autowired
    private RedisService redisListService;

    @Autowired
    private List<Command> commands;

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
    @DisplayName("redis cache: get answer from hash")
    void test1() {
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

        final String realResponseAnswer = Bot.work("/list", 1, stateMachine, scrapperClient, commands);
        final String redisAnswer = Bot.work("/list", 1, stateMachine, scrapperClient, commands);

        // Assert
        Mockito.verify(scrapperClient, Mockito.times(1)).getListResponse(1);
        Assertions.assertEquals(realResponseAnswer, redisAnswer);
    }

    @Test
    @DisplayName("redis cache: invalidate data")
    void test2() {
        // Arrange
        final String url1 = "https://github.com/user/repo2";
        final LinkResponse linkResponse1 =
                new LinkResponse(1, url1, new String[] {"tag1", "tag2", "tag3"}, new String[0]);
        final String url2 = "https://github.com/user/repo2";
        final LinkResponse linkResponse2 =
                new LinkResponse(1, url2, new String[] {"tag1", "tag2", "tag3"}, new String[0]);

        Mockito.when(scrapperClient.getListResponse(1))
                .thenReturn(new ListLinksResponse(new LinkResponse[] {linkResponse1, linkResponse2}, 1));

        Mockito.when(scrapperClient.getTrackResponse(1, url2, new String[] {"tag1", "tag2", "tag3"}, new String[0]))
                .thenReturn(linkResponse2);

        // Act
        final String cacheBeforeInvalidation = redisListService.get(1);

        Bot.work("/track", 1, stateMachine, scrapperClient, commands);
        Bot.work(url2, 1, stateMachine, scrapperClient, commands);
        Bot.work("tag1 tag2   tag3", 1, stateMachine, scrapperClient, commands);
        Bot.work("-", 1, stateMachine, scrapperClient, commands);
        final String cacheAfterInvalidation = redisListService.get(1);

        // Assert
        Assertions.assertNotNull(cacheBeforeInvalidation);
        Assertions.assertNull(cacheAfterInvalidation);
    }
}
