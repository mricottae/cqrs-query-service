package com.mricotta.cqrs.query.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.mricotta.cqrs.query.dto.ProductResponse;
import com.mricotta.cqrs.query.entity.Product;
import com.mricotta.cqrs.query.event.ProductEvent;
import com.mricotta.cqrs.query.event.ProductPayload;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toDto_mapsAllFields() {
        var now = Instant.now();
        var product = new Product(1L, "Mouse", "Wireless mouse", new BigDecimal("19.99"), 10, now, now, 3L);

        var dto = mapper.toDto(product);

        assertThat(dto).isEqualTo(
                new ProductResponse(1L, "Mouse", "Wireless mouse", new BigDecimal("19.99"), 10, now, now, 3L));
    }

    @Test
    void toDtoList_mapsEveryElement() {
        var products = List.of(Product.builder().id(1L).name("A").build(), Product.builder().id(2L).name("B").build());

        var dtos = mapper.toDtoList(products);

        assertThat(dtos).extracting(ProductResponse::id, ProductResponse::name)
                .containsExactly(tuple(1L, "A"), tuple(2L, "B"));
    }

    @Test
    void toEntity_flattensEventPayloadAndTakesAggregateVersion() {
        var createdAt = Instant.parse("2026-09-14T09:00:00Z");
        var updatedAt = Instant.parse("2026-09-14T10:00:00Z");
        var event = new ProductEvent(UUID.randomUUID(), "ProductUpdated", updatedAt, 1L, 4L,
                new ProductPayload(1L, "Mouse", "Wireless mouse", new BigDecimal("19.99"), 10, createdAt, updatedAt));

        var product = mapper.toEntity(event);

        assertThat(product)
                .extracting(Product::getId, Product::getName, Product::getDescription, Product::getPrice,
                        Product::getStock, Product::getCreatedAt, Product::getUpdatedAt, Product::getVersion)
                .containsExactly(1L, "Mouse", "Wireless mouse", new BigDecimal("19.99"), 10, createdAt, updatedAt, 4L);
    }
}
