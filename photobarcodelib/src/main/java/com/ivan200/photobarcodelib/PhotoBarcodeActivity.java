package com.ivan200.photobarcodelib;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ivan200.photobarcodelib.images.ExifData;
import com.ivan200.photobarcodelib.images.ImageHelper;
import com.ivan200.photobarcodelib.orientation.OrientationHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.util.Consumer;
import androidx.core.view.ViewCompat;

public class PhotoBarcodeActivity extends AppCompatActivity {
    private static final int RC_HANDLE_GMS = 9001;

    private static final String TAG = "PhotoBarcodeScanner";

    private PhotoBarcodeScanner mPhotoBarcodeScanner;
    private PhotoBarcodeScannerBuilder mPhotoBarcodeScannerBuilder;

    private BarcodeDetector barcodeDetector;

    private CameraSourcePreview mCameraSourcePreview;

    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    private SoundPoolPlayer mSoundPoolPlayer;

    /**
     * true if no further barcode should be detected or given as a result
     */
    private boolean mDetectionConsumed = false;

    private boolean mFlashOn = false;

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    SavePictureTask savePictureTask;
    Future<?> savePictureFuture;
    class SavePictureTask implements Runnable {
        byte[] jpeg;
        Consumer<File> onPictureListener;
        Consumer<Throwable> onErrorListener;

        SavePictureTask(byte[] jpeg, Consumer<File> onPictureListener, Consumer<Throwable> onErrorListener) {
            this.jpeg = jpeg;
            this.onPictureListener = onPictureListener;
            this.onErrorListener = onErrorListener;
        }
        public void run() {
            PhotoBarcodeActivity.this.savePicture(jpeg, onPictureListener, onErrorListener);
            jpeg = null;
        }
    }

    File mCurrentFile;

    ImageView focusImage;
    ImageButton takePictureButton;
    FloatingActionButton redoButton;
    LinearLayout takePictureLayout;
    ImageView previewImage;
    LinearLayout flashOnButton;
    ImageButton flashToggleIcon;
    TextView topTextView;
    ImageView centerTracker;
    View screenButton;
    FrameLayout topLayout;

    OrientationHelper orientationHelper;
    float currentItemRotationAngle = 0f;

    ExifData imageExifData;

    private Runnable onVolumeKeysDownListener;

