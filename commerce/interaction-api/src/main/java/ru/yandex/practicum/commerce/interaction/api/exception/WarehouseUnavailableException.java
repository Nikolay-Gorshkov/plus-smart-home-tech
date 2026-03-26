package ru.yandex.practicum.commerce.interaction.api.exception;

import org.springframework.http.HttpStatus;

public class WarehouseUnavailableException extends ServiceApiException {

    public WarehouseUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE,
                "Warehouse service is temporarily unavailable",
                "Сервис склада временно недоступен");
    }
}
