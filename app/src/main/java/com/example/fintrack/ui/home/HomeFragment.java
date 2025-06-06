package com.example.fintrack.ui.home;

import android.graphics.Color;
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
import com.example.fintrack.databinding.FragmentHomeBinding;
import com.example.fintrack.util.CurrencyManager;
import com.example.fintrack.util.SimpleItemSelectedListener;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// fragment for home overview
public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private TransactionViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate view binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // navigate to add transaction screen
        binding.fabAdd.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigation_home_to_addTransactionFragment)
        );

        // display current month label
        String[] shortMonths = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
        String currentMonth = shortMonths[Calendar.getInstance().get(Calendar.MONTH)];
        binding.tvCurrentMonth.setText(currentMonth);

        // show todays date as title
        String today = new SimpleDateFormat("MMMM d yyyy", Locale.getDefault())
                .format(new Date());
        binding.tvOverviewTitle.setText(today);

        // setup breakdown month spinner
        String[] fullMonths = new DateFormatSymbols(Locale.getDefault()).getMonths();
        List<String> breakdownMonths = new ArrayList<>();
        for (int i = 0; i < 12; i++) breakdownMonths.add(fullMonths[i]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                breakdownMonths
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSummary.setAdapter(adapter);
        binding.spinnerSummary.setSelection(Calendar.getInstance().get(Calendar.MONTH), false);
        binding.spinnerSummary.setOnItemSelectedListener(new SimpleItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                // update dashboard pie based on spinner selection
                updateDashboard(viewModel.getAllTransactions().getValue());
            }
        });

        // initialize view model and observe transaction data
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), list -> {
            if (list == null) return;

            // recalc overview section for current month
            initializeOverview(list, currentMonth);

            // rebuild main and mini pie charts
            updateDashboard(list);
            updateMiniPie(list);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // prevent memory leak
    }

    // calculate and display income and expenses overview
    private void initializeOverview(List<Transaction> allTx, String targetMonth) {
        double inc = 0, exp = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("MMM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        for (Transaction t : allTx) {
            cal.setTimeInMillis(t.getDate());
            if (!fmt.format(cal.getTime()).equals(targetMonth)) continue;
            if ("income".equalsIgnoreCase(t.getType())) inc += t.getAmount();
            else exp += t.getAmount();
        }
        CurrencyManager cm = CurrencyManager.get(requireContext());
        binding.tvIncomeOverView.setText(String.format("Income\n%s%.2f", cm.getCurrencySymbol(), inc));
        binding.tvExpensesOverview.setText(String.format("Expenses\n%s%.2f", cm.getCurrencySymbol(), exp));
    }

    // rebuild breakdown pie chart for selected month
    private void updateDashboard(List<Transaction> allTx) {
        if (allTx == null) return;

        int monthIndex = binding.spinnerSummary.getSelectedItemPosition();
        String[] shortMonths = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
        String selMonth = shortMonths[monthIndex];

        Map<String, Float> sums = new HashMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("MMM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        for (Transaction t : allTx) {
            cal.setTimeInMillis(t.getDate());
            if (!fmt.format(cal.getTime()).equals(selMonth)) continue;
            if (!"income".equalsIgnoreCase(t.getType())) {
                float amt = (float) t.getAmount();
                sums.put(t.getCategory(),
                        sums.getOrDefault(t.getCategory(), 0f) + amt);
            }
        }

        // prepare pie entries for categories
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

        PieChart pie = binding.pieChart;
        pie.getDescription().setEnabled(false);
        Legend legend = pie.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);

        pie.setData(data);
        pie.invalidate();
    }

    // build mini pie for income vs expenses for current month
    private void updateMiniPie(List<Transaction> allTx) {
        if (allTx == null) return;

        String[] shortMonths = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
        String curr = shortMonths[Calendar.getInstance().get(Calendar.MONTH)];

        float totalIn = 0f, totalOut = 0f;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("MMM", Locale.getDefault());
        for (Transaction t : allTx) {
            cal.setTimeInMillis(t.getDate());
            if (!fmt.format(cal.getTime()).equals(curr)) continue;
            float amt = (float) t.getAmount();
            if ("income".equalsIgnoreCase(t.getType())) totalIn += amt;
            else totalOut += amt;
        }

        // prepare entries for mini pie
        List<PieEntry> entries = new ArrayList<>();
        if (totalIn > 0)  entries.add(new PieEntry(totalIn, ""));
        if (totalOut > 0) entries.add(new PieEntry(totalOut, ""));

        PieDataSet ds = new PieDataSet(entries, "");
        ds.setSliceSpace(2f);
        ds.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
        ds.setDrawValues(false);

        PieChart miniPie = binding.miniPieChart;
        miniPie.getDescription().setEnabled(false);
        miniPie.getLegend().setEnabled(false);
        miniPie.setData(new PieData(ds));
        miniPie.invalidate();
    }
}
