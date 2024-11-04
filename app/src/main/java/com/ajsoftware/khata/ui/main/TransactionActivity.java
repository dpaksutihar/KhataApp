package com.ajsoftware.khata.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.adapters.ConsumerManagementAdapter;
import com.ajsoftware.khata.databinding.ActivityTransactionBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class TransactionActivity extends AppCompatActivity {
    ActivityTransactionBinding binding;

    String[] ConsumerType = {"Vendor", "Customer"};
    ConsumerManagementAdapter consumerManagementAdapter;
    ArrayList<ConsumerModel> consumerModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));

        setSpinner();
        setClicks();
    }

    private void setClicks() {
        binding.backIcon.setOnClickListener(v -> finish());

    }

    private void setSpinner() {

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(TransactionActivity.this, android.R.layout.simple_list_item_1, ConsumerType);
        binding.consumerType.setAdapter(typeAdapter);

        // Set an initial value for Constants.Customer_click
        Constants.Customer_click = "Vendor";

        setUpRecyclerView(ConsumerType[0]);

        binding.consumerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                consumerModelArrayList.clear();
                if (consumerManagementAdapter != null) {
                    consumerManagementAdapter.notifyDataSetChanged();
                }

                if (position == 0) {
                    Constants.Customer_click = "Vendor";
                } else if (position == 1) {
                    Constants.Customer_click = "Customer";
                }

                setUpRecyclerView(ConsumerType[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setUpRecyclerView(String type) {

        consumerModelArrayList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(type).addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                consumerModelArrayList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    ConsumerModel consumerModel = documentSnapshot.toObject(ConsumerModel.class);
                    consumerModelArrayList.add(consumerModel);
                }

                Collections.sort(consumerModelArrayList, (model1, model2) ->
                        Long.compare(model2.getTimestamp(), model1.getTimestamp())
                );
                consumerManagementAdapter = new ConsumerManagementAdapter(TransactionActivity.this, consumerModelArrayList, new ConsumerManagementAdapter.OptionsClick() {

                    @Override
                    public void linkTrans(ConsumerModel consumerModel) {
//                        OpenBottomSheet(consumerModel);
                    }

                    @Override
                    public void editConsumer(ConsumerModel consumerModel) {
                        startActivity(new Intent(TransactionActivity.this, AddConsumerAccount.class).putExtra("MODEL", consumerModel).putExtra(Constants.CUSTOMER_TYPE, type));
                    }

                    @Override
                    public void delConsumer(ConsumerModel consumerModel) {
//                        if (consumerModel.getAmount().equals("0")) {
//                            waitingDialog.show();
//                            FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.CONSUMERS).document(consumerModel.getId()).delete().addOnSuccessListener(unused -> {
//                                waitingDialog.dismiss();
//                                ToastUtils.successToast(TransactionActivity.this, "Deleted");
//                                bottomSheetDialog.dismiss();
//                                getConsumers();
//                            }).addOnFailureListener(e -> {
//                                waitingDialog.dismiss();
//                                ToastUtils.errorToast(TransactionActivity.this, e.getMessage());
//                            });
//                        } else {
//                            ToastUtils.errorToast(TransactionActivity.this, "UnLink transactions to proceed");
//                        }
                    }

                    @Override
                    public void viewAccount(ConsumerModel consumerModel) {
                        startActivity(new Intent(TransactionActivity.this, ViewAccountActivity.class).putExtra("MODEL", consumerModel).putExtra(Constants.CUSTOMER_TYPE, type));
                    }
                }, type);

                binding.recyclerView.setAdapter(consumerManagementAdapter);
                consumerManagementAdapter.notifyDataSetChanged();
            }
        });

    }
}