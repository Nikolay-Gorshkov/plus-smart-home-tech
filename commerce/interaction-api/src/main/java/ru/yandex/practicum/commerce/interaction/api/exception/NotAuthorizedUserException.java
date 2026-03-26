package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class NotAuthorizedUserException extends ServiceApiException {

    public NotAuthorizedUserException() {
        super(HttpStatus.UNAUTHORIZED,
                "Username must not be blank",
                "Имя пользователя не должно быть пустым");
    }
}
