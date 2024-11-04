package com.ajsoftware.khata.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ActivityTransactionRecordingBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.Objects;

public class TransactionRecordingActivity extends AppCompatActivity {

    ActivityTransactionRecordingBinding binding;
    String[] transactionType = {"Sale", "Purchase"};
    //    String[] category = {"Office Supplies", "Rent", "Sales", "Others"};
    WaitingDialog waitingDialog;
    String CUSTOMER_TYPE, id = "";
    ConsumerModel intentConsumerModel;
    TransactionRecordingModel transactionRecordingModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTransactionRecordingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));

        setViews();
        getFromIntent();
        setClicks();
    }

    @SuppressLint("SetTextI18n")
    private void getFromIntent() {

        if (getIntent().hasExtra("MODEL")) {
            intentConsumerModel = (ConsumerModel) getIntent().getSerializableExtra("MODEL");
        }

        if (getIntent().hasExtra(Constants.CUSTOMER_TYPE)) {
            CUSTOMER_TYPE = getIntent().getStringExtra(Constants.CUSTOMER_TYPE);
        }

        if (getIntent().hasExtra("EDIT_MODEL")) {
            transactionRecordingModel = (TransactionRecordingModel) getIntent().getSerializableExtra("EDIT_MODEL");
            assert transactionRecordingModel != null;
            setViewsForEdit(transactionRecordingModel);
        }

    }

    @SuppressLint("SetTextI18n")
    private void setViewsForEdit(TransactionRecordingModel transactionRecordingModel) {

        binding.etAmount.setText(transactionRecordingModel.getAmount());
        binding.etAmountPaid.setText(transactionRecordingModel.getAmountPaid());
        binding.etDescription.setText(transactionRecordingModel.getDesc());
        binding.date.setText(transactionRecordingModel.getDate());

        id = transactionRecordingModel.getId();

        if (transactionRecordingModel.getTransType().equals(transactionType[0])) {
            binding.transactionType.setSelection(0);
        } else if (transactionRecordingModel.getTransType().equals(transactionType[1])) {
            binding.transactionType.setSelection(1);
        }

        binding.btnAdd.setText("Update");

    }

    private void setViews() {
        ArrayAdapter<String> tranAdapter = new ArrayAdapter<>(TransactionRecordingActivity.this, android.R.layout.simple_list_item_1, transactionType);
        binding.transactionType.setAdapter(tranAdapter);
    }

    @SuppressLint("SetTextI18n")
    private void setClicks() {

        waitingDialog = new WaitingDialog(TransactionRecordingActivity.this);

        binding.backIcon.setOnClickListener(view -> finish());

        binding.date.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(TransactionRecordingActivity.this);
            datePickerDialog.setOnDateSetListener((datePicker, year, month, day) -> binding.date.setText(day + "/" + (month + 1) + "/" + year));
            datePickerDialog.show();
        });

        binding.addNow.setOnClickListener(view -> {

            String amount = binding.etAmount.getText().toString();
            String amountPaid = binding.etAmountPaid.getText().toString();
            String desc = binding.etDescription.getText().toString();
            String date = binding.date.getText().toString();

            if (date.equals("Tap to select date")) {
                binding.date.setError("Required");
                binding.date.requestFocus();
            } else if (TextUtils.isEmpty(amount) || Integer.parseInt(amount) < 0) {
                binding.etAmount.setError("Valid amount is Required");
                binding.etAmount.requestFocus();
                binding.date.setError(null);
            }
            else if (TextUtils.isEmpty(amountPaid) || Integer.parseInt(amountPaid) < 0) {
                binding.etAmountPaid.setError("Valid amount is Required");
                binding.etAmountPaid.requestFocus();
                binding.etAmount.setError(null);
            }

            else if (TextUtils.isEmpty(desc)) {
                binding.etDescription.setError("Required");
                binding.etDescription.requestFocus();
                binding.etAmount.setError(null);
            } else {
                waitingDialog.show();
                if (id.equals("")) {
                    id = String.valueOf(System.currentTimeMillis());
                }
                long currentTimeMillis = System.currentTimeMillis();

                TransactionRecordingModel model = new TransactionRecordingModel(id, binding.transactionType.getSelectedItem().toString(), amountPaid, date, amount, desc, "", currentTimeMillis);
                FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(CUSTOMER_TYPE).document(intentConsumerModel.getId()).collection(Constants.TRAMS).document(id).set(model).addOnSuccessListener(unused -> {
                    waitingDialog.dismiss();
                    ToastUtils.successToast(TransactionRecordingActivity.this, "Transaction Added");


                    finish();


                }).addOnFailureListener(e -> {
                    waitingDialog.dismiss();
                    ToastUtils.successToast(TransactionRecordingActivity.this, e.getMessage());
                });
            }
        });

    }
}