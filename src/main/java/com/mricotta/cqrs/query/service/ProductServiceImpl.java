package com.mricotta.cqrs.query.service;

import com.mricotta.cqrs.query.dto.ProductResponse;
import com.mricotta.cqrs.query.exception.ProductNotFoundException;
import com.mricotta.cqrs.query.mapper.ProductMapper;
import com.mricotta.cqrs.query.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponse> getProducts() {
        return productMapper.toDtoList(productRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
