package com.ivan200.photobarcodelib;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.ivan200.photobarcodelib.images.ImageHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Consumer;

@SuppressWarnings({"WeakerAccess", "unused"})
public class PhotoBarcodeScannerBuilder {

    protected Activity mActivity;
    protected ViewGroup mRootView;

    protected CameraSource mCameraSource;

    protected BarcodeDetector mBarcodeDetector;

    protected boolean mUsed = false; //used to check if a builder is only used

    protected int mFacing = CameraSource.CAMERA_FACING_BACK;
    protected boolean mAutoFocusEnabled = true;

    protected PhotoBarcodeScanner.OnResultListener onResultListener;

    protected int mTrackerColor = Color.parseColor("#F44336"); //Material Red 500

    protected boolean mBleepEnabled = false;

    protected boolean mFlashEnabledByDefault = false;

    protected int mBarcodeFormats = Barcode.ALL_FORMATS;

    protected String mText = "";

    protected int mScannerMode = PhotoBarcodeScanner.SCANNER_MODE_FREE;

    protected int mTrackerResourceID = R.drawable.ic_camera_barcode_square;
    protected int mTrackerDetectedResourceID = R.drawable.ic_camera_barcode_square_green;

    protected boolean takingPictureMode = false;
    public boolean isTakingPictureMode() {
        return takingPictureMode;
    }
    public PhotoBarcodeScannerBuilder withTakingPictureMode(){
        takingPictureMode = true;
        return this;
    }

    protected boolean focusOnTap = true;
    public boolean isFocusOnTap() {
        return focusOnTap;
    }
    public PhotoBarcodeScannerBuilder withFocusOnTap(boolean enable){
        focusOnTap = enable;
        return this;
    }

    protected boolean previewImage = true;
    public boolean isPreviewImage() {
        return previewImage;
    }
    public PhotoBarcodeScannerBuilder withPreviewImage(boolean enable){
        previewImage = enable;
        return this;
    }

    protected PhotoBarcodeScanner.OnPictureListener pictureListener;
    public PhotoBarcodeScanner.OnPictureListener getPictureListener() {
        return pictureListener;
    }
    public PhotoBarcodeScannerBuilder withPictureListener(@NonNull PhotoBarcodeScanner.OnPictureListener pictureListener){
        this.pictureListener = pictureListener;
        return this;
    }

    protected PhotoBarcodeScanner.OnErrorListener errorListener;
    public PhotoBarcodeScanner.OnErrorListener getErrorListener() {
        if(errorListener == null){
            errorListener = getDefaultErrorListener();
        }
        return errorListener;
    }
    public PhotoBarcodeScannerBuilder withErrorListener(@NonNull PhotoBarcodeScanner.OnErrorListener errorListener){
        this.errorListener = errorListener;
        return this;
    }

    protected boolean cameraFullScreenMode = false;
    public boolean isCameraFullScreenMode() {
        return cameraFullScreenMode;
    }
    public PhotoBarcodeScannerBuilder withCameraFullScreenMode(boolean cameraFullScreenMode) {
        this.cameraFullScreenMode = cameraFullScreenMode;
        return this;
    }

    protected float requestedFps = 25.0f;
    public float getRequestedFps() {
        return requestedFps;
    }
    public PhotoBarcodeScannerBuilder withRequestedFps(float requestedFps) {
        this.requestedFps = requestedFps;
        return this;
    }

    protected int imageLargerSide = ImageHelper.defaultMaxImageSize;
    public int getImageLargerSide() {
        return imageLargerSide;
    }
    public PhotoBarcodeScannerBuilder withImageLargerSide(int size) {
        this.imageLargerSide = size;
        return this;
    }

    protected boolean cameraTryFixOrientation = true;
    public boolean isCameraTryFixOrientation() {
        return cameraTryFixOrientation;
    }
    public PhotoBarcodeScannerBuilder withCameraTryFixOrientation(boolean cameraTryFixOrientation) {
        this.cameraTryFixOrientation = cameraTryFixOrientation;
        return this;
    }

    protected boolean hasThumbnails = false;
    public boolean hasThumbnails() {
        return hasThumbnails;
    }
    public PhotoBarcodeScannerBuilder withThumbnails(boolean enabled) {
        this.hasThumbnails = enabled;
        return this;
    }


    protected boolean cameraLockRotate = true;
    public boolean isCameraLockRotate() {
        return cameraLockRotate;
    }
    public PhotoBarcodeScannerBuilder withCameraLockRotate(boolean cameraLockRotate) {
        this.cameraLockRotate = cameraLockRotate;
        return this;
    }

    protected boolean cameraShutterSound = true;
    public boolean hasCameraShutterSound() {
        return cameraShutterSound;
    }
    public PhotoBarcodeScannerBuilder withCameraShutterSound(boolean cameraShutterSound) {
        this.cameraShutterSound = cameraShutterSound;
        return this;
    }

    protected Consumer<Throwable> minorErrorHandler;
    public Consumer<Throwable> getMinorErrorHandler() {
        return minorErrorHandler;
    }
    public PhotoBarcodeScannerBuilder withMinorErrorHandler(Consumer<Throwable> minorErrorHandler) {
        this.minorErrorHandler = minorErrorHandler;
        return this;
    }

    /**
     * Default constructor
     */
    public PhotoBarcodeScannerBuilder() {

    }

