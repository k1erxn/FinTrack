package com.example.fintrack.ui.history;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fintrack.R;
import com.example.fintrack.data.Transaction;
import com.example.fintrack.viewmodel.TransactionViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {
    private TransactionViewModel viewModel;
    private TransactionAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        RecyclerView rv = root.findViewById(R.id.rvTransactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), list ->
                adapter.setTransactions(list)
        );

        adapter.setOnEditClickListener(tx -> {
            Bundle args = new Bundle();
            args.putInt("transactionId", tx.getId());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_historyFragment_to_editTransactionFragment, args);
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                Transaction tx = adapter.getTransactionAt(pos);
                viewModel.delete(tx);
            }
        }).attachToRecyclerView(rv);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu,
                                    @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.history_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_csv) {
            exportCsv();
            return true;
        } else if (id == R.id.action_export_pdf) {
            exportPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportCsv() {
        List<Transaction> list = viewModel.getAllTransactions().getValue();
        if (list == null || list.isEmpty()) {
            Toast.makeText(requireContext(), "no transactions to export", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File csvFile = new File(requireContext().getCacheDir(), "transactions.csv");
            FileWriter writer = new FileWriter(csvFile);
            writer.append("ID,Amount,Date,Category,Type\n");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (Transaction t : list) {
                writer.append(String.valueOf(t.getId()))
                        .append(",")
                        .append(String.format(Locale.getDefault(),"%.2f", t.getAmount()))
                        .append(",")
                        .append(sdf.format(new Date(t.getDate())))
                        .append(",")
                        .append(t.getCategory())
                        .append(",")
                        .append(t.getType())
                        .append("\n");
            }
            writer.close();

            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    csvFile);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/csv");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "share csv"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "csv export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportPdf() {
        List<Transaction> list = viewModel.getAllTransactions().getValue();
        if (list == null || list.isEmpty()) {
            Toast.makeText(requireContext(), "no transactions to export", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = doc.startPage(pi);
        Canvas c = page.getCanvas();
        Paint p = new Paint();
        p.setTextSize(12);

        int y = 25;
        c.drawText("ID  Amount  Date       Category  Type", 10, y, p);
        y += p.getTextSize() + 5;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        for (Transaction t : list) {
            String line = t.getId() + "  "
                    + String.format(Locale.getDefault(),"%.2f", t.getAmount()) + "  "
                    + sdf.format(new Date(t.getDate())) + "  "
                    + t.getCategory() + "  "
                    + t.getType();
            c.drawText(line, 10, y, p);
            y += p.getTextSize() + 5;
        }
        doc.finishPage(page);

        try {
            File pdfFile = new File(requireContext().getCacheDir(), "transactions.pdf");
            doc.writeTo(new FileOutputStream(pdfFile));
            doc.close();

            Uri uri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    pdfFile);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("application/pdf");
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "share pdf"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "pdf export failed", Toast.LENGTH_SHORT).show();
        }
    }
}
