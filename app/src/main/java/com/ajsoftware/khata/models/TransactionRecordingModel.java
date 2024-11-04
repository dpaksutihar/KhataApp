package com.ajsoftware.khata.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
@Entity(tableName = "transaction_data")
public class TransactionRecordingModel implements Serializable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    String transType, amountPaid, date, amount, desc, status;
    private long timestamp;
    private String totalBalance;


    public TransactionRecordingModel() {

    }

    public TransactionRecordingModel(String id, String transType, String amountPaid, String date, String amount, String desc, String status, long timestamp) {
        this.id = id;
        this.transType = transType;
        this.amountPaid = amountPaid;
        this.date = date;
        this.amount = amount;
        this.desc = desc;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(String totalBalance) {
        this.totalBalance = totalBalance;
    }




}
