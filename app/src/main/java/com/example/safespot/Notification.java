package com.example.safespot;

public class Notification {
    private int id; // Unique ID of the notification
    private String message; // The message of the notification
    private String location; // The location information
    private String timestamp; // The time and date of the notification

    // Default constructor
    public Notification() {
    }

    // Constructor with fields
    public Notification(int id, String message, String location, String timestamp) {
        this.id = id;
        this.message = message;
        this.location = location;
        this.timestamp = timestamp;
    }

    // Getters and setters for each field

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", location='" + location + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}

