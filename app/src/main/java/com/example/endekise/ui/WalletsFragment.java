package com.example.endekise.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.endekise.R;
import com.example.endekise.adapter.WalletAdapter;
import com.example.endekise.viewmodel.DashboardViewModel;

public class WalletsFragment extends Fragment {

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        // Grid Layout: Show 2 cards side-by-side!
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewWalletsGrid);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);

        WalletAdapter adapter = new WalletAdapter();
        recyclerView.setAdapter(adapter);

        // Fetch the wallets from SQLite
        viewModel.getAllAccounts().observe(getViewLifecycleOwner(), adapter::setWallets);

        // The Add Button logic
        View btnAddWallet = view.findViewById(R.id.btnAddWallet);
        btnAddWallet.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add New Wallet screen opening soon...", Toast.LENGTH_SHORT).show();
            // We will build a pop-up dialog for this later!
        });
    }
}