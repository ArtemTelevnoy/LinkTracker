package backend.academy.scrapper.exceptions;

public abstract class LinkTrackerException extends RuntimeException {
    public LinkTrackerException(String message) {
        super(message);
    }

    public LinkTrackerException(String message, Throwable cause) {
        super(message, cause);
    }
}
