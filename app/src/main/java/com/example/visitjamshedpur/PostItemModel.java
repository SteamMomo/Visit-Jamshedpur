package com.example.visitjamshedpur;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;

public class PostItemModel {

    ArrayList<String> imageDownloadUrl, imageDeleteUrl;
    String authorName, postDescription, postTitle, postProfile, authorIdentity, authorId, postId;
    String timestamp;
    int likes;

    public PostItemModel(ArrayList<String> imageDownloadUrl, ArrayList<String> imageDeleteUrl, String authorName, String postDescription, String postTitle, String postProfile, String authorIdentity, String authorId, String postId, int likes, String timestamp) {
        this.imageDownloadUrl = imageDownloadUrl;
        this.imageDeleteUrl = imageDeleteUrl;
        this.authorName = authorName;
        this.postDescription = postDescription;
        this.postTitle = postTitle;
        this.postProfile = postProfile;
        this.authorIdentity = authorIdentity;
        this.authorId = authorId;
        this.postId = postId;
        this.likes = likes;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPostId() {
        return postId;
    }

    public String getPostProfile() {
        return postProfile;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorIdentity() {
        return authorIdentity;
    }

    public ArrayList<String> getImageDownloadUrl() {
        return imageDownloadUrl;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public ArrayList<String> getImageDeleteUrl() {
        return imageDeleteUrl;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public String getAuthorId() {
        return authorId;
    }

    public int getLikes() {
        return likes;
    }
}
