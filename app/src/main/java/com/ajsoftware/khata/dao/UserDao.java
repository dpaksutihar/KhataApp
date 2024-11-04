package com.ajsoftware.khata.dao;

import androidx.room.Dao;
import androidx.room.Query;

import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.models.roomdatabase.UserDataEntity;


import java.util.List;
@Dao
public interface UserDao {
    @Query("SELECT * FROM user_data")
    List<UserDataEntity> getAllUsers();
}
