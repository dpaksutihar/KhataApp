package com.ajsoftware.khata.models;

import com.ajsoftware.khata.models.roomdatabase.ConsumerEntity;
import com.ajsoftware.khata.models.roomdatabase.TransactionEntity;
import com.ajsoftware.khata.models.roomdatabase.UserDataEntity;

import java.util.List;

// BackupModel.java
public class BackupModel {
    private List<UserDataEntity> users;
    private List<ConsumerEntity> consumers;
    private List<TransactionEntity> transactions;

    public List<UserDataEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserDataEntity> users) {
        this.users = users;
    }

    public List<ConsumerEntity> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<ConsumerEntity> consumers) {
        this.consumers = consumers;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }
}

