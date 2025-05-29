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
import com.example.fintrack.util.CurrencyManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // view type for date header
    private static final int TYPE_HEADER = 0;
    // view type for transaction item
    private static final int TYPE_ITEM   = 1;

    public interface OnItemClickListener {
        void onItemClick(Transaction tx);
    }
    public interface OnEditClickListener {
        void onEditClick(Transaction tx);
    }

    private OnItemClickListener listener;
    private OnEditClickListener editListener;

    private final List<Object> items = new ArrayList<>();

    public void setOnItemClickListener(OnItemClickListener l) {
        listener = l;
    }
    public void setOnEditClickListener(OnEditClickListener l) {
        editListener = l;
    }

    // populate list with date headers and transactions
    public void setTransactions(List<Transaction> list) {
        items.clear();
        if (list != null && !list.isEmpty()) {
            SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String lastDate = "";
            for (Transaction t : list) {
                String day = df.format(t.getDate());
                if (!day.equals(lastDate)) {
                    items.add(day);
                    lastDate = day;
                }
                items.add(t);
            }
        }
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int pos) {
        return (items.get(pos) instanceof String)
                ? TYPE_HEADER
                : TYPE_ITEM;
    }

    public Transaction getTransactionAt(int position) {
        Object o = items.get(position);
        if (o instanceof Transaction) {
            return (Transaction) o;
        }
        return null;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            // inflate the date header layout
            View v = inf.inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderHolder(v);
        } else {
            // inflate the transaction item layout
            View v = inf.inflate(R.layout.item_transaction, parent, false);
            return new TransactionHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int pos) {
        if (vh instanceof DateHeaderHolder) {
            String date = (String) items.get(pos);
            ((DateHeaderHolder) vh).tvDate.setText(date);
        } else {
            Transaction t = (Transaction) items.get(pos);
            ((TransactionHolder) vh).bind(t, listener, editListener);
        }
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class DateHeaderHolder extends RecyclerView.ViewHolder {
        final TextView tvDate;
        DateHeaderHolder(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDateHeader);
        }
    }

    static class TransactionHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvAmount, tvCategory, tvDate;
        private final ImageButton btnEdit;

        TransactionHolder(View v) {
            super(v);
            ivIcon     = v.findViewById(R.id.ivIcon);
            tvAmount   = v.findViewById(R.id.tvAmount);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvDate     = v.findViewById(R.id.tvDate);
            btnEdit    = v.findViewById(R.id.btnEdit);
        }

        // bind transaction data to views
        void bind(Transaction t,
                  OnItemClickListener clickListener,
                  OnEditClickListener editListener) {
            // choose icon based on category
            int iconRes;
            switch (t.getCategory().toLowerCase(Locale.ROOT)) {
                case "food":   iconRes = R.drawable.ic_food;   break;
                case "rent":   iconRes = R.drawable.ic_home;   break;
                case "salary": iconRes = R.drawable.ic_salary; break;
                default:       iconRes = R.drawable.ic_other;  break;
            }
            ivIcon.setImageResource(iconRes);

            // format and display date
            String dateText = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    .format(t.getDate());
            tvDate.setText(dateText);

            // display category and amount with currency symbol
            tvCategory.setText(t.getCategory());
            boolean isIncome = "income".equalsIgnoreCase(t.getType());
            CurrencyManager cm = CurrencyManager.get(itemView.getContext());
            String disp = (isIncome ? "+" : "")
                    + cm.getCurrencySymbol()
                    + String.format(Locale.getDefault(), "%.2f", Math.abs(t.getAmount()));
            tvAmount.setText(disp);
            tvAmount.setTextColor(ContextCompat.getColor(
                    itemView.getContext(),
                    isIncome ? R.color.incomeColor : R.color.expenseColor));

            // set click actions for item and edit button
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onItemClick(t);
            });
            btnEdit.setOnClickListener(v -> {
                if (editListener != null) editListener.onEditClick(t);
            });
        }
    }
}
