package backend.academy.scrapper.service;

import backend.academy.scrapper.apiRecords.stack.Answers;
import backend.academy.scrapper.apiRecords.stack.Comments;
import backend.academy.scrapper.apiRecords.stack.Question;
import backend.academy.scrapper.clients.StackClient;
import backend.academy.scrapper.link.LinkBody;
import backend.academy.scrapper.link.LinkUpdatesInfo;
import backend.academy.scrapper.link.ListLinkUpdatesInfo;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StackService implements SiteService {
    private final StackClient client;

    @Override
    public Instant getLastUpdateOrNow(String url) {
        final Answers answers = getAnswers(url);
        final Comments comments = getComments(url);

        final Stream<Instant> answersTimes = Arrays.stream(answers.items()).map(Answers.AnswerInfo::creation_date);
        final Stream<Instant> commentsTimes = Arrays.stream(comments.items()).map(Comments.CommentInfo::creation_date);

        return Stream.concat(answersTimes, commentsTimes)
                .reduce((max, cur) -> max.isAfter(cur) ? max : cur)
                .orElse(Instant.now());
    }

    @Override
    public ListLinkUpdatesInfo getLinkUpdate(@NotNull LinkBody link) {
        final Answers.AnswerInfo[] answers = Arrays.stream(
                        getAnswers(link.url()).items())
                .filter(o -> o.creation_date().isAfter(link.lastUpdated()))
                .toArray(Answers.AnswerInfo[]::new);
        final Comments.CommentInfo[] comments = Arrays.stream(
                        getComments(link.url()).items())
                .filter(o -> o.creation_date().isAfter(link.lastUpdated()))
                .toArray(Comments.CommentInfo[]::new);

        if (answers.length == 0 && comments.length == 0) {
            return null;
        }

        final Stream<Instant> answersTimes = Arrays.stream(answers).map(Answers.AnswerInfo::creation_date);
        final Stream<Instant> commentsTimes = Arrays.stream(comments).map(Comments.CommentInfo::creation_date);
        final String question = getQuestion(link.url()).items()[0].title();

        final Instant actualTime = Stream.concat(answersTimes, commentsTimes)
                .reduce((max, cur) -> max.isAfter(cur) ? max : cur)
                .orElse(Instant.now());

        final Stream<LinkUpdatesInfo> answersUpdateMessages = Arrays.stream(answers)
                .map(o -> new LinkUpdatesInfo(
                        String.format(
                                "new answer: question=%s, creation_date=%s, answer_preview=%s",
                                question, o.creation_date(), previewPart(o.body())),
                        o.owner().display_name()));

        final Stream<LinkUpdatesInfo> commentsUpdateMessages = Arrays.stream(comments)
                .map(o -> new LinkUpdatesInfo(
                        String.format(
                                "new comment: question=%s, creation_date=%s, comment_preview=%s",
                                question, o.creation_date(), previewPart(o.body())),
                        o.owner().display_name()));

        return new ListLinkUpdatesInfo(
                actualTime,
                Stream.concat(answersUpdateMessages, commentsUpdateMessages).toArray(LinkUpdatesInfo[]::new));
    }

    private Question getQuestion(String url) {
        return client.get(questionApiUrl(url), Question.class);
    }

    private Answers getAnswers(String url) {
        return client.get(answersApiUrl(url), Answers.class);
    }

    private Comments getComments(String url) {
        return client.get(commentsApiUrl(url), Comments.class);
    }

    private static String questionApiUrl(String url) {
        final String[] linkParts = url.split("/");
        return String.format("/2.3/questions/%s?site=stackoverflow", linkParts[4]);
    }

    private static String answersApiUrl(String url) {
        final String[] linkParts = url.split("/");
        return String.format("/2.3/questions/%s/answers?site=stackoverflow&filter=withbody", linkParts[4]);
    }

    private static String commentsApiUrl(String url) {
        final String[] linkParts = url.split("/");
        return String.format("/2.3/questions/%s/comments?site=stackoverflow&filter=withbody", linkParts[4]);
    }
}
