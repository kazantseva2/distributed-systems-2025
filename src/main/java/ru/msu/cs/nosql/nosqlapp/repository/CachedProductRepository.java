package ru.msu.cs.nosql.nosqlapp.repository;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import org.springframework.stereotype.Component;
import ru.msu.cs.nosql.nosqlapp.model.Product;

import java.util.concurrent.TimeUnit;

@Component
public class CachedProductRepository {

    private final ProductRepository productRepository;
    private final HazelcastInstance hazelcastInstance;

    private final IMap<String, Product> productCache;
    private final IMap<String, Object> lockMap;
    private final IMap<String, AggregatedRating> ratingCache;
    private final IMap<String, Long> rateLimitMap; // rate limiting по userId
    private final ITopic<String> productUpdateTopic;

    public CachedProductRepository(ProductRepository productRepository, HazelcastInstance hazelcastInstance) {
        this.productRepository = productRepository;
        this.hazelcastInstance = hazelcastInstance;
        this.productCache = hazelcastInstance.getMap("product");
        this.lockMap = hazelcastInstance.getMap("lockedProduct");
        this.ratingCache = hazelcastInstance.getMap("aggregatedRating");
        this.rateLimitMap = hazelcastInstance.getMap("rateLimit");
        this.productUpdateTopic = hazelcastInstance.getTopic("product_update_topic");

        productUpdateTopic.addMessageListener(message ->
                System.out.println("Product updated: " + message.getMessageObject()));
    }

    public Product getProductById(String productId) {
        return productCache.computeIfAbsent(productId, productRepository::findById);
    }

    public Product saveProduct(Product product) {
        lockMap.lock(product.getId(), 10, TimeUnit.SECONDS);
        try {
            Product updated = productRepository.save(product);
            productCache.put(updated.getId(), updated);
            productUpdateTopic.publish(product.getId());
            return updated;
        } finally {
            lockMap.unlock(product.getId());
        }
    }

    public void deleteProduct(String productId) {
        productRepository.deleteProduct(productId);
        productCache.delete(productId);
        ratingCache.delete(productId);
    }

    public void updateAggregatedRating(String productId, double avgRating, int countReviews) {
        lockMap.lock(productId, 10, TimeUnit.SECONDS);
        try {
            productRepository.updateAggregatedRating(productId, avgRating, countReviews);

            AggregatedRating agg = new AggregatedRating(avgRating, countReviews);
            ratingCache.put(productId, agg);

            Product cached = productCache.get(productId);
            if (cached != null) {
                cached.setAggregatedRating(avgRating);
                cached.setCountReviews(countReviews);
                productCache.put(productId, cached);
            }

            productUpdateTopic.publish(productId);
        } finally {
            lockMap.unlock(productId);
        }
    }

    public AggregatedRating getAggregatedRating(String productId) {
        return ratingCache.get(productId);
    }

    public boolean checkRateLimit(String userId, int maxReviewsPerMinute) {
        long now = System.currentTimeMillis();
        rateLimitMap.lock(userId);
        try {
            Long lastTimestamp = rateLimitMap.get(userId);
            if (lastTimestamp == null || now - lastTimestamp > TimeUnit.MINUTES.toMillis(1)) {
                rateLimitMap.put(userId, now, 1, TimeUnit.MINUTES); // TTL 1 минута
                return true;
            } else {
                return false; // превышен лимит
            }
        } finally {
            rateLimitMap.unlock(userId);
        }
    }

    public static class AggregatedRating {
        private double avgRating;
        private int countReviews;

        public AggregatedRating(double avgRating, int countReviews) {
            this.avgRating = avgRating;
            this.countReviews = countReviews;
        }

        public double getAvgRating() {
            return avgRating;
        }

        public int getCountReviews() {
            return countReviews;
        }
    }
}
