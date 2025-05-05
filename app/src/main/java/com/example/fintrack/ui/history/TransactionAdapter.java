package com.example.fintrack.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fintrack.R;
import com.example.fintrack.data.Transaction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.Holder> {

    private final List<Transaction> items = new ArrayList<>();

    // replace list and refresh
    public void setTransactions(List<Transaction> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_transaction, p, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        Transaction t = items.get(pos);
        h.amount.setText(String.format(Locale.getDefault(), "%.2f", t.getAmount()));
        h.category.setText(t.getCategory());
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(t.getDate());
        h.date.setText(date);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView amount, category, date;
        Holder(View v) {
            super(v);
            amount   = v.findViewById(R.id.tvAmount);
            category = v.findViewById(R.id.tvCategory);
            date     = v.findViewById(R.id.tvDate);
        }
    }
}
