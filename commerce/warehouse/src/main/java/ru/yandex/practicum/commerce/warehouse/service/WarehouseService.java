package ru.yandex.practicum.commerce.warehouse.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.QuantityState;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.api.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.interaction.api.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.interaction.api.exception.ProductInShoppingCartNotInWarehouse;
import ru.yandex.practicum.commerce.interaction.api.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.interaction.api.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.request.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.request.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.interaction.api.request.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.warehouse.config.WarehouseAddressProperties;
import ru.yandex.practicum.commerce.warehouse.entity.OrderBookingEntity;
import ru.yandex.practicum.commerce.warehouse.entity.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseProductRepository warehouseProductRepository;
    private final OrderBookingRepository orderBookingRepository;
    private final WarehouseMapper warehouseMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final WarehouseAddressProperties warehouseAddressProperties;

    public WarehouseService(WarehouseProductRepository warehouseProductRepository,
                            OrderBookingRepository orderBookingRepository,
                            WarehouseMapper warehouseMapper,
                            ShoppingStoreClient shoppingStoreClient,
                            WarehouseAddressProperties warehouseAddressProperties) {
        this.warehouseProductRepository = warehouseProductRepository;
        this.orderBookingRepository = orderBookingRepository;
        this.warehouseMapper = warehouseMapper;
        this.shoppingStoreClient = shoppingStoreClient;
        this.warehouseAddressProperties = warehouseAddressProperties;
    }

    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        if (warehouseProductRepository.existsById(request.productId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(request.productId());
        }
        warehouseProductRepository.save(warehouseMapper.toEntity(request));
        syncQuantityState(request.productId(), 0L);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto shoppingCartDto) {
        return inspectProducts(shoppingCartDto.products(), false, false);
    }

    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProductEntity entity = warehouseProductRepository.findById(request.productId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.productId()));
        entity.setQuantity(entity.getQuantity() + request.quantity());
        warehouseProductRepository.save(entity);
        syncQuantityState(entity.getProductId(), entity.getQuantity());
    }

    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        BookedProductsDto bookedProductsDto = inspectProducts(request.products(), true, true);
        OrderBookingEntity orderBookingEntity = orderBookingRepository.findById(request.orderId())
                .orElseGet(OrderBookingEntity::new);
        orderBookingEntity.setOrderId(request.orderId());
        orderBookingEntity.setProducts(new LinkedHashMap<>(request.products()));
        orderBookingEntity.setDeliveryId(orderBookingEntity.getDeliveryId());
        orderBookingRepository.save(orderBookingEntity);
        return bookedProductsDto;
    }

    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBookingEntity orderBookingEntity = orderBookingRepository.findById(request.orderId())
                .orElseGet(OrderBookingEntity::new);
        orderBookingEntity.setOrderId(request.orderId());
        orderBookingEntity.setDeliveryId(request.deliveryId());
        if (orderBookingEntity.getProducts() == null) {
            orderBookingEntity.setProducts(new LinkedHashMap<>());
        }
        orderBookingRepository.save(orderBookingEntity);
    }

    public void acceptReturn(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            WarehouseProductEntity entity = warehouseProductRepository.findById(entry.getKey()).orElse(null);
            if (entity == null) {
                continue;
            }
            entity.setQuantity(entity.getQuantity() + entry.getValue());
            warehouseProductRepository.save(entity);
            syncQuantityState(entity.getProductId(), entity.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {
        return warehouseMapper.toAddressDto(warehouseAddressProperties);
    }

    private BookedProductsDto inspectProducts(Map<UUID, Long> products, boolean reduceStock, boolean strictAssemblyMode) {
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;
        List<String> missingProducts = new ArrayList<>();
        List<String> lowQuantityProducts = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            WarehouseProductEntity product = warehouseProductRepository.findById(entry.getKey()).orElse(null);
            if (product == null) {
                missingProducts.add(entry.getKey().toString());
                continue;
            }
            if (product.getQuantity() < entry.getValue()) {
                lowQuantityProducts.add(entry.getKey() + " requested=" + entry.getValue() + " available=" + product.getQuantity());
                continue;
            }

            long quantity = entry.getValue();
            totalWeight += quantity * product.getWeight();
            totalVolume += quantity * product.getWidth() * product.getHeight() * product.getDepth();
            fragile = fragile || product.isFragile();

            if (reduceStock) {
                product.setQuantity(product.getQuantity() - quantity);
                warehouseProductRepository.save(product);
                syncQuantityState(product.getProductId(), product.getQuantity());
            }
        }

        if (!missingProducts.isEmpty()) {
            if (strictAssemblyMode) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Products are unavailable in warehouse: " + String.join(", ", missingProducts)
                );
            }
            throw new ProductInShoppingCartNotInWarehouse(
                    "Products are absent in warehouse: " + String.join(", ", missingProducts)
            );
        }
        if (!lowQuantityProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "Insufficient quantity in warehouse: " + String.join("; ", lowQuantityProducts)
            );
        }

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }

    private void syncQuantityState(UUID productId, long quantity) {
        try {
            QuantityState quantityState = resolveQuantityState(quantity);
            shoppingStoreClient.setProductQuantityState(
                    productId,
                    quantityState,
                    new SetProductQuantityStateRequest(productId, quantityState)
            );
        } catch (Exception ignored) {
            // Warehouse may be used independently from the storefront, so this sync is best-effort.
        }
    }

    private QuantityState resolveQuantityState(long quantity) {
        if (quantity <= 0) {
            return QuantityState.ENDED;
        }
        if (quantity < 10) {
            return QuantityState.FEW;
        }
        if (quantity <= 100) {
            return QuantityState.ENOUGH;
        }
        return QuantityState.MANY;
    }
}
