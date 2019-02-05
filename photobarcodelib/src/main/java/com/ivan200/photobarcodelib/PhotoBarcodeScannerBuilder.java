package com.ivan200.photobarcodelib;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.ivan200.photobarcodelib.images.ImageHelper;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;

@SuppressWarnings({"WeakerAccess", "unused"})
public class PhotoBarcodeScannerBuilder {

    protected Activity mActivity;
    protected CameraSource mCameraSource;
    protected BarcodeDetector mBarcodeDetector;
    protected boolean mUsed = false; //used to check if a builder is only used
    protected int mFacing = CameraSource.CAMERA_FACING_BACK;
    protected boolean mAutoFocusEnabled = true;
    protected Consumer<Barcode> onResultListener;
    protected int mTrackerColor = Color.parseColor("#F44336"); //Material Red 500
    protected boolean mSoundEnabled = true;
    protected boolean mFlashEnabledByDefault = false;
    protected int mBarcodeFormats = Barcode.ALL_FORMATS;
    protected String mText = "";
    protected String mGalleryName;
    protected int mScannerMode = PhotoBarcodeScanner.SCANNER_MODE_FREE;
    protected int mTrackerResourceID = R.drawable.ic_camera_barcode_square;
    protected int mTrackerDetectedResourceID = R.drawable.ic_camera_barcode_square_green;
    protected boolean takingPictureMode = false;
    protected boolean focusOnTap = true;
    protected boolean previewImage = true;
    protected Consumer<File> pictureListener;
    protected Consumer<Throwable> errorListener;
    protected boolean cameraFullScreenMode = false;
    protected float requestedFps = 25.0f;
    protected int imageLargerSide = ImageHelper.defaultMaxImageSize;
    protected boolean cameraTryFixOrientation = true;
    protected boolean hasThumbnails = false;
    protected boolean cameraLockRotate = true;
    protected Consumer<Throwable> minorErrorHandler;

    /**
     * Default constructor
     */
    public PhotoBarcodeScannerBuilder() {

    }

    /**
     * Construct a PhotoBarcodeScannerBuilder by passing the activity to use for the generation
     *
     * @param activity current activity which will contain the drawer
     */
    public PhotoBarcodeScannerBuilder(@NonNull Activity activity) {
        this.mActivity = activity;
    }

    private static Size getDeviceDisplaySizePixels(Activity activity) {
        Display d = activity.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            return new Size(realDisplayMetrics.widthPixels, realDisplayMetrics.heightPixels);
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);
            return new Size(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
    }

    public boolean isTakingPictureMode() {
        return takingPictureMode;
    }

    /**
     * Activate takingPicture mode instead of taking barcode mode
     */
    public PhotoBarcodeScannerBuilder withTakingPictureMode() {
        takingPictureMode = true;
        return this;
    }

    public boolean isFocusOnTap() {
        return focusOnTap;
    }

    /**
     * Allow focus picture when user tap on screen
     */
    public PhotoBarcodeScannerBuilder withFocusOnTap(boolean enable) {
        focusOnTap = enable;
        return this;
    }

    public boolean isPreviewImage() {
        return previewImage;
    }

    /**
     * allow preview image and redo it before it returned
     */
    public PhotoBarcodeScannerBuilder withPreviewImage(boolean enable) {
        previewImage = enable;
        return this;
    }

    /**
     * set listener to take picture
     * file will saved in context.getFilesDir()/photos
     */
    public PhotoBarcodeScannerBuilder withPictureListener(@NonNull Consumer<File> pictureListener) {
        this.pictureListener = pictureListener;
        return this;
    }

    public Consumer<File> getPictureListener() {
        return pictureListener;
    }

    /**
     * Set listener of errors which should? be shown to user
     */
    public PhotoBarcodeScannerBuilder withErrorListener(Consumer<Throwable> errorListener) {
        this.errorListener = errorListener;
        return this;
    }

