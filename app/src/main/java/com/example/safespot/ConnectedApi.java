package com.example.safespot;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ConnectedApi {
    @GET("get_connected_users.php")
    Call<List<CardUser>> getConnectedUsers(@Query("app_user_id") int appUserId);
}

