package ru.yandex.practicum.commerce.warehouse.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.warehouse.entity.WarehouseProductEntity;

@Component
public class WarehouseMapper {

    public WarehouseProductEntity toEntity(NewProductInWarehouseRequest request) {
        WarehouseProductEntity entity = new WarehouseProductEntity();
        entity.setProductId(request.productId());
        entity.setFragile(Boolean.TRUE.equals(request.fragile()));
        entity.setWidth(request.dimension().width());
        entity.setHeight(request.dimension().height());
        entity.setDepth(request.dimension().depth());
        entity.setWeight(request.weight());
        entity.setQuantity(0L);
        return entity;
    }

    public AddressDto toAddressDto(String address) {
        return new AddressDto(address, address, address, address, address);
    }
}
