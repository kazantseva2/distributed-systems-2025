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
    private final IMap<String, RateLimit> rateLimitMap; // rate limiting по userId
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
        Product saved = productRepository.save(product); // ID теперь точно есть
        lockMap.lock(saved.getId(), 10, TimeUnit.SECONDS);
        try {
            productCache.put(saved.getId(), saved);
            productUpdateTopic.publish(saved.getId());
            return saved;
        } finally {
            lockMap.unlock(saved.getId());
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
        rateLimitMap.lock(userId);
        try {
            RateLimit rate = rateLimitMap.get(userId);
            long now = System.currentTimeMillis();

            if (rate == null || now - rate.getFirstRequestTimestamp() > 60_000) {
                // первый отзыв или прошла минута — сброс
                rateLimitMap.put(userId, new RateLimit(1, now));
                return true;
            }

            if (rate.getCount() < maxReviewsPerMinute) {
                rate.setCount(rate.getCount() + 1);
                rateLimitMap.put(userId, rate);
                return true;
            }

            // лимит превышен
            return false;

        } finally {
            rateLimitMap.unlock(userId);
        }
    }

    public static class RateLimit implements java.io.Serializable {
        private int count;
        private long firstRequestTimestamp;

        public RateLimit(int count, long firstRequestTimestamp) {
            this.count = count;
            this.firstRequestTimestamp = firstRequestTimestamp;
        }

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }

        public long getFirstRequestTimestamp() { return firstRequestTimestamp; }
        public void setFirstRequestTimestamp(long ts) { this.firstRequestTimestamp = ts; }
    }

    public void clearRateLimit() {
        rateLimitMap.clear();
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

