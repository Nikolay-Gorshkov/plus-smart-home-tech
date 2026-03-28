package ru.yandex.practicum.commerce.interaction.api.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;

public record CreateNewOrderRequest(
        @NotNull @Valid ShoppingCartDto shoppingCart,
        @NotNull @Valid AddressDto deliveryAddress
) {
}
