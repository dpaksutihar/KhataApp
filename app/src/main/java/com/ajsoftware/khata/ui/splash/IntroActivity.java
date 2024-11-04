package com.ajsoftware.khata.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ajsoftware.khata.databinding.ActivityIntroBinding;
import com.ajsoftware.khata.ui.auth.LoginActivity;
import com.ajsoftware.khata.ui.auth.SignUpActivity;

public class IntroActivity extends AppCompatActivity {
    ActivityIntroBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        click_listeners();
        transparent_Status_And_Navigation();
    }

    private void click_listeners() {

        binding.adminLoginBtn.setOnClickListener(v -> startActivity(new Intent(IntroActivity.this, LoginActivity.class).putExtra("loginAs", "Admin")));
        binding.loginBtn.setOnClickListener(v -> startActivity(new Intent(IntroActivity.this, LoginActivity.class).putExtra("loginAs", "User")));
        binding.signUpBtn.setOnClickListener(v -> startActivity(new Intent(IntroActivity.this, SignUpActivity.class)));

    }

    @SuppressLint("ObsoleteSdkInt")
    private void transparent_Status_And_Navigation() {
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            set_Window_Flag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_VISIBLE);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            set_Window_Flag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
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