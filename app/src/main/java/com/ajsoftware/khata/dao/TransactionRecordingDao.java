package com.ajsoftware.khata.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.models.roomdatabase.TransactionEntity;

import java.util.List;
@Dao
public interface TransactionRecordingDao {
    @Query("SELECT * FROM transaction_data")
    List<TransactionEntity> getAllTransactions();
}
