package com.ivan200.photobarcodelib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.FrameLayout;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

        List<String> cameraPermissions = new ArrayList<>();
        cameraPermissions.add(Manifest.permission.CAMERA);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                mPhotoBarcodeScannerBuilder.isTakingPictureMode() && !mPhotoBarcodeScannerBuilder.isSoundEnabled()){
            //to disable shutter sound on camera we need check permission for it
            cameraPermissions.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        }

        if(mPhotoBarcodeScannerBuilder.isTakingPictureMode() && mPhotoBarcodeScannerBuilder.mGalleryName != null){
            cameraPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                cameraPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }

        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : cameraPermissions){
            if(ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED){
                deniedPermissions.add(permission);
            }
        }

        if (!deniedPermissions.isEmpty()) {
            requestCameraPermission(deniedPermissions);
        } else {
            //Open activity
            EventBus.getDefault().postSticky(this);
            Intent intent = new Intent(activity, PhotoBarcodeActivity.class);
            activity.startActivity(intent);
        }
    }

    private void requestCameraPermission(List<String> cameraPermissions) {
        List<String> blockedPermissions = new ArrayList<>();
        for (String permission : cameraPermissions){
            if(ActivityCompat.shouldShowRequestPermissionRationale(mPhotoBarcodeScannerBuilder.getActivity(), permission)){
                blockedPermissions.add(permission);
            }
        }
        ActivityCompat.requestPermissions(mPhotoBarcodeScannerBuilder.getActivity(), cameraPermissions.toArray(new String[0]), RC_HANDLE_CAMERA_PERM);
        if(blockedPermissions.isEmpty()){
            return;
        }

        int messageId;
        String action;
        switch (blockedPermissions.get(0)) {
            case Manifest.permission.ACCESS_NOTIFICATION_POLICY:
                messageId = R.string.permission_notification_rationale;
                action = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS;
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                messageId = R.string.permission_sdcard_rationale;
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
                break;
            case Manifest.permission.CAMERA:
            default:
                messageId = R.string.permission_camera_rationale;
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
                break;
        }

        String finalAction = action;

        Snackbar.make(mPhotoBarcodeScannerBuilder.mRootView, messageId,
                Snackbar.LENGTH_LONG)
                .setAction(android.R.string.ok, view ->
                        openAppSettings(mPhotoBarcodeScannerBuilder.getActivity(), finalAction))
                .show();
    }

    public void openAppSettings(Activity activity, String action){
        Intent intent = new Intent();
        intent.setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.putExtra(Intent.EXTRA_PACKAGE_NAME, activity.getPackageName());
        }
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        }
    }

    public PhotoBarcodeScannerBuilder getPhotoBarcodeScannerBuilder() {
        return mPhotoBarcodeScannerBuilder;
    }
}
