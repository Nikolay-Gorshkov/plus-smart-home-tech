package ru.yandex.practicum.commerce.shopping.store.service;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.api.dto.PageProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductCategory;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductState;
import ru.yandex.practicum.commerce.interaction.api.dto.QuantityState;
import ru.yandex.practicum.commerce.interaction.api.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shopping.store.entity.ProductEntity;
import ru.yandex.practicum.commerce.shopping.store.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shopping.store.repository.ProductRepository;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public PageProductDto getProducts(ProductCategory category, Integer page, Integer size, List<String> sort) {
        PageRequest pageRequest = PageRequest.of(
                page == null || page < 0 ? 0 : page,
                size == null || size < 1 ? 20 : size,
                buildSort(sort)
        );
        return productMapper.toPageDto(productRepository.findAllByProductCategory(category, pageRequest));
    }

    public ProductDto create(ProductDto productDto) {
        ProductEntity entity = productMapper.toEntity(productDto);
        entity.setProductId(productDto.productId() == null ? UUID.randomUUID() : productDto.productId());
        return productMapper.toDto(productRepository.save(entity));
    }

    public ProductDto update(ProductDto productDto) {
        ProductEntity entity = getProductEntity(productDto.productId());
        productMapper.updateEntity(entity, productDto);
        return productMapper.toDto(productRepository.save(entity));
    }

    public boolean remove(UUID productId) {
        ProductEntity entity = getProductEntity(productId);
        entity.setProductState(ProductState.DEACTIVATE);
        productRepository.save(entity);
        return true;
    }

    public boolean setQuantityState(UUID productId, QuantityState quantityState) {
        ProductEntity entity = getProductEntity(productId);
        entity.setQuantityState(quantityState);
        productRepository.save(entity);
        return true;
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        return productMapper.toDto(getProductEntity(productId));
    }

    private ProductEntity getProductEntity(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Sort buildSort(List<String> sortValues) {
        if (sortValues == null || sortValues.isEmpty()) {
            return Sort.by(Sort.Order.asc("productName"));
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (int index = 0; index < sortValues.size(); index++) {
            String sortValue = sortValues.get(index);
            if (sortValue == null || sortValue.isBlank()) {
                continue;
            }

            if (sortValue.contains(",")) {
                orders.add(toOrder(sortValue));
                continue;
            }

            if (index + 1 < sortValues.size() && isDirection(sortValues.get(index + 1))) {
                orders.add(new Sort.Order(Sort.Direction.fromString(sortValues.get(index + 1)), sortValue));
                index++;
                continue;
            }

            orders.add(new Sort.Order(Sort.Direction.ASC, sortValue));
        }

        if (orders.isEmpty()) {
            return Sort.by(Sort.Order.asc("productName"));
        }
        return Sort.by(orders);
    }

    private Sort.Order toOrder(String sortValue) {
        String[] parts = sortValue.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromString(parts[1])
                : Sort.Direction.ASC;
        return new Sort.Order(direction, property);
    }

    private boolean isDirection(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        try {
            Sort.Direction.fromString(candidate);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
