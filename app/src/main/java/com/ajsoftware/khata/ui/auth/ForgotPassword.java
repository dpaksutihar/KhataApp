package com.ajsoftware.khata.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ActivityForgotPasswordBinding;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    ActivityForgotPasswordBinding binding;
    WaitingDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SetDecorView();
        clicks();
    }

    private void clicks() {

        waitingDialog = new WaitingDialog(ForgotPassword.this);

        binding.backIcon.setOnClickListener(v -> finish());

        binding.sendBtn.setOnClickListener(view -> {

            String email = binding.etEmailAddress.getText().toString().trim();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmailAddress.setError(getString(R.string.valid_email_is_required));
                binding.etEmailAddress.requestFocus();
            } else {
                waitingDialog.show();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(unused -> {
                    waitingDialog.dismiss();
                    ToastUtils.successToast(ForgotPassword.this, getString(R.string.reset_link_sent_to_your_email));
                    finish();
                }).addOnFailureListener(e -> {
                    waitingDialog.dismiss();
                    ToastUtils.errorToast(ForgotPassword.this, e.getMessage());
                });
            }
        });
    }

    private void SetDecorView() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
    }

}