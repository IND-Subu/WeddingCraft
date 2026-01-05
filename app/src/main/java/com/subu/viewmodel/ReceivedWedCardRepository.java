package com.subu.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class ReceivedWedCardRepository {

    private final WedCardDao dao;
    private final LiveData<List<WedCardEntity>> allReceivedCards;
    private final LiveData<List<WedCardEntity>> newCards;
    private final ExecutorService executor;


    public ReceivedWedCardRepository(Application application) {
        WeddingCraftDatabase db = WeddingCraftDatabase.getDatabase(application);
        dao = db.wedCardDao();
        allReceivedCards = dao.getAllReceivedCards();
        executor = WeddingCraftDatabase.databaseWriteExecutor;
        newCards = dao.getNewCards();
    }

    public LiveData<List<WedCardEntity>> getAllReceivedCards() {
        return allReceivedCards;
    }

    public LiveData<List<WedCardEntity>> getNewCards(){
        return newCards;
    }
    public void insert(WedCardEntity card) {
        WeddingCraftDatabase.databaseWriteExecutor.execute(() -> dao.insert(card));
    }

    public void update(WedCardEntity card) {
        executor.execute(() -> dao.update(card));
    }

    public void delete(WedCardEntity card) {
        executor.execute(() -> dao.delete(card));
    }

    public void deleteAll() {
        executor.execute(dao::deleteAll);
    }

    public void markAsViewed(long id) {
        WeddingCraftDatabase.databaseWriteExecutor.execute(() -> dao.markAsViewed(id));
    }

    public void markAsDownloaded(String fileName) {
        executor.execute(() -> dao.markAsDownloaded(fileName)); // ✅ NEW
    }
}
