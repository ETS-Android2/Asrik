package com.mitrukahitesh.asrik.apis;

import com.mitrukahitesh.asrik.models.BotReply;
import com.mitrukahitesh.asrik.models.PinCodeDetails;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CustomRetrofit {

    @GET("/pincode/{code}")
    Call<PinCodeDetails> getPinCodeDetails(@Path(value = "code", encoded = true) String code);

    @POST("/new_request/{code}")
    Call<String> notifyAdmin(@Path(value = "code", encoded = true) String code, @Body JSONObject object);

    @POST("/request_verified/{code}/{seeker}")
    Call<String> notifyUsersForRequestVerified(@Path(value = "code", encoded = true) String code, @Path(value = "seeker", encoded = true) String seeker, @Body JSONObject object);

    @POST("/request_rejected/{seeker}")
    Call<String> notifyRequestRejection(@Path(value = "seeker", encoded = true) String seeker, @Body JSONObject object);

    @POST("/blood_camp/{code}/{id}")
    Call<String> notifyBloodCamp(@Path(value = "code", encoded = true) String code, @Path(value = "id", encoded = true) String id, @Body JSONObject object);

    @POST("/new_message/{receiver}")
    Call<String> notifyNewMessage(@Path(value = "receiver", encoded = true) String receiver, @Body JSONObject body);

    @GET("/bot/{message}")
    Call<BotReply> getBotReply(@Path(value = "message", encoded = true) String message);
}
