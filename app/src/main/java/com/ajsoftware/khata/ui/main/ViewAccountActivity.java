package com.ajsoftware.khata.ui.main;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.ajsoftware.khata.R;
import com.ajsoftware.khata.adapters.VendorTransactionAdapter;
import com.ajsoftware.khata.databinding.ActivityViewAccountBinding;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;
import com.ajsoftware.khata.utils.WaitingDialog;
import com.gkemon.XMLtoPDF.PdfGenerator;
import com.gkemon.XMLtoPDF.PdfGeneratorListener;
import com.gkemon.XMLtoPDF.model.SuccessResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ViewAccountActivity extends AppCompatActivity {

    ActivityViewAccountBinding binding;
    ConsumerModel intentConsumerModel;
    String CUSTOMER_TYPE;
    int balance;
    VendorTransactionAdapter vendorTransactionAdapter;
    ArrayList<TransactionRecordingModel> consumerModelArrayList;
    int amount = 0;
    ArrayList<String> dateList;
    String fromDate, toDate;
    private static final int PERMISSION_REQUEST_CODE = 555;
    private PdfGenerator.XmlToPDFLifecycleObserver xmlToPDFLifecycleObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));
        xmlToPDFLifecycleObserver = new PdfGenerator.XmlToPDFLifecycleObserver(ViewAccountActivity.this);
        getLifecycle().addObserver(xmlToPDFLifecycleObserver);

        getFromIntent();
        setClicks();
    }

    @SuppressLint("SetTextI18n")
    private void getFromIntent() {

        if (getIntent().hasExtra("MODEL")) {
            intentConsumerModel = (ConsumerModel) getIntent().getSerializableExtra("MODEL");
            binding.name.setText(intentConsumerModel.getName());
            binding.phone.setText("Phone No: " + intentConsumerModel.getPhoneNo());
            binding.address.setText("Address: " + intentConsumerModel.getAddress());
            binding.balance.setText("Amount: " + intentConsumerModel.getAmount());
        }

        if (getIntent().hasExtra(Constants.CUSTOMER_TYPE)) {
            CUSTOMER_TYPE = getIntent().getStringExtra(Constants.CUSTOMER_TYPE);
        }

        getDates();
        getTransactions(fromDate, toDate);

    }

    private void getDates() {

        dateList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(CUSTOMER_TYPE)
                .document(intentConsumerModel.getId())
                .collection(Constants.TRAMS).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            dateList.clear();
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
                                dateList.add(transactionRecordingModel.getDate());
                            }

                            List<String> uniqueList = dateList.stream()
                                    .distinct()
                                    .collect(Collectors.toList());

                            fromDate = dateList.get(0);
                            toDate = dateList.get(uniqueList.size() - 1);

                            Log.i("DATE", fromDate + " " + toDate);

                            ArrayAdapter<String> fromDateAdapter = new ArrayAdapter<>(ViewAccountActivity.this, android.R.layout.simple_list_item_1, uniqueList);
                            binding.fromDateSpinner.setAdapter(fromDateAdapter);
                            binding.fromDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    fromDate = uniqueList.get(i);
                                    getTransactions(fromDate, toDate);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                }
                            });

                            ArrayAdapter<String> toDateAdapter = new ArrayAdapter<>(ViewAccountActivity.this, android.R.layout.simple_list_item_1, uniqueList);
                            binding.toDateSpinner.setAdapter(toDateAdapter);
                            binding.toDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    toDate = uniqueList.get(i);
                                    getTransactions(fromDate, toDate);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                }
                            });
                            binding.toDateSpinner.setSelection(uniqueList.size() - 1);
                        }
                    }
                });

    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void getTransactions(String fromDate, String toDate) {

        consumerModelArrayList = new ArrayList<>();
        amount = 0;
        FirebaseFirestore.getInstance().collection(Constants.USERS)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(CUSTOMER_TYPE)
                .document(intentConsumerModel.getId())
                .collection(Constants.TRAMS)
                .addSnapshotListener((value, error) -> {
                    if (value != null && !value.isEmpty()) {
                        consumerModelArrayList.clear();
                        amount = 0;
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
                            Log.i("TRANS", transactionRecordingModel.getId() + " " + transactionRecordingModel.getDate());
                            if (transactionRecordingModel.getDate().equals(fromDate) || transactionRecordingModel.getDate().equals(toDate)) {
                                Log.i("RANGE", toDate + " " + fromDate);
                                consumerModelArrayList.add(transactionRecordingModel);
                                amount += Integer.parseInt(transactionRecordingModel.getAmount());
                            }
                        }

                        binding.balance.setText("Amount: " + amount);

                        balance = amount;

                        Collections.sort(consumerModelArrayList, (model1, model2) ->
                                Long.compare(model2.getTimestamp(), model1.getTimestamp())
                        );

                        // Calculate total balance for all transactions
                        int totalBalance = calculateTotalBalance(consumerModelArrayList);

                        // Set total balance to the first item
                        if (!consumerModelArrayList.isEmpty()) {
                            consumerModelArrayList.get(0).setTotalBalance(String.valueOf(totalBalance));
                        }

                        vendorTransactionAdapter = new VendorTransactionAdapter(ViewAccountActivity.this, consumerModelArrayList, transactionRecordingModel -> startActivity(new Intent(ViewAccountActivity.this, TransactionRecordingActivity.class).putExtra("EDIT_MODEL", transactionRecordingModel).putExtra("MODEL", intentConsumerModel).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE)));
                        binding.recyclerView.setAdapter(vendorTransactionAdapter);
                        vendorTransactionAdapter.notifyDataSetChanged();


                    } else {
                        // Handle error or empty case
                    }
                });


    }

    public int calculateTotalBalance(List<TransactionRecordingModel> transactions) {
        int totalBalance = 0;

        for (TransactionRecordingModel transaction : transactions) {
            int transactionBalance = Integer.parseInt(transaction.getAmount()) - Integer.parseInt(transaction.getAmountPaid());
            totalBalance += transactionBalance;
        }

        return totalBalance;
    }

    private void setClicks() {

        binding.exportBtn.setOnClickListener(view -> {

            if (!checkPermission()) {
                requestPermission();
            } else {
                shareFile();
            }

        });

        if (vendorTransactionAdapter == null) {
            vendorTransactionAdapter = new VendorTransactionAdapter(ViewAccountActivity.this, consumerModelArrayList, transactionRecordingModel -> startActivity(new Intent(ViewAccountActivity.this, TransactionRecordingActivity.class).putExtra("EDIT_MODEL", transactionRecordingModel).putExtra("MODEL", intentConsumerModel).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE)));

            binding.recyclerView.setAdapter(vendorTransactionAdapter);
        }

        // Set the OnRemindClickListener for the adapter
        vendorTransactionAdapter.setOnRemindClickListener(new VendorTransactionAdapter.OnRemindClickListener() {
            @Override
            public void onRemindClick(TransactionRecordingModel transactionRecordingModel) {
                sendReminderForTransaction(transactionRecordingModel);
            }
        });

        binding.backIcon.setOnClickListener(view -> finish());

        binding.sendReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and request SMS permission if not granted

                sendReminderMessage();

            }
        });

        binding.addTrans.setOnClickListener(v -> startActivity(new Intent(ViewAccountActivity.this, TransactionRecordingActivity.class).putExtra("MODEL", intentConsumerModel).putExtra(Constants.CUSTOMER_TYPE, CUSTOMER_TYPE)));

    }


    private void sendReminderMessage() {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Name: ").append(intentConsumerModel.getName()).append("\n");
        messageBuilder.append("Phone No: ").append(intentConsumerModel.getPhoneNo()).append("\n");
        messageBuilder.append("Address: ").append(intentConsumerModel.getAddress()).append("\n");
        messageBuilder.append("Amount: ").append(balance).append("\n\n");

        messageBuilder.append("All Transactions:\n\n");

        for (TransactionRecordingModel transaction : consumerModelArrayList) {
            messageBuilder.append("Date: ").append(transaction.getDate()).append("\n");
            messageBuilder.append("Amount: ").append(transaction.getAmount()).append("\n");
            messageBuilder.append("Amount Paid: ").append(transaction.getAmountPaid()).append("\n");

            // Handle Balance Due
            int amount2 = Integer.parseInt(transaction.getAmount());
            int amountPaid = Integer.parseInt(transaction.getAmountPaid());
            int transactionBalance = amount2 - amountPaid;

            String balanceDue = String.valueOf(Math.abs(transactionBalance));

            // Display due amount (negative balance) without a negative sign

            messageBuilder.append("Balance Due: ").append(balanceDue).append("\n\n");
        }

        Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
        whatsappIntent.setData(Uri.parse("whatsapp://send?phone=" + intentConsumerModel.getPhoneNo() + "&text=" + Uri.encode(messageBuilder.toString())));

        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + intentConsumerModel.getPhoneNo()));
        smsIntent.putExtra("sms_body", messageBuilder.toString());

        Intent chooserIntent = Intent.createChooser(whatsappIntent, "Send Reminder");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{smsIntent});

        startActivity(chooserIntent);
    }

    //For Specific Transaction
    private void sendReminderForTransaction(TransactionRecordingModel transactionRecordingModel) {
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append("Name: ").append(intentConsumerModel.getName()).append("\n");
        messageBuilder.append("Phone No: ").append(intentConsumerModel.getPhoneNo()).append("\n");
        messageBuilder.append("Address: ").append(intentConsumerModel.getAddress()).append("\n\n");

        messageBuilder.append("Date: ").append(transactionRecordingModel.getDate()).append("\n");
        messageBuilder.append("Amount: ").append(transactionRecordingModel.getAmount()).append("\n");
        messageBuilder.append("Amount Paid: ").append(transactionRecordingModel.getAmountPaid()).append("\n");

        // Handle Balance Due
        int amount2 = Integer.parseInt(transactionRecordingModel.getAmount());
        int amountPaid = Integer.parseInt(transactionRecordingModel.getAmountPaid());
        int transactionBalance = amount2 - amountPaid;

        String balanceDue = String.valueOf(Math.abs(transactionBalance));

        // Display due amount (negative balance) without a negative sign

        messageBuilder.append("Balance Due: ").append(balanceDue).append("\n");

        Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
        whatsappIntent.setData(Uri.parse("whatsapp://send?phone=" + intentConsumerModel.getPhoneNo() + "&text=" + Uri.encode(messageBuilder.toString())));

        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:" + intentConsumerModel.getPhoneNo()));
        smsIntent.putExtra("sms_body", messageBuilder.toString());

        Intent chooserIntent = Intent.createChooser(whatsappIntent, "Send Reminder");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{smsIntent});

        startActivity(chooserIntent);
    }

    public void shareFile() {

        PdfGenerator.getBuilder()
                .setContext(ViewAccountActivity.this)
                .fromViewSource()
                .fromView(binding.getRoot())
                .setFileName("Records_" + intentConsumerModel.getName())
                .setFolderNameOrPath(Environment.getExternalStorageDirectory().getAbsolutePath())
                .actionAfterPDFGeneration(PdfGenerator.ActionAfterPDFGeneration.SHARE)
                .savePDFSharedStorage(xmlToPDFLifecycleObserver)
                .build(new PdfGeneratorListener() {
                    @Override
                    public void showLog(String log) {
                        super.showLog(log);
                        Log.i("PDF log", "" + log);

                    }

                    @Override
                    public void onSuccess(SuccessResponse response) {
                        super.onSuccess(response);
                        Log.i("PDF Status", "STORED AT " + response.getPath());
                    }

                    @Override
                    public void onStartPDFGeneration() {
                        Log.i("PDF Status", "STARTED");
                    }

                    @Override
                    public void onFinishPDFGeneration() {

                        Log.i("PDF Status", "FINISHED");

                        String packageID = getPackageName();

                        File file = new File(getExternalFilesDir(String.valueOf(Environment.getExternalStorageDirectory())) + "/Records.pdf");

                        String filePath = file.getAbsolutePath();
                        Log.d("FilePath", filePath);

                        File file1 = new File(filePath);

                        if (file1.exists()) {

                            Uri uri = FileProvider.getUriForFile(ViewAccountActivity.this, packageID + ".fileprovider", file1);

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("application/pdf");
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Records");
                            startActivity(Intent.createChooser(intent, "Share PDF"));
                        } else {
                            Log.i("PDF Status", "Not Found");
                        }
                    }
                });
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(ViewAccountActivity.this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(ViewAccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ViewAccountActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(ViewAccountActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(ViewAccountActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

}


