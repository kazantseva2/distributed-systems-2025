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
    private final IMap<String, Product> cacheMap;
    private final IMap<String, Product> lockMap;
    private final ITopic<String> productUpdateTopic;

    public CachedProductRepository(ProductRepository productRepository, HazelcastInstance hazelcastInstance) {
        this.productRepository = productRepository;
        this.hazelcastInstance = hazelcastInstance;
        this.cacheMap = hazelcastInstance.getMap("product");
        this.lockMap = hazelcastInstance.getMap("lockedProduct");
        this.productUpdateTopic = hazelcastInstance.getTopic("product_update_topic");

        productUpdateTopic.addMessageListener(message ->
                System.out.println("Product updated: " + message.getMessageObject()));
    }

    public Product getProductById(String productId) {
        return cacheMap.computeIfAbsent(productId, id -> productRepository.findById(id));
    }

    public Product saveProduct(Product product) {
        lockMap.lock(product.getId(), 10, TimeUnit.SECONDS);
        Product updatedProduct;
        try {
            updatedProduct = productRepository.save(product);
            cacheMap.put(updatedProduct.getId(), updatedProduct);
            productUpdateTopic.publish(product.getId());
        } finally {
            lockMap.unlock(product.getId());
        }
        return updatedProduct;
    }

    public void deleteProduct(String productId) {
        productRepository.deleteProduct(productId);
        cacheMap.delete(productId);
    }

    public void updateAggregatedRating(String productId, double avgRating, int countReviews) {
        lockMap.lock(productId, 10, TimeUnit.SECONDS);
        try {
            productRepository.updateAggregatedRating(productId, avgRating, countReviews);
            Product cached = cacheMap.get(productId);
            if (cached != null) {
                cached.setAggregatedRating(avgRating);
                cached.setCountReviews(countReviews);
                cacheMap.put(productId, cached);
            }
            productUpdateTopic.publish(productId);
        } finally {
            lockMap.unlock(productId);
        }
    }
}
