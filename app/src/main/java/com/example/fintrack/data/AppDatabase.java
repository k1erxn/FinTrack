package com.example.fintrack.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

// define database with transactions entity version two export schema false
@Database(entities = {Transaction.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    // get dao to access transactions
    public abstract TransactionDao transactionDao();

    // migration adds photo uri column
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // run sql to add photo uri column to table
            db.execSQL(
                    "ALTER TABLE `transactions` ADD COLUMN `photo_uri` TEXT"
            );
        }
    };

    // get or create database instance
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            // ensure single instance thread safe
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // build database with migration fallback to destructive migration fallback on downgrade
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "fintrack_db")
                            .addMigrations(MIGRATION_1_2)
                            .fallbackToDestructiveMigration()
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
