package com.ajsoftware.khata.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ItemTransactionBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    Context context;
    ArrayList<TransactionRecordingModel> transactionRecordingModelArrayList;
    OptionClick optionClick;
    String params;

    public TransactionAdapter(Context context, ArrayList<TransactionRecordingModel> transactionRecordingModelArrayList, OptionClick optionClick, String params) {
        this.context = context;
        this.transactionRecordingModelArrayList = transactionRecordingModelArrayList;
        this.optionClick = optionClick;
        this.params = params;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        TransactionRecordingModel transactionRecordingModel = transactionRecordingModelArrayList.get(position);

        holder.binding.category.setText(transactionRecordingModel.getAmountPaid());
        holder.binding.transType.setText("(".concat(transactionRecordingModel.getTransType().concat(")")));
        holder.binding.desc.setText(transactionRecordingModel.getDesc());
        holder.binding.amount.setText(transactionRecordingModel.getAmount().concat(" NRs"));
        holder.binding.createdOn.setText(transactionRecordingModel.getDate());

        if (params.equals(Constants.CONSUMERS)){

            holder.binding.checkedLayout.setVisibility(View.VISIBLE);

//            if (transactionRecordingModel.getAssociated_to().equals(consumerModel.getId())) {
//                holder.binding.removeBtn.setVisibility(View.VISIBLE);
//            } else {
//                holder.binding.addBtn.setVisibility(View.VISIBLE);
//            }

            if (transactionRecordingModel.getStatus().equals(Constants.SENT)) {
                holder.binding.checkedBtn.setChecked(true);
            } else if (transactionRecordingModel.getStatus().equals(Constants.RECEIVED)) {
                holder.binding.checkedBtn.setChecked(false);
            }

        }
        else if (params.equals(Constants.TRAMS)){

            holder.binding.checkedLayout.setVisibility(View.GONE);

            if (transactionRecordingModel.getStatus().equals(Constants.SENT)) {
                holder.binding.sentIcon.setVisibility(View.VISIBLE);
            } else if (transactionRecordingModel.getStatus().equals(Constants.RECEIVED)) {
                holder.binding.receivedIcon.setVisibility(View.VISIBLE);
            }

        }

        holder.binding.addBtn.setOnClickListener(view -> {
            if (holder.binding.checkedBtn.isChecked()) {
                transactionRecordingModel.setStatus(Constants.SENT);
            } else if (!holder.binding.checkedBtn.isChecked()) {
                transactionRecordingModel.setStatus(Constants.RECEIVED);
            }
            optionClick.onAddClick(transactionRecordingModel);
        });
        holder.binding.removeBtn.setOnClickListener(view -> optionClick.onRemoveClick(transactionRecordingModel));

    }

    @Override
    public int getItemCount() {
        return transactionRecordingModelArrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ItemTransactionBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemTransactionBinding.bind(itemView);
        }
    }

    public interface OptionClick {
        void onAddClick(TransactionRecordingModel transactionRecordingModel);

        void onRemoveClick(TransactionRecordingModel transactionRecordingModel);
    }
}
