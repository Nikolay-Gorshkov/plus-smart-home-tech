package ru.yandex.practicum.commerce.interaction.api.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public class SpecifiedProductAlreadyInWarehouseException extends ServiceApiException {

    public SpecifiedProductAlreadyInWarehouseException(UUID productId) {
        super(HttpStatus.BAD_REQUEST,
                "Product with id " + productId + " is already registered in warehouse",
                "Товар с таким описанием уже зарегистрирован на складе");
    }
}
