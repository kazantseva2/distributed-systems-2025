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
    private ModerationStatus moderationStatus;

    public Review(String id, String productId, String userId, int rating, String title, String text, Date date, ModerationStatus moderationStatus) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.title = title;
        this.text = text;
        this.date = date;
        this.moderationStatus = moderationStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }
}
