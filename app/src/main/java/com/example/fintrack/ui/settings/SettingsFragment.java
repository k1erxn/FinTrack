package com.example.fintrack.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.fintrack.databinding.FragmentSettingsBinding;
import com.example.fintrack.network.NetworkClient;
import com.example.fintrack.network.RestCountriesApi;
import com.example.fintrack.network.CountryCurrencyResponse;
import com.example.fintrack.network.CurrencyInfo;
import com.example.fintrack.util.CurrencyManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private CurrencyManager cm;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        cm = CurrencyManager.get(requireContext());

        // 1) Create & set an initially-empty adapter on the spinner
        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>()          // start empty
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spCurrency.setAdapter(adapter);

        // Prepare a map to hold code → symbol mappings
        Map<String, String> currencySymbols = new HashMap<>();

        // 2) Fetch currency codes via RestCountriesApi
        RestCountriesApi api = NetworkClient.getRetrofit()
                .create(RestCountriesApi.class);
        api.getCurrencies().enqueue(new Callback<List<CountryCurrencyResponse>>() {
            @Override
            public void onResponse(Call<List<CountryCurrencyResponse>> call,
                                   Response<List<CountryCurrencyResponse>> response) {
                if (binding == null) return;

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(getContext(),
                            "Failed to load currencies",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Extract unique currency codes & fill symbol map
                Set<String> codesSet = new HashSet<>();
                for (CountryCurrencyResponse country : response.body()) {
                    if (country.currencies == null) continue;
                    for (Map.Entry<String, CountryCurrencyResponse.CurrencyInfo> entry : country.currencies.entrySet()) {
                        String code = entry.getKey();
                        CountryCurrencyResponse.CurrencyInfo info = entry.getValue();
                        if (info.symbol != null && !codesSet.contains(code)) {
                            codesSet.add(code);
                            currencySymbols.put(code, info.symbol);
                        }
                    }
                }

                // Sort and push into spinner adapter
                List<String> sorted = new ArrayList<>(codesSet);
                java.util.Collections.sort(sorted);
                adapter.clear();
                adapter.addAll(sorted);
                adapter.notifyDataSetChanged();

                // Restore saved selection
                int savedPos = adapter.getPosition(cm.getCurrencyCode());
                if (savedPos >= 0) {
                    binding.spCurrency.setSelection(savedPos);
                }
            }

            @Override
            public void onFailure(Call<List<CountryCurrencyResponse>> call,
                                  Throwable t) {
                Toast.makeText(getContext(),
                        "Failed to load currencies",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 3) When user picks a new currency, save both code and symbol
        binding.spCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onNothingSelected(AdapterView<?> parent) { }
            @Override public void onItemSelected(AdapterView<?> parent,
                                                 View view,
                                                 int pos,
                                                 long id) {
                String code = adapter.getItem(pos);
                cm.setCurrencyCode(code);
                String sym = currencySymbols.get(code);
                if (sym != null) {
                    cm.setCurrencySymbol(sym);
                }
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

        // App version dialog
        binding.btnAppVersion.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("App Version")
                        .setMessage("FinTrack v1.0\nThe key to managing your finances wisely.")
                        .setPositiveButton("OK", null)
                        .show()
        );

        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
