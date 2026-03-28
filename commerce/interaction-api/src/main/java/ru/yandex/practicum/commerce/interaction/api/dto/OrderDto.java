package ru.yandex.practicum.commerce.interaction.api.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record OrderDto(
        @NotNull UUID orderId,
        UUID shoppingCartId,
        @NotNull Map<UUID, Long> products,
        UUID paymentId,
        UUID deliveryId,
        OrderState state,
        Double deliveryWeight,
        Double deliveryVolume,
        Boolean fragile,
        BigDecimal totalPrice,
        BigDecimal deliveryPrice,
        BigDecimal productPrice
) {
}
