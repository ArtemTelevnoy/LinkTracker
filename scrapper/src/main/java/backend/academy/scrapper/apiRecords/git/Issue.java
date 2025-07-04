package backend.academy.scrapper.apiRecords.git;

import java.time.Instant;

public record Issue(String title, User user, Instant created_at, String body) {
    public record User(String login) {}
}
