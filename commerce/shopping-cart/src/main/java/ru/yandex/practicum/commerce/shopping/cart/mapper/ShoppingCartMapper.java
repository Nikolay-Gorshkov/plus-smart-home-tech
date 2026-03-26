package ru.yandex.practicum.commerce.shopping.cart.mapper;

import java.util.LinkedHashMap;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.shopping.cart.entity.ShoppingCartEntity;

@Component
public class ShoppingCartMapper {

    public ShoppingCartDto toDto(ShoppingCartEntity entity) {
        return new ShoppingCartDto(entity.getShoppingCartId(), new LinkedHashMap<>(entity.getProducts()));
    }
}
