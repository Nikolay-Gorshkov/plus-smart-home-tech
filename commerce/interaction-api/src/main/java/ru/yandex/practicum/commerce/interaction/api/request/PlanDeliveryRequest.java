package ru.yandex.practicum.commerce.interaction.api.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;

public record PlanDeliveryRequest(
        @NotNull UUID orderId,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) Double deliveryWeight,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) Double deliveryVolume,
        @NotNull Boolean fragile,
        @NotNull @Valid AddressDto fromAddress,
        @NotNull @Valid AddressDto toAddress
) {
}
