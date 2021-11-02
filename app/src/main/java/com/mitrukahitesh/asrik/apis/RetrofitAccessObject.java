package com.mitrukahitesh.asrik.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAccessObject {
    private static CustomRetrofit customRetrofit;
    private static final String BASE_URL = "http://192.168.1.68:3000/";

    private RetrofitAccessObject() {
    }

    public static CustomRetrofit getRetrofitAccessObject() {
        if (customRetrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            customRetrofit = retrofit.create(CustomRetrofit.class);
        }
        return customRetrofit;
    }

}
