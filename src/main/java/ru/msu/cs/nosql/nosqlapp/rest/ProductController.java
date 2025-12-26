package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.model.Product;
import ru.msu.cs.nosql.nosqlapp.repository.CachedProductRepository;
import ru.msu.cs.nosql.nosqlapp.repository.ProductRepository;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private ProductRepository productRepository;
    private final CachedProductRepository cachedProductRepository;

    public ProductController(ProductRepository productRepository, CachedProductRepository cachedProductRepository) {
        this.productRepository = productRepository;
        this.cachedProductRepository = cachedProductRepository;
    }

    @GetMapping
    public List<Product> listAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable("id") String id) {
        return cachedProductRepository.getProductById(id);
    }

    @PostMapping
    public Product saveProduct(@RequestBody Product product) {
        return cachedProductRepository.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable("id") String id) {
        cachedProductRepository.deleteProduct(id);
    }
}
