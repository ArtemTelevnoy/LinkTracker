package backend.academy.scrapper.apiRecords.git;

import java.time.Instant;

public record PullRequest(String title, User user, String body, Instant created_at) {
    public record User(String login) {}
}
