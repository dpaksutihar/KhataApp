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
import com.ajsoftware.khata.databinding.ActivityLoginBinding;
import com.ajsoftware.khata.ui.main.AdminMainActivity;
import com.ajsoftware.khata.ui.main.HomeActivity;
import com.ajsoftware.khata.utils.ToastUtils;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    WaitingDialog waitingDialog;
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setViews();
        transparent_Status_And_Navigation();
        clicks();
    }

    private void setViews() {

        type = getIntent().getStringExtra("loginAs").toString();

        if (type.equals("Admin")) {
            binding.forgotPassword.setVisibility(View.INVISIBLE);
            binding.backToSignUp.setVisibility(View.INVISIBLE);
        }

    }

    private void clicks() {

        password_Toggle();

        waitingDialog = new WaitingDialog(LoginActivity.this);

        binding.backToSignUp.setOnClickListener(v -> finish());

        binding.forgotPassword.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPassword.class)));

        binding.loginBtn.setOnClickListener(view -> {

            String email = binding.etEmailAddress.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (areValid(email, password)) {

                if (type.equals("Admin")) {

                    if (email.equals("admin@admin.com")) {
                        waitingDialog.show();
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                            waitingDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, AdminMainActivity.class));
                            finishAffinity();
                        }).addOnFailureListener(e -> {
                            waitingDialog.dismiss();
                            ToastUtils.errorToast(LoginActivity.this, e.getMessage());
                        });
                    } else {
                        ToastUtils.errorToast(LoginActivity.this, "Invalid login");
                    }

                } else {

                    if (email.equals("admin@admin.com")) {
                        ToastUtils.errorToast(LoginActivity.this, "Invalid login");
                    } else {
                        waitingDialog.show();
                        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
                            waitingDialog.dismiss();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finishAffinity();
                        }).addOnFailureListener(e -> {
                            waitingDialog.dismiss();
                            ToastUtils.errorToast(LoginActivity.this, e.getMessage());
                        });
                    }
                }
            }
        });
    }

    private boolean areValid(String email, String password) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmailAddress.setError(getString(R.string.valid_email_is_required));
            binding.etEmailAddress.requestFocus();
            return false;
        }
        if (password.length() < 6) {
            binding.etPassword.setError(getString(R.string._6_characters_are_required));
            binding.etPassword.requestFocus();
            binding.etEmailAddress.setError(null);
            return false;
        } else {
            return true;
        }
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


}