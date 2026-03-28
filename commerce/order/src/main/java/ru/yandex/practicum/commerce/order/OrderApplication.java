package ru.yandex.practicum.commerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.yandex.practicum.commerce.interaction.api.client.DeliveryClient;
import ru.yandex.practicum.commerce.interaction.api.client.PaymentClient;
import ru.yandex.practicum.commerce.interaction.api.client.WarehouseClient;

@SpringBootApplication
@EnableFeignClients(clients = {DeliveryClient.class, PaymentClient.class, WarehouseClient.class})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
