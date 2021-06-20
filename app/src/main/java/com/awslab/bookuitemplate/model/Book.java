package com.awslab.bookuitemplate.model;

import java.io.Serializable;

// implement Serializable interface is important to send the book object to another activity
public class Book implements Serializable {

    private String title,description,author,imgUrl, review,isFav;
    private int pages;
    private float rating;
    private int drawableResource; // this for testing purpos...

    public Book() {
    }

    public Book(int drawableResource) {
        this.drawableResource = drawableResource;
    }

    public Book(String title, String description, String author, String imgUrl, int pages, String review, float rating) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.imgUrl = imgUrl;
        this.pages = pages;
        this.review = review;
        this.rating = rating;
        this.drawableResource = 0; //测试用
        this.isFav = "no";
    }


    public String getIsFav() {
        return isFav;
    }

    public void setIsFav(String title) {
        this.isFav = isFav;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getDrawableResource() {
        return drawableResource;
    }

    public void setDrawableResource(int drawableResource) {
        this.drawableResource = drawableResource;
    }
}
