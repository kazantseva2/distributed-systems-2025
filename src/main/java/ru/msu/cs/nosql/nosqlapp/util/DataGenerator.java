package ru.msu.cs.nosql.nosqlapp.util;

import ru.msu.cs.nosql.nosqlapp.model.*;
import ru.msu.cs.nosql.nosqlapp.repository.ProductRepository;
import ru.msu.cs.nosql.nosqlapp.repository.ReviewRepository;
import ru.msu.cs.nosql.nosqlapp.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public DataGenerator(ProductRepository productRepository, UserRepository userRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public void generateData(int productsCount, int usersCount, int reviewsCount) {
        List<Product> products = new ArrayList<>();
        List<User> users = new ArrayList<>();
        Random random = new Random();

        long startTime = System.currentTimeMillis();

        System.out.println("=== Data generation started ===");
        System.out.println("Products: " + productsCount +
                ", Users: " + usersCount +
                ", Reviews: " + reviewsCount);

        // -----------------------
        // Генерация продуктов
        // -----------------------
        System.out.println("Generating products...");
        for (int i = 1; i <= productsCount; i++) {
            Product p = new Product("prod-" + i, "Product " + i, 0.0, 0);
            products.add(p);
            productRepository.save(p);

            if (i % 1000 == 0 || i == productsCount) {
                System.out.println("  Products generated: " + i + "/" + productsCount);
            }
        }

        // -----------------------
        // Генерация пользователей
        // -----------------------
        System.out.println("Generating users...");
        for (int i = 1; i <= usersCount; i++) {
            User u = new User("user-" + i, "User " + i, 0);
            users.add(u);
            userRepository.save(u);

            if (i % 5000 == 0 || i == usersCount) {
                System.out.println("  Users generated: " + i + "/" + usersCount);
            }
        }

        // -----------------------
        // Генерация отзывов
        // -----------------------
        System.out.println("Generating reviews...");
        for (int i = 1; i <= reviewsCount; i++) {
            Product product = products.get(random.nextInt(products.size()));
            User user = users.get(random.nextInt(users.size()));

            int rating = RandomUtils.randomRating();
            Review review = new Review(
                    "rev-" + i,
                    product.getId(),
                    user.getId(),
                    rating,
                    "Review " + i,
                    RandomUtils.randomText(rating),
                    DateUtils.randomDate(),
                    ModerationStatus.APPROVED
            );

            reviewRepository.save(review);

            // Обновление агрегированного рейтинга
            double totalRating = product.getAggregatedRating() * product.getCountReviews();
            totalRating += rating;
            product.setCountReviews(product.getCountReviews() + 1);
            product.setAggregatedRating(totalRating / product.getCountReviews());
            productRepository.updateAggregatedRating(
                    product.getId(),
                    product.getAggregatedRating(),
                    product.getCountReviews()
            );

            userRepository.incrementReviewCount(user.getId());

            // 🔹 Прогресс
            if (i % 10_000 == 0 || i == reviewsCount) {
                long elapsedSec = (System.currentTimeMillis() - startTime) / 1000;
                System.out.println("  Reviews generated: " + i + "/" + reviewsCount +
                        " (" + elapsedSec + " sec)");
            }
        }

        long totalTimeSec = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("=== Data generation completed in " + totalTimeSec + " sec ===");
    }

}
