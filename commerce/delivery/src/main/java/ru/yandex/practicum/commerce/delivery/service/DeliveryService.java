package ru.yandex.practicum.commerce.delivery.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.delivery.config.DeliveryPricingProperties;
import ru.yandex.practicum.commerce.delivery.entity.DeliveryAddressEmbeddable;
import ru.yandex.practicum.commerce.delivery.entity.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.interaction.api.client.OrderClient;
import ru.yandex.practicum.commerce.interaction.api.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryState;
import ru.yandex.practicum.commerce.interaction.api.exception.BadRequestException;
import ru.yandex.practicum.commerce.interaction.api.exception.NoDeliveryFoundException;
import ru.yandex.practicum.commerce.interaction.api.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.interaction.api.request.PlanDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.api.request.ShippedToDeliveryRequest;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;
    private final DeliveryPricingProperties pricingProperties;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           OrderClient orderClient,
                           WarehouseClient warehouseClient,
                           DeliveryPricingProperties pricingProperties) {
        this.deliveryRepository = deliveryRepository;
        this.orderClient = orderClient;
        this.warehouseClient = warehouseClient;
        this.pricingProperties = pricingProperties;
    }

    public DeliveryDto planDelivery(PlanDeliveryRequest request) {
        validateRequest(request);
        DeliveryEntity entity = deliveryRepository.findByOrderId(request.orderId())
                .orElseGet(DeliveryEntity::new);

        if (entity.getDeliveryId() == null) {
            entity.setDeliveryId(UUID.randomUUID());
            entity.setCreatedAt(Instant.now());
        }

        entity.setOrderId(request.orderId());
        entity.setFromAddress(DeliveryAddressEmbeddable.fromDto(request.fromAddress()));
        entity.setToAddress(DeliveryAddressEmbeddable.fromDto(request.toAddress()));
        entity.setDeliveryWeight(request.deliveryWeight());
        entity.setDeliveryVolume(request.deliveryVolume());
        entity.setFragile(Boolean.TRUE.equals(request.fragile()));
        entity.setState(entity.getState() == null ? DeliveryState.CREATED : entity.getState());
        return toDto(deliveryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(PlanDeliveryRequest request) {
        validateRequest(request);

        BigDecimal current = pricingProperties.getBaseCost();
        BigDecimal multiplier = resolveWarehouseMultiplier(request.fromAddress());
        current = normalize(current.add(pricingProperties.getBaseCost().multiply(multiplier)));

        if (Boolean.TRUE.equals(request.fragile())) {
            current = normalize(current.add(current.multiply(pricingProperties.getFragileFactor())));
        }

        current = normalize(current.add(BigDecimal.valueOf(request.deliveryWeight()).multiply(pricingProperties.getWeightFactor())));
        current = normalize(current.add(BigDecimal.valueOf(request.deliveryVolume()).multiply(pricingProperties.getVolumeFactor())));

        if (!sameStreet(request.fromAddress(), request.toAddress())) {
            current = normalize(current.add(current.multiply(pricingProperties.getStreetFactor())));
        }

        return current.stripTrailingZeros();
    }

    public DeliveryDto pickupDelivery(UUID deliveryId) {
        DeliveryEntity entity = getDeliveryOrThrow(deliveryId);
        if (entity.getState() == DeliveryState.IN_PROGRESS || entity.getState() == DeliveryState.DELIVERED) {
            return toDto(entity);
        }
        entity.setState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(entity);
        try {
            orderClient.assembly(entity.getOrderId());
            warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(entity.getOrderId(), entity.getDeliveryId()));
        } catch (FeignException exception) {
            throw new BadRequestException("Dependent service is unavailable");
        }
        return toDto(entity);
    }

    public DeliveryDto successfulDelivery(UUID deliveryId) {
        DeliveryEntity entity = getDeliveryOrThrow(deliveryId);
        entity.setState(DeliveryState.DELIVERED);
        deliveryRepository.save(entity);
        try {
            orderClient.delivery(entity.getOrderId());
        } catch (FeignException exception) {
            throw new BadRequestException("Order service is unavailable");
        }
        return toDto(entity);
    }

    public DeliveryDto failedDelivery(UUID deliveryId) {
        DeliveryEntity entity = getDeliveryOrThrow(deliveryId);
        entity.setState(DeliveryState.FAILED);
        deliveryRepository.save(entity);
        try {
            orderClient.deliveryFailed(entity.getOrderId());
        } catch (FeignException exception) {
            throw new BadRequestException("Order service is unavailable");
        }
        return toDto(entity);
    }

    private void validateRequest(PlanDeliveryRequest request) {
        if (request == null || request.orderId() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Отсутствует идентификатор заказа");
        }
        if (request.fromAddress() == null || request.toAddress() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не заполнены адреса доставки");
        }
        if (request.deliveryWeight() == null || request.deliveryVolume() == null || request.fragile() == null) {
            throw new NotEnoughInfoInOrderToCalculateException("Не заполнены параметры доставки");
        }
    }

    private DeliveryEntity getDeliveryOrThrow(UUID deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new NoDeliveryFoundException(deliveryId));
    }

    private DeliveryDto toDto(DeliveryEntity entity) {
        return new DeliveryDto(
                entity.getDeliveryId(),
                entity.getOrderId(),
                entity.getFromAddress().toDto(),
                entity.getToAddress().toDto(),
                entity.getDeliveryWeight(),
                entity.getDeliveryVolume(),
                entity.getFragile(),
                entity.getState()
        );
    }

    private BigDecimal resolveWarehouseMultiplier(AddressDto fromAddress) {
        String normalizedAddress = normalizeAddress(fromAddress);
        if (normalizedAddress.contains("ADDRESS_2")) {
            return BigDecimal.valueOf(2);
        }
        return BigDecimal.ONE;
    }

    private boolean sameStreet(AddressDto fromAddress, AddressDto toAddress) {
        String fromStreet = fromAddress.street() == null ? "" : fromAddress.street().trim();
        String toStreet = toAddress.street() == null ? "" : toAddress.street().trim();
        return fromStreet.equalsIgnoreCase(toStreet);
    }

    private String normalizeAddress(AddressDto addressDto) {
        return String.join(" ",
                        safe(addressDto.country()),
                        safe(addressDto.city()),
                        safe(addressDto.street()),
                        safe(addressDto.house()),
                        safe(addressDto.flat()))
                .toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
