package ru.msu.cs.nosql.nosqlapp.model;

import org.springframework.data.annotation.Id;

public class User {
    @Id
    private String id;

    private String name;
    private int reviewsCount;
}
