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

        // Генерация продуктов
        for (int i = 1; i <= productsCount; i++) {
            Product p = new Product("prod-" + i, "Product " + i, 0.0, 0);
            products.add(p);
            productRepository.save(p);
        }

        // Генерация пользователей
        for (int i = 1; i <= usersCount; i++) {
            User u = new User("user-" + i, "User " + i, 0);
            users.add(u);
            userRepository.save(u);
        }

        // Генерация отзывов
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
                    RandomUtils.randomText(10),
                    DateUtils.randomDate(),  // <-- Используем исправленный DateUtils
                    ModerationStatus.APPROVED
            );

            reviewRepository.save(review);

            // Обновление агрегированного рейтинга продукта
            double totalRating = product.getAggregatedRating() * product.getCountReviews();
            totalRating += rating;
            product.setCountReviews(product.getCountReviews() + 1);
            product.setAggregatedRating(totalRating / product.getCountReviews());
            productRepository.updateAggregatedRating(product.getId(), product.getAggregatedRating(), product.getCountReviews());

            // Увеличиваем счётчик отзывов пользователя
            userRepository.incrementReviewCount(user.getId());
        }
    }
}
