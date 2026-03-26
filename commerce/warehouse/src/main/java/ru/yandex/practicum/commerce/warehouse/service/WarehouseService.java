package ru.yandex.practicum.commerce.warehouse.service;

import java.security.SecureRandom;
import java.util.ArrayList;
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
import ru.yandex.practicum.commerce.interaction.api.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.interaction.api.request.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.request.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.request.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.warehouse.entity.WarehouseProductEntity;
import ru.yandex.practicum.commerce.warehouse.mapper.WarehouseMapper;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

@Service
@Transactional
public class WarehouseService {

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseProductRepository warehouseProductRepository;
    private final WarehouseMapper warehouseMapper;
    private final ShoppingStoreClient shoppingStoreClient;

    public WarehouseService(WarehouseProductRepository warehouseProductRepository,
                            WarehouseMapper warehouseMapper,
                            ShoppingStoreClient shoppingStoreClient) {
        this.warehouseProductRepository = warehouseProductRepository;
        this.warehouseMapper = warehouseMapper;
        this.shoppingStoreClient = shoppingStoreClient;
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
        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean fragile = false;
        List<String> missingProducts = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : shoppingCartDto.products().entrySet()) {
            WarehouseProductEntity product = warehouseProductRepository.findById(entry.getKey())
                    .orElse(null);
            if (product == null || product.getQuantity() < entry.getValue()) {
                long available = product == null ? 0L : product.getQuantity();
                missingProducts.add(entry.getKey() + " requested=" + entry.getValue() + " available=" + available);
                continue;
            }

            long quantity = entry.getValue();
            totalWeight += quantity * product.getWeight();
            totalVolume += quantity * product.getWidth() * product.getHeight() * product.getDepth();
            fragile = fragile || product.isFragile();
        }

        if (!missingProducts.isEmpty()) {
            throw new ProductInShoppingCartLowQuantityInWarehouse(
                    "Insufficient quantity in warehouse: " + String.join("; ", missingProducts)
            );
        }

        return new BookedProductsDto(totalWeight, totalVolume, fragile);
    }

    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProductEntity entity = warehouseProductRepository.findById(request.productId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.productId()));
        entity.setQuantity(entity.getQuantity() + request.quantity());
        warehouseProductRepository.save(entity);
        syncQuantityState(entity.getProductId(), entity.getQuantity());
    }

    @Transactional(readOnly = true)
    public AddressDto getWarehouseAddress() {
        return warehouseMapper.toAddressDto(CURRENT_ADDRESS);
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
