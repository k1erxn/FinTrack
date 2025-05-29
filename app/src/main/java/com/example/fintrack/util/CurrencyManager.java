package com.example.fintrack.util;

import android.content.Context;
import android.content.SharedPreferences;

public class CurrencyManager {
    private static final String PREFS = "fintrack_prefs";
    private static final String KEY_CURRENCY_CODE   = "key_currency_code";
    private static final String KEY_CURRENCY_SYMBOL = "key_currency_symbol";

    private static CurrencyManager instance;
    private final SharedPreferences prefs;

    private CurrencyManager(Context ctx) {
        prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized CurrencyManager get(Context ctx) {
        if (instance == null) {
            instance = new CurrencyManager(ctx);
        }
        return instance;
    }

    /** Save the three‐letter currency code, e.g. "GBP" or "XYZ". */
    public void setCurrencyCode(String code) {
        prefs.edit()
                .putString(KEY_CURRENCY_CODE, code)
                .apply();
    }

    /** Retrieve the saved code, defaulting to "USD" if none. */
    public String getCurrencyCode() {
        return prefs.getString(KEY_CURRENCY_CODE, "USD");
    }

    /**
     * Save the symbol (or emoji) you want to display, e.g. "£" or "¤".
     * If you never call this, getCurrencySymbol() will return the code instead.
     */
    public void setCurrencySymbol(String symbol) {
        prefs.edit()
                .putString(KEY_CURRENCY_SYMBOL, symbol)
                .apply();
    }

    /**
     * Return the saved symbol if present; otherwise fall back to the code itself.
     * That way, unknown codes still display meaningfully.
     */
    public String getCurrencySymbol() {
        String sym = prefs.getString(KEY_CURRENCY_SYMBOL, null);
        if (sym != null && !sym.isEmpty()) {
            return sym;
        }
        // no symbol saved → show the code
        return getCurrencyCode();
    }
}
