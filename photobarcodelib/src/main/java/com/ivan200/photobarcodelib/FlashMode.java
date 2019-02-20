package com.ivan200.photobarcodelib;

import android.hardware.Camera;

public enum FlashMode {
    OFF(Camera.Parameters.FLASH_MODE_OFF, R.drawable.ic_camera_flash_off),
    ON(Camera.Parameters.FLASH_MODE_ON, R.drawable.ic_camera_flash_on),
    AUTO(Camera.Parameters.FLASH_MODE_AUTO, R.drawable.ic_camera_flash_auto),
    RED_EYE(Camera.Parameters.FLASH_MODE_RED_EYE, R.drawable.ic_camera_flash_red_eye),
    TORCH(Camera.Parameters.FLASH_MODE_TORCH, R.drawable.ic_camera_flash_torch);

    private int resource;
    private String mode;

    public static FlashMode[] allCameraFlashModes = {FlashMode.OFF, FlashMode.AUTO, FlashMode.ON, FlashMode.RED_EYE, FlashMode.TORCH};
    public static FlashMode[] allBarcodeFlashModes = {FlashMode.OFF, FlashMode.TORCH};

    FlashMode(String mode, int resource) {
        this.mode = mode;
        this.resource = resource;
    }

    public int getResource() {
        return resource;
    }

    public String getMode() {
        return mode;
    }
}