package com.ajsoftware.khata.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.adapters.ConsumerManagementAdapter;
import com.ajsoftware.khata.adapters.TransactionAdapter;
import com.ajsoftware.khata.databinding.ActivityBalanceBinding;
import com.ajsoftware.khata.databinding.BottomTransactionBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class BalanceActivity extends AppCompatActivity {

    ActivityBalanceBinding binding;
    ConsumerManagementAdapter consumerManagementAdapter;
    ArrayList<ConsumerModel> consumerModelArrayList;
    BottomSheetDialog bottomSheetDialog;
    TransactionAdapter transactionAdapter;
    ArrayList<TransactionRecordingModel> transactionRecordingModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBalanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));

        setClicks();
        getConsumers();
    }
    private void setClicks() {
         binding.backIcon.setOnClickListener(view -> finish());
    }
    @SuppressLint("NotifyDataSetChanged")
    private void getConsumers() {

        consumerModelArrayList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.CONSUMERS).addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                consumerModelArrayList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    ConsumerModel consumerModel = documentSnapshot.toObject(ConsumerModel.class);
                    consumerModelArrayList.add(consumerModel);
                }
                consumerManagementAdapter = new ConsumerManagementAdapter(BalanceActivity.this, consumerModelArrayList,new ConsumerManagementAdapter.OptionsClick() {

                    @Override
                    public void linkTrans(ConsumerModel consumerModel) {

                     }

                    @Override
                    public void editConsumer(ConsumerModel consumerModel) {
                     }

                    @Override
                    public void delConsumer(ConsumerModel consumerModel) {

                    }

                    @Override
                    public void viewAccount(ConsumerModel consumerModel) {
                        OpenBottomSheet(consumerModel);
                    }
                },Constants.TRAMS);

                binding.recyclerView.setAdapter(consumerManagementAdapter);
                consumerManagementAdapter.notifyDataSetChanged();
            }
        });

    }

    private void OpenBottomSheet(ConsumerModel consumerModel) {

        bottomSheetDialog = new BottomSheetDialog(BalanceActivity.this);
        BottomTransactionBinding bottomTransactionBinding = BottomTransactionBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(bottomTransactionBinding.getRoot());

        transactionRecordingModelArrayList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.TRAMS).addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                transactionRecordingModelArrayList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
//                    if (transactionRecordingModel.getAssociated_to().equals(consumerModel.getId())) {
                        transactionRecordingModelArrayList.add(transactionRecordingModel);
//                    }
                }
                transactionAdapter = new TransactionAdapter(BalanceActivity.this, transactionRecordingModelArrayList, new TransactionAdapter.OptionClick() {
                    @Override
                    public void onAddClick(TransactionRecordingModel transactionRecordingModel) {

                    }

                    @Override
                    public void onRemoveClick(TransactionRecordingModel transactionRecordingModel) {

                    }
                },Constants.TRAMS);

                if (transactionRecordingModelArrayList.size() == 0) {
                    bottomTransactionBinding.emptyLayout.setVisibility(View.VISIBLE);
                } else {
                    bottomTransactionBinding.emptyLayout.setVisibility(View.GONE);
                }

                bottomTransactionBinding.recyclerView.setAdapter(transactionAdapter);
                transactionAdapter.notifyDataSetChanged();

            }
        });

        bottomSheetDialog.show();

    }

}