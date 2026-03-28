package ru.yandex.practicum.commerce.interaction.api.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ShippedToDeliveryRequest(
        @NotNull UUID orderId,
        @NotNull UUID deliveryId
) {
}
