package ru.yandex.practicum.commerce.interaction.api.dto;

import java.util.List;

public record PageableObjectDto(
        long offset,
        List<SortObjectDto> sort,
        boolean unpaged,
        boolean paged,
        int pageNumber,
        int pageSize
) {
}
