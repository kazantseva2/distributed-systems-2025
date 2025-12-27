//package ru.msu.cs.nosql.nosqlapp.util;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import ru.msu.cs.nosql.nosqlapp.repository.ElasticReviewRepository;
//import ru.msu.cs.nosql.nosqlapp.repository.ProductRepository;
//import ru.msu.cs.nosql.nosqlapp.repository.ReviewRepository;
//import ru.msu.cs.nosql.nosqlapp.repository.UserRepository;
//
//@Component
//public class DataGeneratorRunner implements CommandLineRunner {
//
//    private final ProductRepository productRepository;
//    private final UserRepository userRepository;
//    private final ReviewRepository reviewRepository;
//    private final ElasticReviewRepository elasticReviewRepository;
//
//    public DataGeneratorRunner(ProductRepository productRepository,
//                               UserRepository userRepository,
//                               ReviewRepository reviewRepository,
//                               ElasticReviewRepository elasticReviewRepository) {
//        this.productRepository = productRepository;
//        this.userRepository = userRepository;
//        this.reviewRepository = reviewRepository;
//        this.elasticReviewRepository = elasticReviewRepository;
//    }
//
//    @Override
//    public void run(String... args) {
//        // 1️⃣ Очистка существующих данных
//        reviewRepository.deleteAll();
//        productRepository.deleteAll();
//        userRepository.deleteAll();
//
//        System.out.println(userRepository.findAll().size());
//
//        // Очистка индекса Elasticsearch
//        elasticReviewRepository.deleteAll();
//
//        // 2️⃣ Генерация новых данных
//        DataGenerator generator = new DataGenerator(productRepository, userRepository, reviewRepository);
////        generator.generateData(10_000, 100_000, 500_000);
//
//        generator.generateData(5, 3, 10);
//
//        // 3️⃣ Синхронизация с Elasticsearch
//        reviewRepository.findAll().forEach(elasticReviewRepository::save);
//
//        System.out.println("Data generation completed: Mongo + Elasticsearch are in sync.");
//    }
//}
