package com.ajsoftware.khata.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.adapters.ConsumerManagementAdapter;
import com.ajsoftware.khata.adapters.TransactionAdapter;
import com.ajsoftware.khata.databinding.ActivityConsumerManagementBinding;
import com.ajsoftware.khata.databinding.BottomTransactionBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerManagementActivity extends AppCompatActivity {

    ActivityConsumerManagementBinding binding;
    WaitingDialog waitingDialog;
    ConsumerManagementAdapter consumerManagementAdapter;
    ArrayList<ConsumerModel> consumerModelArrayList;

    int amountList = 0;
    int paidList = 0;
    int balanceList = 0;

    BottomSheetDialog bottomSheetDialog;
    TransactionAdapter transactionAdapter;
    ArrayList<TransactionRecordingModel> transactionRecordingModelArrayList;
    String CUSTOMER_TYPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConsumerManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));

        getFromIntent();
        setClicks();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getConsumers();
        calculateTotal();
    }

    private void calculateTotal() {

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(CUSTOMER_TYPE)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                         amountList = 0;
                         paidList = 0;
                         balanceList = 0;

                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){

                            ConsumerModel consumerModel = documentSnapshot.toObject(ConsumerModel.class);
                            String id = consumerModel.getId();

                            FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(CUSTOMER_TYPE)
                                    .document(id).collection(Constants.TRAMS).get()
                                    .addOnSuccessListener(queryDocumentSnapshots1 -> {

                                        if (queryDocumentSnapshots1 !=null && !queryDocumentSnapshots1.isEmpty()){

                                            for (DocumentSnapshot documentSnapshot1 :  queryDocumentSnapshots1.getDocuments()){

                                                TransactionRecordingModel transactionRecordingModel = documentSnapshot1.toObject(TransactionRecordingModel.class);

                                                amountList+=Integer.parseInt(transactionRecordingModel.getAmount());
                                                paidList+= Integer.parseInt(transactionRecordingModel.getAmountPaid());
                                                balanceList+=Integer.parseInt(transactionRecordingModel.getAmount()) - Integer.parseInt(transactionRecordingModel.getAmountPaid());
                                            }

                                            updateTotalAmountTextView(amountList);
                                            updateTotalAmountPaidTextView(paidList);
                                            updateTotalBalanceTextView(balanceList);

                                        }

                                    });

                        }

                    }

                });


    }

    @SuppressLint("SetTextI18n")
    private void getFromIntent() {

        CUSTOMER_TYPE = getIntent().getStringExtra(Constants.CUSTOMER_TYPE);

        if (CUSTOMER_TYPE.equals(Constants.VENDOR)) {
            binding.type.setText("Add a Vendor");
            binding.searchEt.setHint("Search Vendor");
            binding.topBar.setText("Vendor Management");
        }
        if (CUSTOMER_TYPE.equals(Constants.CUSTOMER)) {
            binding.type.setText("Add a Customer");
            binding.searchEt.setHint("Search Customer");
            binding.topBar.setText("Customer Management");
        }
    }

    private void setClicks() {

        waitingDialog = new WaitingDialog(ConsumerManagementActivity.this);

        binding.searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    searchCustomers(s.toString());
                } else {
                    getConsumers();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.backIcon.setOnClickListener(view -> finish());
        binding.addConsumer.setOnClickListener(view -> startActivity(new Intent(ConsumerManagementActivity.this, AddConsumerAccount.class).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE)));
    }

    private void searchCustomers(String search) {

        consumerModelArrayList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(CUSTOMER_TYPE).addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                consumerModelArrayList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    ConsumerModel consumerModel = documentSnapshot.toObject(ConsumerModel.class);
                    if (consumerModel.getName().toLowerCase().contains(search.toLowerCase())) {
                        consumerModelArrayList.add(consumerModel);
                    }
                }
                consumerManagementAdapter = new ConsumerManagementAdapter(ConsumerManagementActivity.this, consumerModelArrayList, new ConsumerManagementAdapter.OptionsClick() {

                    @Override
                    public void linkTrans(ConsumerModel consumerModel) {
                        OpenBottomSheet(consumerModel);
                    }

                    @Override
                    public void editConsumer(ConsumerModel consumerModel) {
                        startActivity(new Intent(ConsumerManagementActivity.this, AddConsumerAccount.class).putExtra("MODEL", consumerModel).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE));
                    }

                    @Override
                    public void delConsumer(ConsumerModel consumerModel) {
                        if (consumerModel.getAmount().equals("0")) {
                            waitingDialog.show();
                            FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.CONSUMERS).document(consumerModel.getId()).delete().addOnSuccessListener(unused -> {
                                waitingDialog.dismiss();
                                ToastUtils.successToast(ConsumerManagementActivity.this, "Deleted");
                                bottomSheetDialog.dismiss();
                                getConsumers();
                            }).addOnFailureListener(e -> {
                                waitingDialog.dismiss();
                                ToastUtils.errorToast(ConsumerManagementActivity.this, e.getMessage());
                            });
                        } else {
                            ToastUtils.errorToast(ConsumerManagementActivity.this, "UnLink transactions to proceed");
                        }
                    }

                    @Override
                    public void viewAccount(ConsumerModel consumerModel) {
                        startActivity(new Intent(ConsumerManagementActivity.this, ViewAccountActivity.class).putExtra("MODEL", consumerModel).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE));
                    }
                }, CUSTOMER_TYPE);

                binding.recyclerView.setAdapter(consumerManagementAdapter);
                consumerManagementAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getConsumers() {

        consumerModelArrayList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(CUSTOMER_TYPE).addSnapshotListener((value, error) -> {
                    if (value != null && !value.isEmpty()) {
                        consumerModelArrayList.clear();
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            ConsumerModel consumerModel = documentSnapshot.toObject(ConsumerModel.class);
                            consumerModelArrayList.add(consumerModel);


                        }

//                        Collections.sort(consumerModelArrayList, (consumer1, consumer2) -> consumer1.getName().compareToIgnoreCase(consumer2.getName()));

                        Collections.sort(consumerModelArrayList, (model1, model2) ->
                                Long.compare(model2.getTimestamp(), model1.getTimestamp())
                        );

                        consumerManagementAdapter = new ConsumerManagementAdapter(ConsumerManagementActivity.this, consumerModelArrayList, new ConsumerManagementAdapter.OptionsClick() {

                            @Override
                            public void linkTrans(ConsumerModel consumerModel) {
                                OpenBottomSheet(consumerModel);
                            }

                            @Override
                            public void editConsumer(ConsumerModel consumerModel) {
                                startActivity(new Intent(ConsumerManagementActivity.this, AddConsumerAccount.class).putExtra("MODEL", consumerModel).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE));
                            }

                            @Override
                            public void delConsumer(ConsumerModel consumerModel) {
                                if (consumerModel.getAmount().equals("0")) {
                                    waitingDialog.show();
                                    FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.CONSUMERS).document(consumerModel.getId()).delete().addOnSuccessListener(unused -> {
                                        waitingDialog.dismiss();
                                        ToastUtils.successToast(ConsumerManagementActivity.this, "Deleted");
                                        bottomSheetDialog.dismiss();
                                        getConsumers();
                                    }).addOnFailureListener(e -> {
                                        waitingDialog.dismiss();
                                        ToastUtils.errorToast(ConsumerManagementActivity.this, e.getMessage());
                                    });
                                } else {
                                    ToastUtils.errorToast(ConsumerManagementActivity.this, "UnLink transactions to proceed");
                                }
                            }

                            @Override
                            public void viewAccount(ConsumerModel consumerModel) {
                                startActivity(new Intent(ConsumerManagementActivity.this, ViewAccountActivity.class).putExtra("MODEL", consumerModel).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE));
                            }
                        }, CUSTOMER_TYPE);

                        binding.recyclerView.setAdapter(consumerManagementAdapter);
                        consumerManagementAdapter.notifyDataSetChanged();
                    }
                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void OpenBottomSheet(ConsumerModel consumerModel) {

        bottomSheetDialog = new BottomSheetDialog(ConsumerManagementActivity.this);
        BottomTransactionBinding bottomTransactionBinding = BottomTransactionBinding.inflate(getLayoutInflater());
        bottomSheetDialog.setContentView(bottomTransactionBinding.getRoot());

        transactionRecordingModelArrayList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.TRAMS).addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                transactionRecordingModelArrayList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
