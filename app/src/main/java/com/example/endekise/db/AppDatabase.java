package com.example.endekise.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Import all your tables
import com.example.endekise.entity.Account;
import com.example.endekise.entity.Budget;
import com.example.endekise.entity.Category;
import com.example.endekise.entity.Tag;
import com.example.endekise.entity.Transaction;
import com.example.endekise.entity.TransactionSplit;
import com.example.endekise.entity.TransactionTag;

@Database(
        entities = {
                Account.class,
                Budget.class,
                Category.class,
                Tag.class,
                Transaction.class,
                TransactionSplit.class,
                TransactionTag.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    // We will put our DAOs (Data Access Objects) here in the next step!
    public abstract com.example.endekise.dao.AccountDao accountDao();
    public abstract com.example.endekise.dao.TransactionDao transactionDao();
    // The Singleton instance
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // This is where the actual SQLite file is created on the phone!
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "endekise_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}