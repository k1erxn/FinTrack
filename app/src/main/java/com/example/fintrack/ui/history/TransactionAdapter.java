package com.example.fintrack.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.data.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {

    public interface OnItemClickListener {
        void onItemClick(Transaction tx);
    }
    public interface OnEditClickListener {
        void onEditClick(Transaction tx);
    }

    private OnItemClickListener listener;
    private OnEditClickListener editListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    public void setOnEditClickListener(OnEditClickListener l) {
        editListener = l;
    }

    private final List<Transaction> items = new ArrayList<>();

    public void setTransactions(List<Transaction> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Transaction t = items.get(position);
        holder.amount.setText(String.format(Locale.getDefault(), "%.2f", t.getAmount()));
        holder.category.setText(t.getCategory());
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(t.getDate());
        holder.date.setText(date);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Transaction getTransactionAt(int position) {
        return items.get(position);
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView amount, category, date;
        ImageButton btnEdit;

        Holder(View itemView) {
            super(itemView);
            amount   = itemView.findViewById(R.id.tvAmount);
            category = itemView.findViewById(R.id.tvCategory);
            date     = itemView.findViewById(R.id.tvDate);
            btnEdit  = itemView.findViewById(R.id.btnEdit);
        }
    }
}
