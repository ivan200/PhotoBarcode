package com.ivan200.photobarcode;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ivan200.photobarcodelib.PhotoBarcodeScanner;
import com.ivan200.photobarcodelib.PhotoBarcodeScannerBuilder;

import androidx.appcompat.app.AlertDialog;
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
                .withAutoFocus(true)
                .withBackFacingCamera()
                .withCameraFullScreenMode(false)
                .withCameraLockRotate(true)
                .withFocusOnTap(true)

                .withTakingPictureMode()
                .withPreviewImage(true)
                .withCameraTryFixOrientation(true)
                .withThumbnails(false)
                .withCameraShutterSound(false)
                .withMinorErrorHandler(Throwable::printStackTrace)
                .withErrorListener(ex -> {
                    ex.printStackTrace();

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(android.R.string.dialog_alert_title));
                    builder.setMessage(ex.getLocalizedMessage());
                    builder.setPositiveButton(MainActivity.this.getString(android.R.string.ok), (dialog, id) -> dialog.dismiss());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                })
                .withPictureListener(file -> {
                    Log.d("TEST", "OK");
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
                .withCenterTracker()
                .withAutoFocus(true)
                .withBackFacingCamera()
                .withFocusOnTap(true)
                .withCameraFullScreenMode(false)
                .withCameraLockRotate(true)
                .withBleepEnabled(true)
                .withMinorErrorHandler(Throwable::printStackTrace)
                .withResultListener((com.google.android.gms.vision.barcode.Barcode barcode) -> {
                    textView.setText(barcode.rawValue);
                    textView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                })
                .build();
        photoBarcodeScanner.start();
    }
}
