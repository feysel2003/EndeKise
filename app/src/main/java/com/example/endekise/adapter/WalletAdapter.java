package com.example.endekise.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.endekise.R;
import com.example.endekise.entity.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletViewHolder> {

    private List<Account> wallets = new ArrayList<>();

    public void setWallets(List<Account> wallets) {
        this.wallets = wallets;
        notifyDataSetChanged(); // Instantly updates UI when money changes
    }

    @NonNull
    @Override
    public WalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new WalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletViewHolder holder, int position) {
        Account currentWallet = wallets.get(position);
        holder.textWalletName.setText(currentWallet.getName());
        holder.textWalletBalance.setText(String.format(Locale.getDefault(), "ETB %.2f", currentWallet.getCurrentBalance()));
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }

    class WalletViewHolder extends RecyclerView.ViewHolder {
        private TextView textWalletName, textWalletBalance;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);
            textWalletName = itemView.findViewById(R.id.textWalletName);
            textWalletBalance = itemView.findViewById(R.id.textWalletBalance);
        }
    }
}