    public Consumer<Throwable> getErrorListener() {
        if (errorListener == null) {
            errorListener = ex -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(mActivity.getString(android.R.string.dialog_alert_title));
                builder.setMessage(ex.getLocalizedMessage());
                builder.setPositiveButton(mActivity.getString(android.R.string.ok), (dialog, id) -> dialog.dismiss());
                AlertDialog dialog = builder.create();
                dialog.show();
            };
        }
        return errorListener;
    }

    public boolean isCameraFullScreenMode() {
        return cameraFullScreenMode;
    }

    /**
     * Mode of taking pictures: FullScreen - 16/9 with horizontal crop or otherwise 4/3 with screen fit
     */
    public PhotoBarcodeScannerBuilder withCameraFullScreenMode(boolean cameraFullScreenMode) {
        this.cameraFullScreenMode = cameraFullScreenMode;
        return this;
    }

    public float getRequestedFps() {
        return requestedFps;
    }

    /**
     * Fps in preview of picture.
     */
    public PhotoBarcodeScannerBuilder withRequestedFps(float requestedFps) {
        this.requestedFps = requestedFps;
        return this;
    }

    public int getImageLargerSide() {
        return imageLargerSide;
    }

    /**
     * Once the picture is taken, it automatically save into phone gallery too (DCIM directory)
     * you need permissions WRITE_EXTERNAL_STORAGE and READ_EXTERNAL_STORAGE to use it
     * null folderName = do not save, empty = without additional folder, folderName = folder name for personal gallery
     */
    public PhotoBarcodeScannerBuilder withSavePhotoToGallery(String folderName) {
        this.mGalleryName = folderName;
        return this;
    }

    public String getGalleryName() {
        return mGalleryName;
    }

    /**
     * Once the picture is taken, it automatically resizes by the maximum side before returning.
     * Or does not change the size if the photo is smaller than this value.
     */
    public PhotoBarcodeScannerBuilder withImageLargerSide(int size) {
        this.imageLargerSide = size;
        return this;
    }

    public boolean isCameraTryFixOrientation() {
        return cameraTryFixOrientation;
    }

    /**
     * Automatically try to rotate image by phone sensors (accelerometer or gyroscope)
     */
    public PhotoBarcodeScannerBuilder withCameraTryFixOrientation(boolean cameraTryFixOrientation) {
        this.cameraTryFixOrientation = cameraTryFixOrientation;
        return this;
    }

    public boolean hasThumbnails() {
        return hasThumbnails;
    }

    /**
     * If this flag is set, in addition to the photo the thumbnail will be saved too
     * in context.getFilesDir()/thumbnails
     */
    public PhotoBarcodeScannerBuilder withThumbnails(boolean enabled) {
        this.hasThumbnails = enabled;
        return this;
    }

    public boolean isCameraLockRotate() {
        return cameraLockRotate;
    }

    /**
     * lock rotate phone and orientation in camera activity (to avoid recreating view)
     */
    public PhotoBarcodeScannerBuilder withCameraLockRotate(boolean cameraLockRotate) {
        this.cameraLockRotate = cameraLockRotate;
        return this;
    }

    public Consumer<Throwable> getMinorErrorHandler() {
        if (minorErrorHandler == null) {
            minorErrorHandler = Throwable::printStackTrace;
        }
        return minorErrorHandler;
    }

    /**
     * Sets error handler of non fatal exceptions
     */
    public PhotoBarcodeScannerBuilder withMinorErrorHandler(Consumer<Throwable> minorErrorHandler) {
        this.minorErrorHandler = minorErrorHandler;
        return this;
    }

    /**
     * Called immediately after a barcode was scanned
     */
    public PhotoBarcodeScannerBuilder withResultListener(@NonNull Consumer<Barcode> onResultListener) {
        this.onResultListener = onResultListener;
        return this;
    }

    /**
     * Sets the activity which will be used as the parent of the PhotoBarcodeScanner activity
     *
     * @param activity current activity which will contain the PhotoBarcodeScanner
     */
    public PhotoBarcodeScannerBuilder withActivity(@NonNull Activity activity) {
        this.mActivity = activity;
        return this;
    }

    /**
     * Makes the barcode scanner use the camera facing back or front
     */
    public PhotoBarcodeScannerBuilder withCameraFacingBack(boolean back) {
        mFacing = back ? CameraSource.CAMERA_FACING_BACK : CameraSource.CAMERA_FACING_FRONT;
        return this;
    }

    /**
     * Enables or disables auto focusing on the camera
     */
    public PhotoBarcodeScannerBuilder withAutoFocus(boolean enabled) {
        mAutoFocusEnabled = enabled;
        return this;
    }

    /**
     * Sets the tracker color used by the barcode scanner, By default this is Material Red 500 (#F44336).
     */
    public PhotoBarcodeScannerBuilder withTrackerColor(int color) {
        mTrackerColor = color;
        return this;
    }

    /**
     * Enables or disables a sound whenever picture taken or a barcode is scanned
     */
    public PhotoBarcodeScannerBuilder withSoundEnabled(boolean enabled) {
        mSoundEnabled = enabled;
        return this;
    }

    /**
     * Shows a text message at the top of the barcode scanner
     */
    public PhotoBarcodeScannerBuilder withText(String text) {
        mText = text;
        return this;
    }

    public PhotoBarcodeScannerBuilder withFlashLightEnabledByDefault(boolean enabled) {
        mFlashEnabledByDefault = enabled;
        return this;
    }

    /**
     * Bit mask (containing values like QR_CODE and so on) that selects which formats this barcode detector should recognize.
     */
    public PhotoBarcodeScannerBuilder withBarcodeFormats(int barcodeFormats) {
        mBarcodeFormats = barcodeFormats;
        return this;
    }

    /**
     * Enables exclusive scanning on EAN-13, EAN-8, UPC-A, UPC-E, Code-39, Code-93, Code-128, ITF and Codabar barcodes.
     */
    public PhotoBarcodeScannerBuilder withOnly2DScanning() {
        mBarcodeFormats = Barcode.EAN_13 | Barcode.EAN_8 | Barcode.UPC_A | Barcode.UPC_E | Barcode.CODE_39 | Barcode.CODE_93 | Barcode.CODE_128 | Barcode.ITF | Barcode.CODABAR;
        return this;
    }

    /**
     * Enables exclusive scanning on QR Code, Data Matrix, PDF-417 and Aztec barcodes.
     */
    public PhotoBarcodeScannerBuilder withOnly3DScanning() {
        mBarcodeFormats = Barcode.QR_CODE | Barcode.DATA_MATRIX | Barcode.PDF417 | Barcode.AZTEC;
        return this;
    }

    /**
     * Enables exclusive scanning on QR Codes, no other barcodes will be detected
     */
    public PhotoBarcodeScannerBuilder withOnlyQRCodeScanning() {
        mBarcodeFormats = Barcode.QR_CODE;
        return this;
    }

    /**
     * Enables the default center tracker. This tracker is always visible and turns green when a barcode is found.\n
     * Please note that you can still scan a barcode outside the center tracker! This is purely a visual change.
     */
    public PhotoBarcodeScannerBuilder withCenterTracker(boolean enabled) {
        mScannerMode = PhotoBarcodeScanner.SCANNER_MODE_CENTER;
        return this;
    }

    /**
     * Enables the center tracker with a custom drawable resource. This tracker is always visible.\n
     * Please note that you can still scan a barcode outside the center tracker! This is purely a visual change.
     *
     * @param trackerResourceId         a drawable resource id
     * @param detectedTrackerResourceId a drawable resource id for the detected tracker state
     */
    public PhotoBarcodeScannerBuilder withCenterTracker(int trackerResourceId, int detectedTrackerResourceId) {
        mScannerMode = PhotoBarcodeScanner.SCANNER_MODE_CENTER;
        mTrackerResourceID = trackerResourceId;
        mTrackerDetectedResourceID = detectedTrackerResourceId;
        return this;
    }

    /**
     * Build a ready to use PhotoBarcodeScanner
     *
     * @return A ready to use PhotoBarcodeScanner
     */
    public PhotoBarcodeScanner build() {
        if (mUsed) {
            throw new RuntimeException("You must not reuse a PhotoBarcodeScanner builder");
        }
        if (mActivity == null) {
            throw new RuntimeException("Please pass an activity to the PhotoBarcodeScannerBuilder");
        }
        mUsed = true;
        buildMobileVisionBarcodeDetector();
        PhotoBarcodeScanner photoBarcodeScanner = new PhotoBarcodeScanner(this);
        photoBarcodeScanner.setOnResultListener(onResultListener);
        return photoBarcodeScanner;
    }

    /**
     * Build a barcode scanner using the Mobile Vision Barcode API
     */
    private void buildMobileVisionBarcodeDetector() {
        String focusMode = Camera.Parameters.FOCUS_MODE_FIXED;
        if (mAutoFocusEnabled) {
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        }
        if (!isTakingPictureMode()) {
            mBarcodeDetector = new BarcodeDetector.Builder(mActivity)
                    .setBarcodeFormats(mBarcodeFormats)
                    .build();
        }

        Size deviceSize = getDeviceDisplaySizePixels(mActivity);
        int previewWidth = deviceSize.getWidth();
        int previewHeight = deviceSize.getHeight();
        if (!isCameraFullScreenMode()) {
            previewHeight = Math.min(previewHeight, previewWidth);
            previewWidth = previewHeight * 4 / 3;
        }

        mCameraSource = new CameraSource.Builder(mActivity, mBarcodeDetector)
                .setFacing(mFacing)
                .setFlashMode(mFlashEnabledByDefault ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setRequestedPreviewSize(previewWidth, previewHeight)
                .setMinImageSize(getImageLargerSide())
                .setFocusMode(focusMode)
                .setRequestedFps(getRequestedFps())
                .build();
    }

    /**
     * Get the activity associated with this builder
     */
    public Activity getActivity() {
        return mActivity;
    }

    /**
     * Get the barcode detector associated with this builder
     */
    public BarcodeDetector getBarcodeDetector() {
        return mBarcodeDetector;
    }

    /**
     * Get the camera source associated with this builder
     */
    public CameraSource getCameraSource() {
        return mCameraSource;
    }


    /**
     * Get the tracker color associated with this builder
     */
    public int getTrackerColor() {
        return mTrackerColor;
    }

    /**
     * Get the text associated with this builder
     */
    public String getText() {
        return mText;
    }

    /**
     * Get the sound enabled value associated with this builder
     */
    public boolean isSoundEnabled() {
        return mSoundEnabled;
    }

    /**
     * Get the flash enabled by default value associated with this builder
     */
    public boolean isFlashEnabledByDefault() {
        return mFlashEnabledByDefault;
    }

    /**
     * Get the tracker detected resource id value associated with this builder
     */
    public int getTrackerDetectedResourceID() {
        return mTrackerDetectedResourceID;
    }

    /**
     * Get the tracker resource id value associated with this builder
     */
    public int getTrackerResourceID() {
        return mTrackerResourceID;
    }

    /**
     * Get the scanner mode value associated with this builder
     */
    public int getScannerMode() {
        return mScannerMode;
    }

    public void clean() {
        mActivity = null;
    }
}
