package com.ajsoftware.khata.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
@Entity(tableName = "user_data")
public class UserModel implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "uid")
    String uid;
    String name, email, phoneNo, created_on, status;

    public UserModel() {
    }

    public UserModel(String name, String uid, String email, String phoneNo, String created_on, String status) {
        this.name = name;
        this.uid = uid;
        this.phoneNo = phoneNo;
        this.email = email;
        this.created_on = created_on;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getCreated_on() {
        return created_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
