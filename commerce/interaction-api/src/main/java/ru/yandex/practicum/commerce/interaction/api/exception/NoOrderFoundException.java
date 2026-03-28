package ru.yandex.practicum.commerce.interaction.api.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public class NoOrderFoundException extends ServiceApiException {

    public NoOrderFoundException(UUID orderId) {
        this(HttpStatus.BAD_REQUEST, "Order with id " + orderId + " was not found", "Не найден заказ");
    }

    public NoOrderFoundException(HttpStatus status, String message, String userMessage) {
        super(status, message, userMessage);
    }
}
