package com.mricotta.cqrs.query.mapper;

import com.mricotta.cqrs.query.dto.ProductResponse;
import com.mricotta.cqrs.query.entity.Product;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    ProductResponse toDto(Product product);

    List<ProductResponse> toDtoList(List<Product> products);

    Product toEntity(ProductResponse productResponse);
}
