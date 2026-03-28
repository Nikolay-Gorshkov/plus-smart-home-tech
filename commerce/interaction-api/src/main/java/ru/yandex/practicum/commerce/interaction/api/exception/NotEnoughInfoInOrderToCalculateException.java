package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class NotEnoughInfoInOrderToCalculateException extends ServiceApiException {

    public NotEnoughInfoInOrderToCalculateException(String message) {
        super(HttpStatus.BAD_REQUEST, message, "Недостаточно информации в заказе для расчёта");
    }
}
