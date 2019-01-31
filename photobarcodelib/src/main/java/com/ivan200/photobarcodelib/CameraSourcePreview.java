/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ivan200.photobarcodelib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;

import java.io.IOException;

import androidx.core.app.ActivityCompat;

public class CameraSourcePreview extends ViewGroup {
    private static final String TAG = "CameraSourcePreview";

    private Context mContext;
    private SurfaceView mSurfaceView;
    private boolean mStartRequested;

    private boolean mSurfaceAvailable;
    public boolean isSurfaceAvailable() {
        return mSurfaceAvailable;
    }

    private CameraSource mCameraSource;
    private GraphicOverlay mOverlay;

    private boolean safeToTakePicture = false;

    public boolean isSafeToTakePicture() {
        return safeToTakePicture;
    }

    public void setSafeToTakePicture(boolean safeToTakePicture) {
        this.safeToTakePicture = safeToTakePicture;
    }

    protected boolean cameraFullScreenMode = false;
    public boolean isCameraFullScreenMode() {
        return cameraFullScreenMode;
    }
    public void setCameraFullScreenMode(boolean cameraFullScreenMode) {
        this.cameraFullScreenMode = cameraFullScreenMode;
    }

    OrientationEventListener orientationListener;
    int actualDeviceOrientation;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        addView(mSurfaceView);

        orientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                setActualDeviceOrientation();
            }
        };

        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable();
        } else {
            orientationListener.disable();
        }
    }

    private boolean setActualDeviceOrientation() {
        WindowManager windowService = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if(windowService!= null){
            int deviceOrientation = windowService.getDefaultDisplay().getOrientation();
            if (actualDeviceOrientation != deviceOrientation) {
                actualDeviceOrientation = deviceOrientation;
                if (mCameraSource!= null) {
                    mCameraSource.resetCameraRotation();
                }
                return true;
            }
        }
        return false;
    }

    public void start(CameraSource cameraSource) throws IOException, RuntimeException {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            startIfReady();
        }
    }

    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws IOException, RuntimeException {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    private void startIfReady() throws RuntimeException, IOException {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            if (mOverlay != null) {
                Size size = mCameraSource.getPreviewSize();
                int min = Math.min(size.getWidth(), size.getHeight());
                int max = Math.max(size.getWidth(), size.getHeight());
                if (isPortraitMode()) {
                    // Swap width and height sizes when in portrait, since it will be rotated by
                    // 90 degrees
                    mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
                } else {
                    mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
                }
                mOverlay.clear();
            }
            mStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            try {
                startIfReady();
                fillLayoutPreview(CameraSourcePreview.this.getLeft(), CameraSourcePreview.this.getTop(), CameraSourcePreview.this.getRight(), CameraSourcePreview.this.getBottom());
            } catch (SecurityException se) {
                notifyError(new RuntimeException("Do not have permission to start the camera", se));
            } catch (IOException e) {
                notifyError(e);
            } catch (RuntimeException e) {
                notifyError(e);
            }
        }

        void notifyError(Exception e) {
            Context context = getContext();
            if (context instanceof Activity) {
                PhotoBarcodeActivity.handleError((Activity) context,e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
            safeToTakePicture = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            safeToTakePicture = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        fillLayoutPreview(left, top, right, bottom);
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startIfReady();
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    void fillLayoutPreview(int left, int top, int right, int bottom){
        int previewWidth = CameraSource.DEFAULT_PREVIEW_WIDTH;
        int previewHeight = CameraSource.DEFAULT_PREVIEW_HEIGHT;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = previewWidth;
            previewWidth = previewHeight;
            previewHeight = tmp;
        }

        final int viewWidth = right - left;
        final int viewHeight = bottom - top;

        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;

        if (isCameraFullScreenMode()) {
            float widthRatio = (float) viewWidth / (float) previewWidth;
            float heightRatio = (float) viewHeight / (float) previewHeight;

            // To fill the view with the camera preview, while also preserving the correct aspect ratio,
            // it is usually necessary to slightly oversize the child and to crop off portions along one
            // of the dimensions.  We scale up based on the dimension requiring the most correction, and
            // compute a crop offset for the other dimension.
            if (widthRatio > heightRatio) {
                childWidth = viewWidth;
                childHeight = (int) ((float) previewHeight * widthRatio);
                childYOffset = (childHeight - viewHeight) / 2;
            } else {
                childWidth = (int) ((float) previewWidth * heightRatio);
                childHeight = viewHeight;
                childXOffset = (childWidth - viewWidth) / 2;
            }

            for (int i = 0; i < getChildCount(); ++i) {
                // One dimension will be cropped.  We shift child over or up by this offset and adjust
                // the size to maintain the proper aspect ratio.
                getChildAt(i).layout(
                        -1 * childXOffset, -1 * childYOffset,
                        childWidth - childXOffset, childHeight - childYOffset);
            }
        } else {
            // Computes height and width for potentially doing fit width.
            if (isPortraitMode()) {
                childWidth = viewWidth;
                childHeight = (int) (((float) viewWidth / (float) previewWidth) * previewHeight);

                // If height is too tall using fit width, does fit height instead.
                if (childHeight > viewHeight) {
                    childHeight = viewHeight;
                    childWidth = (int) (((float) viewHeight / (float) previewHeight) * previewWidth);
//                    childXOffset = (viewWidth - childWidth)/2;
                }
            } else {
                childHeight = viewHeight;
                childWidth = (int) (((float) viewHeight / (float) previewHeight) * previewWidth);

                // If height is too width using fit height, does fit width instead.
                if (childWidth > viewWidth) {
                    childWidth = viewWidth;
                    childHeight = (int) (((float) viewWidth / (float) previewWidth) * previewHeight);
//                    childYOffset = (viewHeight - childHeight) / 2;
                }
            }

            for (int i = 0; i < getChildCount(); ++i) {
                getChildAt(i).layout(childXOffset, childYOffset, childWidth + childXOffset, childHeight + childYOffset);
            }
        }
    }

    public boolean isPortraitMode() {
        int orientation = mContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Log.d(TAG, "isPortraitMode returning false by default");
        return false;
    }
}
