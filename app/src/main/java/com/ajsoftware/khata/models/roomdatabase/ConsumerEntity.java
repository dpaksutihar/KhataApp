package com.ajsoftware.khata.models.roomdatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.ajsoftware.khata.models.TransactionRecordingModel;

import java.io.Serializable;
import java.util.List;

@Entity(tableName = "consumer_data")

public class ConsumerEntity implements Serializable {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "phone_no")
    private String phoneNo;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "amount")
    private String amount;

    @ColumnInfo(name = "amount_paid")
    private String amountPaid;

    @ColumnInfo(name = "amount_left")
    private String amountLeft;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @Ignore
    private List<TransactionRecordingModel> transactionRecordingModelList;

    public ConsumerEntity() {
    }

    public ConsumerEntity(String id, String name, String phoneNo, String address, String amount, String amountPaid, String amountLeft, long timestamp) {
        this.id = id;
        this.name = name;
        this.phoneNo = phoneNo;
        this.address = address;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.amountLeft = amountLeft;
        this.timestamp = timestamp;
    }

    // Getters and setters

    // ... (other methods if needed)


    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
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

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getAmountLeft() {
        return amountLeft;
    }

    public void setAmountLeft(String amountLeft) {
        this.amountLeft = amountLeft;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<TransactionRecordingModel> getTransactionRecordingModelList() {
        return transactionRecordingModelList;
    }

    public void setTransactionRecordingModelList(List<TransactionRecordingModel> transactionRecordingModelList) {
        this.transactionRecordingModelList = transactionRecordingModelList;
    }
}


