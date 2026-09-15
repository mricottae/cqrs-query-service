package com.mricotta.cqrs.query.service;

import com.mricotta.cqrs.query.entity.Product;
import com.mricotta.cqrs.query.event.ProductEvent;
import com.mricotta.cqrs.query.mapper.ProductMapper;
import com.mricotta.cqrs.query.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductProjectionServiceImpl implements ProductProjectionService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Read-compare-save is safe without extra locking: events for one product share a partition key,
     * so they are consumed sequentially by a single listener thread.
     */
    @Override
    public void apply(ProductEvent event) {
        var alreadyApplied = productRepository.findById(event.aggregateId())
                .map(Product::getVersion)
                .filter(storedVersion -> storedVersion >= event.aggregateVersion())
                .isPresent();

        if (alreadyApplied) {
            log.debug("Ignoring {} for product {}: version {} already applied",
                    event.eventType(), event.aggregateId(), event.aggregateVersion());
            return;
        }

        productRepository.save(productMapper.toEntity(event));
        log.info("Applied {} for product {} (version {})",
                event.eventType(), event.aggregateId(), event.aggregateVersion());
    }
}
