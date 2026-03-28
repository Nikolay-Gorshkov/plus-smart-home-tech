package ru.yandex.practicum.commerce.interaction.api.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public class NoDeliveryFoundException extends ServiceApiException {

    public NoDeliveryFoundException(UUID deliveryId) {
        super(HttpStatus.NOT_FOUND,
                "Delivery with id " + deliveryId + " was not found",
                "Не найдена доставка");
    }
}
