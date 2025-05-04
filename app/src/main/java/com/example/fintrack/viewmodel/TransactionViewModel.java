// File: app/src/main/java/com/example/fintrack/viewmodel/TransactionViewModel.java
package com.example.fintrack.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.fintrack.data.Transaction;
import com.example.fintrack.data.TransactionRepository;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepository repo;
    private final LiveData<List<Transaction>> allTransactions;

    public TransactionViewModel(@NonNull Application app) {
        super(app);
        repo = new TransactionRepository(app);               // init repo
        allTransactions = repo.getAllTransactions();         // live data
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public void insert(Transaction tx) {
        repo.insert(tx);
    }

    public void update(Transaction tx) {
        repo.update(tx);
    }

    public void delete(Transaction tx) {
        repo.delete(tx);
    }
}
