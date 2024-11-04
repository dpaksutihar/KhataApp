package com.ajsoftware.khata.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;

import com.ajsoftware.khata.databinding.BottomDialogActiveBinding;
import com.ajsoftware.khata.databinding.BottomTransactionBinding;
import com.ajsoftware.khata.databinding.DialogProgressBinding;
import com.ajsoftware.khata.ui.main.BalanceActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;

public class BlockDialog {

    BottomSheetDialog dialog;

    public BlockDialog(Context context) {
        BottomDialogActiveBinding binding = BottomDialogActiveBinding.inflate(((android.app.Activity) context).getLayoutInflater());
        dialog = new BottomSheetDialog(context);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(binding.getRoot());
        dialog.setCancelable(false);

        binding.toggleAccount.setOnClickListener(v -> System.exit(0));
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

}
