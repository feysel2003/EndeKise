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

        // --- ETHIOPIAN LOCALIZED GRADIENT BRANDING ---
        String nameLower = walletName.toLowerCase();
        int colorStart;
        int colorEnd;

        if (nameLower.contains("cbe") || nameLower.contains("commercial bank")) {
            // CBE: Deep Purple to Magenta
            colorStart = android.graphics.Color.parseColor("#4A148C");
            colorEnd = android.graphics.Color.parseColor("#880E4F");
        } else if (nameLower.contains("telebirr")) {
            // Telebirr: Light Blue to Cyan
            colorStart = android.graphics.Color.parseColor("#0277BD");
            colorEnd = android.graphics.Color.parseColor("#00BCD4");
        } else if (nameLower.contains("awash")) {
            // Awash Bank: Deep Blue to Light Blue
            colorStart = android.graphics.Color.parseColor("#0D47A1");
            colorEnd = android.graphics.Color.parseColor("#1976D2");
        } else if (nameLower.contains("dashen")) {
            // Dashen: Deep Orange to Yellow
            colorStart = android.graphics.Color.parseColor("#E65100");
            colorEnd = android.graphics.Color.parseColor("#FBC02D");
        } else if (nameLower.contains("boa") || nameLower.contains("abyssinia")) {
            // Bank of Abyssinia: Gold to Yellow
            colorStart = android.graphics.Color.parseColor("#F57F17");
            colorEnd = android.graphics.Color.parseColor("#FBC02D");
        } else if (currentWallet.getType().equalsIgnoreCase("Cash")) {
            // Cash: Forest Green to Light Green
            colorStart = android.graphics.Color.parseColor("#1B5E20");
            colorEnd = android.graphics.Color.parseColor("#4CAF50");
        } else {
            // Default: Sleek Dark Slate to Gray
            colorStart = android.graphics.Color.parseColor("#263238");
            colorEnd = android.graphics.Color.parseColor("#455A64");
        }

        // Apply the beautiful dynamic gradient!
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                new int[]{colorStart, colorEnd});
        gd.setCornerRadius(60f); // Match the MaterialCardView corner radius (roughly 20dp)

        // Remove the flat color and set the gradient background!
        holder.cardView.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);
        holder.cardView.setBackground(gd);

        // --- Handle Long Press to Delete ---
        holder.cardView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onWalletLongClick(currentWallet);
            }
            return true;
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