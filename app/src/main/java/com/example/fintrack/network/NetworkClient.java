package com.example.fintrack.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// client to provide retrofit instance
public class NetworkClient {
    private static final String BASE_URL = "https://restcountries.com/v3.1/"; // api base url

    // retrofit configured with base url and gson converter
    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    // return the singleton retrofit instance
    public static Retrofit getRetrofit() {
        return retrofit;
    }
}
