package com.ajsoftware.khata.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.ajsoftware.khata.databinding.ActivitySplashBinding;
import com.ajsoftware.khata.ui.main.AdminMainActivity;
import com.ajsoftware.khata.ui.main.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        hide_SystemUI();
        Set_Up_Splash();

    }

    private void Set_Up_Splash() {
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    startActivity(new Intent(SplashActivity.this, IntroActivity.class));
                    finish();
                } else {
                    if (FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("admin@admin.com")) {
                        startActivity(new Intent(SplashActivity.this, AdminMainActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                        finish();
                    }
                }
            }
        }).start();
    }

    private void hide_SystemUI() {
        View decorView = getWindow().getDecorView();
        int flags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(flags);
    }
}