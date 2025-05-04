// File: app/src/main/java/com/example/fintrack/data/AppDatabase.java
package com.example.fintrack.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Transaction.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE; // singleton instance

    public abstract TransactionDao transactionDao(); // DAO access

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fintrack_db"
                            )
                            .fallbackToDestructiveMigration() // reset on schema change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
