package ru.yandex.practicum.commerce.interaction.api.request;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssemblyProductsForOrderRequest(
        @NotNull Map<UUID, Long> products,
        @NotNull UUID orderId
) {
}
