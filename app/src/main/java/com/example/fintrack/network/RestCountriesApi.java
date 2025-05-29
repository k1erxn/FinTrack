package com.example.fintrack.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RestCountriesApi { // interface to fetch currency data
    @GET("all")
    Call<List<CountryCurrencyResponse>> getCurrencies();
}
