package com.example.fintrack.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.fintrack.data.Transaction;
import com.example.fintrack.data.TransactionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// viewmodel for managing transaction data
public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepository repo;
    private final LiveData<List<Transaction>> source;
    private final MediatorLiveData<List<Transaction>> allTransactions = new MediatorLiveData<>();

    // initialize repository and mediator live data
    public TransactionViewModel(@NonNull Application app) {
        super(app);
        repo = new TransactionRepository(app);
        source = repo.getAllTransactions();
        allTransactions.addSource(source, allTransactions::setValue);
    }

    // expose live data list of transactions
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    // insert a new transaction in background
    public void insert(Transaction tx) {
        repo.insert(tx);
    }

    // update an existing transaction in background
    public void update(Transaction tx) {
        repo.update(tx);
    }

    // delete a transaction in background
    public void delete(Transaction tx) {
        repo.delete(tx);
    }

    // sort the transactions list by date asc or desc
    public void sortByDate(boolean asc) {
        List<Transaction> list = new ArrayList<>(allTransactions.getValue());
        Collections.sort(list, (a, b) ->
                asc
                        ? Long.compare(a.getDate(), b.getDate())
                        : Long.compare(b.getDate(), a.getDate())
        );
        allTransactions.setValue(list);
    }

    // sort the transactions list by amount asc or desc
    public void sortByAmount(boolean asc) {
        List<Transaction> list = new ArrayList<>(allTransactions.getValue());
        Collections.sort(list, (a, b) ->
                asc
                        ? Double.compare(a.getAmount(), b.getAmount())
                        : Double.compare(b.getAmount(), a.getAmount())
        );
        allTransactions.setValue(list);
    }

    // get a single transaction by its id
    public LiveData<Transaction> getTransactionById(int id) {
        return repo.getTransactionById(id);
    }
}
