package ru.yandex.practicum.commerce.delivery.controller;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interaction.api.client.DeliveryApi;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.request.PlanDeliveryRequest;

@RestController
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Override
    public DeliveryDto planDelivery(@Valid PlanDeliveryRequest request) {
        return deliveryService.planDelivery(request);
    }

    @Override
    public BigDecimal deliveryCost(@Valid PlanDeliveryRequest request) {
        return deliveryService.deliveryCost(request);
    }

    @Override
    public DeliveryDto pickupDelivery(UUID deliveryId) {
        return deliveryService.pickupDelivery(deliveryId);
    }

    @Override
    public DeliveryDto successfulDelivery(UUID deliveryId) {
        return deliveryService.successfulDelivery(deliveryId);
    }

    @Override
    public DeliveryDto failedDelivery(UUID deliveryId) {
        return deliveryService.failedDelivery(deliveryId);
    }

    @PostMapping(value = "/api/v1/delivery/pickup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DeliveryDto pickupDeliveryAlias(@RequestBody UUID deliveryId) {
        return deliveryService.pickupDelivery(deliveryId);
    }
}
