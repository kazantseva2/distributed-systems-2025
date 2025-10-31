package ru.msu.cs.nosql.nosqlapp.model;

import org.springframework.data.annotation.Id;

public class Product {
    @Id
    private String id;

    private String name;
    private double aggregatedRating;
    private int countReviews;

    public Product(String id, String name, double aggregatedRating, int countReviews) {
        this.id = id;
        this.name = name;
        this.aggregatedRating = aggregatedRating;
        this.countReviews = countReviews;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAggregatedRating() {
        return aggregatedRating;
    }

    public void setAggregatedRating(double aggregatedRating) {
        this.aggregatedRating = aggregatedRating;
    }

    public int getCountReviews() {
        return countReviews;
    }

    public void setCountReviews(int countReviews) {
        this.countReviews = countReviews;
    }
}
