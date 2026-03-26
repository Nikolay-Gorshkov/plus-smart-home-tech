package ru.yandex.practicum.commerce.shopping.store.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.commerce.interaction.api.dto.PageProductDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductCategory;
import ru.yandex.practicum.commerce.shopping.store.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shopping.store.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    void shouldSupportSplitSortQueryParameter() {
        when(productRepository.findAllByProductCategory(eq(ProductCategory.CONTROL), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(productMapper.toPageDto(any()))
                .thenReturn(new PageProductDto(0L, 0, true, true, 0, List.of(), 0, List.of(), 0, null, true));

        productService.getProducts(ProductCategory.CONTROL, 0, 20, List.of("productName", "DESC"));

        verify(productRepository).findAllByProductCategory(eq(ProductCategory.CONTROL), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("productName");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void shouldSupportCommaSeparatedSortQueryParameter() {
        when(productRepository.findAllByProductCategory(eq(ProductCategory.CONTROL), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(productMapper.toPageDto(any()))
                .thenReturn(new PageProductDto(0L, 0, true, true, 0, List.of(), 0, List.of(), 0, null, true));

        productService.getProducts(ProductCategory.CONTROL, 0, 20, List.of("productName,DESC"));

        verify(productRepository).findAllByProductCategory(eq(ProductCategory.CONTROL), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("productName");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }
}
