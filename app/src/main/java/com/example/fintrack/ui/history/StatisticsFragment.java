package com.example.fintrack.ui.history;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fintrack.R;
import com.example.fintrack.data.Transaction;
import com.example.fintrack.viewmodel.TransactionViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {
    private BarChart chart;
    private TextView tvPeriod;
    private ImageButton btnPrev, btnNext;
    private TransactionViewModel viewModel;
    private List<Transaction> allTx;
    private int offsetMonths = 0;
    private final int WINDOW = 4;

    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             ViewGroup container,
                             Bundle saved) {
        View root = inf.inflate(R.layout.fragment_statistics, container, false);

        chart    = root.findViewById(R.id.barChart);
        tvPeriod = root.findViewById(R.id.tvPeriodRange);
        btnPrev  = root.findViewById(R.id.btnPrev);
        btnNext  = root.findViewById(R.id.btnNext);

        setupChartAppearance();
        hookupButtons();

        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        viewModel.getAllTransactions()
                .observe(getViewLifecycleOwner(), list -> {
                    allTx = list;
                    updateChart();
                });

        return root;
    }

    private void hookupButtons() {
        btnPrev.setOnClickListener(v -> {
            offsetMonths += WINDOW;
            updateChart();
        });
        btnNext.setOnClickListener(v -> {
            if (offsetMonths >= WINDOW) {
                offsetMonths -= WINDOW;
                updateChart();
            }
        });
    }

    private void setupChartAppearance() {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);
        x.setXOffset(100f);
        chart.getAxisRight().setEnabled(false);
    }

    private void updateChart() {
        if (allTx == null) return;

        Calendar cal = Calendar.getInstance();
        String[] labels = new String[WINDOW];
        Map<String, Float> incMap = new LinkedHashMap<>();
        Map<String, Float> expMap = new LinkedHashMap<>();

        // initialize
        for (int i = WINDOW - 1; i >= 0; i--) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.MONTH, -i - offsetMonths);
            String m = new SimpleDateFormat("MMM", Locale.getDefault())
                    .format(cal.getTime());
            labels[WINDOW - 1 - i] = m;
            incMap.put(m,  0f);
            expMap.put(m,  0f);
        }

        for (Transaction t : allTx) {
            cal.setTimeInMillis(t.getDate());
            String m = new SimpleDateFormat("MMM", Locale.getDefault())
                    .format(cal.getTime());
            if (!incMap.containsKey(m)) continue;
            float prevI = incMap.get(m), prevE = expMap.get(m);
            if ("income".equalsIgnoreCase(t.getType()))
                incMap.put(m, prevI + (float)t.getAmount());
            else
                expMap.put(m, prevE + (float)t.getAmount());
        }

        // build entries
        List<BarEntry> inE = new ArrayList<>();
        List<BarEntry> exE = new ArrayList<>();
        for (int i = 0; i < WINDOW; i++) {
            inE.add(new BarEntry(i, incMap.get(labels[i])));
            exE.add(new BarEntry(i, expMap.get(labels[i])));
        }

        // set period text
        tvPeriod.setText(labels[0] + " – " + labels[WINDOW-1]);
        // enable/disable arrows
        btnNext.setEnabled(offsetMonths >= WINDOW);

        // chart data
        BarDataSet dsI = new BarDataSet(inE,  "income");
        dsI.setColor(Color.parseColor("#4CAF50"));
        BarDataSet dsE = new BarDataSet(exE, "expenses");
        dsE.setColor(Color.parseColor("#F44336"));

        BarData data = new BarData(dsI, dsE);
        float barW = 0.3f, barS = 0.05f, grpS = 0.3f;
        data.setBarWidth(barW);
        chart.setData(data);

        chart.getAxisLeft().setAxisMinimum(0f);

        chart.getXAxis().setAxisMinimum(0f);
        chart.getXAxis().setAxisMaximum(WINDOW);

// label each bar group
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        chart.groupBars(0f, 0.3f, 0.05f);

        chart.setVisibleXRangeMaximum(WINDOW);

        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();


    }
}
