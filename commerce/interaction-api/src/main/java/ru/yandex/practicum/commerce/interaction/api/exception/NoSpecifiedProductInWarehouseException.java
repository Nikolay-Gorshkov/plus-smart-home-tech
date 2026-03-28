package ru.yandex.practicum.commerce.interaction.api.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public class NoSpecifiedProductInWarehouseException extends ServiceApiException {

    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super(HttpStatus.BAD_REQUEST,
                "Product with id " + productId + " is not registered in warehouse",
                "Нет информации о товаре на складе");
    }

    public NoSpecifiedProductInWarehouseException(String message) {
        super(HttpStatus.BAD_REQUEST, message, "Нет информации о товаре на складе");
    }
}
