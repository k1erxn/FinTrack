package com.example.fintrack.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fintrack.databinding.FragmentSettingsBinding;
import com.example.fintrack.util.CurrencyManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private CurrencyManager cm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        cm = CurrencyManager.get(requireContext());

        // currency dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                Arrays.asList("USD","EUR","GBP")
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCurrency.setAdapter(adapter);

        // pre select saved currency
        int savedPos = adapter.getPosition(cm.getCurrencyCode());
        if (savedPos >= 0) binding.spCurrency.setSelection(savedPos);

        // when user picks a new currency, save it
        binding.spCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) { }
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String code = (String) parent.getItemAtPosition(pos);
                cm.setCurrencyCode(code);
            }
        });

        // Contact Us opens email app
        binding.btnContactUs.setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:"));
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@fintrack.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "FinTrack Feedback");
            startActivity(Intent.createChooser(email, "Send feedback"));
        });

        // App version
        binding.btnAppVersion.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("App Version")
                    .setMessage("FinTrack v1.0\nYour companion for smarter finances.")
                    .setPositiveButton("OK", null)
                    .show();
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
