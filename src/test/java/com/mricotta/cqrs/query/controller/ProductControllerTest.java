package com.mricotta.cqrs.query.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mricotta.cqrs.query.dto.ProductResponse;
import com.mricotta.cqrs.query.exception.ProductNotFoundException;
import com.mricotta.cqrs.query.service.ProductService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void getProducts_returnsList() throws Exception {
        given(productService.getProducts()).willReturn(List.of(response(1L), response(2L)));

        mockMvc.perform(get("/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getProductById_returnsProduct() throws Exception {
        given(productService.getProductById(1L)).willReturn(response(1L));

        mockMvc.perform(get("/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mouse"))
                .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    void getProductById_whenMissing_returnsNotFound() throws Exception {
        given(productService.getProductById(42L)).willThrow(new ProductNotFoundException(42L));

        mockMvc.perform(get("/v1/products/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product with id 42 not found"))
                .andExpect(jsonPath("$.path").value("/v1/products/42"));
    }

    @Test
    void getProductById_withNonNumericId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/v1/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value 'abc' for parameter 'id'"));

        verifyNoInteractions(productService);
    }

    @Test
    void getProducts_fromAllowedOrigin_includesCorsHeader() throws Exception {
        given(productService.getProducts()).willReturn(List.of());

        mockMvc.perform(get("/v1/products").header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    private static ProductResponse response(Long id) {
        var now = Instant.now();
        return new ProductResponse(id, "Mouse", "Wireless mouse", new BigDecimal("19.99"), 10, now, now, 0L);
    }
}
