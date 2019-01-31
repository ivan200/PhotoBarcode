package com.ivan200.photobarcodelib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Consumer;

public class PhotoBarcodeScanner {

    /**
     * Request codes
     */
    public static final int RC_HANDLE_CAMERA_PERM = 2;

    /**
     * Scanner modes
     */
    public static final int SCANNER_MODE_FREE = 1;
    public static final int SCANNER_MODE_CENTER = 2;

    protected final PhotoBarcodeScannerBuilder mPhotoBarcodeScannerBuilder;

    private FrameLayout mContentView; //Content frame for fragments

    private Consumer<Barcode> onResultListener;

    public PhotoBarcodeScanner(@NonNull PhotoBarcodeScannerBuilder photoBarcodeScannerBuilder) {
        this.mPhotoBarcodeScannerBuilder = photoBarcodeScannerBuilder;
    }

    public void setOnResultListener(Consumer<Barcode> onResultListener) {
        this.onResultListener = onResultListener;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBarcodeScannerResult(Barcode barcode){
        onResultListener.accept(barcode);
        EventBus.getDefault().removeStickyEvent(barcode);

        EventBus.getDefault().unregister(this);
        mPhotoBarcodeScannerBuilder.clean();
    }

    /**
     * Start a scan for a barcode
     *
     * This opens a new activity with the parameters provided by the PhotoBarcodeScannerBuilder
     */
    public void start(){
        EventBus.getDefault().register(this);

        Activity activity = mPhotoBarcodeScannerBuilder.getActivity();
        if(activity == null){
            throw new RuntimeException("Could not start scan: Activity reference lost (please rebuild the PhotoBarcodeScanner before calling start)");
        }
        PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mPhotoBarcodeScannerBuilder.getErrorListener().accept(new RuntimeException("Device has not camera feature"));
            return;
        }

        int mCameraPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
        if (mCameraPermission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }else{
            //Open activity
            EventBus.getDefault().postSticky(this);
            Intent intent = new Intent(activity, PhotoBarcodeActivity.class);
            activity.startActivity(intent);
        }
    }

    private void requestCameraPermission() {
        final String[] mPermissions = new String[]{Manifest.permission.CAMERA};
        if (!ActivityCompat.shouldShowRequestPermissionRationale(mPhotoBarcodeScannerBuilder.getActivity(), Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(mPhotoBarcodeScannerBuilder.getActivity(), mPermissions, RC_HANDLE_CAMERA_PERM);
            return;
        }
        View.OnClickListener listener = view -> ActivityCompat.requestPermissions(mPhotoBarcodeScannerBuilder.getActivity(), mPermissions, RC_HANDLE_CAMERA_PERM);
        Snackbar.make(mPhotoBarcodeScannerBuilder.mRootView, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, listener)
                .show();
    }

    public PhotoBarcodeScannerBuilder getPhotoBarcodeScannerBuilder() {
        return mPhotoBarcodeScannerBuilder;
    }

}
