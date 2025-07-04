package backend.academy.scrapper.applicationTests;

import backend.academy.scrapper.apiRecords.git.*;
import backend.academy.scrapper.apiRecords.stack.*;
import backend.academy.scrapper.clients.GitHubClient;
import backend.academy.scrapper.clients.StackClient;
import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.LinkType;
import backend.academy.scrapper.link.LinkUpdatesInfo;
import backend.academy.scrapper.link.ListLinkUpdatesInfo;
import backend.academy.scrapper.service.GitHubService;
import backend.academy.scrapper.service.StackService;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SiteServiceResponsesTests {
    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private StackClient stackClient;

    @InjectMocks
    private GitHubService gitHubService;

    @InjectMocks
    private StackService stackService;

    @Test
    @DisplayName("gitHubService responses")
    void test1() {
        // Arrange
        final String url = "https://github.com/SomeUser/SomeRepository";
        final LinkBody linkBody = new LinkBody(123, url, Instant.parse("2024-09-01T10:15:30Z"), LinkType.GITHUB);

        final PullRequest[] pulls = getPrs();
        Mockito.when(gitHubClient.get("/repos/SomeUser/SomeRepository/pulls", PullRequest[].class))
                .thenReturn(pulls);

        final Issue[] issues = getIssues();
        Mockito.when(gitHubClient.get("/repos/SomeUser/SomeRepository/issues", Issue[].class))
                .thenReturn(issues);

        final ListLinkUpdatesInfo expected =
                new ListLinkUpdatesInfo(Instant.parse("2025-09-01T10:15:30Z"), new LinkUpdatesInfo[] {
                    new LinkUpdatesInfo("new pr: name=title3, createdAt=2025-09-01T10:15:30Z, preview=body3", "login3"),
                    new LinkUpdatesInfo(
                            "new issue: name=title2, createdAt=2024-11-01T10:15:30Z, preview=body2", "login2"),
                    new LinkUpdatesInfo(
                            "new issue: name=title3, createdAt=2025-09-01T10:15:30Z, preview=body3", "login3")
                });

        // Act
        final ListLinkUpdatesInfo actual = gitHubService.getLinkUpdate(linkBody);

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    private static @NotNull PullRequest @NotNull [] getPrs() {
        final PullRequest pull1 = new PullRequest(
                "title1", new PullRequest.User("login1"), "body1", Instant.parse("2021-09-01T10:15:30Z"));
        final PullRequest pull2 = new PullRequest(
                "title2", new PullRequest.User("login2"), "body2", Instant.parse("2024-09-01T10:15:30Z"));
        final PullRequest pull3 = new PullRequest(
                "title3", new PullRequest.User("login3"), "body3", Instant.parse("2025-09-01T10:15:30Z"));

        return new PullRequest[] {pull1, pull2, pull3};
    }

    private static @NotNull Issue @NotNull [] getIssues() {
        final Issue issue1 =
                new Issue("title1", new Issue.User("login1"), Instant.parse("2022-09-01T10:15:30Z"), "body1");
        final Issue issue2 =
                new Issue("title2", new Issue.User("login2"), Instant.parse("2024-11-01T10:15:30Z"), "body2");
        final Issue issue3 =
                new Issue("title3", new Issue.User("login3"), Instant.parse("2025-09-01T10:15:30Z"), "body3");

        return new Issue[] {issue1, issue2, issue3};
    }

    @Test
    @DisplayName("stackService responses")
    void test2() {
        // Arrange
        final String url = "https://stackoverflow.com/questions/12345/some-question-text";
        final LinkBody linkBody = new LinkBody(123, url, Instant.parse("2024-09-01T10:15:30Z"), LinkType.STACKOVERFLOW);

        final Answers answers = getAnswers();
        Mockito.when(stackClient.get("/2.3/questions/12345/answers?site=stackoverflow&filter=withbody", Answers.class))
                .thenReturn(answers);

        final Comments comments = getComments();
        Mockito.when(stackClient.get(
                        "/2.3/questions/12345/comments?site=stackoverflow&filter=withbody", Comments.class))
                .thenReturn(comments);

        final Question question = new Question(new Question.QuestionInfo[] {new Question.QuestionInfo("title")});
        Mockito.when(stackClient.get("/2.3/questions/12345?site=stackoverflow", Question.class))
                .thenReturn(question);

        final ListLinkUpdatesInfo expected =
                new ListLinkUpdatesInfo(Instant.parse("2025-09-01T10:15:30Z"), new LinkUpdatesInfo[] {
                    new LinkUpdatesInfo(
                            "new answer: question=title, creation_date=2025-09-01T10:15:30Z, answer_preview=body3",
                            "name3"),
                    new LinkUpdatesInfo(
                            "new comment: question=title, creation_date=2024-11-01T10:15:30Z, comment_preview=body2",
                            "name2"),
                    new LinkUpdatesInfo(
                            "new comment: question=title, creation_date=2025-09-01T10:15:30Z, comment_preview=body3",
                            "name3")
                });

        // Act
        final ListLinkUpdatesInfo actual = stackService.getLinkUpdate(linkBody);

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    private static @NotNull Comments getComments() {
        final Comments.CommentInfo commentInfo1 =
                new Comments.CommentInfo(new Owner("name1"), Instant.parse("2022-09-01T10:15:30Z"), "body1");
        final Comments.CommentInfo commentInfo2 =
                new Comments.CommentInfo(new Owner("name2"), Instant.parse("2024-11-01T10:15:30Z"), "body2");
        final Comments.CommentInfo commentInfo3 =
                new Comments.CommentInfo(new Owner("name3"), Instant.parse("2025-09-01T10:15:30Z"), "body3");

        return new Comments(new Comments.CommentInfo[] {commentInfo1, commentInfo2, commentInfo3});
    }

    private static @NotNull Answers getAnswers() {
        final Answers.AnswerInfo answerInfo1 =
                new Answers.AnswerInfo(new Owner("name1"), Instant.parse("2021-09-01T10:15:30Z"), "body1");
        final Answers.AnswerInfo answerInfo2 =
                new Answers.AnswerInfo(new Owner("name2"), Instant.parse("2024-09-01T10:15:30Z"), "body2");
        final Answers.AnswerInfo answerInfo3 =
                new Answers.AnswerInfo(new Owner("name3"), Instant.parse("2025-09-01T10:15:30Z"), "body3");

        return new Answers(new Answers.AnswerInfo[] {answerInfo1, answerInfo2, answerInfo3});
    }
}
