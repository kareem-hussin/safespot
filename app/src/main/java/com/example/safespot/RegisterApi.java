package com.example.safespot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RegisterApi {

    @POST("register.php")  // The endpoint of your server-side script
    Call<RegisterResponse> registerUser(@Body RegisterData data);
}
