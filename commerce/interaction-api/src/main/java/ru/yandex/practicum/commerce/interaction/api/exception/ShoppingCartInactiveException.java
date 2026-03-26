package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class ShoppingCartInactiveException extends ServiceApiException {

    public ShoppingCartInactiveException() {
        super(HttpStatus.CONFLICT,
                "Shopping cart is deactivated",
                "Корзина деактивирована");
    }
}
