package com.ajsoftware.khata.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.models.roomdatabase.ConsumerEntity;
import com.ajsoftware.khata.models.roomdatabase.TransactionEntity;
import com.ajsoftware.khata.models.roomdatabase.UserDataEntity;

@Database(entities = {UserDataEntity.class, ConsumerEntity.class, TransactionEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract ConsumerDao consumerDao();
    public abstract TransactionRecordingDao transactionDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
