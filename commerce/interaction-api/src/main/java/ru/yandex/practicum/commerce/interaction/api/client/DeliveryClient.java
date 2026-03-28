package ru.yandex.practicum.commerce.interaction.api.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "delivery")
public interface DeliveryClient extends DeliveryApi {
}
