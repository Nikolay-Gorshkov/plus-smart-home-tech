package ru.yandex.practicum.commerce.shopping.store.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.api.client.ShoppingStoreApi;
import ru.yandex.practicum.commerce.interaction.api.dto.PageProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductCategory;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.QuantityState;
import ru.yandex.practicum.commerce.interaction.api.exception.BadRequestException;
import ru.yandex.practicum.commerce.interaction.api.request.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shopping.store.service.ProductService;

@RestController
public class ShoppingStoreController implements ShoppingStoreApi {

    private final ProductService productService;

    public ShoppingStoreController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public PageProductDto getProducts(ProductCategory category, Integer page, Integer size, List<String> sort) {
        return productService.getProducts(category, page, size, sort);
    }

    @Override
    public ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.create(productDto);
    }

    @Override
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        return productService.update(productDto);
    }

    @Override
    public Boolean removeProductFromStore(@RequestBody UUID productId) {
        return productService.remove(productId);
    }

    @Override
    public Boolean setProductQuantityState(UUID productId, QuantityState quantityState, SetProductQuantityStateRequest request) {
        SetProductQuantityStateRequest payload = request;
        if (payload == null && productId != null && quantityState != null) {
            payload = new SetProductQuantityStateRequest(productId, quantityState);
        }
        if (payload == null) {
            throw new BadRequestException("Product quantity state payload is missing");
        }
        return productService.setQuantityState(payload.productId(), payload.quantityState());
    }

    @org.springframework.web.bind.annotation.PostMapping(
            value = "/api/v1/shopping-store/quantityState",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Boolean setProductQuantityStateFromBody(@Valid @RequestBody SetProductQuantityStateRequest request) {
        return productService.setQuantityState(request.productId(), request.quantityState());
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return productService.getProduct(productId);
    }
}
