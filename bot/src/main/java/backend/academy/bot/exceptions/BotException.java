package backend.academy.bot.exceptions;

public abstract class BotException extends RuntimeException {
    public BotException(String message) {
        super(message);
    }
}
