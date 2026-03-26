package ru.yandex.practicum.commerce.shopping.cart.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import feign.FeignException;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.api.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.interaction.api.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.interaction.api.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.interaction.api.exception.WarehouseUnavailableException;
import ru.yandex.practicum.commerce.interaction.api.request.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.shopping.cart.entity.ShoppingCartEntity;
import ru.yandex.practicum.commerce.shopping.cart.entity.ShoppingCartStatus;
import ru.yandex.practicum.commerce.shopping.cart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.shopping.cart.repository.ShoppingCartRepository;

@Service
@Transactional
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository,
                               ShoppingCartMapper shoppingCartMapper,
                               WarehouseClient warehouseClient,
                               CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.shoppingCartMapper = shoppingCartMapper;
        this.warehouseClient = warehouseClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {
        validateUsername(username);
        return shoppingCartMapper.toDto(getOrCreateActiveCart(username));
    }

    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        validateUsername(username);
        ShoppingCartEntity cart = getOrCreateActiveCart(username);

        Map<UUID, Long> updatedProducts = new LinkedHashMap<>(cart.getProducts());
        if (products == null || products.isEmpty()) {
            return shoppingCartMapper.toDto(cart);
        }
        products.forEach((productId, quantity) -> updatedProducts.merge(productId, quantity, Long::sum));
        validateWithWarehouse(cart.getShoppingCartId(), updatedProducts);

        cart.setProducts(updatedProducts);
        return shoppingCartMapper.toDto(shoppingCartRepository.save(cart));
    }

    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        validateUsername(username);
        ShoppingCartEntity cart = getOrCreateActiveCart(username);

        if (!cart.getProducts().containsKey(request.productId())) {
            throw new NoProductsInShoppingCartException("Product " + request.productId() + " is not present in shopping cart");
        }

        Map<UUID, Long> updatedProducts = new LinkedHashMap<>(cart.getProducts());
        updatedProducts.put(request.productId(), request.newQuantity());
        validateWithWarehouse(cart.getShoppingCartId(), updatedProducts);

        cart.setProducts(updatedProducts);
        return shoppingCartMapper.toDto(shoppingCartRepository.save(cart));
    }

    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        validateUsername(username);
        ShoppingCartEntity cart = getOrCreateActiveCart(username);

        Map<UUID, Long> updatedProducts = new LinkedHashMap<>(cart.getProducts());
        boolean removed = false;
        for (UUID productId : productIds) {
            removed = updatedProducts.remove(productId) != null || removed;
        }

        if (!removed) {
            throw new NoProductsInShoppingCartException("None of requested products were found in shopping cart");
        }

        cart.setProducts(updatedProducts);
        return shoppingCartMapper.toDto(shoppingCartRepository.save(cart));
    }

    public void deactivate(String username) {
        validateUsername(username);
        ShoppingCartEntity cart = getOrCreateActiveCart(username);
        cart.setStatus(ShoppingCartStatus.DEACTIVATED);
        shoppingCartRepository.save(cart);
    }

    private ShoppingCartEntity getOrCreateActiveCart(String username) {
        return shoppingCartRepository.findByUsernameAndStatus(username, ShoppingCartStatus.ACTIVE)
                .orElseGet(() -> createCart(username));
    }

    private ShoppingCartEntity createCart(String username) {
        ShoppingCartEntity entity = new ShoppingCartEntity();
        entity.setShoppingCartId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setStatus(ShoppingCartStatus.ACTIVE);
        entity.setProducts(new LinkedHashMap<>());
        return shoppingCartRepository.save(entity);
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
    }
    private void validateWithWarehouse(UUID shoppingCartId, Map<UUID, Long> products) {
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto(shoppingCartId, products);
        circuitBreakerFactory.create("warehouse")
                .run(() -> warehouseClient.checkProductQuantityEnoughForShoppingCart(shoppingCartDto),
                        throwable -> {
                            throw mapWarehouseThrowable(throwable);
                        });
    }

    private RuntimeException mapWarehouseThrowable(Throwable throwable) {
        if (throwable instanceof FeignException feignException && feignException.status() == 400) {
            throw new ProductInShoppingCartLowQuantityInWarehouse("Товаров на складе недостаточно");
        }
        throw new WarehouseUnavailableException();
    }
}
