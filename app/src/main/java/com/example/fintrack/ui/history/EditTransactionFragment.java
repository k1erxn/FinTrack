package com.example.fintrack.ui.history;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.fintrack.data.Transaction;
import com.example.fintrack.databinding.FragmentEditTransactionBinding;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

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

    private Transaction currentTx;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // initialize view binding and view model
        binding = FragmentEditTransactionBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        txId = getArguments().getInt("transactionId");

        // setup category dropdown
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("Food", "Rent", "Salary", "Other")
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCategory.setAdapter(catAdapter);

        // setup date picker button
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder
                .<Long>datePicker()
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        binding.btnPickDate.setOnClickListener(v ->
                picker.show(getParentFragmentManager(), "DATE_PICKER")
        );
        picker.addOnPositiveButtonClickListener(sel -> {
            selectedDate = sel;
            binding.btnPickDate.setText(sdf.format(selectedDate));
        });

        // load and display existing transaction data
        viewModel.getTransactionById(txId)
                .observe(getViewLifecycleOwner(), tx -> {
                    if (tx == null) return;
                    currentTx = tx;

                    binding.etAmount.setText(String.valueOf(tx.getAmount()));
                    selectedDate = tx.getDate();
                    binding.btnPickDate.setText(sdf.format(selectedDate));
                    binding.etDescription.setText(tx.getDescription());
                    int pos = catAdapter.getPosition(tx.getCategory());
                    if (pos >= 0) binding.spCategory.setSelection(pos);

                    String photoUri = tx.getPhotoUri();
                    if (!TextUtils.isEmpty(photoUri)) {
                        binding.ivPhotoPreview.setVisibility(View.VISIBLE);
                        Glide.with(this)
                                .load(Uri.parse(photoUri))
                                .centerCrop()
                                .into(binding.ivPhotoPreview);
                    }
                });

        // handle update action
        binding.btnUpdate.setOnClickListener(v -> {
            String amtText = binding.etAmount.getText().toString().trim();
            double amt = Double.parseDouble(amtText);
            String cat = (String) binding.spCategory.getSelectedItem();
            String desc = binding.etDescription.getText().toString().trim();

            Transaction updated = new Transaction(
                    amt,
                    selectedDate,
                    cat,
                    cat.equalsIgnoreCase("salary") ? "income" : "expense",
                    desc
            );
            updated.setId(txId);

            if (currentTx != null && currentTx.getPhotoUri() != null) {
                updated.setPhotoUri(currentTx.getPhotoUri());
            }

            viewModel.update(updated);
            NavHostFragment.findNavController(this).popBackStack();
        });

        // handle delete action
        binding.btnDelete.setOnClickListener(v ->
                viewModel.getTransactionById(txId).observe(getViewLifecycleOwner(), tx -> {
                    if (tx != null) viewModel.delete(tx);
                    NavHostFragment.findNavController(this).popBackStack();
                })
        );

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
