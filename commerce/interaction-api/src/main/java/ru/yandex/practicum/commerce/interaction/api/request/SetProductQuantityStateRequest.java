package ru.yandex.practicum.commerce.interaction.api.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.interaction.api.dto.QuantityState;

public record SetProductQuantityStateRequest(
        @NotNull UUID productId,
        @NotNull QuantityState quantityState
) {
}
