package backend.academy.scrapper.apiRecords.stack;

public record Question(QuestionInfo[] items) {
    public record QuestionInfo(String title) {}
}
