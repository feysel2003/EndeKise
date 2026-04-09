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
    private List<com.example.endekise.entity.Account> accounts = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    // This updates the list automatically when new data is added
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        notifyDataSetChanged(); // Refreshes the UI
    }

    public void setAccounts(List<com.example.endekise.entity.Account> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    public Transaction getTransactionAt(int position) {
        return transactions.get(position);
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

        // --- 1. Find the Wallet Name ---
        String walletName = "Unknown Wallet";
        for (com.example.endekise.entity.Account acc : accounts) {
            if (acc.getAccountId() == currentTx.getAccountId()) {
                walletName = acc.getName();
                break;
            }
        }

        // --- 2. Get the Category Name ---
        // (Remember, we temporarily saved the Category in the 'payee' column in Step 5!)
        String categoryName = currentTx.getPayee();
        if (categoryName == null || categoryName.isEmpty()) {
            categoryName = "General";
        }

        // --- 3. Set the Title Text (Wallet • Category • Note) ---
        String noteText = currentTx.getNote();
        if (noteText == null || noteText.trim().isEmpty()) {
            // If there is no note, just show: "CBE Bank • Food & Drinks"
            holder.textNote.setText(walletName + " • " + categoryName);
        } else {
            // If there is a note, show: "CBE Bank • Food & Drinks • Lunch"
            holder.textNote.setText(walletName + " • " + categoryName + " • " + noteText);
        }

        // --- 4. Set Date ---
        Date date = new Date(currentTx.getDateTimestamp());
        holder.textDate.setText(dateFormat.format(date));

        // --- 5. COLOR CODING LOGIC! ---
        double amount = currentTx.getAmount();
        if (amount < 0) {
            // It's an Expense (Red)
            holder.textAmount.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
            holder.textAmount.setText(String.format(Locale.getDefault(), "- ETB %.2f", Math.abs(amount)));
        } else {
            // It's an Income (Green)
            holder.textAmount.setTextColor(android.graphics.Color.parseColor("#388E3C"));
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