package com.example.safespot;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NotificationsApi {
    @GET("notifications")
    Call<List<com.example.safespot.Notification>> getNotifications(@Query("userId") int userId);
}


