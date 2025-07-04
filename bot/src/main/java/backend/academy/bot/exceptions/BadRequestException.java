package backend.academy.bot.exceptions;

import backend.academy.dto.api.ApiErrorResponse;

public class BadRequestException extends BotException {
    public BadRequestException(ApiErrorResponse response) {
        super(response.toString());
    }
}
