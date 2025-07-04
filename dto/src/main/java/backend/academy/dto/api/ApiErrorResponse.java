package backend.academy.dto.api;

public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, String[] stacktrace) {}
