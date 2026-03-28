package ru.yandex.practicum.commerce.payment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.api.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentState;
import ru.yandex.practicum.commerce.interaction.api.exception.BadRequestException;
import ru.yandex.practicum.commerce.interaction.api.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.interaction.api.exception.NoPaymentFoundException;
import ru.yandex.practicum.commerce.interaction.api.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.payment.config.PaymentProperties;
import ru.yandex.practicum.commerce.payment.entity.PaymentEntity;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;
    private final PaymentProperties paymentProperties;

    public PaymentService(PaymentRepository paymentRepository,
                          ShoppingStoreClient shoppingStoreClient,
                          OrderClient orderClient,
                          PaymentProperties paymentProperties) {
        this.paymentRepository = paymentRepository;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
        this.paymentProperties = paymentProperties;
    }

    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto orderDto) {
        if (orderDto == null || orderDto.products() == null || orderDto.products().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("В заказе отсутствуют товары");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : orderDto.products().entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new NotEnoughInfoInOrderToCalculateException("Количество товара должно быть положительным");
            }
            try {
                BigDecimal productPrice = shoppingStoreClient.getProduct(entry.getKey()).price();
                total = total.add(productPrice.multiply(BigDecimal.valueOf(entry.getValue())));
            } catch (FeignException exception) {
                throw new NotEnoughInfoInOrderToCalculateException("Невозможно получить данные о товаре " + entry.getKey());
            }
        }
        return normalize(total);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto orderDto) {
        BigDecimal productTotal = orderDto.productPrice() == null ? productCost(orderDto) : normalize(orderDto.productPrice());
        BigDecimal deliveryTotal = normalizeRequired(orderDto.deliveryPrice(), "Не указана стоимость доставки");
        BigDecimal feeTotal = calculateFee(productTotal);
        return normalize(productTotal.add(feeTotal).add(deliveryTotal));
    }

    public PaymentDto payment(OrderDto orderDto) {
        if (orderDto == null || orderDto.orderId() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Отсутствует идентификатор заказа");
        }

        BigDecimal productTotal = orderDto.productPrice() == null ? productCost(orderDto) : normalize(orderDto.productPrice());
        BigDecimal deliveryTotal = normalizeRequired(orderDto.deliveryPrice(), "Не указана стоимость доставки");
        BigDecimal feeTotal = calculateFee(productTotal);
        BigDecimal totalPayment = normalize(productTotal.add(feeTotal).add(deliveryTotal));

        PaymentEntity entity = new PaymentEntity();
        entity.setPaymentId(UUID.randomUUID());
        entity.setOrderId(orderDto.orderId());
        entity.setProductTotal(productTotal);
        entity.setDeliveryTotal(deliveryTotal);
        entity.setFeeTotal(feeTotal);
        entity.setTotalPayment(totalPayment);
        entity.setState(PaymentState.PENDING);
        entity.setCreatedAt(Instant.now());
        paymentRepository.save(entity);

        return new PaymentDto(entity.getPaymentId(), totalPayment, deliveryTotal, feeTotal);
    }

    public void paymentSuccess(UUID paymentId) {
        PaymentEntity entity = getPaymentOrThrow(paymentId);
        entity.setState(PaymentState.SUCCESS);
        paymentRepository.save(entity);
        try {
            orderClient.payment(entity.getOrderId());
        } catch (FeignException exception) {
            if (exception.status() == 400 || exception.status() == 404) {
                throw new NoOrderFoundException(HttpStatus.NOT_FOUND,
                        "Order with id " + entity.getOrderId() + " was not found",
                        "Заказ не найден");
            }
            throw new BadRequestException("Order service is unavailable");
        }
    }

    public void paymentFailed(UUID paymentId) {
        PaymentEntity entity = getPaymentOrThrow(paymentId);
        entity.setState(PaymentState.FAILED);
        paymentRepository.save(entity);
        try {
            orderClient.paymentFailed(entity.getOrderId());
        } catch (FeignException exception) {
            if (exception.status() == 400 || exception.status() == 404) {
                throw new NoOrderFoundException(HttpStatus.NOT_FOUND,
                        "Order with id " + entity.getOrderId() + " was not found",
                        "Заказ не найден");
            }
            throw new BadRequestException("Order service is unavailable");
        }
    }

    private PaymentEntity getPaymentOrThrow(UUID paymentId) {
        if (paymentId == null) {
            throw new NoPaymentFoundException(null);
        }
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException(HttpStatus.NOT_FOUND,
                        "Payment with id " + paymentId + " was not found",
                        "Заказ не найден"));
    }

    private BigDecimal calculateFee(BigDecimal productTotal) {
        return normalize(productTotal.multiply(paymentProperties.getTaxRate()));
    }

    private BigDecimal normalizeRequired(BigDecimal value, String message) {
        if (value == null) {
            throw new NotEnoughInfoInOrderToCalculateException(message);
        }
        return normalize(value);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
