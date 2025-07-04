package backend.academy.scrapper.exceptions;

public class NotExistApiException extends LinkTrackerException {
    public NotExistApiException(String url) {
        super(String.format("api %s isn't exist", url));
    }
}
