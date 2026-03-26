package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ServiceApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message, message);
    }
}
