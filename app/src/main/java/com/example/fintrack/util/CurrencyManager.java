package com.example.fintrack.util;
import android.content.Context;
import android.content.SharedPreferences;

public class CurrencyManager {
    private static final String PREFS = "fintrack_prefs";
    private static final String KEY_CURRENCY = "key_currency";
    private static CurrencyManager instance;
    private final SharedPreferences prefs;

    private CurrencyManager(Context ctx){
        prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized CurrencyManager get(Context ctx){
        if(instance == null) instance = new CurrencyManager(ctx);
        return instance;
    }

    public void setCurrencyCode(String code){
        prefs.edit().putString(KEY_CURRENCY, code).apply();
    }

    public String getCurrencyCode(){
        return prefs.getString(KEY_CURRENCY, "USD");
    }

    public String getCurrencySymbol(){
        switch(getCurrencyCode()){
            case "EUR": return "€";
            case "GBP": return "£";
            case "USD":
            default:    return "$";
        }
    }
}
