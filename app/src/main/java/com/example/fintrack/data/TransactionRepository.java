package com.example.fintrack.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// repository for transaction data
public class TransactionRepository {
    private final TransactionDao dao;
    private final LiveData<List<Transaction>> allTransactions;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // background executor for db operations

    // initialize repository and dao
    public TransactionRepository(Application app) {
        dao = AppDatabase.getInstance(app).transactionDao(); // get data access object
        allTransactions = dao.getAllTransactions(); // load transactions live
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<Transaction> getTransactionById(int id) {
        return dao.getTransactionById(id);
    }

    public void insert(Transaction tx) {
        executor.execute(() -> dao.insert(tx)); // insert in background
    }

    public void update(Transaction tx) {
        executor.execute(() -> dao.update(tx)); // update in background
    }

    public void delete(Transaction tx) {
        executor.execute(() -> dao.delete(tx)); // delete in background
    }
}
