package ru.yandex.practicum.commerce.order.mapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderState;
import ru.yandex.practicum.commerce.interaction.api.request.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.request.PlanDeliveryRequest;
import ru.yandex.practicum.commerce.order.entity.OrderAddressEmbeddable;
import ru.yandex.practicum.commerce.order.entity.OrderEntity;

@Component
public class OrderMapper {

    public OrderEntity toEntity(CreateNewOrderRequest request, BookedProductsDto bookedProductsDto) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderId(UUID.randomUUID());
        entity.setUsername(request.shoppingCart().username());
        entity.setShoppingCartId(request.shoppingCart().shoppingCartId());
        entity.setProducts(new LinkedHashMap<>(request.shoppingCart().products()));
        entity.setState(OrderState.NEW);
        entity.setDeliveryWeight(bookedProductsDto.deliveryWeight());
        entity.setDeliveryVolume(bookedProductsDto.deliveryVolume());
        entity.setFragile(bookedProductsDto.fragile());
        entity.setDeliveryAddress(OrderAddressEmbeddable.fromDto(request.deliveryAddress()));
        entity.setCreatedAt(Instant.now());
        return entity;
    }

    public OrderDto toDto(OrderEntity entity) {
        return new OrderDto(
                entity.getOrderId(),
                entity.getShoppingCartId(),
                new LinkedHashMap<>(entity.getProducts()),
                entity.getPaymentId(),
                entity.getDeliveryId(),
                entity.getState(),
                entity.getDeliveryWeight(),
                entity.getDeliveryVolume(),
                entity.getFragile(),
                entity.getTotalPrice(),
                entity.getDeliveryPrice(),
                entity.getProductPrice()
        );
    }

    public List<OrderDto> toDtoList(List<OrderEntity> entities) {
        return entities.stream().map(this::toDto).toList();
    }

    public PlanDeliveryRequest toPlanDeliveryRequest(OrderEntity entity, AddressDto fromAddress) {
        return new PlanDeliveryRequest(
                entity.getOrderId(),
                entity.getDeliveryWeight(),
                entity.getDeliveryVolume(),
                Boolean.TRUE.equals(entity.getFragile()),
                fromAddress,
                entity.getDeliveryAddress().toDto()
        );
    }
}
