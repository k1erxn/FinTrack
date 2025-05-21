package com.example.fintrack.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
    private final List<Transaction> items = new ArrayList<>();

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }

    public void setOnEditClickListener(OnEditClickListener l) {
        editListener = l;
    }

    public void setTransactions(List<Transaction> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int pos) {
        Transaction t = items.get(pos);

        // icon by category
        int iconRes;
        switch (t.getCategory().toLowerCase(Locale.ROOT)) {
            case "food":
                iconRes = R.drawable.ic_food;
                break;
            case "rent":
                iconRes = R.drawable.ic_home;
                break;
            case "salary":
                iconRes = R.drawable.ic_salary;
                break;
            default:
                iconRes = R.drawable.ic_other;
                break;
        }
        h.ivIcon.setImageResource(iconRes);

        // date text
        String dateText = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(t.getDate());
        h.tvDate.setText(dateText);

        // category label
        h.tvCategory.setText(t.getCategory());

        // amount with sign and color
        boolean isIncome = "income".equalsIgnoreCase(t.getType());
        String sign = isIncome ? "+" : "-";
        String amt = String.format(Locale.getDefault(), "%.2f", Math.abs(t.getAmount()));
        h.tvAmount.setText(sign + "€" + amt);
        int colorRes = isIncome
                ? R.color.incomeColor
                : R.color.expenseColor;
        h.tvAmount.setTextColor(
                ContextCompat.getColor(h.itemView.getContext(), colorRes)
        );

        // row click
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(t);
        });

        // edit button click
        h.btnEdit.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Transaction getTransactionAt(int pos) {
        return items.get(pos);
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvAmount, tvCategory, tvDate;
        ImageButton btnEdit;

        Holder(View v) {
            super(v);
            ivIcon   = v.findViewById(R.id.ivIcon);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvDate   = v.findViewById(R.id.tvDate);
            btnEdit  = v.findViewById(R.id.btnEdit);
        }
    }
}
