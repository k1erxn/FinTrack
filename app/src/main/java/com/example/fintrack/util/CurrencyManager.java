package com.example.fintrack.util;

import android.content.Context;
import android.content.SharedPreferences;

public class CurrencyManager {
    private static final String PREFS = "fintrack_prefs";
    private static final String KEY_CURRENCY_CODE   = "key_currency_code";
    private static final String KEY_CURRENCY_SYMBOL = "key_currency_symbol";

    private static CurrencyManager instance;
    private final SharedPreferences prefs;

    // singleton constructor uses application prefs
    private CurrencyManager(Context ctx) {
        prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // get single instance of manager
    public static synchronized CurrencyManager get(Context ctx) {
        if (instance == null) {
            instance = new CurrencyManager(ctx);
        }
        return instance;
    }

    // save the currency code used for display
    public void setCurrencyCode(String code) {
        prefs.edit()
                .putString(KEY_CURRENCY_CODE, code)
                .apply();
    }

    // retrieve saved currency code default USD
    public String getCurrencyCode() {
        return prefs.getString(KEY_CURRENCY_CODE, "USD");
    }

    // save the currency symbol to use in UI
    public void setCurrencySymbol(String symbol) {
        prefs.edit()
                .putString(KEY_CURRENCY_SYMBOL, symbol)
                .apply();
    }

    // get saved symbol or fallback to code if missing
    public String getCurrencySymbol() {
        String sym = prefs.getString(KEY_CURRENCY_SYMBOL, null);
        if (sym != null && !sym.isEmpty()) {
            return sym;
        }
        // fallback to showing code when no symbol
        return getCurrencyCode();
    }
}
