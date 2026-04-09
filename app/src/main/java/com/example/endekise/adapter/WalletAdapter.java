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

    // --- 1. NEW: The Interface for Long-Clicks ---
    private OnWalletLongClickListener longClickListener;

    public interface OnWalletLongClickListener {
        void onWalletLongClick(Account account);
    }

    public void setOnWalletLongClickListener(OnWalletLongClickListener listener) {
        this.longClickListener = listener;
    }

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

        String walletName = currentWallet.getName();
        holder.textWalletName.setText(walletName);
        holder.textWalletType.setText(currentWallet.getType());
        holder.textWalletBalance.setText(String.format(Locale.getDefault(), "ETB %,.2f", currentWallet.getCurrentBalance()));

        // ETHIOPIAN LOCALIZED COLOR BRANDING
        String nameLower = walletName.toLowerCase();
        if (nameLower.contains("cbe") || nameLower.contains("commercial bank")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#4A148C"));
        } else if (nameLower.contains("telebirr")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#00BCD4"));
        } else if (nameLower.contains("awash")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#1565C0"));
        } else if (nameLower.contains("dashen")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#F9A825"));
        } else if (nameLower.contains("boa") || nameLower.contains("abyssinia")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#F57F17"));
        } else if (currentWallet.getType().equalsIgnoreCase("Cash")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#2E7D32"));
        } else if (currentWallet.getType().equalsIgnoreCase("Mobile Money")) {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#00897B"));
        } else {
            holder.cardView.setCardBackgroundColor(android.graphics.Color.parseColor("#2A2D43"));
        }

        // --- 2. NEW: Fire the Long Click Event ---
        holder.cardView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onWalletLongClick(currentWallet);
            }
            return true; // Return true means we handled the long click!
        });
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