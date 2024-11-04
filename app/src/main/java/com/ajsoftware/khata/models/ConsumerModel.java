package com.ajsoftware.khata.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.List;
@Entity(tableName = "consumer_data")

public class ConsumerModel implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    String  name, phoneNo, address, amount, amount_paid, amount_left;

    private long timestamp;

    public ConsumerModel() {
    }

    public ConsumerModel(String id, String name, String phoneNo, String address, String amount, String amount_paid,String amount_left,long timestamp) {
        this.id = id;
        this.name = name;
        this.phoneNo = phoneNo;
        this.address = address;
        this.amount = amount;
        this.amount_paid = amount_paid;
        this.amount_left = amount_left;
        this.timestamp = timestamp;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount_paid() {
        return amount_paid;
    }

    public void setAmount_paid(String amount_paid) {
        this.amount_paid = amount_paid;
    }

    public String getAmount_left() {
        return amount_left;
    }

    public void setAmount_left(String amount_left) {
        this.amount_left = amount_left;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

