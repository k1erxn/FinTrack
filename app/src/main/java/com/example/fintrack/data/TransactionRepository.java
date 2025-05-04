// File: app/src/main/java/com/example/fintrack/data/TransactionRepository.java
package com.example.fintrack.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {
    private final TransactionDao dao;
    private final LiveData<List<Transaction>> allTransactions;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TransactionRepository(Application app) {
        dao = AppDatabase.getInstance(app).transactionDao();  // get DAO
        allTransactions = dao.getAllTransactions();           // live data list
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public void insert(Transaction tx) {
        executor.execute(() -> dao.insert(tx));  // off UI thread
    }

    public void update(Transaction tx) {
        executor.execute(() -> dao.update(tx));
    }

    public void delete(Transaction tx) {
        executor.execute(() -> dao.delete(tx));
    }
}
