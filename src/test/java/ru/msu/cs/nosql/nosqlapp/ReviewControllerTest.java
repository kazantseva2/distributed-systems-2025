package ru.msu.cs.nosql.nosqlapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.msu.cs.nosql.nosqlapp.model.*;
import ru.msu.cs.nosql.nosqlapp.repository.CachedProductRepository;
import ru.msu.cs.nosql.nosqlapp.repository.ProductRepository;
import ru.msu.cs.nosql.nosqlapp.repository.ReviewRepository;

import java.util.Date;

import static com.mongodb.assertions.Assertions.assertNotNull;
import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CachedProductRepository cachedProductRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private String userId = "user1";

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
        productRepository.deleteAll();
        cachedProductRepository.clearRateLimit();
        testProduct = productRepository.save(new Product(null, "Laptop", 0.0, 0));
    }

    @Test
    void testCreateReviewAndCheckAggregatedRating() throws Exception {
        Review review = new Review(null,
                testProduct.getId(),
                userId,
                5,
                "Great laptop",
                "Really satisfied with the purchase",
                new Date(),
                ModerationStatus.APPROVED);

        String reviewJson = objectMapper.writeValueAsString(review);

        // Сохраняем отзыв через контроллер
        mockMvc.perform(post("/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Great laptop"))
                .andExpect(jsonPath("$.rating").value(5));

        // Проверяем агрегированный рейтинг через CachedProductRepository
        CachedProductRepository.AggregatedRating agg = cachedProductRepository.getAggregatedRating(testProduct.getId());
        assert agg != null;
        assert agg.getAvgRating() == 5.0;
        assert agg.getCountReviews() == 1;
    }

    @Test
    void testRateLimitExceeded() throws Exception {
        // 5 отзывов — ок
        for (int i = 0; i < 5; i++) {
            Review review = new Review(
                    null,
                    testProduct.getId(),
                    userId,
                    4,
                    "Review " + i,
                    "Text " + i,
                    new Date(),
                    ModerationStatus.APPROVED
            );

            mockMvc.perform(post("/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(review)))
                    .andExpect(status().isOk());
        }

        // 6-й — превышение лимита
        Review review6 = new Review(
                null,
                testProduct.getId(),
                userId,
                3,
                "Extra review",
                "Exceeding limit",
                new Date(),
                ModerationStatus.APPROVED
        );

        mockMvc.perform(post("/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review6)))
                .andExpect(result -> {
                    Exception ex = result.getResolvedException();
                    assertNotNull(ex);
                    assertTrue(ex.getMessage().contains("Rate limit exceeded"));
                });
    }


    @Test
    void testDeleteReviewAndUpdateRating() throws Exception {
        // Создаем отзыв
        Review review = new Review(null,
                testProduct.getId(),
                userId,
                5,
                "Review to delete",
                "Text",
                new Date(),
                ModerationStatus.APPROVED);
        review = reviewRepository.save(review);

        // Обновляем агрегированный рейтинг
        cachedProductRepository.updateAggregatedRating(testProduct.getId(), 5.0, 1);

        // Удаляем через контроллер
        mockMvc.perform(delete("/review/" + review.getId()))
                .andExpect(status().isOk());

        // Проверяем, что агрегированный рейтинг сброшен
        CachedProductRepository.AggregatedRating agg = cachedProductRepository.getAggregatedRating(testProduct.getId());
        assert agg != null;
        assert agg.getAvgRating() == 0.0;
        assert agg.getCountReviews() == 0;
    }
}
