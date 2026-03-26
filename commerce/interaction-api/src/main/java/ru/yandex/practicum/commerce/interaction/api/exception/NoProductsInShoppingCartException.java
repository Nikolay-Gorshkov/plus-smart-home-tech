package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class NoProductsInShoppingCartException extends ServiceApiException {

    public NoProductsInShoppingCartException(String message) {
        super(HttpStatus.BAD_REQUEST, message, "Нет искомых товаров в корзине");
    }
}
