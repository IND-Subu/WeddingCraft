package com.subu.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WedCardDao {

    @Insert
    void insert(WedCardEntity card);

    @Update
    void update(WedCardEntity card);

    @Delete
    void delete(WedCardEntity card);

    @Query("DELETE FROM wedcards")
    void deleteAll();

    @Query("DELETE FROM wedcards WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM wedcards ORDER BY receivedAt DESC")
    LiveData<List<WedCardEntity>> getAllReceivedCards();

    @Query("SELECT * FROM wedcards WHERE isNew = 1 ORDER BY receivedAt DESC")
    LiveData<List<WedCardEntity>> getNewCards();

    @Query("UPDATE wedcards SET isVisited = 1 WHERE id = :id")
    void markAsViewed(long id);

    @Query("UPDATE wedcards SET isDownload = 1 WHERE fileName = :fileName")
    void markAsDownloaded(String fileName); // ✅ NEW
}
