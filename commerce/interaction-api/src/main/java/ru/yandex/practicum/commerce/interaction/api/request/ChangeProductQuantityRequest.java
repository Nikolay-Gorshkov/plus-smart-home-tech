package ru.yandex.practicum.commerce.interaction.api.request;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChangeProductQuantityRequest(
        @NotNull UUID productId,
        @NotNull @Min(1) Long newQuantity
) {
}
