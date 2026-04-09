package com.example.endekise.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.endekise.entity.Account;
import com.example.endekise.entity.Transaction;
import com.example.endekise.repository.AppRepository;

import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private AppRepository repository;
    private LiveData<List<Account>> allAccounts;
    private LiveData<List<Transaction>> allTransactions;

    public DashboardViewModel(Application application) {
        super(application);
        repository = new AppRepository(application);
        allAccounts = repository.getAllAccounts();
        allTransactions = repository.getAllTransactions();
    }

    // The UI will "observe" these. When the DB changes, the UI updates instantly!
    public LiveData<List<Account>> getAllAccounts() { return allAccounts; }
    public LiveData<List<Transaction>> getAllTransactions() { return allTransactions; }

    // Helper methods for the UI to save data
    public void insertAccount(Account account) { repository.insertAccount(account); }
    public void insertTransaction(Transaction transaction) { repository.insertTransaction(transaction); }
    public LiveData<Double> getTotalAmount() {
        return repository.getTotalAmount();
    }
    public void deleteTransaction(Transaction transaction) {
        repository.deleteTransaction(transaction);
    }
    public void executeTransfer(int fromAccountId, int toAccountId, double amount) {
        repository.executeTransfer(fromAccountId, toAccountId, amount);
    }

    public LiveData<List<com.example.endekise.entity.Category>> getAllCategories() {
        return repository.getAllCategories();
    }
    public void insertCategory(com.example.endekise.entity.Category category) {
        repository.insertCategory(category);
    }
}