package com.example.endekise.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.endekise.R;
import com.example.endekise.entity.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    // This updates the list automatically when new data is added
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged(); // Refreshes the UI
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction currentTx = transactions.get(position);

        // Set Note (or show "No Note" if empty)
        if (currentTx.getNote() == null || currentTx.getNote().isEmpty()) {
            holder.textNote.setText("Expense");
        } else {
            holder.textNote.setText(currentTx.getNote());
        }

        // Set Amount
        holder.textAmount.setText(String.format(Locale.getDefault(), "ETB %.2f", currentTx.getAmount()));

        // Set Date (Converting from long timestamp to readable string)
        Date date = new Date(currentTx.getDateTimestamp());
        holder.textDate.setText(dateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    // The ViewHolder holds the views from our XML layout
    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private TextView textNote, textDate, textAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textNote = itemView.findViewById(R.id.textNote);
            textDate = itemView.findViewById(R.id.textDate);
            textAmount = itemView.findViewById(R.id.textAmount);
        }
    }
}