package com.ajsoftware.khata.ui.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import com.ajsoftware.khata.databinding.ActivityOtpBinding;
import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.ui.main.HomeActivity;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    ActivityOtpBinding binding;
    WaitingDialog waitingDialog;
    UserModel userModel;
    String VerificationID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getDataFromIntent();
        setClicks();

    }

    @SuppressLint("SetTextI18n")
    private void getDataFromIntent() {

        userModel = (UserModel) getIntent().getSerializableExtra(Constants.MODEL);
        binding.phone.setText("Verify your OTP for " + userModel.getPhoneNo());

        StartCountdown();
        sendCode(userModel.getPhoneNo());

    }

    private void sendCode(String phoneNo) {

        waitingDialog = new WaitingDialog(OtpActivity.this);

        PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance()).setActivity(OtpActivity.this).setTimeout(60L, TimeUnit.SECONDS).setPhoneNumber(phoneNo).setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential onVerificationCompleted) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                ToastUtils.errorToast(OtpActivity.this, "VERIFICATION FAILED" + e.getMessage());
                Log.i("VERIFICATION FAILED", e.getMessage());
            }

            @Override
            public void onCodeSent(@NonNull String verifyID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verifyID, forceResendingToken);

                VerificationID = verifyID;
                ToastUtils.successToast(OtpActivity.this, "Verification Code Sent");

            }
        }).build();

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
    }

    private void StartCountdown() {

        CountDownTimer timer = new CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                binding.counter.setText("Resend (" + (millisUntilFinished / 1000) + ")");
                binding.counter.setVisibility(View.VISIBLE);
                binding.resendPin.setVisibility(View.GONE);
                binding.verifyBtn.setVisibility(View.VISIBLE);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                binding.verifyBtn.setVisibility(View.VISIBLE);
                binding.resendPin.setVisibility(View.VISIBLE);
            }
        };
        timer.start();
    }

    private void setClicks() {

        waitingDialog = new WaitingDialog(OtpActivity.this);

        binding.backIcon.setOnClickListener(view -> finish());

        binding.resendPin.setOnClickListener(v -> sendCode(userModel.getPhoneNo()));

        binding.verifyBtn.setOnClickListener(v -> {

            String otp = binding.pinView.getText().toString();

            if (otp.length() == 6) {
                VerifyCode(otp);
            } else {
                ToastUtils.errorToast(OtpActivity.this, "OTP is required");
            }

        });

    }

    private void VerifyCode(String otp) {
        waitingDialog.show();
        PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(VerificationID, otp);
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnSuccessListener(authResult1 -> {
            FirebaseAuth.getInstance().signOut();
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(userModel.getEmail(), getIntent().getStringExtra(Constants.PASSWORD).toString()).addOnSuccessListener(authResult -> {
                userModel.setUid((Objects.requireNonNull(FirebaseAuth.getInstance().getUid())));
                FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).set(userModel).addOnSuccessListener(unused -> {
                    waitingDialog.dismiss();
                    startActivity(new Intent(OtpActivity.this, HomeActivity.class));
                    finishAffinity();
                });
            }).addOnFailureListener(e -> {
                waitingDialog.dismiss();
                ToastUtils.errorToast(OtpActivity.this, e.getMessage() + "");
            });
        });
    }
}