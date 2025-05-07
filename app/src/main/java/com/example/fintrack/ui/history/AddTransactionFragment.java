// File: app/src/main/java/com/example/fintrack/ui/history/AddTransactionFragment.java
package com.example.fintrack.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fintrack.R;
import com.example.fintrack.data.Transaction;
import com.example.fintrack.databinding.FragmentAddTransactionBinding;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Arrays;

public class AddTransactionFragment extends Fragment {
    private FragmentAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private long selectedDate = System.currentTimeMillis();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // set up category spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("Food", "Rent", "Salary", "Other")
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCategory.setAdapter(adapter);

        // date picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .<Long>datePicker()
                .setSelection(selectedDate)
                .build();
        binding.btnPickDate.setOnClickListener(v ->
                datePicker.show(getParentFragmentManager(), "DATE_PICKER")
        );
        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = selection;
            binding.btnPickDate.setText(datePicker.getHeaderText());
        });

        // save and navigate to history
        binding.btnSave.setOnClickListener(v -> {
            String amtText = binding.etAmount.getText().toString().trim();
            if (amtText.isEmpty() || Double.parseDouble(amtText) <= 0) {
                binding.tilAmount.setError("enter valid amount");
                return;
            }
            binding.tilAmount.setError(null);

            double amount = Double.parseDouble(amtText);
            String category = (String) binding.spCategory.getSelectedItem();
            Transaction tx = new Transaction(amount, selectedDate, category, "expense");
            viewModel.insert(tx);

            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_addTransactionFragment_to_historyFragment);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
