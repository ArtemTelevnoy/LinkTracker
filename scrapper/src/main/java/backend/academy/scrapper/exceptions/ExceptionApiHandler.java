package backend.academy.scrapper.exceptions;

import backend.academy.dto.api.ApiErrorResponse;
import backend.academy.dto.api.SupportedErrorCodeException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RestControllerAdvice
public class ExceptionApiHandler {

    @ExceptionHandler(WebClientResponseException.BadRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse webClientResponseException(WebClientResponseException.BadRequest e) {
        log.warn("Exception WebClientResponseException.BadRequest was thrown. Some path variable was invalid");
        return response("Invalid path variable in response", "400", e);
    }

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        HttpMediaTypeNotSupportedException.class,
        HttpMessageNotReadableException.class,
        MethodArgumentNotValidException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse badArgumentsException(Exception e) {
        log.warn(
                "Exception {} was thrown. Some request params was invalid",
                e.getClass().getSimpleName());
        return response("Invalid request's params, header or body", "400", e);
    }

    @ExceptionHandler(NotExistApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse notExistApiException(NotExistApiException e) {
        log.warn("Exception NotExistApiException was thrown. link api isn't exist");
        return response("link api isn't exist", "400", e);
    }

    @ExceptionHandler(SupportedErrorCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse errorCodeException(SupportedErrorCodeException e) {
        log.warn("Exception ErrorCodeException was thrown");
        return response("error code when requesting", "5xx", e);
    }

    @ExceptionHandler(DuplicateLinkException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse nullPointerException(DuplicateLinkException e) {
        log.warn("Exception DuplicateLinkException was thrown. duplicate link in repository");
        return response("Duplicate link in repository", "400", e);
    }

    @ExceptionHandler(NoSuchLinkException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse noSuchLinkException(NoSuchLinkException e) {
        log.warn("Exception NoSuchLinkException was thrown. link isn't tracked");
        return response("Link isn't tracked", "404", e);
    }

    @ExceptionHandler(HttpClientErrorException.NotFound.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse httpClientErrorException(HttpClientErrorException.NotFound e) {
        log.warn("Exception HttpClientErrorException.NotFound was thrown. link isn't exist");
        return response("Link isn't exist", "400", e);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse constraintViolationException(ConstraintViolationException e) {
        log.warn("Exception ConstraintViolationException was thrown. invalid link format");
        return response("Invalid link format", "400", e);
    }

    private static ApiErrorResponse response(String description, String code, Exception e) {
        return new ApiErrorResponse(description, code, e.getClass().getSimpleName(), e.getMessage(), null);
    }
}
