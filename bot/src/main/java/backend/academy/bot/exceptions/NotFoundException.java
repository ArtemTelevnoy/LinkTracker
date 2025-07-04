package backend.academy.bot.exceptions;

import backend.academy.dto.api.ApiErrorResponse;

public class NotFoundException extends BotException {
    public NotFoundException(ApiErrorResponse response) {
        super(response.toString());
    }
}
