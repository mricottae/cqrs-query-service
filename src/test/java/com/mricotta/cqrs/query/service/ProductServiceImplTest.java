package com.mricotta.cqrs.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.mricotta.cqrs.query.dto.ProductResponse;
import com.mricotta.cqrs.query.entity.Product;
import com.mricotta.cqrs.query.exception.ProductNotFoundException;
import com.mricotta.cqrs.query.mapper.ProductMapper;
import com.mricotta.cqrs.query.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProducts_returnsMappedList() {
        var products = List.of(Product.builder().id(1L).build(), Product.builder().id(2L).build());
        var expected = List.of(response(1L), response(2L));
        given(productRepository.findAll()).willReturn(products);
        given(productMapper.toDtoList(products)).willReturn(expected);

        var result = productService.getProducts();

        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    void getProductById_whenFound_returnsDto() {
        var product = Product.builder().id(1L).build();
        var expected = response(1L);
        given(productRepository.findById(1L)).willReturn(Optional.of(product));
        given(productMapper.toDto(product)).willReturn(expected);

        var result = productService.getProductById(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getProductById_whenMissing_throwsProductNotFoundException() {
        given(productRepository.findById(42L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(42L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 42 not found");
        then(productMapper).should(never()).toDto(any());
    }

    private static ProductResponse response(Long id) {
        var now = Instant.now();
        return new ProductResponse(id, "Mouse", "Wireless mouse", new BigDecimal("19.99"), 10, now, now);
    }
}
