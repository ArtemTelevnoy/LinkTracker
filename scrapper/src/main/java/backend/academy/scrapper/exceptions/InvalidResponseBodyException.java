package backend.academy.scrapper.exceptions;

public class InvalidResponseBodyException extends LinkTrackerException {
    public InvalidResponseBodyException(String url, Throwable cause) {
        super(String.format("Invalid body of response by url=%s", url), cause);
    }
}
