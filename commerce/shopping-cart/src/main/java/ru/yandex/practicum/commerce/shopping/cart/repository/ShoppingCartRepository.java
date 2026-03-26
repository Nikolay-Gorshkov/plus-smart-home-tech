package ru.yandex.practicum.commerce.shopping.cart.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.shopping.cart.entity.ShoppingCartStatus;
import ru.yandex.practicum.commerce.shopping.cart.entity.ShoppingCartEntity;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCartEntity, UUID> {

    Optional<ShoppingCartEntity> findByUsernameAndStatus(String username, ShoppingCartStatus status);
}
