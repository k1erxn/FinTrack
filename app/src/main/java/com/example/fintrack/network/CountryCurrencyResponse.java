// com/example/fintrack/network/CountryCurrencyResponse.java
package com.example.fintrack.network;

import java.util.Map;

public class CountryCurrencyResponse {
    // matches the JSON structure at restcountries.com/v3.1/all
    public Map<String, CurrencyInfo> currencies;

    public static class CurrencyInfo {
        public String name;
        public String symbol;
    }
}
