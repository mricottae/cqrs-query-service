package com.mricotta.cqrs.query.event;

import java.util.Arrays;
import java.util.Optional;

public enum ProductEventType {

    PRODUCT_CREATED("ProductCreated"),
    PRODUCT_UPDATED("ProductUpdated");

    private final String wireName;

    ProductEventType(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static Optional<ProductEventType> fromWireName(String wireName) {
        return Arrays.stream(values())
                .filter(type -> type.wireName.equals(wireName))
                .findFirst();
    }
}
