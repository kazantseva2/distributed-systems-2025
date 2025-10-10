package ru.msu.cs.nosql.nosqlapp.model;

import org.springframework.data.annotation.Id;

public class Product {
    @Id
    private String id;

    private String name;
    private float aggregatedRating;
    private int countReviews;
}
