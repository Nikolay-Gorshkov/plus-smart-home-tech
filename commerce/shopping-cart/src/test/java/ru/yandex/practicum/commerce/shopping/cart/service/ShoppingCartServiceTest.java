package ru.yandex.practicum.commerce.shopping.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.interaction.api.request.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.shopping.cart.entity.ShoppingCartEntity;
import ru.yandex.practicum.commerce.shopping.cart.entity.ShoppingCartStatus;
import ru.yandex.practicum.commerce.shopping.cart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.shopping.cart.repository.ShoppingCartRepository;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private ru.yandex.practicum.commerce.interaction.api.client.WarehouseClient warehouseClient;

    @Mock
    private CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private ShoppingCartService shoppingCartService;

    @Captor
    private ArgumentCaptor<ShoppingCartEntity> cartCaptor;

    @Test
    void shouldCreateNewActiveCartWhenExistingCartWasDeactivated() {
        String username = "user";
        UUID productId = UUID.randomUUID();
        when(shoppingCartRepository.findByUsernameAndStatus(username, ShoppingCartStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(shoppingCartRepository.save(any(ShoppingCartEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(circuitBreakerFactory.create("warehouse")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            return null;
        });
        when(shoppingCartMapper.toDto(any(ShoppingCartEntity.class)))
                .thenAnswer(invocation -> {
                    ShoppingCartEntity entity = invocation.getArgument(0);
                    return new ShoppingCartDto(entity.getShoppingCartId(), entity.getProducts());
                });

        ShoppingCartDto result = shoppingCartService.addProducts(username, Map.of(productId, 2L));

        verify(shoppingCartRepository, times(2)).save(cartCaptor.capture());
        ShoppingCartEntity persistedCart = cartCaptor.getAllValues().getLast();
        assertEquals(ShoppingCartStatus.ACTIVE, persistedCart.getStatus());
        assertEquals(2L, result.products().get(productId));
    }

    @Test
    void shouldUpdateQuantityInExistingActiveCart() {
        String username = "user";
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ShoppingCartEntity activeCart = new ShoppingCartEntity();
        activeCart.setShoppingCartId(cartId);
        activeCart.setUsername(username);
        activeCart.setStatus(ShoppingCartStatus.ACTIVE);
        activeCart.setProducts(new LinkedHashMap<>(Map.of(productId, 1L)));

        when(shoppingCartRepository.findByUsernameAndStatus(username, ShoppingCartStatus.ACTIVE))
                .thenReturn(Optional.of(activeCart));
        when(shoppingCartRepository.save(any(ShoppingCartEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(circuitBreakerFactory.create("warehouse")).thenReturn(circuitBreaker);
        when(circuitBreaker.run(any(), any())).thenAnswer(invocation -> {
            ((java.util.function.Supplier<?>) invocation.getArgument(0)).get();
            return null;
        });
        when(shoppingCartMapper.toDto(any(ShoppingCartEntity.class)))
                .thenAnswer(invocation -> {
                    ShoppingCartEntity entity = invocation.getArgument(0);
                    return new ShoppingCartDto(entity.getShoppingCartId(), entity.getProducts());
                });

        ShoppingCartDto result = shoppingCartService.changeProductQuantity(
                username,
                new ChangeProductQuantityRequest(productId, 5L)
        );

        assertEquals(5L, result.products().get(productId));
        verify(shoppingCartRepository).save(eq(activeCart));
    }
}
