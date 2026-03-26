package ru.yandex.practicum.commerce.interaction.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record DimensionDto(
        @NotNull @DecimalMin(value = "1.0", inclusive = true) Double width,
        @NotNull @DecimalMin(value = "1.0", inclusive = true) Double height,
        @NotNull @DecimalMin(value = "1.0", inclusive = true) Double depth
) {
}
