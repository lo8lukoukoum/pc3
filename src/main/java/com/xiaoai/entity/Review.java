package com.xiaoai.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class Review {
    private int reviewId;
    private int productId;
    private int userId;
    private int rating; // Assuming 0 or negative if not rated
    private String commentText;
    private String imageUrl;
    private String videoUrl;
    private boolean isAnonymous;
    private Timestamp reviewDate;
    private String status;

    public Review() {
    }

    public Review(int productId, int userId, int rating, String commentText, String imageUrl, String videoUrl, boolean isAnonymous, String status) {
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.commentText = commentText;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.isAnonymous = isAnonymous;
        this.status = status;
    }
    
    public Review(int reviewId, int productId, int userId, int rating, String commentText, String imageUrl, String videoUrl, boolean isAnonymous, Timestamp reviewDate, String status) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.commentText = commentText;
        this.imageUrl = imageUrl;
        this.videoUrl = videoUrl;
        this.isAnonymous = isAnonymous;
        this.reviewDate = reviewDate;
        this.status = status;
    }

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public Timestamp getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Timestamp reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return reviewId == review.reviewId &&
               productId == review.productId &&
               userId == review.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, productId, userId);
    }

    @Override
    public String toString() {
        return "Review{" +
               "reviewId=" + reviewId +
               ", productId=" + productId +
               ", userId=" + userId +
               ", rating=" + rating +
               ", commentText='" + commentText + '\'' +
               ", isAnonymous=" + isAnonymous +
               ", reviewDate=" + reviewDate +
               ", status='" + status + '\'' +
               '}';
    }
}
