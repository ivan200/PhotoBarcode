package com.ivan200.photobarcode;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ivan200.photobarcodelib.PhotoBarcodeScanner;
import com.ivan200.photobarcodelib.PhotoBarcodeScannerBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    PhotoBarcodeScanner photoBarcodeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        imageView = findViewById(R.id.image);
        textView = findViewById(R.id.text);

        FloatingActionButton fabPicture = findViewById(R.id.fab_picture);
        fabPicture.setOnClickListener(view -> takePicture());

        FloatingActionButton fabBarcode = findViewById(R.id.fab_barcode);
        fabBarcode.setOnClickListener(view -> takeBarcode());
    }

    private void takePicture(){
        photoBarcodeScanner = new PhotoBarcodeScannerBuilder(this)
                .withTakingPictureMode()
                .withPictureListener(file -> {
                    textView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageURI(Uri.fromFile(file));
                })
                .build();
        photoBarcodeScanner.start();
    }

    private void takeBarcode() {
        photoBarcodeScanner = new PhotoBarcodeScannerBuilder(this)
                .withCenterTracker(true)
                .withResultListener((Barcode barcode) -> {
                    textView.setText(barcode.rawValue);
                    textView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                })
                .build();
        photoBarcodeScanner.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(photoBarcodeScanner != null){
            photoBarcodeScanner.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(photoBarcodeScanner != null){
            photoBarcodeScanner.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
