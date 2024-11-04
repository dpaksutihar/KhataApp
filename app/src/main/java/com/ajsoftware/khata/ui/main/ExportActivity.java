package com.ajsoftware.khata.ui.main;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
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
import com.ajsoftware.khata.adapters.ConsumerManagementAdapter;
import com.ajsoftware.khata.adapters.TransactionAdapter;
import com.ajsoftware.khata.databinding.ActivityExportBinding;
import com.ajsoftware.khata.models.TransactionRecordingModel;
import com.ajsoftware.khata.utils.Constants;
import com.gkemon.XMLtoPDF.PdfGenerator;
import com.gkemon.XMLtoPDF.PdfGeneratorListener;
import com.gkemon.XMLtoPDF.model.SuccessResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ExportActivity extends AppCompatActivity {
    ActivityExportBinding binding;

    String[] ConsumerType = {"Vendor", "Customer"};
    TransactionAdapter transactionAdapter;
    ArrayList<TransactionRecordingModel> transactionRecordingModelArrayList;
    String fromDate, toDate;
    ArrayList<String> dateList;
    ArrayList<String> docList = new ArrayList<>();
    private static final int PERMISSION_REQUEST_CODE = 555;
    private PdfGenerator.XmlToPDFLifecycleObserver xmlToPDFLifecycleObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setStatusBarColor(getColor(R.color.blue));
        xmlToPDFLifecycleObserver = new PdfGenerator.XmlToPDFLifecycleObserver(ExportActivity.this);
        getLifecycle().addObserver(xmlToPDFLifecycleObserver);

        setSpinner();
        setClicks();
    }

    private void setSpinner() {

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ConsumerType);
        binding.consumerType.setAdapter(typeAdapter);

        setDateRange(ConsumerType[0]);
        getConsumers(fromDate, toDate, ConsumerType[0]);

        binding.consumerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    setDateRange(ConsumerType[0]);
                } else if (position == 1) {
                    setDateRange(ConsumerType[1]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setDateRange(String type) {

        transactionRecordingModelArrayList = new ArrayList<>();
        dateList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(type).get().addOnSuccessListener(value -> {

            if (value != null && !value.isEmpty()) {

                transactionRecordingModelArrayList.clear();
                dateList.clear();

                for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                    String documentId = documentSnapshot.getId();
                    FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(type).document(documentId).collection(Constants.TRAMS).get().addOnSuccessListener(queryDocumentSnapshots -> {

                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {

                            for (DocumentSnapshot transactionSnapshot : queryDocumentSnapshots.getDocuments()) {

                                TransactionRecordingModel transactionRecordingModel = transactionSnapshot.toObject(TransactionRecordingModel.class);
                                dateList.add(transactionRecordingModel.getDate());
                            }

                            List<String> uniqueList = dateList.stream()
                                    .distinct()
                                    .collect(Collectors.toList());

                            fromDate = uniqueList.get(0);
                            toDate = uniqueList.get(uniqueList.size() - 1);

                            ArrayAdapter<String> fromDateAdapter = new ArrayAdapter<>(ExportActivity.this, android.R.layout.simple_list_item_1, uniqueList);
                            binding.fromDateSpinner.setAdapter(fromDateAdapter);
                            binding.fromDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    fromDate = uniqueList.get(i);
                                    getConsumers(fromDate, toDate, type);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                }
                            });

                            ArrayAdapter<String> toDateAdapter = new ArrayAdapter<>(ExportActivity.this, android.R.layout.simple_list_item_1, uniqueList);
                            binding.toDateSpinner.setAdapter(toDateAdapter);
                            binding.toDateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    toDate = uniqueList.get(i);
                                    getConsumers(fromDate, toDate, type);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {
                                }
                            });
                            binding.toDateSpinner.setSelection(uniqueList.size() - 1);

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void setDefaultDate() {
        // Get the current date as the default date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date currentDate = Calendar.getInstance().getTime();
        fromDate = dateFormat.format(currentDate);
        toDate = fromDate;
    }

    private void setClicks() {
        binding.backIcon.setOnClickListener(v -> finish());
        binding.exportBtn.setOnClickListener(view -> {

            if (!checkPermission()) {
                requestPermission();
            } else {
                shareFile();
            }

        });

        binding.export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPermission()) {
                    requestPermission();
                } else {
                    shareFile();
                }
            }
        });

    }

    private void getConsumers(String fromDate, String toDate, String type) {
        transactionRecordingModelArrayList = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(Constants.USERS)
                .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection(type)
                .get().addOnSuccessListener(value -> {
                    if (value != null && !value.isEmpty()) {
                        transactionRecordingModelArrayList.clear();
                        docList.clear();
                        for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                            docList.add(documentSnapshot.getId().toString());
                            Log.i("SALE", "REPEATED 0");
                        }

                        for (String ID : docList) {
                            Log.i("SALE", "REPEATED 1");
                            FirebaseFirestore.getInstance().collection(Constants.USERS).document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).collection(type)
                                    .document(ID).collection(Constants.TRAMS).addSnapshotListener((value1, error1) -> {
                                        if (value1 != null && !value1.isEmpty()) {
                                            for (DocumentSnapshot documentSnapshot : value1.getDocuments()) {
                                                Log.i("SALE", "REPEATED 2");
                                                TransactionRecordingModel transactionRecordingModel = documentSnapshot.toObject(TransactionRecordingModel.class);
                                                if (transactionRecordingModel.getDate().equals(fromDate) || transactionRecordingModel.getDate().equals(toDate)) {
                                                    transactionRecordingModelArrayList.add(transactionRecordingModel);
                                                }
                                            }

                                            for (TransactionRecordingModel model : transactionRecordingModelArrayList) {
                                                Log.i("MODEL0", model.getId() + "");
                                            }

                                            transactionAdapter = new TransactionAdapter(ExportActivity.this, transactionRecordingModelArrayList, new TransactionAdapter.OptionClick() {
                                                @Override
                                                public void onAddClick(TransactionRecordingModel transactionRecordingModel) {

                                                }

                                                @Override
                                                public void onRemoveClick(TransactionRecordingModel transactionRecordingModel) {

                                                }
                                            }, type);
                                            binding.recyclerView.setAdapter(transactionAdapter);
                                            transactionAdapter.notifyDataSetChanged();

                                        }
                                    });
                        }
                    }
                });
    }

    public void shareFile() {

        PdfGenerator.getBuilder()
                .setContext(ExportActivity.this)
                .fromViewSource()
                .fromView(binding.getRoot())
                .setFileName("All_Records")
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

                            Uri uri = FileProvider.getUriForFile(ExportActivity.this, packageID + ".fileprovider", file1);

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
            return ContextCompat.checkSelfPermission(ExportActivity.this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(ExportActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(ExportActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(ExportActivity.this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(ExportActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


}