package ru.yandex.practicum.commerce.interaction.api.client;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.request.PlanDeliveryRequest;

@Validated
public interface DeliveryApi {

    @PutMapping("/api/v1/delivery")
    DeliveryDto planDelivery(@Valid @RequestBody PlanDeliveryRequest request);

    @PostMapping("/api/v1/delivery/cost")
    BigDecimal deliveryCost(@Valid @RequestBody PlanDeliveryRequest request);

    @PostMapping("/api/v1/delivery/picked")
    DeliveryDto pickupDelivery(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/successful")
    DeliveryDto successfulDelivery(@RequestBody UUID deliveryId);

    @PostMapping("/api/v1/delivery/failed")
    DeliveryDto failedDelivery(@RequestBody UUID deliveryId);
}
