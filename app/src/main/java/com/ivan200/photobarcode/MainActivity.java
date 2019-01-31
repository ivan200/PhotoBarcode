package com.ivan200.photobarcode;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ivan200.photobarcodelib.PhotoBarcodeScanner;
import com.ivan200.photobarcodelib.PhotoBarcodeScannerBuilder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        final PhotoBarcodeScanner photoBarcodeScanner = new PhotoBarcodeScannerBuilder()
                .withActivity(this)
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
        final PhotoBarcodeScanner photoBarcodeScanner = new PhotoBarcodeScannerBuilder()
                .withActivity(this)
                .withCenterTracker(true)
                .withResultListener((Barcode barcode) -> {
                    textView.setText(barcode.rawValue);
                    textView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                })
                .build();
        photoBarcodeScanner.start();
    }
}
