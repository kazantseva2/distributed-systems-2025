package ru.msu.cs.nosql.nosqlapp.model;

import org.springframework.data.annotation.Id;

public class User {
    @Id
    private String id;

    private String name;
    private int reviewsCount;

    public User(String id, String name, int reviewsCount) {
        this.id = id;
        this.name = name;
        this.reviewsCount = reviewsCount;
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

    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }
}
