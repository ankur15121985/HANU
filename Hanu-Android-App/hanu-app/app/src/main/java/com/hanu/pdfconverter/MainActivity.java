package com.hanu.pdfconverter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Uri selectedPdfUri = null;
    private String selectedFormat = null; // "excel", "word", "ppt"

    private TextView tvFileName;
    private TextView tvFileSize;
    private View fileInfoCard;
    private View placeholderCard;
    private MaterialCardView cardExcel, cardWord, cardPpt;
    private MaterialButton btnConvert;
    private View selectedIndicatorExcel, selectedIndicatorWord, selectedIndicatorPpt;

    private final ActivityResultLauncher<Intent> pdfPickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                selectedPdfUri = result.getData().getData();
                if (selectedPdfUri != null) {
                    displayFileInfo(selectedPdfUri);
                }
            }
        });

    private final ActivityResultLauncher<String[]> permissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
            boolean allGranted = true;
            for (Boolean granted : permissions.values()) {
                if (!granted) { allGranted = false; break; }
            }
            if (allGranted) openFilePicker();
            else showSnackbar("Storage permission needed to pick files");
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvFileName = findViewById(R.id.tv_file_name);
        tvFileSize = findViewById(R.id.tv_file_size);
        fileInfoCard = findViewById(R.id.file_info_card);
        placeholderCard = findViewById(R.id.placeholder_card);
        cardExcel = findViewById(R.id.card_excel);
        cardWord = findViewById(R.id.card_word);
        cardPpt = findViewById(R.id.card_ppt);
        btnConvert = findViewById(R.id.btn_convert);
        selectedIndicatorExcel = findViewById(R.id.indicator_excel);
        selectedIndicatorWord = findViewById(R.id.indicator_word);
        selectedIndicatorPpt = findViewById(R.id.indicator_ppt);

        fileInfoCard.setVisibility(View.GONE);
        btnConvert.setEnabled(false);
    }

    private void setupClickListeners() {
        // Pick PDF
        View pickArea = findViewById(R.id.pick_pdf_area);
        pickArea.setOnClickListener(v -> checkPermissionsAndPick());

        MaterialButton btnPickPdf = findViewById(R.id.btn_pick_pdf);
        btnPickPdf.setOnClickListener(v -> checkPermissionsAndPick());

        // Format cards
        cardExcel.setOnClickListener(v -> selectFormat("excel"));
        cardWord.setOnClickListener(v -> selectFormat("word"));
        cardPpt.setOnClickListener(v -> selectFormat("ppt"));

        // Convert button
        btnConvert.setOnClickListener(v -> startConversion());

        // Remove file
        View btnRemoveFile = findViewById(R.id.btn_remove_file);
        if (btnRemoveFile != null) {
            btnRemoveFile.setOnClickListener(v -> removeFile());
        }
    }

    private void checkPermissionsAndPick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openFilePicker();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openFilePicker();
            } else {
                permissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
            }
        } else {
            openFilePicker();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(Intent.createChooser(intent, "Select PDF File"));
    }

    private void displayFileInfo(Uri uri) {
        String fileName = "Unknown File";
        long fileSize = 0;

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (nameIdx != -1) fileName = cursor.getString(nameIdx);
                if (sizeIdx != -1) fileSize = cursor.getLong(sizeIdx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvFileName.setText(fileName);
        tvFileSize.setText(formatFileSize(fileSize));
        placeholderCard.setVisibility(View.GONE);
        fileInfoCard.setVisibility(View.VISIBLE);

        updateConvertButton();
        showSnackbar("PDF selected! Now choose output format.");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        else if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        else return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }

    private void selectFormat(String format) {
        selectedFormat = format;

        // Reset all cards
        cardExcel.setStrokeWidth(0);
        cardWord.setStrokeWidth(0);
        cardPpt.setStrokeWidth(0);
        selectedIndicatorExcel.setVisibility(View.GONE);
        selectedIndicatorWord.setVisibility(View.GONE);
        selectedIndicatorPpt.setVisibility(View.GONE);

        int strokeWidth = (int) (2 * getResources().getDisplayMetrics().density);

        switch (format) {
            case "excel":
                cardExcel.setStrokeWidth(strokeWidth);
                cardExcel.setStrokeColor(ContextCompat.getColor(this, R.color.excel_green));
                selectedIndicatorExcel.setVisibility(View.VISIBLE);
                break;
            case "word":
                cardWord.setStrokeWidth(strokeWidth);
                cardWord.setStrokeColor(ContextCompat.getColor(this, R.color.word_blue));
                selectedIndicatorWord.setVisibility(View.VISIBLE);
                break;
            case "ppt":
                cardPpt.setStrokeWidth(strokeWidth);
                cardPpt.setStrokeColor(ContextCompat.getColor(this, R.color.ppt_orange));
                selectedIndicatorPpt.setVisibility(View.VISIBLE);
                break;
        }

        updateConvertButton();
    }

    private void updateConvertButton() {
        boolean canConvert = selectedPdfUri != null && selectedFormat != null;
        btnConvert.setEnabled(canConvert);
        if (canConvert) {
            String label = "Convert to " + getFormatLabel();
            btnConvert.setText(label);
        } else {
            btnConvert.setText("Convert");
        }
    }

    private String getFormatLabel() {
        if (selectedFormat == null) return "";
        switch (selectedFormat) {
            case "excel": return "Excel (.xlsx)";
            case "word": return "Word (.docx)";
            case "ppt": return "PowerPoint (.pptx)";
            default: return "";
        }
    }

    private void startConversion() {
        if (selectedPdfUri == null || selectedFormat == null) return;

        Intent intent = new Intent(this, ConvertActivity.class);
        intent.putExtra("pdf_uri", selectedPdfUri.toString());
        intent.putExtra("format", selectedFormat);
        intent.putExtra("file_name", tvFileName.getText().toString());
        startActivity(intent);
    }

    private void removeFile() {
        selectedPdfUri = null;
        fileInfoCard.setVisibility(View.GONE);
        placeholderCard.setVisibility(View.VISIBLE);
        updateConvertButton();
    }

    private void showSnackbar(String msg) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT).show();
    }
}
