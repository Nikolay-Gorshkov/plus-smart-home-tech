package ru.yandex.practicum.commerce.interaction.api.dto;

public record BookedProductsDto(
        double deliveryWeight,
        double deliveryVolume,
        boolean fragile
) {
}
