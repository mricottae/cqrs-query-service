package com.mricotta.cqrs.query.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mricotta.cqrs.query.entity.Product;
import com.mricotta.cqrs.query.event.ProductEvent;
import com.mricotta.cqrs.query.event.ProductPayload;
import com.mricotta.cqrs.query.mapper.ProductMapper;
import com.mricotta.cqrs.query.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductProjectionServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductProjectionServiceImpl projectionService;

    @Test
    void apply_whenProductNotProjectedYet_savesIt() {
        var event = event(0L);
        var mapped = Product.builder().id(1L).version(0L).build();
        given(productRepository.findById(1L)).willReturn(Optional.empty());
        given(productMapper.toEntity(event)).willReturn(mapped);

        projectionService.apply(event);

        then(productRepository).should().save(mapped);
    }

    @Test
    void apply_whenEventIsNewerThanStoredProduct_overwritesIt() {
        var event = event(2L);
        var mapped = Product.builder().id(1L).version(2L).build();
        given(productRepository.findById(1L)).willReturn(Optional.of(Product.builder().id(1L).version(1L).build()));
        given(productMapper.toEntity(event)).willReturn(mapped);

        projectionService.apply(event);

        then(productRepository).should().save(mapped);
    }

    @ParameterizedTest
    @ValueSource(longs = {2L, 3L})
    void apply_whenEventIsDuplicateOrStale_ignoresIt(long storedVersion) {
        var event = event(2L);
        given(productRepository.findById(1L))
                .willReturn(Optional.of(Product.builder().id(1L).version(storedVersion).build()));

        projectionService.apply(event);

        then(productRepository).should(never()).save(any());
        verifyNoInteractions(productMapper);
    }

    private static ProductEvent event(long version) {
        var now = Instant.now();
        return new ProductEvent(UUID.randomUUID(), "ProductUpdated", now, 1L, version,
                new ProductPayload(1L, "Mouse", "Wireless mouse", new BigDecimal("19.99"), 10, now, now));
    }
}
