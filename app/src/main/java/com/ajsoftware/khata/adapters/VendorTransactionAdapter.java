package com.ajsoftware.khata.adapters;

import static com.ajsoftware.khata.utils.Constants.CUSTOMER_TYPE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ItemVendorTransactionsBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.ui.main.ViewAccountActivity;
import com.ajsoftware.khata.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class VendorTransactionAdapter extends RecyclerView.Adapter<VendorTransactionAdapter.ViewHolder> {

    Context context;
    ArrayList<TransactionRecordingModel> consumerModelArrayList;
    OptionClick optionClick;
    ConsumerModel consumerModel;

    public interface OnRemindClickListener {
        void onRemindClick(TransactionRecordingModel transactionRecordingModel);
    }

    private OnRemindClickListener onRemindClickListener;

    public void setOnRemindClickListener(OnRemindClickListener listener) {
        this.onRemindClickListener = listener;
    }

    public VendorTransactionAdapter(Context context, ArrayList<TransactionRecordingModel> consumerModelArrayList, OptionClick optionClick) {
        this.context = context;
        this.consumerModelArrayList = consumerModelArrayList;
        this.optionClick = optionClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vendor_transactions, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionRecordingModel transactionRecordingModel = consumerModelArrayList.get(position);

        holder.binding.amount.setText("Amount: " + transactionRecordingModel.getAmount() + " NRs");

        if (Constants.Customer_click.equals("Vendor")) {
            holder.binding.amountPaid.setText("Amount Paid: " + transactionRecordingModel.getAmountPaid() + " NRs");
        }

        else if (Constants.Customer_click.equals("Customer")) {

            holder.binding.amountPaid.setText("Amount Received: " + transactionRecordingModel.getAmountPaid() + " NRs");

        }
        int amount = Integer.parseInt(transactionRecordingModel.getAmount());
        int amountPaid = Integer.parseInt(transactionRecordingModel.getAmountPaid());

        // Calculate the balance (positive for excess, negative for due)
        int transactionBalance = amount - amountPaid;

        // Display due amount (negative balance) without a negative sign
        holder.binding.due.setText("Due: " + Math.abs(transactionBalance) + " NRs");

        holder.binding.date.setText("Date: " + transactionRecordingModel.getDate() + "");

        ViewAccountActivity activity = (ViewAccountActivity) holder.itemView.getContext();

        // Calculate total balance up to the current position
        int totalBalance = calculateTotalBalance(position);

        // Display the total balance with or without a minus sign
        holder.binding.balance.setText("" + Math.abs(totalBalance) + " NRs");

        if (Constants.Customer_click.equals("Vendor")) {

            //Vendor

            if (totalBalance >= 0) {
                // Positive balance (excess), set blue arrow
                holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_send_small, 0, 0, 0);
            } else {
                // Negative balance (due), set yellow arrow
                holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.received_icon_small, 0, 0, 0);
            }

        } else if (Constants.Customer_click.equals("Customer")) {


            if (totalBalance >= 0) {
                holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.received_icon_small, 0, 0, 0);
            } else {
                holder.binding.balance.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_send_small, 0, 0, 0);
            }
        }


        // Set arrow color based on the sign of the total balance

        holder.itemView.setOnClickListener(v -> optionClick.onEditClick(transactionRecordingModel));

        holder.binding.remindTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onRemindClickListener != null) {
                    onRemindClickListener.onRemindClick(transactionRecordingModel);
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return consumerModelArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemVendorTransactionsBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemVendorTransactionsBinding.bind(itemView);
        }
    }

    public interface OptionClick {
        void onEditClick(TransactionRecordingModel transactionRecordingModel);


    }


    public int calculateTotalBalance(int position) {
        int totalBalance = 0;

        // Iterate through transactions from the most recent to the current position
        for (int i = consumerModelArrayList.size() - 1; i >= position; i--) {
            TransactionRecordingModel transaction = consumerModelArrayList.get(i);

            // Calculate balance for each transaction
            int amount = Integer.parseInt(transaction.getAmount());
            int amountPaid = Integer.parseInt(transaction.getAmountPaid());

            // Adjust the formula to correctly calculate the transaction balance
            int transactionBalance = amount - amountPaid;

            // Update running total balance
            totalBalance += transactionBalance;
        }

        // Return the total balance for the current position
        return totalBalance;
    }

}


