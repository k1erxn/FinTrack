package com.example.fintrack.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.R;
import com.example.fintrack.viewmodel.TransactionViewModel;

public class HistoryFragment extends Fragment {
    private TransactionViewModel vm;
    private TransactionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             ViewGroup container,
                             Bundle saved) {
        View root = inf.inflate(R.layout.fragment_history, container, false);
        RecyclerView rv = root.findViewById(R.id.rvTransactions);
        adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        vm = new ViewModelProvider(this).get(TransactionViewModel.class);
        vm.getAllTransactions().observe(getViewLifecycleOwner(), list -> {
            adapter.setTransactions(list);
        });
        return root;
    }
}
