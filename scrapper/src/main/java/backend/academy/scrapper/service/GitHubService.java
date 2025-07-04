package backend.academy.scrapper.service;

import backend.academy.scrapper.apiRecords.git.Issue;
import backend.academy.scrapper.apiRecords.git.PullRequest;
import backend.academy.scrapper.clients.GitHubClient;
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
public class GitHubService implements SiteService {
    private final GitHubClient client;

    @Override
    public Instant getLastUpdateOrNow(String url) {
        final PullRequest[] prs = getPrs(url);
        final Issue[] issues = getIssues(url);

        final Stream<Instant> prsTimes = Arrays.stream(prs).map(PullRequest::created_at);
        final Stream<Instant> issuesTimes = Arrays.stream(issues).map(Issue::created_at);

        return Stream.concat(prsTimes, issuesTimes)
                .reduce((max, cur) -> max.isAfter(cur) ? max : cur)
                .orElse(Instant.now());
    }

    @Override
    public ListLinkUpdatesInfo getLinkUpdate(@NotNull LinkBody link) {
        final PullRequest[] prs = Arrays.stream(getPrs(link.url()))
                .filter(o -> o.created_at().isAfter(link.lastUpdated()))
                .toArray(PullRequest[]::new);
        final Issue[] issues = Arrays.stream(getIssues(link.url()))
                .filter(o -> o.created_at().isAfter(link.lastUpdated()))
                .toArray(Issue[]::new);

        if (prs.length == 0 && issues.length == 0) {
            return null;
        }

        final Stream<Instant> prsTimes = Arrays.stream(prs).map(PullRequest::created_at);
        final Stream<Instant> issuesTimes = Arrays.stream(issues).map(Issue::created_at);

        final Instant actualTime = Stream.concat(prsTimes, issuesTimes)
                .reduce((max, cur) -> max.isAfter(cur) ? max : cur)
                .orElse(Instant.now());

        final Stream<LinkUpdatesInfo> prsUpdateMessages = Arrays.stream(prs)
                .map(o -> new LinkUpdatesInfo(
                        String.format(
                                "new pr: name=%s, createdAt=%s, preview=%s",
                                o.title(), o.created_at(), previewPart(o.body())),
                        o.user().login()));

        final Stream<LinkUpdatesInfo> issuesUpdateMessages = Arrays.stream(issues)
                .map(o -> new LinkUpdatesInfo(
                        String.format(
                                "new issue: name=%s, createdAt=%s, preview=%s",
                                o.title(), o.created_at(), previewPart(o.body())),
                        o.user().login()));

        return new ListLinkUpdatesInfo(
                actualTime,
                Stream.concat(prsUpdateMessages, issuesUpdateMessages).toArray(LinkUpdatesInfo[]::new));
    }

    private PullRequest[] getPrs(String url) {
        return client.get(prsApiUrl(url), PullRequest[].class);
    }

    private Issue[] getIssues(String url) {
        return client.get(issuesApiUrl(url), Issue[].class);
    }

    private static String prsApiUrl(@NotNull String url) {
        final String[] linkParts = url.split("/");
        return String.format("/repos/%s/%s/pulls", linkParts[3], linkParts[4]);
    }

    private static String issuesApiUrl(@NotNull String url) {
        final String[] linkParts = url.split("/");
        return String.format("/repos/%s/%s/issues", linkParts[3], linkParts[4]);
    }
}
