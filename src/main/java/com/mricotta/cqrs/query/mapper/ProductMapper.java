package com.mricotta.cqrs.query.mapper;

import com.mricotta.cqrs.query.dto.ProductResponse;
import com.mricotta.cqrs.query.entity.Product;
import com.mricotta.cqrs.query.event.ProductEvent;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    ProductResponse toDto(Product product);

    List<ProductResponse> toDtoList(List<Product> products);

    @Mapping(target = "id", source = "payload.id")
    @Mapping(target = "name", source = "payload.name")
    @Mapping(target = "description", source = "payload.description")
    @Mapping(target = "price", source = "payload.price")
    @Mapping(target = "stock", source = "payload.stock")
    @Mapping(target = "createdAt", source = "payload.createdAt")
    @Mapping(target = "updatedAt", source = "payload.updatedAt")
    @Mapping(target = "version", source = "aggregateVersion")
    Product toEntity(ProductEvent event);
}
