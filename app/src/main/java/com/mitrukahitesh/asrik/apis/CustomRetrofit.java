package com.mitrukahitesh.asrik.apis;

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
}
