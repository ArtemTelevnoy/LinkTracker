package backend.academy.scrapper.apiRecords.stack;

import java.time.Instant;

public record Answers(AnswerInfo[] items) {
    public record AnswerInfo(Owner owner, Instant creation_date, String body) {}
}
