package backend.academy.scrapper.exceptions;

public class NoSuchLinkException extends LinkTrackerException {
    public NoSuchLinkException(long linkId, Throwable cause) {
        super(String.format("link with id %d isn't exist", linkId), cause);
    }

    public NoSuchLinkException(long linkId) {
        super(String.format("link with id %d isn't exist", linkId));
    }

    public NoSuchLinkException(String url, Throwable cause) {
        super(String.format("link %s isn't tracked", url), cause);
    }

    public NoSuchLinkException(String url) {
        super(String.format("link %s isn't tracked", url));
    }
}
