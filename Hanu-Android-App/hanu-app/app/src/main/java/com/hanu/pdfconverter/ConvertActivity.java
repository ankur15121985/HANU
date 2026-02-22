package com.hanu.pdfconverter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConvertActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvStatus;
    private TextView tvProgress;
    private TextView tvFileName;
    private View layoutConverting;
    private View layoutSuccess;
    private View layoutError;
    private MaterialButton btnShareFile;
    private MaterialButton btnOpenFile;
    private MaterialButton btnConvertAnother;
    private MaterialButton btnRetry;

    private String pdfUriString;
    private String format;
    private String inputFileName;
    private File outputFile;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);

        pdfUriString = getIntent().getStringExtra("pdf_uri");
        format = getIntent().getStringExtra("format");
        inputFileName = getIntent().getStringExtra("file_name");

        initViews();
        startConversion();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);
        tvProgress = findViewById(R.id.tv_progress);
        tvFileName = findViewById(R.id.tv_converting_filename);
        layoutConverting = findViewById(R.id.layout_converting);
        layoutSuccess = findViewById(R.id.layout_success);
        layoutError = findViewById(R.id.layout_error);
        btnShareFile = findViewById(R.id.btn_share);
        btnOpenFile = findViewById(R.id.btn_open);
        btnConvertAnother = findViewById(R.id.btn_convert_another);
        btnRetry = findViewById(R.id.btn_retry);

        tvFileName.setText(inputFileName != null ? inputFileName : "your PDF");

        layoutSuccess.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);

        btnConvertAnother.setOnClickListener(v -> finish());
        btnRetry.setOnClickListener(v -> {
            layoutError.setVisibility(View.GONE);
            layoutConverting.setVisibility(View.VISIBLE);
            startConversion();
        });

        btnShareFile.setOnClickListener(v -> shareFile());
        btnOpenFile.setOnClickListener(v -> openFile());
    }

    private void startConversion() {
        Uri pdfUri = Uri.parse(pdfUriString);
        updateProgress(0, "Reading PDF file…");

        executor.execute(() -> {
            try {
                PdfConverter converter = new PdfConverter(this);
                converter.setProgressCallback((progress, status) ->
                    mainHandler.post(() -> updateProgress(progress, status))
                );

                String outputFileName = getOutputFileName();
                File outputDir = new File(getExternalFilesDir(null), "Hanu_Converted");
                if (!outputDir.exists()) outputDir.mkdirs();
                outputFile = new File(outputDir, outputFileName);

                switch (format) {
                    case "excel":
                        converter.pdfToExcel(pdfUri, outputFile);
                        break;
                    case "word":
                        converter.pdfToWord(pdfUri, outputFile);
                        break;
                    case "ppt":
                        converter.pdfToPowerPoint(pdfUri, outputFile);
                        break;
                }

                mainHandler.post(() -> showSuccess());

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> showError(e.getMessage()));
            }
        });
    }

    private String getOutputFileName() {
        String baseName = inputFileName != null
            ? inputFileName.replace(".pdf", "").replace(".PDF", "")
            : "converted";

        switch (format) {
            case "excel": return baseName + "_hanu.xlsx";
            case "word":  return baseName + "_hanu.docx";
            case "ppt":   return baseName + "_hanu.pptx";
            default:      return baseName + "_hanu.xlsx";
        }
    }

    private void updateProgress(int progress, String status) {
        progressBar.setProgress(progress);
        tvStatus.setText(status);
        tvProgress.setText(progress + "%");
    }

    private void showSuccess() {
        layoutConverting.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.VISIBLE);

        TextView tvSuccessName = findViewById(R.id.tv_success_filename);
        TextView tvSuccessSize = findViewById(R.id.tv_success_size);
        TextView tvSuccessFormat = findViewById(R.id.tv_success_format);

        if (outputFile != null && outputFile.exists()) {
            tvSuccessName.setText(outputFile.getName());
            tvSuccessSize.setText(formatFileSize(outputFile.length()));
        }

        String formatLabel = "";
        switch (format) {
            case "excel": formatLabel = "Excel Spreadsheet (.xlsx)"; break;
            case "word":  formatLabel = "Word Document (.docx)"; break;
            case "ppt":   formatLabel = "PowerPoint Presentation (.pptx)"; break;
        }
        tvSuccessFormat.setText(formatLabel);
    }

    private void showError(String error) {
        layoutConverting.setVisibility(View.GONE);
        layoutSuccess.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);

        TextView tvError = findViewById(R.id.tv_error_message);
        tvError.setText(error != null ? error : "An unknown error occurred. Please try again.");
    }

    private void shareFile() {
        if (outputFile == null || !outputFile.exists()) return;
        Uri fileUri = FileProvider.getUriForFile(this,
            getPackageName() + ".provider", outputFile);

        String mimeType = getMimeType();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share converted file"));
    }

    private void openFile() {
        if (outputFile == null || !outputFile.exists()) return;
        Uri fileUri = FileProvider.getUriForFile(this,
            getPackageName() + ".provider", outputFile);

        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setDataAndType(fileUri, getMimeType());
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(viewIntent);
        } catch (Exception e) {
            shareFile(); // Fallback to share if no viewer
        }
    }

    private String getMimeType() {
        switch (format) {
            case "excel": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "word":  return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "ppt":   return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default:      return "*/*";
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        else return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
