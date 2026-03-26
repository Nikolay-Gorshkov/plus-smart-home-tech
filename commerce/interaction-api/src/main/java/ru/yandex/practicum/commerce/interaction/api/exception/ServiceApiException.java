package ru.yandex.practicum.commerce.interaction.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;

public abstract class ServiceApiException extends RuntimeException {

    private final HttpStatus status;
    private final String userMessage;

    protected ServiceApiException(HttpStatus status, String message, String userMessage) {
        super(message);
        this.status = status;
        this.userMessage = userMessage;
    }

    @JsonIgnore
    public HttpStatus getStatus() {
        return status;
    }

    @JsonProperty("httpStatus")
    public String getHttpStatus() {
        return status.toString();
    }

    public String getUserMessage() {
        return userMessage;
    }
}
