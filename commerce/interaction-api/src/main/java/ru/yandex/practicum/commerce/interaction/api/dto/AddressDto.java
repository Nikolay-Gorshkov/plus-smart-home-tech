package ru.yandex.practicum.commerce.interaction.api.dto;

public record AddressDto(
        String country,
        String city,
        String street,
        String house,
        String flat
) {
}
