/*
    Interface to be implemented by retrofit to make various
    HTTP requests
 */

package com.mitrukahitesh.asrik.apis;

import com.mitrukahitesh.asrik.models.chat.BotReply;
import com.mitrukahitesh.asrik.models.pin.PinCodeDetails;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CustomRetrofit {

    /**
     * Get Postal Code details
     */
    @GET("/pincode/{code}")
    Call<PinCodeDetails> getPinCodeDetails(@Path(value = "code", encoded = true) String code);

    /**
     * Notify admin when new request is posted
     */
    @POST("/new_request/{code}")
    Call<String> notifyAdmin(@Path(value = "code", encoded = true) String code, @Body JSONObject object);

    /**
     * Notify users when admin verifies request
     */
    @POST("/request_verified/{code}/{seeker}")
    Call<String> notifyUsersForRequestVerified(@Path(value = "code", encoded = true) String code, @Path(value = "seeker", encoded = true) String seeker, @Body JSONObject object);

    /**
     * Notify users when admin rejects request
     */
    @POST("/request_rejected/{seeker}")
    Call<String> notifyRequestRejection(@Path(value = "seeker", encoded = true) String seeker, @Body JSONObject object);

    /**
     * Notify users about new blood donation camp
     */
    @POST("/blood_camp/{code}/{id}")
    Call<String> notifyBloodCamp(@Path(value = "code", encoded = true) String code, @Path(value = "id", encoded = true) String id, @Body JSONObject object);

    /**
     * Notify user when he/she receives new message
     */
    @POST("/new_message/{receiver}")
    Call<String> notifyNewMessage(@Path(value = "receiver", encoded = true) String receiver, @Body JSONObject body);

    /**
     * Send message to bot and get its reply
     */
    @GET("/bot/{message}")
    Call<BotReply> getBotReply(@Path(value = "message", encoded = true) String message);
}
