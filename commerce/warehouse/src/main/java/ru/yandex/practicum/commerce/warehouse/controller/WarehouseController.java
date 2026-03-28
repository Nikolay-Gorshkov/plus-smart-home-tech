package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.api.client.WarehouseApi;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.api.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.request.ShippedToDeliveryRequest;

import java.util.Map;
import java.util.UUID;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@RestController
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @Override
    public void newProductInWarehouse(@Valid NewProductInWarehouseRequest request) {
        warehouseService.newProductInWarehouse(request);
    }

    @Override
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);
    }

    @Override
    public void addProductToWarehouse(@Valid AddProductToWarehouseRequest request) {
        warehouseService.addProductToWarehouse(request);
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(@Valid AssemblyProductsForOrderRequest request) {
        return warehouseService.assemblyProductsForOrder(request);
    }

    @Override
    public void shippedToDelivery(@Valid ShippedToDeliveryRequest request) {
        warehouseService.shippedToDelivery(request);
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        warehouseService.acceptReturn(products);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }
}
