package ru.yandex.practicum.commerce.interaction.api.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ServiceApiException {

    public ProductNotFoundException(UUID productId) {
        super(HttpStatus.NOT_FOUND,
                "Product with id " + productId + " was not found",
                "Товар не найден");
    }
}
