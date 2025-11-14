package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.model.Review;
import ru.msu.cs.nosql.nosqlapp.repository.ReviewRepository;

import java.util.List;

@RestController
public class ReviewController {
    private ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/review")
    public List<Review> listAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping("/review/{id}")
    public Review getReviewById(@PathVariable("id") String id) {
        return reviewRepository.findById(id);
    }

    @PostMapping("/review")
    public Review saveProduct(@RequestBody Review review) {
        Review savedReview = reviewRepository.save(review);
        return savedReview;
    }

    @DeleteMapping("/review/{id}")
    public void deleteReview(@PathVariable("id") String id) {
        reviewRepository.deleteReview(id);
    }
}
