package backend.academy.scrapper.exceptions;

public class DuplicateLinkException extends LinkTrackerException {
    public DuplicateLinkException(long linkId, Throwable cause) {
        super(String.format("link with id %d is duplicate", linkId), cause);
    }
}
