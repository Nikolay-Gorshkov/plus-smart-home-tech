package ru.yandex.practicum.commerce.interaction.api.dto;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ShoppingCartDto(
        @NotNull UUID shoppingCartId,
        @NotNull Map<UUID, Long> products,
        String username
) {

    public ShoppingCartDto(UUID shoppingCartId, Map<UUID, Long> products) {
        this(shoppingCartId, products, null);
    }
}
