package com.example.fintrack.network;

import java.util.Map;

// response object for currency data
public class CountryCurrencyResponse {
    // map currency code to currency info
    public Map<String, CurrencyInfo> currencies;

    // holds name and symbol for currency
    public static class CurrencyInfo {
        // currency full name
        public String name;
        // currency symbol sign
        public String symbol;
    }
}
