package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.model.RatingOperator;
import ru.msu.cs.nosql.nosqlapp.model.Review;
import ru.msu.cs.nosql.nosqlapp.repository.ElasticReviewRepository;
import ru.msu.cs.nosql.nosqlapp.repository.ReviewRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {
    private ReviewRepository reviewRepository;
    private final ElasticReviewRepository elasticReviewRepository;

    public ReviewController(ReviewRepository reviewRepository, ElasticReviewRepository elasticReviewRepository) {
        this.reviewRepository = reviewRepository;
        this.elasticReviewRepository = elasticReviewRepository;
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
        Review savedReview = reviewRepository.save(review);
        elasticReviewRepository.save(savedReview);
        return savedReview;
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable("id") String id) {
        reviewRepository.deleteReview(id);
        elasticReviewRepository.deleteById(id);
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
