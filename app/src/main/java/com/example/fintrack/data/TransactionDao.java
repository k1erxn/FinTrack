// File: app/src/main/java/com/example/fintrack/data/TransactionDao.java
package com.example.fintrack.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao // mark as DAO
public interface TransactionDao {
    @Insert // add new record
    long insert(Transaction transaction);

    @Update // update existing
    int update(Transaction transaction);

    @Delete // delete record
    int delete(Transaction transaction);

    @Query("SELECT * FROM transactions ORDER BY date DESC") // live list sorted by newest
    LiveData<List<Transaction>> getAllTransactions();
}
