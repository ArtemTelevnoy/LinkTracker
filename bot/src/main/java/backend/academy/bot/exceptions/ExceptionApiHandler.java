package backend.academy.bot.exceptions;

import backend.academy.dto.api.ApiErrorResponse;
import backend.academy.dto.api.SupportedErrorCodeException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ExceptionApiHandler {

    @ExceptionHandler({
        MethodArgumentTypeMismatchException.class,
        HttpMediaTypeNotSupportedException.class,
        HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse badArgumentsException(@NotNull Exception e) {
        log.warn(
                "Exception {} was thrown. Some request params was invalid",
                e.getClass().getName());

        return new ApiErrorResponse(
                "Invalid request's params, header or body", "400", e.getClass().getName(), e.getMessage(), null);
    }

    @ExceptionHandler(SupportedErrorCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse errorCodeException(@NotNull SupportedErrorCodeException e) {
        log.warn("Exception ErrorCodeException was thrown");
        return new ApiErrorResponse(
                "error code when requesting", e.errorCode(), e.getClass().getName(), e.getMessage(), null);
    }
}
