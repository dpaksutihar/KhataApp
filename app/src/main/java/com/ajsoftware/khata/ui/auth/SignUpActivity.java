package com.ajsoftware.khata.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ActivitySignUpBinding;
import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {
    ActivitySignUpBinding binding;
    WaitingDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        transparent_Status_And_Navigation();
        clicks();
    }

    private void clicks() {

        password_Toggle();

        waitingDialog = new WaitingDialog(SignUpActivity.this);

        binding.signupBtn.setOnClickListener(v -> {

            String first_name = binding.etFirstName.getText().toString().trim();
            String phoneNo = "+"+binding.ccp.getSelectedCountryCode() + binding.etPhone.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();

            if (areValid(first_name, email, phoneNo, password)) {

                waitingDialog.show();

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                    waitingDialog.dismiss();
                    ToastUtils.errorToast(SignUpActivity.this, "Email already exists");
                    FirebaseAuth.getInstance().signOut();
                }).addOnFailureListener(e -> {
                    waitingDialog.dismiss();
                    UserModel userModel = new UserModel(first_name, "", email, phoneNo, String.valueOf(System.currentTimeMillis()), "active");
                    startActivity(new Intent(SignUpActivity.this, OtpActivity.class).putExtra(Constants.MODEL, userModel).putExtra(Constants.PASSWORD, password));
                });

            }
        });

        binding.backArrow.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class).putExtra("loginAs", "User"));
            finish();
        });

        binding.backToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private boolean areValid(String firstName, String email, String phoneNo, String password) {
        if (firstName.isEmpty()) {
            binding.etFirstName.setError(getString(R.string.first_name_is_required));
            binding.etFirstName.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.valid_email_is_required));
            binding.etEmail.requestFocus();
            binding.etFirstName.setError(null);
            return false;
        }
        if (!Patterns.PHONE.matcher(phoneNo).matches()) {
            binding.etPhone.setError(getString(R.string.valid_phone_is_required));
            binding.etPhone.requestFocus();
            binding.etEmail.setError(null);
            return false;
        }
        if (password.length() < 6) {
            binding.etPassword.setError(getString(R.string._6_characters_are_required));
            binding.etPassword.requestFocus();
            binding.etPhone.setError(null);
            return false;
        } else {
            return true;
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private void transparent_Status_And_Navigation() {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            set_Window_Flag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            set_Window_Flag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
    }

    private void set_Window_Flag(final int bits, boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void password_Toggle() {
        binding.visibilityOn.setOnClickListener(v -> {
            binding.visibilityOn.setVisibility(View.GONE);
            binding.visibilityOff.setVisibility(View.VISIBLE);
            binding.etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        });
        binding.visibilityOff.setOnClickListener(v -> {
            binding.visibilityOn.setVisibility(View.VISIBLE);
            binding.visibilityOff.setVisibility(View.GONE);
            binding.etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}