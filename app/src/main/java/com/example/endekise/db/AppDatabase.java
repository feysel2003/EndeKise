package com.example.endekise.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.endekise.dao.AccountDao;
import com.example.endekise.dao.TransactionDao;
import com.example.endekise.entity.Account;
import com.example.endekise.entity.Budget;
import com.example.endekise.entity.Category;
import com.example.endekise.entity.Tag;
import com.example.endekise.entity.Transaction;
import com.example.endekise.entity.TransactionSplit;
import com.example.endekise.entity.TransactionTag;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {Account.class, Budget.class, Category.class, Tag.class, Transaction.class, TransactionSplit.class, TransactionTag.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AccountDao accountDao();
    public abstract TransactionDao transactionDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "endekise_database")
                            .addCallback(sRoomDatabaseCallback) // <--- THIS IS NEW!
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // THIS CODE RUNS THE VERY FIRST TIME THE APP IS INSTALLED
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Let's create two default wallets for our user!
                AccountDao dao = INSTANCE.accountDao();
                dao.insertAccount(new Account("Cash in Wallet", "Cash", 0.0, "ETB"));
                dao.insertAccount(new Account("Commercial Bank (CBE)", "Bank", 0.0, "ETB"));
            });
        }
    };
}