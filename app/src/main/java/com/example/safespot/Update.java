package com.example.safespot;

import com.google.gson.annotations.SerializedName;

public class Update {
    private String id;
    private String userId;
    private String location;
    private String timedate;
    private String name;
    private String photo; // Base64-encoded photo

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimedate() {
        return timedate;
    }

    public void setTimedate(String timedate) {
        this.timedate = timedate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoBlob() {
        return photo;
    }

    public void setPhotoBlob(String photoBlob) {
        this.photo = photoBlob;
    }
}



