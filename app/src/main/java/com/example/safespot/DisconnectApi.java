package com.example.safespot;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface DisconnectApi {
    @FormUrlEncoded
    @POST("delete_user_card.php")
    Call<GenericResponse> deleteUserCardLink(
            @Field("app_user_id") int appUserId,
            @Field("card_user_id") String cardUserId
    );
}
