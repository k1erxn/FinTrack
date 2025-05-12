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
    private final TransactionRepository repo;         // init repo
    private final LiveData<List<Transaction>> allTransactions;  // live list

    public TransactionViewModel(@NonNull Application app) {
        super(app);
        repo = new TransactionRepository(app)  ;             // init repo
        allTransactions = repo.getAllTransactions()  ;       // live data
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public void insert(Transaction tx) {
        repo.insert(tx);                             // insert tx
    }

    public void update(Transaction tx) {
        repo.update(tx);                                  // update tx
    }

    public void delete(Transaction tx) {
        repo.delete(tx);                                // delete tx
    }

    public LiveData<Transaction> getTransactionById(int id) {
        return repo.getTransactionById(id);                 // fetch by id
    }
}
