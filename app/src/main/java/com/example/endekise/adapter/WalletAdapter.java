package com.example.endekise.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.endekise.R;
import com.example.endekise.entity.Account;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

    private List<Account> wallets = new ArrayList<>();

    public void setWallets(List<Account> wallets) {
        this.wallets = wallets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet_card, parent, false);
        return new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        Account currentWallet = wallets.get(position);

        holder.textWalletName.setText(currentWallet.getName());
        holder.textWalletType.setText(currentWallet.getType());
        holder.textWalletBalance.setText(String.format(Locale.getDefault(), "ETB %,.2f", currentWallet.getCurrentBalance()));

        // --- DYNAMIC CARD COLORS ---
        // Let's give different wallet types different colored cards!
        if (currentWallet.getType().equalsIgnoreCase("Cash")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#00BFA5")); // Vibrant Teal
        } else if (currentWallet.getType().equalsIgnoreCase("Credit Card")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#FF5252")); // Red
        } else {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#7C4DFF")); // Default Purple for Banks
        }
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }

    class WalletViewHolder extends RecyclerView.ViewHolder {
        private TextView textWalletName, textWalletType, textWalletBalance;
        private MaterialCardView cardView;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);
            textWalletName = itemView.findViewById(R.id.textWalletName);
            textWalletType = itemView.findViewById(R.id.textWalletType);
            textWalletBalance = itemView.findViewById(R.id.textWalletBalance);
            cardView = (MaterialCardView) itemView;
        }
    }
}