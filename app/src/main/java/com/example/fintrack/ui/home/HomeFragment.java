package com.example.fintrack.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.fintrack.R;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.fintrack.data.Transaction;
import com.example.fintrack.databinding.FragmentHomeBinding;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.example.fintrack.util.SimpleItemSelectedListener;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private TransactionViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.fabAdd.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigation_home_to_addTransactionFragment)
        );


        // month selector spinner
        Spinner spMonth = binding.spMonth;
        String[] months = new DateFormatSymbols(Locale.getDefault()).getMonths();
        List<String> monthList = new ArrayList<>();
        for (int i = 0; i < 12; i++) monthList.add(months[i]);
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                monthList
        );
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(monthAdapter);
        spMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        // pie chart and summary cards
        PieChart pieChart = binding.pieChart;
        TextView tvIncome = binding.tvIncome;
        TextView tvExpenses = binding.tvExpenses;

        // viewmodel to get transactions
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), list -> {
            updateDashboard(list);
        });

        // update when month changes
        spMonth.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                updateDashboard(viewModel.getAllTransactions().getValue());
            }
        });


        return root;
    }

    private void updateDashboard(List<Transaction> allTx) {
        if (allTx == null) return;

        // refs
        int month = binding.spMonth.getSelectedItemPosition();
        TextView tvIncome = binding.tvIncome;
        TextView tvExpenses = binding.tvExpenses;
        PieChart pieChart = binding.pieChart;

        // calculate totals
        double inc = 0, exp = 0;
        Map<String, Float> sums = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        for (Transaction t : allTx) {
            cal.setTimeInMillis(t.getDate());
            if (cal.get(Calendar.MONTH) != month) continue;
            if ("income".equalsIgnoreCase(t.getType())) {
                inc += t.getAmount();
            } else {
                exp += t.getAmount();
                String cat = t.getCategory();
                sums.put(cat, sums.getOrDefault(cat, 0f) + (float)t.getAmount());
            }
        }

        // update cards
        tvIncome.setText(String.format(Locale.getDefault(), "Income\n€%.2f", inc));
        tvExpenses.setText(String.format(Locale.getDefault(), "Expenses\n€%.2f", exp));

        // build pie entries
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> e : sums.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
        }

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setSliceSpace(3f);
        ds.setSelectionShift(5f);
        ds.setColors(
                getResources().getColor(R.color.pie_red),
                getResources().getColor(R.color.pie_blue),
                getResources().getColor(R.color.pie_green),
                getResources().getColor(R.color.pie_yellow),
                getResources().getColor(R.color.pie_purple)
        );

        PieData data = new PieData(ds);
        data.setValueTextSize(12f);
        data.setValueTextColor(getResources().getColor(android.R.color.white));

        pieChart.setData(data);
        pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
