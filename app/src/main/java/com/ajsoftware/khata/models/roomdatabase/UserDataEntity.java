package com.ajsoftware.khata.models.roomdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "user_data")
public class UserDataEntity implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "uid")
    private String uid;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "phone_no")
    private String phoneNo;

    @ColumnInfo(name = "created_on")
    private String createdOn;

    @ColumnInfo(name = "status")
    private String status;

    // Constructors, getters, and setters

    public UserDataEntity() {
    }

    public UserDataEntity(String uid, String name, String email, String phoneNo, String createdOn, String status) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phoneNo = phoneNo;
        this.createdOn = createdOn;
        this.status = status;
    }

    // Getters and Setters

    // ... (other methods if needed)


    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

