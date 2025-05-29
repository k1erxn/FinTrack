package com.example.fintrack.ui.history;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fintrack.data.Transaction;
import com.example.fintrack.databinding.FragmentAddTransactionBinding;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.Arrays;

public class AddTransactionFragment extends Fragment {
    private static final int REQ_ATTACH_PHOTO = 9876;

    private FragmentAddTransactionBinding binding;
    private TransactionViewModel viewModel;
    private long selectedDate = System.currentTimeMillis();
    private Uri attachedPhotoUri = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // initialize view model for transactions
        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);

        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);

        // category spinner setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("Food", "Rent", "Salary", "Other")
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCategory.setAdapter(adapter);

        // date picker setup
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

        // attach photo intent
        binding.btnAttachPhoto.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pick.setType("image/*");
            startActivityForResult(pick, REQ_ATTACH_PHOTO);
        });

        // save transaction logic
        binding.btnSave.setOnClickListener(v -> {
            String amtText = binding.etAmount.getText().toString().trim();
            if (amtText.isEmpty() || Double.parseDouble(amtText) <= 0) {
                binding.tilAmount.setError("Enter valid amount");
                return;
            }
            binding.tilAmount.setError(null);

            double amount = Double.parseDouble(amtText);
            String category = (String) binding.spCategory.getSelectedItem();
            String type = category.equalsIgnoreCase("salary")
                    ? "income"
                    : "expense";

            Transaction tx = new Transaction(
                    amount,
                    selectedDate,
                    category,
                    type,
                    binding.etDescription.getText().toString().trim()
            );

            // include photo uri if attached
            if (attachedPhotoUri != null) {
                tx.setPhotoUri(attachedPhotoUri.toString());
            }

            viewModel.insert(tx);
            requireActivity().onBackPressed();
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // handle photo selection result
        if (requestCode == REQ_ATTACH_PHOTO
                && resultCode == Activity.RESULT_OK
                && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                attachedPhotoUri = uri;
                binding.ivPhotoPreview.setImageURI(uri);
                binding.ivPhotoPreview.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
