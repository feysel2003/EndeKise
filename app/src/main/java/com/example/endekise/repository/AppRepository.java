package com.example.endekise.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.endekise.dao.AccountDao;
import com.example.endekise.dao.TransactionDao;
import com.example.endekise.db.AppDatabase;
import com.example.endekise.entity.Account;
import com.example.endekise.entity.Transaction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {

    private AccountDao accountDao;
    private TransactionDao transactionDao;
    private LiveData<List<Account>> allAccounts;
    private LiveData<List<Transaction>> allTransactions;

    // This ExecutorService acts as our background worker thread so the app doesn't freeze!
    private ExecutorService executorService;

    public AppRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        accountDao = db.accountDao();
        transactionDao = db.transactionDao();

        allAccounts = accountDao.getAllAccounts();
        allTransactions = transactionDao.getAllTransactions();

        executorService = Executors.newSingleThreadExecutor();
    }

    // --- READ METHODS (LiveData automatically runs in the background) ---
    public LiveData<List<Account>> getAllAccounts() { return allAccounts; }
    public LiveData<List<Transaction>> getAllTransactions() { return allTransactions; }

    // --- WRITE METHODS (Must be sent to the background executor) ---
    public void insertAccount(Account account) {
        executorService.execute(() -> accountDao.insertAccount(account));
    }

    public void insertTransaction(Transaction transaction) {
        // Now it calls our new ultra-safe ledger method!
        executorService.execute(() ->
                transactionDao.insertTransactionAndUpdateAccount(transaction, accountDao)
        );
    }
    public LiveData<Double> getTotalAmount() {
        return transactionDao.getTotalAmount();
    }
    public void deleteTransaction(Transaction transaction) {
        executorService.execute(() -> transactionDao.deleteTransaction(transaction));
    }

    public void executeTransfer(int fromAccountId, int toAccountId, double amount) {
        executorService.execute(() ->
                transactionDao.executeTransfer(fromAccountId, toAccountId, amount, accountDao)
        );
    }
}