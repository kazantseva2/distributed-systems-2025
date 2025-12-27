package ru.msu.cs.nosql.nosqlapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.msu.cs.nosql.nosqlapp.model.Product;
import ru.msu.cs.nosql.nosqlapp.repository.ProductRepository;
import ru.msu.cs.nosql.nosqlapp.repository.CachedProductRepository;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CachedProductRepository cachedProductRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
    }

    @Test
    public void testCreateProductAndGetById() throws Exception {
        Product product = new Product(
                null,
                "Test Product",
                0.0,
                0
        );

        // Сохраняем через контроллер
        String responseContent = mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Product saved = objectMapper.readValue(responseContent, Product.class);

        assertNotNull(saved.getId());
        assertEquals("Test Product", saved.getName());

        // Проверяем получение по id
        mockMvc.perform(get("/product/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void testListAllProducts() throws Exception {
        productRepository.save(new Product(null, "TV", 0.0, 0));
        productRepository.save(new Product(null, "Phone", 0.0, 0));

        mockMvc.perform(get("/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testDeleteProduct() throws Exception {
        Product product = productRepository.save(new Product(null, "Tablet", 0.0, 0));

        mockMvc.perform(delete("/product/" + product.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/product/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // возвращается null при отсутствии продукта
    }

    @Test
    void testUpdateAggregatedRating() {
        Product product = productRepository.save(new Product(null, "Camera", 0.0, 0));

        cachedProductRepository.updateAggregatedRating(product.getId(), 4.5, 10);

        CachedProductRepository.AggregatedRating agg = cachedProductRepository.getAggregatedRating(product.getId());
        assert agg != null;
        assert agg.getAvgRating() == 4.5;
        assert agg.getCountReviews() == 10;

        Product cached = cachedProductRepository.getProductById(product.getId());
        assert cached.getAggregatedRating() == 4.5;
        assert cached.getCountReviews() == 10;
    }
}
