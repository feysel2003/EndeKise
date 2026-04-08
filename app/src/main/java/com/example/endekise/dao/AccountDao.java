package com.example.endekise.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.endekise.entity.Account;

import java.util.List;

@Dao
public interface AccountDao {

    // 1. Insert a new wallet/bank
    @Insert
    void insertAccount(Account account);

    // 2. Get all wallets (LiveData means the UI updates instantly if a balance changes!)
    @Query("SELECT * FROM accounts")
    LiveData<List<Account>> getAllAccounts();

    // 3. Update the balance when money is spent or added
    @Query("UPDATE accounts SET currentBalance = currentBalance + :amount WHERE accountId = :accountId")
    void updateBalance(int accountId, double amount);
}