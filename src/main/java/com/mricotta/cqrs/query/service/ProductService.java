package com.mricotta.cqrs.query.service;

import com.mricotta.cqrs.query.dto.ProductResponse;
import java.util.List;

public interface ProductService {

    List<ProductResponse> getProducts();

    ProductResponse getProductById(Long id);
}
