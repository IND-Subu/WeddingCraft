package com.subu.viewmodel;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {WedCardEntity.class}, version = 2, exportSchema = false)
public abstract class WeddingCraftDatabase extends RoomDatabase {
    public abstract WedCardDao wedCardDao();

    // 👇 Executor for background DB operations
    private static volatile WeddingCraftDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static WeddingCraftDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WeddingCraftDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    WeddingCraftDatabase.class,
                                    "weddingcraft_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
