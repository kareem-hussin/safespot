package com.example.safespot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

public class CustomScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private boolean isFlashlightOn = false; // Variable to track flashlight state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner); // Use your custom scanner layout

        // Initialize the DecoratedBarcodeView
        barcodeView = findViewById(R.id.barcode_scanner);
        ImageView flashlightToggle = findViewById(R.id.flashlightToggle);
        barcodeView.setStatusText("");

        // Start scanning
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    Log.d("CustomScannerActivity", "Scanned: " + result.getText());
                    Intent intent = new Intent();
                    intent.putExtra("SCANNED_ID", result.getText());
                    setResult(RESULT_OK, intent);
                    finish(); // End this activity and return the scanned result
                }
            }

            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                // Optionally handle result points
            }
        });

        // Back button to exit the scanner
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Flashlight toggle button
        flashlightToggle.setOnClickListener(v -> toggleFlashlight());
    }

    private void toggleFlashlight() {
        if (isFlashlightOn) {
            barcodeView.setTorchOff();
            isFlashlightOn = false;
            Toast.makeText(this, "Flashlight turned off", Toast.LENGTH_SHORT).show();
        } else {
            barcodeView.setTorchOn();
            isFlashlightOn = true;
            Toast.makeText(this, "Flashlight turned on", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume(); // Resume scanning
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause(); // Pause scanning to save resources
    }
}
