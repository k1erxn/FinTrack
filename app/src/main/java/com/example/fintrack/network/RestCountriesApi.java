// com/example/fintrack/network/RestCountriesApi.java
package com.example.fintrack.network;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RestCountriesApi {
    // fetch all countries; we'll only read their "currencies" field
    @GET("all")
    Call<List<CountryCurrencyResponse>> getCurrencies();
}
