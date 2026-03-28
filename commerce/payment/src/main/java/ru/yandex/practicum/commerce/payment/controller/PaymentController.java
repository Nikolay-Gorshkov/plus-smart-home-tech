package ru.yandex.practicum.commerce.payment.controller;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.api.client.PaymentApi;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

@RestController
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public PaymentDto payment(@Valid OrderDto orderDto) {
        return paymentService.payment(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(@Valid OrderDto orderDto) {
        return paymentService.getTotalCost(orderDto);
    }

    @Override
    public void paymentSuccess(UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    public BigDecimal productCost(@Valid OrderDto orderDto) {
        return paymentService.productCost(orderDto);
    }

    @Override
    public void paymentFailed(UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }

    @PostMapping(value = "/api/v1/payment/success", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void paymentSuccessAlias(@RequestBody UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }
}
