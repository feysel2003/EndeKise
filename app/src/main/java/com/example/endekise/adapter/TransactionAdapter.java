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

        // Set Note
        if (currentTx.getNote() == null || currentTx.getNote().isEmpty()) {
            holder.textNote.setText("Transaction");
        } else {
            holder.textNote.setText(currentTx.getNote());
        }

        // Set Date
        Date date = new Date(currentTx.getDateTimestamp());
        holder.textDate.setText(dateFormat.format(date));

        // COLOR CODING LOGIC!
        double amount = currentTx.getAmount();
        if (amount < 0) {
            // It's an Expense (Red)
            holder.textAmount.setTextColor(android.graphics.Color.parseColor("#D32F2F")); // Red
            // Format to show positive number in the string since we add the "-" manually
            holder.textAmount.setText(String.format(Locale.getDefault(), "- ETB %.2f", Math.abs(amount)));
        } else {
            // It's an Income (Green)
            holder.textAmount.setTextColor(android.graphics.Color.parseColor("#388E3C")); // Green
            holder.textAmount.setText(String.format(Locale.getDefault(), "+ ETB %.2f", amount));
        }
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