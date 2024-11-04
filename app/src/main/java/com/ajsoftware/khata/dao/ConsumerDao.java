package com.ajsoftware.khata.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.roomdatabase.ConsumerEntity;

import java.util.List;

@Dao
public interface ConsumerDao {
    @Query("SELECT * FROM consumer_data")
    List<ConsumerEntity> getAllConsumers();
}
