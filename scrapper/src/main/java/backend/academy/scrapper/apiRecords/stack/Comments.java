package backend.academy.scrapper.apiRecords.stack;

import java.time.Instant;

public record Comments(CommentInfo[] items) {
    public record CommentInfo(Owner owner, Instant creation_date, String body) {}
}
