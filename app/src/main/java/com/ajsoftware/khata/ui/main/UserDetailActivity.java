package com.ajsoftware.khata.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ActivityUserDetailBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserDetailActivity extends AppCompatActivity {

    ActivityUserDetailBinding binding;
    UserModel userModel;
    WaitingDialog waitingDialog;
    String STATUS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));

        getDataFromIntent();
        setClicks();
    }

    private void setClicks() {

        binding.backIcon.setOnClickListener(view -> finish());

        binding.toggleAccount.setOnClickListener(v -> {
            if (userModel.getStatus().equals(Constants.ACTIVE)) {
                STATUS = Constants.NOT_ACTIVE;
            } else if (userModel.getStatus().equals(Constants.NOT_ACTIVE)) {
                STATUS = Constants.ACTIVE;
            }
            updateStatus(STATUS);
        });
    }

    private void updateStatus(String status) {

        waitingDialog = new WaitingDialog(UserDetailActivity.this);
        waitingDialog.show();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(userModel.getUid()).update("status", status).addOnSuccessListener(unused -> {
            waitingDialog.dismiss();
            ToastUtils.successToast(UserDetailActivity.this, "Updated");
            finish();
        });

    }

    @SuppressLint("SetTextI18n")
    private void getDataFromIntent() {

        userModel = (UserModel) getIntent().getSerializableExtra(Constants.MODEL);

        binding.name.setText(userModel.getName());

        binding.etName.setText(userModel.getName());
        binding.etPhoneNo.setText(userModel.getPhoneNo());
        binding.etEmail.setText(userModel.getEmail());
        binding.createdOn.setText(getDate(userModel.getCreated_on()));
        if (userModel.getStatus().equals(Constants.ACTIVE)) {
            STATUS = Constants.ACTIVE;
            binding.accountStatus.setText("Active");
            binding.toggleAccount.setCardBackgroundColor(getColor(R.color.incomeColor));
        } else if (userModel.getStatus().equals(Constants.NOT_ACTIVE)) {
            STATUS = Constants.NOT_ACTIVE;
            binding.accountStatus.setText("Not Active");
            binding.toggleAccount.setCardBackgroundColor(getColor(R.color.expenseColor));
        }

    }

    private String getDate(String timeStamp) {
        Date date = new Date(Long.parseLong(timeStamp));
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd MMM, yyyy");
        return formatter.format(date);
    }
}