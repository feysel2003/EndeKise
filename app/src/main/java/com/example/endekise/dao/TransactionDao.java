package com.example.endekise.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction; // Room's ACID annotation

import java.util.List;

@Dao
public abstract class TransactionDao {

    // 1. Log a new expense or income
    @Insert
    public abstract void insertTransaction(com.example.endekise.entity.Transaction transaction);

    // 2. Get all history sorted by newest first
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    public abstract LiveData<List<com.example.endekise.entity.Transaction>> getAllTransactions();

    // 3. Get the total sum of all transactions
    @Query("SELECT SUM(amount) FROM transactions")
    public abstract LiveData<Double> getTotalAmount();
    // 3. THE ENTERPRISE FEATURE: ACID Compliant Account Transfer
    // If the phone crashes exactly between deducting the Bank and adding to Cash,
    // the @Transaction annotation guarantees the whole operation rolls back. No money lost!
    @Transaction
    public void executeTransfer(int fromAccountId, int toAccountId, double amount, AccountDao accountDao) {

        // Deduct money from the sender account (e.g., Bank)
        accountDao.updateBalance(fromAccountId, -amount);

        // Add money to the receiving account (e.g., Cash Wallet)
        accountDao.updateBalance(toAccountId, amount);

        // (In the future, we will also log the transfer history here)
    }
    @androidx.room.Delete
    public abstract void deleteTransaction(com.example.endekise.entity.Transaction transaction);
    // --- TRUE LEDGER LOGIC ---
    // This saves the transaction AND updates the wallet balance instantly.
    @Transaction
    public void insertTransactionAndUpdateAccount(com.example.endekise.entity.Transaction transaction, AccountDao accountDao) {
        // 1. Save the receipt
        insertTransaction(transaction);

        // 2. Add or subtract the money from the specific wallet!
        // (If the amount is negative, adding it will naturally subtract it)
        accountDao.updateBalance(transaction.getAccountId(), transaction.getAmount());
    }

    // Get the sum of expenses grouped by Category for the Pie Chart
    @Query("SELECT payee AS categoryName, SUM(amount) AS totalAmount FROM transactions WHERE amount < 0 GROUP BY payee")
    public abstract LiveData<List<com.example.endekise.entity.CategorySum>> getExpenseSumByCategory();

    @Query("SELECT SUM(amount) FROM transactions WHERE amount > 0")
    public abstract LiveData<Double> getTotalIncome();

    @Query("SELECT SUM(amount) FROM transactions WHERE amount < 0")
    public abstract LiveData<Double> getTotalExpense();

}
