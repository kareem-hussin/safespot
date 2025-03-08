package com.example.safespot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoginApi {
    @POST("login.php")
    Call<LoginResponse> loginUser(@Body LoginData data);  // Assuming LoginData is your request model
}


