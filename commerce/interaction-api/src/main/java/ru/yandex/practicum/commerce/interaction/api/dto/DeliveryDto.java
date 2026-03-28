package ru.yandex.practicum.commerce.interaction.api.dto;

import java.util.UUID;

public record DeliveryDto(
        UUID deliveryId,
        UUID orderId,
        AddressDto fromAddress,
        AddressDto toAddress,
        Double deliveryWeight,
        Double deliveryVolume,
        Boolean fragile,
        DeliveryState state
) {
}
