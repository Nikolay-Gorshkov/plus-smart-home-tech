package ru.yandex.practicum.commerce.interaction.api.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public class NoPaymentFoundException extends ServiceApiException {

    public NoPaymentFoundException(UUID paymentId) {
        super(HttpStatus.NOT_FOUND,
                "Payment with id " + paymentId + " was not found",
                "Не найдена оплата");
    }
}
