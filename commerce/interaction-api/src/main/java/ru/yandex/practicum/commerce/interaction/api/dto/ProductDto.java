package ru.yandex.practicum.commerce.interaction.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductDto(
        UUID productId,
        @NotBlank String productName,
        @NotBlank String description,
        String imageSrc,
        @NotNull QuantityState quantityState,
        @NotNull ProductState productState,
        @NotNull ProductCategory productCategory,
        @NotNull @DecimalMin(value = "1.0", inclusive = true) BigDecimal price
) {
}
