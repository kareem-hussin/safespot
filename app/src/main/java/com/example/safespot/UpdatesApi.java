package com.example.safespot;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UpdatesApi {
    @GET("get_updates.php")
    Call<List<Update>> getUpdates(@Query("app_user_id") int appUserId);
}