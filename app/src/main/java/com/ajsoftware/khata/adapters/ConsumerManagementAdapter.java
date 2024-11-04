package com.ajsoftware.khata.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.datastore.preferences.protobuf.Option;
import androidx.recyclerview.widget.RecyclerView;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ItemCustomersBinding;
import com.ajsoftware.khata.databinding.ItemCustomersManagementBinding;
import com.ajsoftware.khata.databinding.PopupLayoutBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.ui.main.ConsumerManagementActivity;
import com.ajsoftware.khata.utils.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ConsumerManagementAdapter extends RecyclerView.Adapter<ConsumerManagementAdapter.ViewHolder> {

    Context context;
    List<ConsumerModel> consumerModelArrayList;
    OptionsClick optionsClick;
    int amount = 0;
    int amountPaid = 0;
    int left_receiving_amount = 0;
    PopupWindow popup;
    String params;

    public ConsumerManagementAdapter(Context context, List<ConsumerModel> consumerModelArrayList, OptionsClick optionsClick, String params) {
        this.context = context;
        this.consumerModelArrayList = consumerModelArrayList;
        this.optionsClick = optionsClick;
        this.params = params;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customers_management, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ConsumerModel consumerModel = consumerModelArrayList.get(position);
        holder.binding.name.setText(consumerModel.getName());

        holder.binding.createdOn.setText("Created on: " + getDate(consumerModel.getId()));

        holder.binding.optionsBtn.setOnClickListener(view -> {
            popup = popupDisplay(consumerModel);
            popup.showAsDropDown(view, -160, 0);
        });

        if (params.equals(Constants.CONSUMERS)) {
//            holder.binding.optionsBtn.setVisibility(View.VISIBLE);
        } else if (params.equals(Constants.TRAMS)) {
            holder.binding.viewAccounts.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(view -> optionsClick.viewAccount(consumerModel));

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(params).document(consumerModel.getId()).collection(Constants.TRAMS).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                amount = 0;
                amountPaid = 0; //
                left_receiving_amount = 0;

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
                    amount = amount + Integer.parseInt(transactionRecordingModel.getAmount());
                    amountPaid = amountPaid + Integer.parseInt(transactionRecordingModel.getAmountPaid());
                    left_receiving_amount = left_receiving_amount + Integer.parseInt(transactionRecordingModel.getAmount()) - Integer.parseInt(transactionRecordingModel.getAmountPaid());
                }
                consumerModel.setAmount(String.valueOf(amount));
                holder.binding.amount.setText("Amount: " + String.valueOf(amount).concat("NRs"));

                if (Constants.Customer_click.equals("Vendor")) {
                    holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_send_small, 0, 0, 0);
                    holder.binding.balance.setText("Balance: " + String.valueOf(left_receiving_amount).concat("NRs"));
                    holder.binding.recieved.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_paid_small_red, 0, 0, 0);
                    holder.binding.recieved.setText("Paid: " + String.valueOf(amountPaid).concat("NRs"));
                } else if (Constants.Customer_click.equals("Customer")) {
                    //for  customers
                    holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.balance_icon_small2, 0, 0, 0);
                    holder.binding.balance.setText("Balance: " + String.valueOf(left_receiving_amount).concat("NRs"));
                    holder.binding.recieved.setCompoundDrawablesWithIntrinsicBounds(R.drawable.receive_icon_small2, 0, 0, 0);
                    holder.binding.recieved.setText("Received: " + String.valueOf(amountPaid).concat("NRs"));
                }
            } else {
                consumerModel.setAmount("0");
                consumerModel.setAmount_paid("0");
                consumerModel.setAmount_left("0");
                holder.binding.amount.setText("Amount 0 NRs");

                if (Constants.Customer_click.equals("Vendor")) {
                    // VENDOR
                    holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_send_small, 0, 0, 0);
                    holder.binding.balance.setText("Balance 0 NRs");
                    holder.binding.recieved.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_paid_small_red, 0, 0, 0);
                    holder.binding.recieved.setText("Paid 0 NRs");
                } else if (Constants.Customer_click.equals("Customer")) {
                    //CUSTOMER
                    holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.balance_icon_small2, 0, 0, 0);
                    holder.binding.balance.setText("Balance 0 NRs");
                    holder.binding.recieved.setCompoundDrawablesWithIntrinsicBounds(R.drawable.receive_icon_small2, 0, 0, 0);
                    holder.binding.recieved.setText("Received 0 NRs");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return consumerModelArrayList.size();
    }

    public void updateList(List<ConsumerModel> filteredList) {
        consumerModelArrayList.clear();
        consumerModelArrayList.addAll(filteredList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCustomersManagementBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCustomersManagementBinding.bind(itemView);
        }
    }

    public interface OptionsClick {
        void linkTrans(ConsumerModel consumerModel);

        void editConsumer(ConsumerModel consumerModel);

        void delConsumer(ConsumerModel consumerModel);

        void viewAccount(ConsumerModel consumerModel);

    }

    private String getDate(String timeStamp) {
        Date date = new Date(Long.parseLong(timeStamp));
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd MMM, yyyy");
        return formatter.format(date);
    }

    private PopupWindow popupDisplay(ConsumerModel consumerModel) {

        PopupLayoutBinding popupLayoutBinding;

        final PopupWindow popupWindow = new PopupWindow(context, null, R.style.PopUpBg);
        popupLayoutBinding = PopupLayoutBinding.inflate((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));

        popupLayoutBinding.delete.setOnClickListener(view -> {
            popup.dismiss();
            optionsClick.delConsumer(consumerModel);
        });
        popupLayoutBinding.edit.setOnClickListener(view -> {
            popup.dismiss();
            optionsClick.editConsumer(consumerModel);
        });
        popupLayoutBinding.link.setOnClickListener(view -> {
            popup.dismiss();
            optionsClick.linkTrans(consumerModel);
        });

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(popupLayoutBinding.getRoot());
        return popupWindow;

    }
}