//                    if (transactionRecordingModel.getAssociated_to().equals("") || transactionRecordingModel.getAssociated_to().equals(consumerModel.getId())) {
                    transactionRecordingModelArrayList.add(transactionRecordingModel);
//                    }
                }
                transactionAdapter = new TransactionAdapter(ConsumerManagementActivity.this, transactionRecordingModelArrayList, new TransactionAdapter.OptionClick() {
                    @Override
                    public void onAddClick(TransactionRecordingModel transactionRecordingModel) {

                        waitingDialog.show();
                        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                .collection(Constants.TRAMS).document(transactionRecordingModel.getId())
                                .update(Constants.AssociatedField, consumerModel.getId(),
                                        Constants.STATUS, transactionRecordingModel.getStatus(),
                                        Constants.DATE, getDate())
                                .addOnSuccessListener(unused -> {
                                    waitingDialog.dismiss();
                                    ToastUtils.successToast(ConsumerManagementActivity.this, "Updated");
                                    bottomSheetDialog.dismiss();
                                    getConsumers();
                                }).addOnFailureListener(e -> {
                                    waitingDialog.dismiss();
                                    ToastUtils.errorToast(ConsumerManagementActivity.this, e.getMessage());
                                });
                    }

                    @Override
                    public void onRemoveClick(TransactionRecordingModel transactionRecordingModel) {
                        waitingDialog.show();
                        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.TRAMS)
                                .document(transactionRecordingModel.getId()).update(Constants.AssociatedField, "", Constants.STATUS, "").addOnSuccessListener(unused -> {
                                    waitingDialog.dismiss();
                                    ToastUtils.successToast(ConsumerManagementActivity.this, "Updated");
                                    bottomSheetDialog.dismiss();
                                    getConsumers();
                                }).addOnFailureListener(e -> {
                                    waitingDialog.dismiss();
                                    ToastUtils.errorToast(ConsumerManagementActivity.this, e.getMessage());
                                });
                    }
                }, Constants.CONSUMERS);

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

    private String getDate() {
        Date date = new Date(System.currentTimeMillis());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("d/MM/yyyy");
        return formatter.format(date);
    }


    private void updateTotalAmountTextView(int totalAmount) {
        binding.amount.setText("Amount: " + totalAmount + " NRs");
    }

    private void updateTotalAmountPaidTextView(int totalAmountPaid) {

        if (Constants.Customer_click.equals("Vendor")) {
            binding.paid.setText("Paid: " + totalAmountPaid + " NRs");
        }
        else if (Constants.Customer_click.equals("Customer")){
            binding.paid.setText("Received: " + totalAmountPaid + " NRs");
        }
        }

    private void updateTotalBalanceTextView(int totalBalance) {
        int arrowDrawable = R.drawable.received_icon_small;

        if (CUSTOMER_TYPE.equals(Constants.VENDOR)) {
            arrowDrawable = R.drawable.arrow_send_small;
        }
        if (totalBalance == 0) {
            binding.balance.setText("0 NRs");
        } else {
            binding.balance.setCompoundDrawablesWithIntrinsicBounds(arrowDrawable, 0, 0, 0);
            binding.balance.setText(totalBalance + " NRs");
        }


    }



}