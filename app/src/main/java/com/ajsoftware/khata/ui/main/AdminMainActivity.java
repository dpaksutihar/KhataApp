package com.ajsoftware.khata.ui.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.adapters.UsersAdapter;
import com.ajsoftware.khata.databinding.ActivityAdminMainBinding;
import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.ui.splash.IntroActivity;
import com.ajsoftware.khata.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminMainActivity extends AppCompatActivity {

    ActivityAdminMainBinding binding;
    UsersAdapter usersAdapter;
    ArrayList<UserModel> userModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        transparent_Status_And_Navigation();

        getUsers();
        setClicks();

    }

    private void setClicks() {
        binding.logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(AdminMainActivity.this, IntroActivity.class));
            finish();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getUsers() {

        userModelArrayList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.USERS).addSnapshotListener((value, error) -> {
            if (value != null && !value.isEmpty()) {
                userModelArrayList.clear();
                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    userModelArrayList.add(userModel);
                }
                usersAdapter = new UsersAdapter(AdminMainActivity.this, userModelArrayList, userModel -> startActivity(new Intent(AdminMainActivity.this, UserDetailActivity.class).putExtra(Constants.MODEL, userModel)));
                binding.recyclerView.setAdapter(usersAdapter);
                usersAdapter.notifyDataSetChanged();
            }
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
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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