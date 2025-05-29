package com.example.fintrack.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction); // add new transaction

    @Update
    int update(Transaction transaction); // update existing transaction

    @Delete
    int delete(Transaction transaction); // remove a transaction

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    LiveData<List<Transaction>> getAllTransactions(); // all transactions newest first

    @Query("SELECT * FROM transactions WHERE id = :id")
    LiveData<Transaction> getTransactionById(int id); // fetch one by its id
}
