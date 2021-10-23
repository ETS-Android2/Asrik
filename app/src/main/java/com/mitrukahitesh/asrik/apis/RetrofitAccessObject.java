package com.mitrukahitesh.asrik.apis;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAccessObject {
    private static CustomRetrofit customRetrofit;
    private static final String BASE_URL = "https://asrik-server.herokuapp.com/";

    private RetrofitAccessObject() {
    }

    public static CustomRetrofit getRetrofitAccessObject() {
        if (customRetrofit == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            customRetrofit = retrofit.create(CustomRetrofit.class);
        }
        return customRetrofit;
    }

}
