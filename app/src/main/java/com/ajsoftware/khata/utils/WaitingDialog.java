package com.ajsoftware.khata.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.DialogProgressBinding;

import java.util.Objects;

public class WaitingDialog {
    Dialog dialog;
    public WaitingDialog(Context context) {
        DialogProgressBinding binding = DialogProgressBinding.inflate(((android.app.Activity)context).getLayoutInflater());
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);
    }
    public void show(){
        dialog.show();
    }
    public void dismiss(){
        dialog.dismiss();
    }
}
