package com.ajsoftware.khata.ui.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ajsoftware.khata.adapters.ConsumersHomeAdapter;
import com.ajsoftware.khata.dao.AppDatabase;
import com.ajsoftware.khata.databinding.ActivityHomeBinding;
import com.ajsoftware.khata.models.BackupModel;
import com.ajsoftware.khata.models.ConsumerModel;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.models.UserModel;
import com.ajsoftware.khata.models.roomdatabase.ConsumerEntity;
import com.ajsoftware.khata.models.roomdatabase.TransactionEntity;
import com.ajsoftware.khata.models.roomdatabase.UserDataEntity;
import com.ajsoftware.khata.ui.splash.SplashActivity;
import com.ajsoftware.khata.utils.BlockDialog;
import com.ajsoftware.khata.utils.Constants;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {
    private AppDatabase appDatabase;

    ActivityHomeBinding binding;


    private ProgressDialog progressDialog;
    private static final int REQUEST_STORAGE_PERMISSION = 1;

    ConsumersHomeAdapter consumersHomeAdapter;
    ArrayList<ConsumerModel> consumerModelArrayList;

    BlockDialog blockDialog;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    int sale = 0;
    int purchase = 0;

    private String downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
    private String backupFolder = "khata_backups";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        transparent_Status_And_Navigation();
        appDatabase = AppDatabase.getDatabase(HomeActivity.this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Backup in progress...");
        progressDialog.setCancelable(false);
        setViews();
        setClicks();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        setupBalanceViews();

        calculateSale();

    }


    private void setupBalanceViews() {
        Log.i("BALANCE_INFO", "Setting up balance views");

        getConsumers();
    }

    private void setViews() {
        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get().addOnSuccessListener(documentSnapshot -> {
            UserModel userModel = documentSnapshot.toObject(UserModel.class);
            binding.userEmail.setText(userModel.getPhoneNo());
            binding.userName.setText(userModel.getName());
            checkStatus(userModel.getStatus());
        });
    }

    private void checkStatus(String status) {

        if (status.equals(Constants.NOT_ACTIVE)) {
            blockDialog = new BlockDialog(HomeActivity.this);
            blockDialog.show();
        }

    }


    private void calculateSale() {
        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.CUSTOMER).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                sale = 0;
                List<Task<Void>> tasks = new ArrayList<>();

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    ConsumerModel consumerModel = documentSnapshot.toObject(ConsumerModel.class);
                    String id = consumerModel.getId();

                    Task<Void> transactionTask = FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.CUSTOMER).document(id).collection(Constants.TRAMS).get().continueWith(transactionTaskResult -> {
                        if (transactionTaskResult.isSuccessful()) {
                            QuerySnapshot transactionSnapshots = transactionTaskResult.getResult();
                            for (DocumentSnapshot transactionSnapshot : transactionSnapshots.getDocuments()) {
                                TransactionRecordingModel transactionRecordingModel = transactionSnapshot.toObject(TransactionRecordingModel.class);
                                sale += Integer.parseInt(transactionRecordingModel.getAmount()) - Integer.parseInt(transactionRecordingModel.getAmountPaid());
                            }
                        }
                        return null;
                    });

                    tasks.add(transactionTask);
                }

                Tasks.whenAllComplete(tasks).addOnSuccessListener(result -> calculatePurchase());
            } else {
                // Handle the error
            }
        });
    }

    private void calculatePurchase() {
        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.VENDOR).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                purchase = 0;
                List<Task<Void>> tasks = new ArrayList<>();

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                    ConsumerModel consumerModel = documentSnapshot.toObject(ConsumerModel.class);
                    String id = consumerModel.getId();

                    Task<Void> transactionTask = FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.VENDOR).document(id).collection(Constants.TRAMS).get().continueWith(transactionTaskResult -> {
                        if (transactionTaskResult.isSuccessful()) {
                            QuerySnapshot transactionSnapshots = transactionTaskResult.getResult();
                            for (DocumentSnapshot transactionSnapshot : transactionSnapshots.getDocuments()) {
                                TransactionRecordingModel transactionRecordingModel = transactionSnapshot.toObject(TransactionRecordingModel.class);
                                purchase += Integer.parseInt(transactionRecordingModel.getAmount()) - Integer.parseInt(transactionRecordingModel.getAmountPaid());
                            }
                        }
                        return null;
                    });

                    tasks.add(transactionTask);
                }

                Tasks.whenAllComplete(tasks).addOnSuccessListener(result -> {
                    Log.i("BALANCE ", "SALE " + sale + " PURCHASE " + purchase);
                    updateUI();
                });
            } else {
                // Handle the error
            }
        });
    }

    private void updateUI() {
        // Calculate net balance
        int netBalance = sale - purchase;

        // Update UI with net balance
        if (netBalance > 0) {
            binding.receivable.setText(netBalance + " NRs");
            binding.payable.setText("0 NRs");
        } else if (netBalance < 0) {
            binding.receivable.setText("0 NRs");
            binding.payable.setText(Math.abs(netBalance) + " NRs");
        } else {
            binding.receivable.setText("0 NRs");
            binding.payable.setText("0 NRs");
        }
    }


    private void getConsumers() {
        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.VENDOR).addSnapshotListener((value, error) -> {
            if (value != null) {
                sale = 0;
                purchase = 0;

                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    String consumerId = documentSnapshot.getId();

                    FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(Constants.VENDOR).document(consumerId).collection(Constants.TRAMS).addSnapshotListener((value1, error1) -> {
                        if (value1 != null) {
                            for (DocumentSnapshot document : value1.getDocuments()) {
                                TransactionRecordingModel transactionRecordingModel = document.toObject(TransactionRecordingModel.class);

                                if (transactionRecordingModel != null && transactionRecordingModel.getAmount() != null) {
                                    if (isSaleTransaction(transactionRecordingModel)) {
                                        sale += Integer.parseInt(transactionRecordingModel.getAmount());
                                    } else if (isPurchaseTransaction(transactionRecordingModel)) {
                                        purchase += Integer.parseInt(transactionRecordingModel.getAmount());
                                    }
                                }
                            }
                            // Update UI after each snapshot
                            updateBalanceUI();
                        }
                    });
                }
            }
        });
    }


    private boolean isSaleTransaction(TransactionRecordingModel transaction) {
        return transaction != null && transaction.getTransType().equals(Constants.SALE_TRANSACTION_TYPE);
    }

    private boolean isPurchaseTransaction(TransactionRecordingModel transaction) {
        return transaction != null && transaction.getTransType().equals(Constants.PURCHASE_TRANSACTION_TYPE);
    }

    private void updateBalanceUI() {
        Log.i("UPDATE_UI", "Updating UI with Sale: " + sale + " Purchase: " + purchase); // Add this line

        String receivableText = sale > 0 ? sale + " NRs" : "0 NRs";
        String payableText = purchase > 0 ? purchase + " NRs" : "0 NRs";

        binding.receivable.setText(receivableText);
        binding.payable.setText(payableText);
    }


    private void setClicks() {

        binding.customerBtn.setOnClickListener(view -> {
            Constants.Customer_click = "Customer";
            startActivity(new Intent(HomeActivity.this, ConsumerManagementActivity.class).putExtra(Constants.CUSTOMER_TYPE, Constants.CUSTOMER));
        });

        binding.backUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    backupData();
                } else {
                    requestPermission();
                }
            }
        });

        binding.vendorBtn.setOnClickListener(view -> {
            Constants.Customer_click = "Vendor";
            startActivity(new Intent(HomeActivity.this, ConsumerManagementActivity.class).putExtra(Constants.CUSTOMER_TYPE, Constants.VENDOR));
        });

        binding.addTransactionBtn.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, TransactionActivity.class)));

        binding.reportingAnalytics.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, AnalysisActivity.class)));

        binding.logout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, SplashActivity.class));
            finishAffinity();
        });

        binding.exportBtn.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ExportActivity.class)));
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with backup
                backupData();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }


    @SuppressLint("StaticFieldLeak")
    private void backupData() {
        progressDialog.show();

        new AsyncTask<Void, Void, BackupModel>() {
            @Override
            protected BackupModel doInBackground(Void... voids) {
                return fetchDataForBackup();
            }

            @Override
            protected void onPostExecute(BackupModel backupData) {
                super.onPostExecute(backupData);
                progressDialog.dismiss();

                if (backupData != null) {
                    saveBackup(backupData);
                } else {
                    showToast("Error creating backup");
                }
            }
        }.execute();
    }

    private BackupModel fetchDataForBackup() {
        try {
            // Fetch data from Room database entities and populate backupData
            List<UserDataEntity> users = appDatabase.userDao().getAllUsers();
            List<ConsumerEntity> consumers = appDatabase.consumerDao().getAllConsumers();
            List<TransactionEntity> transactions = appDatabase.transactionDao().getAllTransactions();

            BackupModel backupData = new BackupModel();
            backupData.setUsers(users);
            backupData.setConsumers(consumers);
            backupData.setTransactions(transactions);

            return backupData;
        } catch (Exception e) {
            Log.e("Backup", "Error fetching data for backup: " + e.getMessage());
            return null;
        }
    }

    private void saveBackup(BackupModel backupData) {
        try {
            Gson gson = new Gson();
            String backupJson = gson.toJson(backupData);

            // Create backup folder if not exists
            File backupPath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), backupFolder);
            if (!backupPath.exists()) {
                if (!backupPath.mkdirs()) {
                    Log.e("Backup", "Failed to create backup folder");
                    showToast("Failed to create backup folder");
                    return;
                }
            }

            // Create backup file with timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date());
            String backupFilename = String.format("backup_%s.json", timestamp);
            File backupFile = new File(backupPath, backupFilename);

            // Write backup data to file
            FileWriter writer = new FileWriter(backupFile);
            writer.write(backupJson);
            writer.flush();
            writer.close();

            showToast("Backup saved to file");
        } catch (Exception e) {
            Log.e("Backup", "Error saving backup: " + e.getMessage());
            showToast("Failed to save backup");
        }
    }

    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }




    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(HomeActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

}
