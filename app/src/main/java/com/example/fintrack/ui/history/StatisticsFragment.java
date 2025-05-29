package com.example.fintrack.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fintrack.data.Transaction;
import com.example.fintrack.databinding.FragmentStatisticsBinding;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// fragment showing transaction statistics chart
public class StatisticsFragment extends Fragment {
    private FragmentStatisticsBinding binding;
    private TransactionViewModel viewModel;
    private List<Transaction> allTx;
    private int offsetMonths = 1;
    private static final int WINDOW = 4;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        BarChart chart = binding.barChart;

        setupChartAppearance(chart); // configure chart look
        setupSummarySpinner();       // prepare month selector

        binding.btnPrev.setOnClickListener(v -> {
            offsetMonths += WINDOW;
            updateChart(chart);      // show previous period
        });
        binding.btnNext.setOnClickListener(v -> {
            if (offsetMonths >= WINDOW) {
                offsetMonths -= WINDOW;
                updateChart(chart);  // show next period
            }
        });

        // get live data of transactions then draw chart
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), list -> {
            allTx = list;
            updateChart(chart);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid memory leak
    }

    // set chart grid axes legend and interaction settings
    private void setupChartAppearance(BarChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setEnabled(false);
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);
        x.setAvoidFirstLastClipping(true);
    }

    // calculate sums by month then update bar entries axis labels and range
    private void updateChart(BarChart chart) {
        if (allTx == null) return;
        Calendar cal = Calendar.getInstance();
        String[] labels = new String[WINDOW];
        Map<String, Float> incMap = new LinkedHashMap<>();
        Map<String, Float> expMap = new LinkedHashMap<>();

        // initialise maps with month labels for window
        for (int i = WINDOW - 1; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.MONTH, -i - offsetMonths);
            String m = new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime());
            labels[WINDOW - 1 - i] = m;
            incMap.put(m, 0f);
            expMap.put(m, 0f);
        }
        // accumulate income and expense per month
        for (Transaction t : allTx) {
            cal.setTimeInMillis(t.getDate());
            String m = new SimpleDateFormat("MMM", Locale.getDefault()).format(cal.getTime());
            if (incMap.containsKey(m)) {
                float amt = (float) t.getAmount();
                if ("income".equalsIgnoreCase(t.getType()))
                    incMap.put(m, incMap.get(m) + amt);
                else
                    expMap.put(m, expMap.get(m) + amt);
            }
        }
        List<BarEntry> inE = new ArrayList<>();
        List<BarEntry> exE = new ArrayList<>();
        for (int i = 0; i < WINDOW; i++) {
            inE.add(new BarEntry(i, incMap.get(labels[i])));
            exE.add(new BarEntry(i, expMap.get(labels[i])));
        }

        binding.tvPeriodRange.setText(labels[0] + " – " + labels[WINDOW - 1]);
        binding.btnNext.setEnabled(offsetMonths >= WINDOW);

        // prepare data sets and assign to chart
        BarDataSet dsI = new BarDataSet(inE, ""); dsI.setColor(Color.parseColor("#4CAF50"));
        BarDataSet dsE = new BarDataSet(exE, ""); dsE.setColor(Color.parseColor("#F44336"));
        dsI.setDrawValues(false);
        dsE.setDrawValues(false);
        BarData data = new BarData(dsI, dsE); data.setBarWidth(0.3f);
        chart.setData(data);

        // adjust axis range to max value
        float max = 0f;
        for (BarEntry e : inE)   max = Math.max(max, e.getY());
        for (BarEntry e : exE)   max = Math.max(max, e.getY());
        YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(0f);
        left.setAxisMaximum(max);
        left.setLabelCount(2, true);

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.groupBars(-0.35f, 0.25f, 0.04f);
        chart.invalidate();
    }

    // spinner to show summary stats for selected month
    private void setupSummarySpinner() {
        String[] months = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
        List<String> monthList = new ArrayList<>();
        for (int i = 0; i < 12; i++) monthList.add(months[i]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, monthList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSummary.setAdapter(adapter);
        binding.spinnerSummary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String selMonth = monthList.get(pos);

                // compute cash in and out for selected month
                float totalIn = 0f, totalOut = 0f;
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat fmt = new SimpleDateFormat("MMM", Locale.getDefault());
                for (Transaction t : allTx) {
                    cal.setTimeInMillis(t.getDate());
                    if (selMonth.equals(fmt.format(cal.getTime()))) {
                        float amt = (float) t.getAmount();
                        if ("income".equalsIgnoreCase(t.getType())) totalIn += amt;
                        else totalOut += amt;
                    }
                }
                binding.tvCashIn.setText(String.format(Locale.getDefault(), "+€%.2f", totalIn));
                binding.tvCashOut.setText(String.format(Locale.getDefault(), "-€%.2f", totalOut));

                // compute average monthly savings over all months
                String[] allMonths = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
                Map<String, Float> monthSavings = new LinkedHashMap<>();
                for (String m : allMonths) {
                    monthSavings.put(m, 0f);
                }
                for (Transaction t : allTx) {
                    cal.setTimeInMillis(t.getDate());
                    String m = fmt.format(cal.getTime());
                    if (monthSavings.containsKey(m)) {
                        float prev = monthSavings.get(m);
                        float amt = (float) t.getAmount();
                        if ("income".equalsIgnoreCase(t.getType())) prev += amt;
                        else prev -= amt;
                        monthSavings.put(m, prev);
                    }
                }
                float sumAll = 0f;
                for (float sav : monthSavings.values()) {
                    sumAll += sav;
                }
                float avgAll = sumAll / monthSavings.size();
                binding.tvAvgSavings.setText(String.format(Locale.getDefault(),
                        "Average monthly savings  €%.2f", avgAll));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}
