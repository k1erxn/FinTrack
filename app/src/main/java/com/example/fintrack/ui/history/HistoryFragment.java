package com.example.fintrack.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.data.Transaction;
import com.example.fintrack.viewmodel.TransactionViewModel;

public class HistoryFragment extends Fragment {
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView rv = root.findViewById(R.id.rvTransactions);

        // set up RecyclerView
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        // observe data
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), list ->
                adapter.setTransactions(list)
        );

        // edit button click
        adapter.setOnEditClickListener(tx -> {
            Bundle args = new Bundle();
            args.putInt("transactionId", tx.getId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_historyFragment_to_editTransactionFragment, args);
        });

        // swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                Transaction tx = adapter.getTransactionAt(pos);
                viewModel.delete(tx);
            }
        }).attachToRecyclerView(rv);

        return root;
    }
}
