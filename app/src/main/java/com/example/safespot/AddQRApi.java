package com.example.safespot;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AddQRApi {
    @FormUrlEncoded
    @POST("addScannedId.php") // Update to match your API endpoint
    Call<GenericResponse> addScannedId(
            @Field("userId") int userId,
            @Field("scannedId") String scannedId
    );
}

