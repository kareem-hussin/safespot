package com.example.safespot;

import com.google.gson.annotations.SerializedName;
import android.util.Base64;

public class CardUser {
    @SerializedName("first_name")
    private String firstName;

    @SerializedName("middle_name")
    private String middleName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("birthdate")
    private String birthdate;

    @SerializedName("photoBlob")
    private String photoBlob;  // Base64-encoded string from API

    @SerializedName("card_user_id")
    private String cardUserId; // Add this field for the unique identifier

    // Getters and setters
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getBirthdate() { return birthdate; }
    public byte[] getPhotoBlob() {
        // Decode Base64 string into byte array
        return photoBlob != null ? Base64.decode(photoBlob, Base64.DEFAULT) : null;
    }
    public String getCardUserId() { return cardUserId; } // Getter for cardUserId

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
    public void setPhotoBlob(String photoBlob) { this.photoBlob = photoBlob; }
    public void setCardUserId(String cardUserId) {this.cardUserId = cardUserId;} // Setter for cardUserId
}
