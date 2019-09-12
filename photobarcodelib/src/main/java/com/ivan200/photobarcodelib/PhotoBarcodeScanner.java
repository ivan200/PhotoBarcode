package com.ivan200.photobarcodelib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;

import com.google.android.gms.vision.barcode.Barcode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;

public class PhotoBarcodeScanner extends PermissionRequester {

    /**
     * Request codes
     */
    public static final int RC_HANDLE_CAMERA_PERM = 1200; //magic number (just number more then most used first 10)

    /**
     * Scanner modes
     */
    public static final int SCANNER_MODE_FREE = 1;
    public static final int SCANNER_MODE_CENTER = 2;

    protected final PhotoBarcodeScannerBuilder mPhotoBarcodeScannerBuilder;

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

    private List<String> getPermissionsToRequest() {
        List<String> cameraPermissions = new ArrayList<>();
        cameraPermissions.add(Manifest.permission.CAMERA);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                mPhotoBarcodeScannerBuilder.isTakingPictureMode() && !mPhotoBarcodeScannerBuilder.isSoundEnabled()){
            //to disable shutter sound on camera we need check permission for it
            cameraPermissions.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
        }

        if(mPhotoBarcodeScannerBuilder.isTakingPictureMode()
                && mPhotoBarcodeScannerBuilder.mGalleryName != null){
            cameraPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                cameraPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        return cameraPermissions;
    }


    public PhotoBarcodeScannerBuilder getPhotoBarcodeScannerBuilder() {
        return mPhotoBarcodeScannerBuilder;
    }

    /**
     * Start a scan for a barcode
     *
     * This opens a new activity with the parameters provided by the PhotoBarcodeScannerBuilder
     */
    public void start(){
        Activity activity = mPhotoBarcodeScannerBuilder.getActivity();
        if(activity == null){
            throw new RuntimeException("Could not start scan: Activity reference lost (please rebuild the PhotoBarcodeScanner before calling start)");
        }
        PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mPhotoBarcodeScannerBuilder.getErrorListener().accept(new RuntimeException("Device has not camera feature"));
            return;
        }

        requestPermissions(getPermissionsToRequest(), activity, RC_HANDLE_CAMERA_PERM);
    }

    @Override
    protected void onPermissionGranted(int requestCode, int resultCode, Intent data) {
        EventBus.getDefault().register(this);

        EventBus.getDefault().postSticky(this);
        Intent intent = new Intent(mPhotoBarcodeScannerBuilder.getActivity(), PhotoBarcodeActivity.class);
        mPhotoBarcodeScannerBuilder.getActivity().startActivity(intent);
    }

    @Override
    protected void onPermissionRejected(int requestCode, String blockedPermission) {

        int messageId;
        String action;
        switch (blockedPermission) {
//            case Manifest.permission.MODIFY_AUDIO_SETTINGS:
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

        Activity activity = mPhotoBarcodeScannerBuilder.getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(android.R.string.dialog_alert_title));
        builder.setMessage(activity.getString(messageId));
        builder.setPositiveButton(activity.getString(android.R.string.ok), (dialog, id) -> {
            dialog.dismiss();
            openAppSettings(mPhotoBarcodeScannerBuilder.getActivity(), finalAction);
        });
        boolean cancellable = mPhotoBarcodeScannerBuilder.getCancelListener() != null;
        if(cancellable){
            builder.setOnCancelListener(dialog -> {
                mPhotoBarcodeScannerBuilder.getCancelListener().run();
                dialog.dismiss();
            });
        }

        builder.setNegativeButton(activity.getString(android.R.string.cancel), (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        if(cancellable){
            dialog.setOnCancelListener(d -> {
                mPhotoBarcodeScannerBuilder.getCancelListener().run();
                d.dismiss();
            });
            dialog.setOnKeyListener((arg0, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mPhotoBarcodeScannerBuilder.getCancelListener() != null) {
                        mPhotoBarcodeScannerBuilder.getCancelListener().run();
                    }
                    arg0.dismiss();
                }
                return true;
            });
        }
        dialog.show();
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
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(intent, RC_HANDLE_CAMERA_PERM);
        }
    }
}
