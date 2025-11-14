package ru.msu.cs.nosql.nosqlapp.rest;

import org.springframework.web.bind.annotation.*;
import ru.msu.cs.nosql.nosqlapp.model.Product;
import ru.msu.cs.nosql.nosqlapp.repository.ProductRepository;

import java.util.List;

@RestController
public class ProductController {
    private ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/product")
    public List<Product> listAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/product/{id}")
    public Product getProductById(@PathVariable("id") String id) {
        return productRepository.findById(id);
    }

    @PostMapping("/product")
    public Product saveProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);
        return savedProduct;
    }

    @DeleteMapping("/product/{id}")
    public void deleteProduct(@PathVariable("id") String id) {
        productRepository.deleteProduct(id);
    }
}
