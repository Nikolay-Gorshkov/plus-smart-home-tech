package ru.yandex.practicum.commerce.interaction.api.dto;

public record SortObjectDto(
        String direction,
        String nullHandling,
        boolean ascending,
        String property,
        boolean ignoreCase
) {
}
