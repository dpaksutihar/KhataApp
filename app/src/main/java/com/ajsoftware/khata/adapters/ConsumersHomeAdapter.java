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
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class ConsumersHomeAdapter extends RecyclerView.Adapter<ConsumersHomeAdapter.ViewHolder> {

    Context context;
    ArrayList<ConsumerModel> consumerModelArrayList;
    int amount = 0;

    public ConsumersHomeAdapter(Context context, ArrayList<ConsumerModel> consumerModelArrayList) {
        this.context = context;
        this.consumerModelArrayList = consumerModelArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customers, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConsumerModel consumerModel = consumerModelArrayList.get(position);
        holder.binding.name.setText(consumerModel.getName());

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(Constants.TRAMS).whereEqualTo(Constants.AssociatedField, consumerModel.getId()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        amount = 0;
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
                            amount = amount + Integer.parseInt(transactionRecordingModel.getAmount());
                        }
                        consumerModel.setAmount(String.valueOf(amount));
                        holder.binding.amount.setText(String.valueOf(amount).concat(" NRs"));
                    } else {
                        consumerModel.setAmount("0");
                        holder.binding.amount.setText("0 NRs");
                    }
                });

    }

    @Override
    public int getItemCount() {
        return consumerModelArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCustomersBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCustomersBinding.bind(itemView);
        }
    }
}
