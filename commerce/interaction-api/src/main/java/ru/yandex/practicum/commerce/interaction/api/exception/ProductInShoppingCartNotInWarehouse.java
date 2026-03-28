package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class ProductInShoppingCartNotInWarehouse extends ServiceApiException {

    public ProductInShoppingCartNotInWarehouse(String message) {
        super(HttpStatus.BAD_REQUEST, message, "Товар из корзины отсутствует на складе");
    }
}
