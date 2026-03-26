package ru.yandex.practicum.commerce.interaction.api.client;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction.api.dto.PageProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductCategory;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.QuantityState;
import ru.yandex.practicum.commerce.interaction.api.request.SetProductQuantityStateRequest;

@Validated
public interface ShoppingStoreApi {

    @GetMapping("/api/v1/shopping-store")
    PageProductDto getProducts(@RequestParam ProductCategory category,
                               @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer size,
                               @RequestParam(required = false) List<String> sort);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean setProductQuantityState(@RequestParam(required = false) UUID productId,
                                    @RequestParam(required = false) QuantityState quantityState,
                                    @RequestBody(required = false) SetProductQuantityStateRequest request);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);
}
