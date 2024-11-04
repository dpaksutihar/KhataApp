package com.ajsoftware.khata.ui.main;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.databinding.ActivityAnalysisBinding;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class AnalysisActivity extends AppCompatActivity {

    ActivityAnalysisBinding binding;
    ArrayList<TransactionRecordingModel> transactionRecordingModelArrayList;
    String[] transactionType = {"Sale", "Purchase"};
    ArrayList<String> docList = new ArrayList<>();
    int total = 0, sale = 0, purchase = 0;
    int amountReceived = 0;
    int amountPaid = 0;
    int totalBalance = 0;
    String fromDate, toDate;
    Set<String> uniqueSet;
    ArrayList<String> dateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnalysisBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));

        setDateSpinner();
        setClicks();
    }

    private void getConsumers(String fromDate, String toDate) {
        transactionRecordingModelArrayList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(Constants.VENDOR)
                .get().addOnSuccessListener(value -> {
                    if (value != null && !value.isEmpty()) {
                        transactionRecordingModelArrayList.clear();
                        docList.clear();
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            docList.add(documentSnapshot.getId().toString());
                            Log.i("SALE", "REPEATED 0");
                        }
                        total = 0;
                        sale = 0;
                        purchase = 0;
                        for (String ID : docList) {
                            Log.i("SALE", "REPEATED 1");
                            FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.VENDOR).document(ID).collection(Constants.TRAMS).addSnapshotListener((value1, error1) -> {
                                if (value1 != null && !value1.isEmpty()) {
                                    for (DocumentSnapshot documentSnapshot : value1.getDocuments()) {
                                        Log.i("SALE", "REPEATED 2");
                                        TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
                                        if (transactionRecordingModel.getDate().equals(fromDate) || transactionRecordingModel.getDate().equals(toDate)) {
                                            if (transactionRecordingModel.getTransType().equals(transactionType[0])) {
                                                sale += Integer.parseInt(transactionRecordingModel.getAmount());
                                            }
                                            if (transactionRecordingModel.getTransType().equals(transactionType[1])) {
                                                purchase += Integer.parseInt(transactionRecordingModel.getAmount());
                                            }
                                            transactionRecordingModelArrayList.add(transactionRecordingModel);
                                        }
                                    }
                                    setChart();
                                }
                            });
                        }
                    }
                });
    }

    private void setDateSpinner() {
        dateList = new ArrayList<>();
        uniqueSet = new LinkedHashSet<>();
        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(Constants.VENDOR)
                .addSnapshotListener((value, error) -> {
                    if (value != null && !value.isEmpty()) {
                        docList.clear();
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            docList.add(documentSnapshot.getId().toString());
                        }

                        for (String ID : docList) {
                            FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                    .collection(Constants.VENDOR).document(ID).collection(Constants.TRAMS)
                                    .addSnapshotListener((value1, error1) -> {
                                        if (value1 != null && !value1.isEmpty()) {
                                            dateList.clear();
                                            for (DocumentSnapshot documentSnapshot : value1.getDocuments()) {
                                                TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
                                                uniqueSet.add(transactionRecordingModel.getDate());
                                            }
                                            dateList.addAll(uniqueSet);
                                            fromDate = dateList.get(0);
                                            toDate = dateList.get(0);
                                            ArrayAdapter<String> fromDateAdapter = new ArrayAdapter<>(AnalysisActivity.this, android.R.layout.simple_list_item_1, dateList);
                                            binding.fromDateSpinner.setAdapter(fromDateAdapter);
                                            binding.fromDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                    fromDate = dateList.get(i);
                                                    getConsumers(fromDate, toDate);
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {
                                                }
                                            });

                                            ArrayAdapter<String> toDateAdapter = new ArrayAdapter<>(AnalysisActivity.this, android.R.layout.simple_list_item_1, dateList);
                                            binding.toDateSpinner.setAdapter(toDateAdapter);
                                            binding.toDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                    toDate = dateList.get(i);
                                                    getConsumers(fromDate, toDate);
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                });
    }

    private void setChart() {
        binding.pieChart.clearChart();
        binding.pieChart.addPieSlice(new PieModel("Sales", sale, getColor(R.color.sentColor)));
        binding.pieChart.addPieSlice(new PieModel("Purchase", purchase, getColor(R.color.receivedColor)));
        binding.pieChart.startAnimation();

        binding.saleText.setText(String.valueOf(sale).concat(" NRs"));
        binding.purchaseText.setText(String.valueOf(purchase).concat(" NRs"));

        binding.detailedPieChart.clearChart();
        total = sale + purchase;

        // Calculate Amount Received and Amount Paid
        amountReceived = calculateAmountReceived(transactionRecordingModelArrayList);
        amountPaid = calculateAmountPaid(transactionRecordingModelArrayList);

// Update UI with Amount Received and Amount Paid
        binding.amountReceivedText.setText("Amount Received: " + amountReceived + " NRs");
        binding.amountPaidText.setText("Amount Paid: " + amountPaid + " NRs");

// Calculate and update Total Balance
        totalBalance = amountPaid - amountReceived;
        int absoluteTotalBalance = Math.abs(totalBalance);

// Update UI with Positive Total Balance
        binding.total.setText("" + absoluteTotalBalance + " NRs");
// Set icons based on total balance
        if (totalBalance > 0) {
            // If received is greater than paid, set received icon
            binding.total.setCompoundDrawablesWithIntrinsicBounds(R.drawable.received_icon_small, 0, 0, 0);
        } else if (totalBalance < 0) {
            // If paid is greater than received, set paid icon
            binding.total.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_send_small, 0, 0, 0);
        } else {
            // Handle case where total balance is zero
            // Set a neutral icon or no icon based on your requirement
            binding.total.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

    }


    private int calculateAmountReceived(ArrayList<TransactionRecordingModel> transactions) {
        int received = 0;
        for (TransactionRecordingModel transaction : transactions) {
            if (transaction.getTransType().equals(transactionType[0])) {
                received += Integer.parseInt(transaction.getAmount());
            }
        }
        return received;
    }

    private int calculateAmountPaid(ArrayList<TransactionRecordingModel> transactions) {
        int paid = 0;
        for (TransactionRecordingModel transaction : transactions) {
            if (transaction.getTransType().equals(transactionType[1])) {
                paid += Integer.parseInt(transaction.getAmount());
            }
        }
        return paid;
    }

    private void setClicks() {
        binding.backIcon.setOnClickListener(view -> finish());
    }
}
