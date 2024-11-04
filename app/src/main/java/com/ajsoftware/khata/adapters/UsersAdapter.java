package com.ajsoftware.khata.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ItemCustomersBinding;
import com.ajsoftware.khata.databinding.LayoutUsersBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    Context context;
    ArrayList<UserModel> userModelArrayList;
    OnClick onClick;

    public UsersAdapter(Context context, ArrayList<UserModel> userModelArrayList, OnClick onClick) {
        this.context = context;
        this.userModelArrayList = userModelArrayList;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_users, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        UserModel userModel = userModelArrayList.get(position);

        holder.binding.name.setText(userModel.getName());

        if (userModel.getStatus().equals(Constants.ACTIVE)){
            holder.binding.status.setText("Active");
            holder.binding.status.setTextColor(context.getColor(R.color.incomeColor));
        }
        else if (userModel.getStatus().equals(Constants.NOT_ACTIVE)){
            holder.binding.status.setText("Not Active");
            holder.binding.status.setTextColor(context.getColor(R.color.expenseColor));
        }

        holder.itemView.setOnClickListener(v -> onClick.onClick(userModel));

    }

    @Override
    public int getItemCount() {
        return userModelArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LayoutUsersBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutUsersBinding.bind(itemView);
        }
    }

    public interface OnClick{
        void onClick(UserModel userModel);
    }
}
