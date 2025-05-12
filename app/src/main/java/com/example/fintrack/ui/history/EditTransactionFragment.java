package com.example.fintrack.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fintrack.data.Transaction;
import com.example.fintrack.databinding.FragmentEditTransactionBinding;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import android.widget.ArrayAdapter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

public class EditTransactionFragment extends Fragment {
    private FragmentEditTransactionBinding binding;
    private TransactionViewModel viewModel;
    private int txId;
    private long selectedDate;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditTransactionBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // read transaction id from args
        txId = getArguments().getInt("transactionId");

        // prepare the date picker for manual selection
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .<Long>datePicker()
                .setSelection(System.currentTimeMillis())
                .build();
        binding.btnPickDate.setOnClickListener(v ->
                picker.show(getParentFragmentManager(), "dp")
        );
        picker.addOnPositiveButtonClickListener(sel -> {
            selectedDate = sel;
            binding.btnPickDate.setText(sdf.format(selectedDate));
        });

        // load the transaction and prefill form
        viewModel.getTransactionById(txId).observe(getViewLifecycleOwner(), tx -> {
            if (tx == null) return;
            // amount
            binding.etAmount.setText(String.valueOf(tx.getAmount()));
            // date
            selectedDate = tx.getDate();
            binding.btnPickDate.setText(sdf.format(selectedDate));
            // category spinner
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    Arrays.asList("Food","Rent","Salary","Other")
            );
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spCategory.setAdapter(catAdapter);
            binding.spCategory.setSelection(
                    catAdapter.getPosition(tx.getCategory())
            );
        });

        // update
        binding.btnUpdate.setOnClickListener(v -> {
            double amt = Double.parseDouble(binding.etAmount.getText().toString().trim());
            String cat = (String) binding.spCategory.getSelectedItem();
            Transaction updated = new Transaction(amt, selectedDate, cat, "expense");
            updated.setId(txId);
            viewModel.update(updated);
            NavHostFragment.findNavController(this).popBackStack();
        });

        // delete
        binding.btnDelete.setOnClickListener(v -> {
            viewModel.getTransactionById(txId).observe(getViewLifecycleOwner(), tx -> {
                if (tx != null) viewModel.delete(tx);
                NavHostFragment.findNavController(this).popBackStack();
            });
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
