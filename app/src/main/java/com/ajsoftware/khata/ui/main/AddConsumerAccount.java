package com.ajsoftware.khata.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ActivityAddConsumerAccountBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Objects;

public class AddConsumerAccount extends AppCompatActivity {

    ActivityAddConsumerAccountBinding binding;
    WaitingDialog waitingDialog;
    ConsumerModel intentConsumerModel;
    String CUSTOMER_TYPE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddConsumerAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));

        getFromIntent();
        setClicks();
    }

    @SuppressLint("SetTextI18n")
    private void getFromIntent() {

        if (getIntent().hasExtra("MODEL")) {
            intentConsumerModel = (ConsumerModel) getIntent().getSerializableExtra("MODEL");
            binding.etName.setText(intentConsumerModel.getName());
            binding.etPhoneNo.setText(intentConsumerModel.getPhoneNo());
            binding.etAddress.setText(intentConsumerModel.getAddress());
            binding.btnAdd.setText("Update Now");
            binding.header.setText("Update " + intentConsumerModel.getName());
        }

        if (getIntent().hasExtra(Constants.CUSTOMER_TYPE)) {
            CUSTOMER_TYPE = getIntent().getStringExtra(Constants.CUSTOMER_TYPE);

            if (CUSTOMER_TYPE.equals("Vendor")){
                binding.header.setText("Add New Vendor");
            }else if (CUSTOMER_TYPE.equals("Customer")){

                binding.header.setText("Add New Customer");
            }

        }

    }

    private void setClicks() {

        waitingDialog = new WaitingDialog(AddConsumerAccount.this);

        binding.backIcon.setOnClickListener(v -> finish());

        binding.addCustomer.setOnClickListener(view -> {
            String phoneRegex = "^[+]?[0-9]{7,15}$";
            String customer_name = binding.etName.getText().toString();
            String customer_phone = "+"+binding.ccp.getSelectedCountryCode()+binding.etPhoneNo.getText().toString();
            String customer_address = binding.etAddress.getText().toString();

            if (TextUtils.isEmpty(customer_name)) {
                binding.etName.setError("Required");
                binding.etName.requestFocus();
            } else if (!customer_phone.matches(phoneRegex)) {
                binding.etPhoneNo.setError("Invalid");
                binding.etPhoneNo.requestFocus();
            } else if (TextUtils.isEmpty(customer_address)) {
                binding.etAddress.setError("Required");
                binding.etAddress.requestFocus();
            } else {
                waitingDialog.show();

                String id, status;
                ConsumerModel addCustomerModel;

                if (binding.btnAdd.getText().toString().equals("Update Now")) {
                    status = CUSTOMER_TYPE+" Updated";
                    id = intentConsumerModel.getId();
                    addCustomerModel = new ConsumerModel(intentConsumerModel.getId(), customer_name, customer_phone, customer_address, intentConsumerModel.getAmount(),intentConsumerModel.getAmount_paid(),intentConsumerModel.getAmount_left(),intentConsumerModel.getTimestamp());
                } else {
                    status = CUSTOMER_TYPE+" Added";
                    id = String.valueOf(System.currentTimeMillis());

                    addCustomerModel = new ConsumerModel(id, customer_name, customer_phone, customer_address, "0","0","0",System.currentTimeMillis());
                }

                FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(CUSTOMER_TYPE).document(id).set(addCustomerModel).addOnSuccessListener(documentReference -> {
                    waitingDialog.dismiss();
                    ToastUtils.successToast(AddConsumerAccount.this, status);
                    finish();
                }).addOnFailureListener(e -> {
                    waitingDialog.dismiss();
                    ToastUtils.successToast(AddConsumerAccount.this, e.getMessage());
                });
            }
        });

    }
}