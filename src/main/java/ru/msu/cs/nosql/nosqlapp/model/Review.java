package ru.msu.cs.nosql.nosqlapp.model;

import org.springframework.data.annotation.Id;
import java.util.Date;

public class Review {
    @Id
    private String id;

    private String productId;
    private String userId;
    private int rating;
    private String title;
    private String text;
    private Date date;
    private String moderationStatus;
}