    private boolean active = false;
    private Runnable onResumeHandler = null;
    private Runnable onBuilderHandler = null;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_camera_barcode_capture);
    }

    void toggleFullScreen(boolean goFullScreen){
        if (getWindow() != null) {
            if (goFullScreen) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPhotoBarcodeScanner(PhotoBarcodeScanner photoBarcodeScanner) {
        this.mPhotoBarcodeScanner = photoBarcodeScanner;
        mPhotoBarcodeScannerBuilder = mPhotoBarcodeScanner.getPhotoBarcodeScannerBuilder();
        if(onBuilderHandler != null){
            onBuilderHandler.run();
            onBuilderHandler = null;
        }
        barcodeDetector = mPhotoBarcodeScanner.getPhotoBarcodeScannerBuilder().getBarcodeDetector();

        toggleFullScreen(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || !mPhotoBarcodeScannerBuilder.isCameraFullScreenMode());

        focusImage = findViewById(R.id.focus_square);
        takePictureButton = findViewById(R.id.btn_takePicture);
        redoButton = findViewById(R.id.btn_redoPicture);
        takePictureLayout = findViewById(R.id.ll_takePicture);
        previewImage = findViewById(R.id.preview_image);
        flashOnButton = findViewById(R.id.flashIconButton);
        flashToggleIcon = findViewById(R.id.flashIcon);
        topTextView = findViewById(R.id.topText);
        mGraphicOverlay = findViewById(R.id.graphicOverlay);
        mCameraSourcePreview = findViewById(R.id.preview);
        centerTracker = findViewById(R.id.barcode_square);
        topLayout = findViewById(R.id.top_layout);
        screenButton = findViewById(R.id.btn_focus);

        startBarcodeDetector();
        startCameraSource();
        setupSensor();
        setupLayout();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(onVolumeKeysDownListener!= null && isActive()){
                    onVolumeKeysDownListener.run();
                    return true;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void setupSensor() {
        if (mPhotoBarcodeScannerBuilder.isCameraLockRotate()) {
            SensorManager sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sManager != null) {
                orientationHelper = new OrientationHelper(this, this::onOrientationChanged);
            }
        }
    }

    //Rotate items on screen if orientation locked
    private void onOrientationChanged(int virtualOrientation) {
        int rotationDuration = 200;
        int angle = OrientationHelper.getDegreesBySurfaceRotation(virtualOrientation, this);

        if ((currentItemRotationAngle + 90) % 360 == angle) {
            currentItemRotationAngle = currentItemRotationAngle + 90;
        } else if ((currentItemRotationAngle - 90 + 360) % 360 == angle) {
            currentItemRotationAngle = currentItemRotationAngle - 90;
        } else {
            currentItemRotationAngle = angle;
        }

        if (redoButton != null) {
            redoButton.animate().rotation(currentItemRotationAngle).setDuration(rotationDuration).start();
        }
        if (takePictureButton != null) {
            takePictureButton.animate().rotation(currentItemRotationAngle).setDuration(rotationDuration).start();
        }
        if (flashToggleIcon != null) {
            flashToggleIcon.animate().rotation(currentItemRotationAngle).setDuration(rotationDuration).start();
        }
    }



    private void setupLayout() {
        String topText = mPhotoBarcodeScannerBuilder.getText();
        if (!mPhotoBarcodeScannerBuilder.getText().equals("")) {
            topTextView.setText(topText);
        }

        setupCenterTracker();
        setupButtons();
    }

    private void setupCenterTracker() {
        if (mPhotoBarcodeScannerBuilder.getScannerMode() == PhotoBarcodeScanner.SCANNER_MODE_CENTER && !mPhotoBarcodeScannerBuilder.isTakingPictureMode()) {
            centerTracker.setImageResource(mPhotoBarcodeScannerBuilder.getTrackerResourceID());
            mGraphicOverlay.setVisibility(View.INVISIBLE);
        }
    }

    private void updateCenterTrackerForDetectedState() {
        if (mPhotoBarcodeScannerBuilder.getScannerMode() == PhotoBarcodeScanner.SCANNER_MODE_CENTER) {
            runOnUiThread(() -> centerTracker.setImageResource(mPhotoBarcodeScannerBuilder.getTrackerDetectedResourceID()));
        }
    }

    private void clearTopMargins (View view){
        ViewGroup.LayoutParams previewImageParams = view.getLayoutParams();
        if(previewImageParams instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) previewImageParams).topMargin = 0;
            ((ViewGroup.MarginLayoutParams) previewImageParams).leftMargin = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ((ViewGroup.MarginLayoutParams) previewImageParams).setMarginStart(0);
            }
        }
    }

    private void checkEnableTorch(boolean flashOn){
        if (flashOn) {
            flashToggleIcon.setImageResource(R.drawable.ic_camera_flash_on);
            enableTorch();
        } else {
            flashToggleIcon.setImageResource(R.drawable.ic_camera_flash_off);
            disableTorch();
        }
    }

    private void setupButtons() {
        if (mPhotoBarcodeScannerBuilder.isFocusOnTap()
                && getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            screenButton.setOnTouchListener(this::focus);
        }

        flashToggleIcon.setOnClickListener(v -> {
            mFlashOn ^= true;
            checkEnableTorch(mFlashOn);
        });
        if (mPhotoBarcodeScannerBuilder.isFlashEnabledByDefault()) {
            flashToggleIcon.setImageResource(R.drawable.ic_camera_flash_off);
        }

        if (mPhotoBarcodeScannerBuilder.isCameraFullScreenMode()) {
            clearTopMargins(mCameraSourcePreview);
            clearTopMargins(previewImage);
        } else {
            takePictureLayout.setBackgroundColor(Color.TRANSPARENT);

            previewImage.setScaleType(ImageView.ScaleType.FIT_START);
            previewImage.setBackgroundColor(Color.BLACK);
            topLayout.setBackgroundColor(Color.BLACK);
        }

        if (mPhotoBarcodeScannerBuilder.isTakingPictureMode()) {
            takePictureLayout.setVisibility(View.VISIBLE);

            //Add an additional padding at the bottom (side) of the screen if there are on-screen buttons or a cutout on the screen
            ViewCompat.setOnApplyWindowInsetsListener(mCameraSourcePreview, (v, insets) -> {
                WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
                int rotation = wm != null ? wm.getDefaultDisplay().getRotation() : 0;
                switch (rotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        flashOnButton.setPadding(0, insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), 0);
                        takePictureLayout.setPadding(insets.getSystemWindowInsetLeft(), 0, 0, insets.getSystemWindowInsetBottom());
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        flashOnButton.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), 0, 0);
                        takePictureLayout.setPadding(0, 0, insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                        break;
                }
                return insets;
            });

            if (mPhotoBarcodeScannerBuilder.isPreviewImage()) {
                redoButton.hide();
                redoButton.setOnClickListener(this::redoPicture);
                previewImage.setVisibility(View.INVISIBLE);
                takePictureButton.setOnClickListener(this::takeTempPicture);
                onVolumeKeysDownListener = ()-> takeTempPicture(null);
            } else {
                redoButton.hide();
                previewImage.setVisibility(View.GONE);
                takePictureButton.setOnClickListener(this::takePicture);
                onVolumeKeysDownListener = ()-> takePicture(null);
            }
        } else {
            takePictureLayout.setVisibility(View.GONE);
            previewImage.setVisibility(View.GONE);
        }

        if (mPhotoBarcodeScannerBuilder.isFocusOnTap()) {
            focusImage.setVisibility(View.INVISIBLE);
        } else {
            focusImage.setVisibility(View.GONE);
        }
    }

    private void startBarcodeDetector() throws SecurityException {
        // check that the device has play services available.
        mSoundPoolPlayer = new SoundPoolPlayer(this);

        if (!mPhotoBarcodeScannerBuilder.isTakingPictureMode()) {
            int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
            if (code != ConnectionResult.SUCCESS) {
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
                dialog.show();
            }
            BarcodeGraphicTracker.NewDetectionListener listener = barcode -> {
                if (!mDetectionConsumed) {
                    mDetectionConsumed = true;
                    Log.d(TAG, "Barcode detected! - " + barcode.displayValue);
                    EventBus.getDefault().postSticky(barcode);
                    updateCenterTrackerForDetectedState();
                    if (mPhotoBarcodeScannerBuilder.isSoundEnabled()) {
                        mSoundPoolPlayer.playShortResource(R.raw.bleep);
                    }
                    mGraphicOverlay.postDelayed(this::finish, 50);
                }
            };
            BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, listener, mPhotoBarcodeScannerBuilder.getTrackerColor());
            barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        CameraSource mCameraSource = mPhotoBarcodeScannerBuilder.getCameraSource();
        if (mCameraSource != null) {
            try {
                mCameraSourcePreview.setCameraFullScreenMode(mPhotoBarcodeScannerBuilder.isCameraFullScreenMode());
                mCameraSourcePreview.start(mCameraSource, mGraphicOverlay);
            } catch (Exception e) {
                mCameraSource.release();
                mCameraSource = null;

                RuntimeException runtimeException = new RuntimeException("Unable to start camera source.", e);
                if(onTakingErrorObserver!= null){
                    onTakingErrorObserver.accept(runtimeException);
                } else {
                    handleError(PhotoBarcodeActivity.this, runtimeException);
                }
            }
        }
    }

    private boolean canUseTorch() {
        try {
            return mPhotoBarcodeScannerBuilder.getCameraSource().canUseFlash();
        } catch (Exception e) {
            handleSilentError(PhotoBarcodeActivity.this, e);
        }
        return false;
    }

    private void enableTorch() {
        try {
            mPhotoBarcodeScannerBuilder.getCameraSource().setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mPhotoBarcodeScannerBuilder.getCameraSource().start();
        } catch (Exception e) {
            handleSilentError(PhotoBarcodeActivity.this, e);
        }
    }

    private void disableTorch() {
        try {
            mPhotoBarcodeScannerBuilder.getCameraSource().setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mPhotoBarcodeScannerBuilder.getCameraSource().start();
        } catch (Exception e) {
            handleSilentError(PhotoBarcodeActivity.this, e);
        }
    }

    //Focus on a specific view point
    private boolean focus(View view, MotionEvent event){
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                CameraSource cameraSource = mPhotoBarcodeScannerBuilder.getCameraSource();
                if (cameraSource == null || mCameraSourcePreview == null || !mCameraSourcePreview.isSurfaceAvailable()) {
                    return false;
                }
                boolean canSetAuto = mPhotoBarcodeScannerBuilder.getCameraSource().setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                if(!canSetAuto){
                    return false;
                }

                runOnUiThread(() -> {
                    focusImage.setVisibility(View.VISIBLE);
                    focusImage.animate()
                            .x(event.getRawX() - focusImage.getWidth() / 2f)
                            .y(event.getRawY() - focusImage.getHeight() / 2f)
                            .setDuration(0)
                            .start();
                });

                int pointerId = event.getPointerId(0);
                int pointerIndex = event.findPointerIndex(pointerId);
                // Get the pointer's current position
                float x = event.getX(pointerIndex);
                float y = event.getY(pointerIndex);
                float touchMajor = event.getTouchMajor();
                float touchMinor = event.getTouchMinor();
                Rect touchRect = new Rect((int) (x - touchMajor / 2), (int) (y - touchMinor / 2),
                        (int) (x + touchMajor / 2), (int) (y + touchMinor / 2));
                Rect focusArea = new Rect();

                focusArea.set(getAreaPoint(touchRect.left, view.getWidth()),
                        getAreaPoint(touchRect.top, view.getHeight()),
                        getAreaPoint(touchRect.right, view.getWidth()),
                        getAreaPoint(touchRect.bottom, view.getHeight()));

                ArrayList<Camera.Area> focusAreas = new ArrayList<>();
                focusAreas.add(new Camera.Area(focusArea, 1000));
                try {
                    mPhotoBarcodeScannerBuilder.getCameraSource().setFocusAreas(focusAreas);
                } catch (Exception ex) {
                    handleSilentError(PhotoBarcodeActivity.this, ex);
                }
                try {
                    mPhotoBarcodeScannerBuilder.getCameraSource().autoFocus(success ->
                            runOnUiThread(() ->
                                    focusImage.setVisibility(View.INVISIBLE)));
                } catch (Exception e) {
                    focusImage.setVisibility(View.INVISIBLE);
                    handleSilentError(PhotoBarcodeActivity.this, e);
                }
                return true;
        }
        return false;
    }

    private int getAreaPoint(int touch, int max) {
        float point = (float) touch / (float) max * 2000f - 1000f;
        int p = (int) point;
        if (p < -1000) p = -1000;
        if (p > 1000) p = 1000;
        return p;
    }


    private Consumer<File> onTakingPictureObserver = file -> {
//            mGraphicOverlay.postDelayed(this::finish, 50);
        if(mPhotoBarcodeScannerBuilder.mGalleryName != null){
            AsyncTask.execute(() -> {
                try {
                    ImageHelper.copyImageToGallery(mPhotoBarcodeScannerBuilder.getActivity(), file, mPhotoBarcodeScannerBuilder.getGalleryName());
                } catch (IOException e) {
                    mPhotoBarcodeScannerBuilder.getMinorErrorHandler().accept(e);
                }
            });
        }
        setResult(Activity.RESULT_OK);
        PhotoBarcodeActivity.this.finish();
        mPhotoBarcodeScannerBuilder.getPictureListener().accept(file);
    };

    private Consumer<Throwable> onTakingErrorObserver = ex -> {
        PhotoBarcodeActivity.this.finish();
        mPhotoBarcodeScannerBuilder.getErrorListener().accept(ex);
    };

    private void takePicture(View v) {
        if (mCameraSourcePreview.isSafeToTakePicture()) {
            mCameraSourcePreview.setSafeToTakePicture(false);
            setCameraShutterSound();
            mPhotoBarcodeScannerBuilder.getCameraSource().takePicture(null, data -> {
                mCameraSourcePreview.setSafeToTakePicture(true);
                if (savePictureFuture != null && !savePictureFuture.isCancelled() && !savePictureFuture.isDone()) {
                    savePictureFuture.cancel(true);
                }
                savePictureTask = new SavePictureTask(data, onTakingPictureObserver, onTakingErrorObserver);
                savePictureFuture = executorService.submit(savePictureTask);
            });
        }
    }

    static void handleError(Activity activity, Throwable e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(e.getLocalizedMessage());
        builder.setPositiveButton(activity.getString(android.R.string.ok), (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void handleSilentError(Activity activity, Throwable e){
        if(mPhotoBarcodeScannerBuilder!= null){
            Consumer<Throwable> minorErrorHandler = mPhotoBarcodeScannerBuilder.getMinorErrorHandler();
            if(minorErrorHandler!= null){
                minorErrorHandler.accept(e);
            }
        }
    }

    void setCameraShutterSound() {
        boolean setSound = mPhotoBarcodeScannerBuilder.isSoundEnabled();
        boolean isShutterSet = mPhotoBarcodeScannerBuilder.getCameraSource().setShutterSound(setSound);
        if (!isShutterSet && !setSound) {
            boolean mute = muteAudio(true);
            if (!mute) {
                return;
            }
            final Handler handler = new Handler();
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                public void run() {
                    handler.post(() -> muteAudio(false));
                }
            }, 2000);
        }
    }

    boolean muteAudio(boolean mute) {
        try {
            boolean hasPermissions = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) == PackageManager.PERMISSION_GRANTED;

            AudioManager mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (mgr != null && hasPermissions) {
                mgr.setStreamMute(AudioManager.STREAM_NOTIFICATION, mute);
                mgr.setStreamMute(AudioManager.STREAM_ALARM, mute);
//                mgr.setStreamMute(AudioManager.STREAM_MUSIC, mute);
                mgr.setStreamMute(AudioManager.STREAM_RING, mute);
                mgr.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
                return true;
            }
        } catch (Exception e) {
            handleSilentError(PhotoBarcodeActivity.this, e);
        }
        return false;
    }


    private void savePicture(byte[] bytes, final Consumer<File> onPictureListener, final Consumer<Throwable> onErrorListener) {
        Exception ex = null;
        try {
            if (mCurrentFile != null) {
                ImageHelper.deleteImageFile(PhotoBarcodeActivity.this, mCurrentFile.getAbsolutePath());
            }
            mCurrentFile = ImageHelper.createImageFile(PhotoBarcodeActivity.this);
            ImageHelper.saveBytes(PhotoBarcodeActivity.this, bytes, mCurrentFile);
            File thumbsFile = null;
            if(mPhotoBarcodeScannerBuilder.hasThumbnails()){
                thumbsFile = new File(ImageHelper.getThumbsDir(PhotoBarcodeActivity.this), mCurrentFile.getName());
            }

            boolean fixOrientation = mPhotoBarcodeScannerBuilder.cameraLockRotate && mPhotoBarcodeScannerBuilder.isCameraTryFixOrientation();
            double rotateAngle = orientationHelper != null ? orientationHelper.getSensorAngle() : 0.0;
            int maxImageSize = mPhotoBarcodeScannerBuilder.getImageLargerSide();
            boolean flipHorizontal = mPhotoBarcodeScannerBuilder.mFacing == CameraSource.CAMERA_FACING_FRONT;

            imageExifData = ImageHelper.resizeFileWithThumb(mCurrentFile, mCurrentFile, thumbsFile,
                    rotateAngle,this, fixOrientation,maxImageSize,flipHorizontal);
        } catch (Exception e) {
            ex = e;
        }

        bytes = null;
        final Exception finalEx = ex;

        runOnUiThread(()->{
            if(finalEx == null){
                onPictureListener.accept(mCurrentFile);
            } else {
                onErrorListener.accept(finalEx);
            }
        });
    }

    private void takeTempPicture(View v) {
        if (mCameraSourcePreview.isSafeToTakePicture()) {
            mCameraSourcePreview.setSafeToTakePicture(false);
            setCameraShutterSound();
            mPhotoBarcodeScannerBuilder.getCameraSource().takePicture(null, data -> {
                savePicture(data, file -> tryResumeAction(() -> {
                    onTempPictureSaved(file);
                }), ex -> tryResumeAction(() -> {
                    mCameraSourcePreview.setSafeToTakePicture(true);
                    onTakingErrorObserver.accept(ex);
                }));
                data = null;
            });
        }
    }

    private void onTempPictureSaved(File file) {
        previewImage.setImageResource(R.drawable.bg_transparent);

        int angle = (imageExifData!= null && imageExifData.isFixed()) ? imageExifData.getRotationDifference() : 0;
        previewImage.setVisibility(View.VISIBLE);
        try {
            previewImage.setBackgroundColor(Color.BLACK);

            Bitmap bmImg = BitmapFactory.decodeFile(file.getAbsolutePath());
            bmImg = ImageHelper.rotateBitmap(bmImg, angle);

            previewImage.setImageBitmap(bmImg);
            AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
            fadeImage.setDuration(200);
            fadeImage.setInterpolator(new DecelerateInterpolator());
            fadeImage.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    CameraSource cameraSource = mPhotoBarcodeScannerBuilder.getCameraSource();
                    if(cameraSource!= null){
                        mPhotoBarcodeScannerBuilder.getCameraSource().restartPreview();
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            previewImage.startAnimation(fadeImage);

            checkEnableTorch(false);
        } catch (Exception ex){
            handleSilentError(PhotoBarcodeActivity.this, ex);
        }

        if(mCameraSourcePreview!= null) {
            mCameraSourcePreview.setSafeToTakePicture(true);
        }

        takePictureButton.setSelected(true);
        redoButton.show();

        onVolumeKeysDownListener = null;
        takePictureButton.setOnClickListener(v1 -> onTakingPictureObserver.accept(mCurrentFile));
    }

    private void redoPicture(View v) {
        takePictureButton.setSelected(false);
        redoButton.hide();
        checkEnableTorch(mFlashOn);

        AlphaAnimation fadeImage = new AlphaAnimation(1, 0);
        fadeImage.setDuration(200);
        fadeImage.setInterpolator(new AccelerateInterpolator());
        fadeImage.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                if(previewImage != null){
                    previewImage.setImageResource(R.drawable.bg_transparent);
                    previewImage.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        previewImage.startAnimation(fadeImage);

        takePictureButton.setOnClickListener(this::takeTempPicture);
        onVolumeKeysDownListener = ()-> takeTempPicture(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mCurrentFile!= null){
            ImageHelper.deleteImageFile(PhotoBarcodeActivity.this, mCurrentFile.getAbsolutePath());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();

    }

    public void tryResumeAction(Runnable onResumeHandler){
        if (isActive()) {
            onResumeHandler.run();
        } else {
            this.onResumeHandler = onResumeHandler;
        }
    }

    public void tryResumeActionWithBuilder(Runnable handler){
        if (mPhotoBarcodeScannerBuilder!= null) {
            handler.run();
        } else {
            this.onBuilderHandler = handler;
        }
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void onResume() {
        super.onResume();
        tryResumeActionWithBuilder(()->{
            if(mPhotoBarcodeScannerBuilder.isCameraLockRotate()){
                OrientationHelper.lockOrientation(this);
            }
        });

        active = true;
        if (onResumeHandler != null) {
            onResumeHandler.run();
            onResumeHandler = null;
        }
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        tryResumeActionWithBuilder(()->{
            if(mPhotoBarcodeScannerBuilder.isCameraLockRotate()){
                OrientationHelper.unlockOrientation(this);
            }
        });

        active = false;
        if (mCameraSourcePreview != null) {
            mCameraSourcePreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            clean();
        }
    }

    private void clean() {
        EventBus.getDefault().removeStickyEvent(PhotoBarcodeScanner.class);
        if(orientationHelper!= null) {
            orientationHelper.unregister();
        }
        if (savePictureFuture != null && !savePictureFuture.isCancelled() && !savePictureFuture.isDone()) {
            savePictureFuture.cancel(true);
        }
        if (mCameraSourcePreview != null) {
            mCameraSourcePreview.release();
            mCameraSourcePreview = null;
        }
        if (mSoundPoolPlayer != null) {
            mSoundPoolPlayer.release();
            mSoundPoolPlayer = null;
        }
        executorService.shutdown();
    }
}