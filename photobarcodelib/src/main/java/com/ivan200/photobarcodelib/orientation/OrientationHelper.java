package com.ivan200.photobarcodelib.orientation;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.Arrays;
import java.util.List;

import androidx.arch.core.util.Function;

import static android.content.Context.SENSOR_SERVICE;

public class OrientationHelper {
    public interface OrientationChangedListener {
        void call(int orientation);
    }

    private OrientationChangedListener virtualOrientationChangedListener;

    public double getSensorAngle(){
        if(provider!= null){
            return provider.sensorAngle;
        } else {
            return 0.0;
        }
    }

    private int sensorOrientation = 0;
    private OrientationProviderBase provider;

    public OrientationHelper(Context context, OrientationChangedListener onOrientationChanged) {
        virtualOrientationChangedListener = onOrientationChanged;
        provider = getCurrentProvider(context);
        if (provider != null) {
            provider.setSensorChangedObserver(this::onSensorChanged);
            provider.registerListener();
        }
    }

    private OrientationProviderBase getCurrentProvider(Context context){
        List<Function<SensorManager, OrientationProviderBase>> providers = Arrays.asList(
                OrientationProviderRotVectorG.providerData,
                OrientationProviderRotVector.providerData,
                OrientationProviderAccMag.providerData,
                OrientationProviderAccel.providerData,
                OrientationProviderGravity.providerData,
                OrientationProviderOrient.providerData
        );

        SensorManager sManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if (sManager != null) {
            for (Function<SensorManager, OrientationProviderBase> providerData : providers) {
                OrientationProviderBase providerInstance = providerData.apply(sManager);
                if (providerInstance.isAllowed()) {
                    return providerInstance;
                }
            }
        }
        return null;
    }


    private void onSensorChanged(double sensorAngle, double inclination) {
        int surfaceRotationByDegrees = getSurfaceRotationByDegrees(sensorAngle, inclination);
        if (sensorOrientation != surfaceRotationByDegrees) {
            sensorOrientation = surfaceRotationByDegrees;
            if (virtualOrientationChangedListener != null) {
                virtualOrientationChangedListener.call(sensorOrientation);
            }
        }
    }

    public void unregister() {
        if(provider!= null){
            provider.unregisterListener();
        }
    }

    private static int getSurfaceRotationByDegrees(double deg, double inclination){
        if(inclination > 85){
            return Surface.ROTATION_0;
        }

        deg = deg%360;
        if (deg >= -45 && deg <= 45) {
            return Surface.ROTATION_0;
        } else if (deg > 45 && deg <= 135) {
            return Surface.ROTATION_90;
        } else if (deg >= -135 && deg < -45) {
            return Surface.ROTATION_270;
        } else {
            return Surface.ROTATION_180;
        }
    }

    public static int getDegreesBySurfaceRotation(int virtualSurfaceRotation, Context context){
        int rotation = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if(windowManager!= null) {
            rotation = windowManager.getDefaultDisplay().getRotation();
        }

        rotation = (virtualSurfaceRotation - rotation + 4) % 4;

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    public static boolean isDeviceRotationLocked(Activity activity){
        boolean rotationIsBlockedBySettings = Settings.System.getInt(activity.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 0;

        int o = activity.getRequestedOrientation();
        boolean rotationIsBlockedByActivity = (o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||  o == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE ||
                o == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || o == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

        return rotationIsBlockedBySettings || rotationIsBlockedByActivity;
    }

    public static boolean isDeviceDefaultOrientationLandscape(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Configuration config = context.getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();
        boolean defaultLandsacpeAndIsInLandscape = (rotation == Surface.ROTATION_0 ||
                rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE;

        boolean defaultLandscapeAndIsInPortrait = (rotation == Surface.ROTATION_90 ||
                rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT;

        return defaultLandsacpeAndIsInLandscape || defaultLandscapeAndIsInPortrait;
    }


    public static void lockOrientation(Activity activity) {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if(windowManager!= null) {
            Display display = windowManager.getDefaultDisplay();
            int rotation = display.getRotation();
            int tempOrientation = activity.getResources().getConfiguration().orientation;

            int orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            switch (tempOrientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Configuration.ORIENTATION_PORTRAIT:
                    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270)
                        orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    else
                        orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
            activity.setRequestedOrientation(orientation);
        }
    }

    public static void unlockOrientation(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
