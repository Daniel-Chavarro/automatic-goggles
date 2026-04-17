package org.java_avanzado.taller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.java_avanzado.taller.controller.dto.request.create.CreateProductRequest;
import org.java_avanzado.taller.controller.dto.response.ProductResponse;
import org.java_avanzado.taller.controller.dto.response.ProductSummaryResponse;
import org.java_avanzado.taller.config.JwtAuthenticationFilter;
import org.java_avanzado.taller.service.ProductService;
import org.java_avanzado.taller.support.TestDataFactory;
import org.java_avanzado.taller.utils.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void given_activeProductsExist_when_getAllProducts_then_returnsSummaryList() throws Exception {
        var product = TestDataFactory.product();
        var summary = ProductSummaryResponse.builder()
                .id(1L)
                .name("Coffee")
                .price(product.getPrice())
                .build();

        when(productService.getAllActiveProducts()).thenReturn(List.of(product));
        when(productMapper.fromProductListToSummaryList(anyList())).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Coffee"))
                .andExpect(jsonPath("$[0].price").value(12.50));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void given_validRequest_when_createProduct_then_returnsProductPayload() throws Exception {
        var createdProduct = TestDataFactory.product();
        var response = ProductResponse.builder()
                .id(1L)
                .name("Coffee")
                .description("Ground coffee")
                .price(createdProduct.getPrice())
                .stock(createdProduct.getQuantity())
                .active(true)
                .build();

        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(createdProduct);
        when(productMapper.fromProductToResponse(createdProduct)).thenReturn(response);

        String requestBody = """
                {
                  \"name\": \"Coffee\",
                  \"description\": \"Ground coffee\",
                  \"price\": 12.50,
                  \"quantity\": 20
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Coffee"))
                .andExpect(jsonPath("$.description").value("Ground coffee"))
                .andExpect(jsonPath("$.price").value(12.50))
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void given_invalidRequest_when_createProduct_then_returnsBadRequest() throws Exception {
        String invalidRequestBody = """
                {
                  \"name\": \"\",
                  \"price\": -1,
                  \"quantity\": -5
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService, productMapper);
    }
}
