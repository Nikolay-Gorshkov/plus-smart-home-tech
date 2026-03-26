package ru.yandex.practicum.commerce.shopping.store.mapper;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.api.dto.PageProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PageableObjectDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.SortObjectDto;
import ru.yandex.practicum.commerce.shopping.store.entity.ProductEntity;

@Component
public class ProductMapper {

    public ProductDto toDto(ProductEntity entity) {
        return new ProductDto(
                entity.getProductId(),
                entity.getProductName(),
                entity.getDescription(),
                entity.getImageSrc(),
                entity.getQuantityState(),
                entity.getProductState(),
                entity.getProductCategory(),
                entity.getPrice()
        );
    }

    public ProductEntity toEntity(ProductDto dto) {
        ProductEntity entity = new ProductEntity();
        updateEntity(entity, dto);
        if (dto.productId() != null) {
            entity.setProductId(dto.productId());
        }
        return entity;
    }

    public void updateEntity(ProductEntity entity, ProductDto dto) {
        entity.setProductName(dto.productName());
        entity.setDescription(dto.description());
        entity.setImageSrc(dto.imageSrc());
        entity.setQuantityState(dto.quantityState());
        entity.setProductState(dto.productState());
        entity.setProductCategory(dto.productCategory());
        entity.setPrice(dto.price());
    }

    public PageProductDto toPageDto(Page<ProductEntity> page) {
        List<SortObjectDto> pageSort = mapSort(page.getSort());
        return new PageProductDto(
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getSize(),
                page.getContent().stream().map(this::toDto).toList(),
                page.getNumber(),
                pageSort,
                page.getNumberOfElements(),
                new PageableObjectDto(
                        page.getPageable().getOffset(),
                        mapSort(page.getPageable().getSort()),
                        page.getPageable().isUnpaged(),
                        page.getPageable().isPaged(),
                        page.getPageable().getPageNumber(),
                        page.getPageable().getPageSize()
                ),
                page.isEmpty()
        );
    }

    private List<SortObjectDto> mapSort(Sort sort) {
        return sort.stream()
                .map(order -> new SortObjectDto(
                        order.getDirection().name(),
                        order.getNullHandling().name(),
                        order.isAscending(),
                        order.getProperty(),
                        order.isIgnoreCase()
                ))
                .toList();
    }
}
