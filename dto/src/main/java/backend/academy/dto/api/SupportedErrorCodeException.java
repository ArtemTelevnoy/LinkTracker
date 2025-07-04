package backend.academy.dto.api;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatusCode;

public class SupportedErrorCodeException extends RuntimeException {
    private final String errorCode;

    public SupportedErrorCodeException(@NotNull HttpStatusCode errorCode) {
        super(String.format("Error code %s while requesting", errorCode));
        this.errorCode = errorCode.toString();
    }

    public String errorCode() {
        return errorCode;
    }
}
