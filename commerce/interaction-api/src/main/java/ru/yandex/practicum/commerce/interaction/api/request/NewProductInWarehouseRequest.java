package ru.yandex.practicum.commerce.interaction.api.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.interaction.api.dto.DimensionDto;

public record NewProductInWarehouseRequest(
        @NotNull UUID productId,
        Boolean fragile,
        @NotNull @Valid DimensionDto dimension,
        @NotNull @DecimalMin(value = "1.0", inclusive = true) Double weight
) {
}