    /**
     * Called immediately after a barcode was scanned
     */
    public PhotoBarcodeScannerBuilder withResultListener(@NonNull PhotoBarcodeScanner.OnResultListener onResultListener){
        this.onResultListener = onResultListener;
        return this;
    }

    /**
     * Construct a PhotoBarcodeScannerBuilder by passing the activity to use for the generation
     *
     * @param activity current activity which will contain the drawer
     */
    public PhotoBarcodeScannerBuilder(@NonNull Activity activity) {
        this.mRootView = activity.findViewById(android.R.id.content);
        this.mActivity = activity;
    }

    /**
     * Sets the activity which will be used as the parent of the PhotoBarcodeScanner activity
     * @param activity current activity which will contain the PhotoBarcodeScanner
     */
    public PhotoBarcodeScannerBuilder withActivity(@NonNull Activity activity) {
        this.mRootView = activity.findViewById(android.R.id.content);
        this.mActivity = activity;
        return this;
    }

    /**
     * Makes the barcode scanner use the camera facing back
     */
    public PhotoBarcodeScannerBuilder withBackFacingCamera(){
        mFacing = CameraSource.CAMERA_FACING_BACK;
        return this;
    }

    /**
     * Makes the barcode scanner use camera facing front
     */
    public PhotoBarcodeScannerBuilder withFrontFacingCamera(){
        mFacing = CameraSource.CAMERA_FACING_FRONT;
        return this;
    }

    /**
     * Either CameraSource.CAMERA_FACING_FRONT or CameraSource.CAMERA_FACING_BACK
     */
    public PhotoBarcodeScannerBuilder withCameraFacing(int cameraFacing){
        mFacing = cameraFacing;
        return this;
    }

    /**
     * Enables or disables auto focusing on the camera
     */
    public PhotoBarcodeScannerBuilder withAutoFocus(boolean enabled){
        mAutoFocusEnabled = enabled;
        return this;
    }

    /**
     * Sets the tracker color used by the barcode scanner, By default this is Material Red 500 (#F44336).
     */
    public PhotoBarcodeScannerBuilder withTrackerColor(int color){
        mTrackerColor = color;
        return this;
    }

    /**
     * Enables or disables a bleep sound whenever a barcode is scanned
     */
    public PhotoBarcodeScannerBuilder withBleepEnabled(boolean enabled){
        mBleepEnabled = enabled;
        return this;
    }

    /**
     * Shows a text message at the top of the barcode scanner
     */
    public PhotoBarcodeScannerBuilder withText(String text){
        mText = text;
        return this;
    }

    /**
     * Shows a text message at the top of the barcode scanner
     */
    public PhotoBarcodeScannerBuilder withFlashLightEnabledByDefault(){
        mFlashEnabledByDefault = true;
        return this;
    }

    /**
     * Bit mask (containing values like QR_CODE and so on) that selects which formats this barcode detector should recognize.
     */
    public PhotoBarcodeScannerBuilder withBarcodeFormats(int barcodeFormats){
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
    public PhotoBarcodeScannerBuilder withOnly3DScanning(){
        mBarcodeFormats = Barcode.QR_CODE | Barcode.DATA_MATRIX | Barcode.PDF417 | Barcode.AZTEC;
        return this;
    }

    /**
     * Enables exclusive scanning on QR Codes, no other barcodes will be detected
     */
    public PhotoBarcodeScannerBuilder withOnlyQRCodeScanning(){
        mBarcodeFormats = Barcode.QR_CODE;
        return this;
    }

    /**
     * Enables the default center tracker. This tracker is always visible and turns green when a barcode is found.\n
     * Please note that you can still scan a barcode outside the center tracker! This is purely a visual change.
     */
    public PhotoBarcodeScannerBuilder withCenterTracker(){
        mScannerMode = PhotoBarcodeScanner.SCANNER_MODE_CENTER;
        return this;
    }

    /**
     * Enables the center tracker with a custom drawable resource. This tracker is always visible.\n
     * Please note that you can still scan a barcode outside the center tracker! This is purely a visual change.
     * @param trackerResourceId a drawable resource id
     * @param detectedTrackerResourceId a drawable resource id for the detected tracker state
     */
    public PhotoBarcodeScannerBuilder withCenterTracker(int trackerResourceId, int detectedTrackerResourceId){
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
        if(mAutoFocusEnabled){
            focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        }
        if(!isTakingPictureMode()){
            mBarcodeDetector = new BarcodeDetector.Builder(mActivity)
                    .setBarcodeFormats(mBarcodeFormats)
                    .build();
        }

        Size deviceSize = getDeviceDisplaySizePixels(mActivity);
        int previewWidth = deviceSize.getWidth();
        int previewHeight = deviceSize.getHeight();
        if(!isCameraFullScreenMode()){
            previewHeight = Math.min(previewHeight, previewWidth);
            previewWidth = previewHeight * 4/3;
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

    private static Size getDeviceDisplaySizePixels(Activity activity){
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

    private PhotoBarcodeScanner.OnErrorListener getDefaultErrorListener(){
        return ex -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mActivity.getString(android.R.string.dialog_alert_title));
            builder.setMessage(ex.getLocalizedMessage());
            builder.setPositiveButton(mActivity.getString(android.R.string.ok), (dialog, id) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        };
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
     * Get the bleep enabled value associated with this builder
     */
    public boolean isBleepEnabled() {
        return mBleepEnabled;
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
