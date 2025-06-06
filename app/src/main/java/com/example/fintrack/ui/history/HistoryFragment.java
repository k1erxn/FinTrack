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
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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
    private boolean dateAscending = false;
    private boolean amountAscending = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // fragment for displaying transaction history
        View root = inflater.inflate(R.layout.fragment_history, container, false);

        // button to view statistics
        MaterialCardView btnStats = root.findViewById(R.id.cardStats);
        btnStats.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigation_history_to_statisticsFragment)
        );

        // button to export transactions as csv
        ImageButton btnExportCsv = root.findViewById(R.id.btnExportCsv);
        btnExportCsv.setOnClickListener(v -> exportCsv());

        // button to export transactions as pdf
        ImageButton btnExportPdf = root.findViewById(R.id.btnExportPdf);
        btnExportPdf.setOnClickListener(v -> exportPdf());

        // setup sort by date toggle
        ImageButton btnFilterDate = root.findViewById(R.id.btnFilterDate);
        btnFilterDate.setOnClickListener(v -> {
            dateAscending = !dateAscending;
            viewModel.sortByDate(dateAscending);
        });

        // setup sort by amount toggle
        ImageButton btnFilterType = root.findViewById(R.id.btnFilterType);
        btnFilterType.setOnClickListener(v -> {
            amountAscending = !amountAscending;
            viewModel.sortByAmount(amountAscending);
        });

        // transactions list and adapter setup
        RecyclerView rv = root.findViewById(R.id.rvTransactions);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TransactionAdapter();
        rv.setAdapter(adapter);

        // initialize view model and observe data
        viewModel = new ViewModelProvider(this)
                .get(TransactionViewModel.class);
        viewModel.getAllTransactions().observe(getViewLifecycleOwner(), list ->
                adapter.setTransactions(list)
        );

        // swipe to delete transaction
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
                if (tx != null) {
                    viewModel.delete(tx);
                } else {
                    adapter.notifyItemChanged(pos);
                }
            }
        }).attachToRecyclerView(rv);

        return root;
    }

    private void exportCsv() {
        // export transactions list to csv file
        List<Transaction> list = viewModel.getAllTransactions().getValue();
        if (list == null || list.isEmpty()) {
            Toast.makeText(requireContext(), "no transactions to export",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File csvFile = new File(requireContext().getCacheDir(),
                    "transactions.csv");
            FileWriter writer = new FileWriter(csvFile);
            writer.append("ID,Amount,Date,Category,Type,Description\n");
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "yyyy-MM-dd", Locale.getDefault());
            for (Transaction t : list) {
                writer.append(String.valueOf(t.getId()))
                        .append(",")
                        .append(String.format(Locale.getDefault(),
                                "%.2f", t.getAmount()))
                        .append(",")
                        .append(sdf.format(new Date(t.getDate())))
                        .append(",")
                        .append(t.getCategory())
                        .append(",")
                        .append(t.getType())
                        .append(",")
                        .append(t.getDescription() != null
                                ? t.getDescription()
                                : "")
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
            Toast.makeText(requireContext(),
                    "csv export failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportPdf() {
        // export transactions list to pdf file
        List<Transaction> list = viewModel.getAllTransactions().getValue();
        if (list == null || list.isEmpty()) {
            Toast.makeText(requireContext(), "no transactions to export",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo pi = new PdfDocument.PageInfo.Builder(
                595, 842, 1).create();
        PdfDocument.Page page = doc.startPage(pi);
        Canvas c = page.getCanvas();
        Paint p = new Paint();
        p.setTextSize(12);

        int y = 25;
        c.drawText("ID  Amount  Date       Category  Type  Desc",
                10, y, p);
        y += p.getTextSize() + 5;
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        for (Transaction t : list) {
            String line = t.getId() + "  "
                    + String.format(Locale.getDefault(),
                    "%.2f", t.getAmount()) + "  "
                    + sdf.format(new Date(t.getDate())) + "  "
                    + t.getCategory() + "  "
                    + t.getType() + "  "
                    + (t.getDescription() != null
                    ? t.getDescription()
                    : "");
            c.drawText(line, 10, y, p);
            y += p.getTextSize() + 5;
        }
        doc.finishPage(page);

        try {
            File pdfFile = new File(requireContext().getCacheDir(),
                    "transactions.pdf");
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
            Toast.makeText(requireContext(),
                    "pdf export failed", Toast.LENGTH_SHORT).show();
        }
    }
}
