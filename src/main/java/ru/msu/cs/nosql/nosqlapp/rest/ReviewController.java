package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.model.RatingOperator;
import ru.msu.cs.nosql.nosqlapp.model.Review;
import ru.msu.cs.nosql.nosqlapp.repository.CachedProductRepository;
import ru.msu.cs.nosql.nosqlapp.repository.ElasticReviewRepository;
import ru.msu.cs.nosql.nosqlapp.repository.RateLimitExceededException;
import ru.msu.cs.nosql.nosqlapp.repository.ReviewRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {
    private ReviewRepository reviewRepository;
    private final ElasticReviewRepository elasticReviewRepository;
    private final CachedProductRepository cachedProductRepository;

    public ReviewController(ReviewRepository reviewRepository,
                            ElasticReviewRepository elasticReviewRepository,
                            CachedProductRepository cachedProductRepository) {
        this.reviewRepository = reviewRepository;
        this.elasticReviewRepository = elasticReviewRepository;
        this.cachedProductRepository = cachedProductRepository;
    }

    @GetMapping
    public List<Review> listAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") String id) {
        return reviewRepository.findById(id);
    }

    @PostMapping
    public Review saveReview(@RequestBody Review review) {
        String userId = review.getUserId();
        if (!cachedProductRepository.checkRateLimit(userId, 5)) {
            throw new RateLimitExceededException("Rate limit exceeded for user: " + userId);
        }

        Review savedReview = reviewRepository.save(review);
        elasticReviewRepository.save(savedReview);
        updateProductAggregatedRating(review.getProductId());

        return savedReview;
    }

    private void updateProductAggregatedRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        int countReviews = reviews.size();

        cachedProductRepository.updateAggregatedRating(productId, avgRating, countReviews);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable("id") String id) {
        Review review = reviewRepository.findById(id);
        if (review != null) {
            reviewRepository.deleteReview(id);
            elasticReviewRepository.deleteById(id);

            // Пересчет агрегированного рейтинга
            updateProductAggregatedRating(review.getProductId());
        }

    }

    @GetMapping("/search")
    public List<Review> advancedSearch(@RequestParam(required = false) String productId, @RequestParam(required = false) Integer rating,
                                       @RequestParam(required = false) RatingOperator ratingOp, @RequestParam(required = false) String text) {
        return elasticReviewRepository.searchByProductAndRatingAndText(productId, rating, ratingOp, text);
    }

    @GetMapping("/analytics/ratings")
    public Map<String, Double> ratingAnalytics(@RequestParam String productId) {
        return elasticReviewRepository.getRatingTrendByProduct(productId);
    }

    @GetMapping("/analytics/negative-words")
    public Map<String, Long> negativeWords() {
        return elasticReviewRepository.getCommonWordsInNegativeReviews();
    }

}
