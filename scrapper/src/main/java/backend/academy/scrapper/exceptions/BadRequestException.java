package backend.academy.scrapper.exceptions;

import backend.academy.dto.api.ApiErrorResponse;

public class BadRequestException extends RuntimeException {
    public BadRequestException(ApiErrorResponse response) {
        super(response.toString());
    }
}
