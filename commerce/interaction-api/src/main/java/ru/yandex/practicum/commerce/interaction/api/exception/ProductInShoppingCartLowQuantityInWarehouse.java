package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class ProductInShoppingCartLowQuantityInWarehouse extends ServiceApiException {

    public ProductInShoppingCartLowQuantityInWarehouse(String message) {
        super(HttpStatus.BAD_REQUEST, message, "Товаров на складе недостаточно");
    }
}
