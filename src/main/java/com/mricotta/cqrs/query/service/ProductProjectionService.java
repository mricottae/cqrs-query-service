package com.mricotta.cqrs.query.service;

import com.mricotta.cqrs.query.event.ProductEvent;

public interface ProductProjectionService {

    /** Upserts the read model from the event; duplicate or out-of-date events are ignored. */
    void apply(ProductEvent event);
}
