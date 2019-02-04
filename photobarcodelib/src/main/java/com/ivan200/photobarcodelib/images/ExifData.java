package com.ivan200.photobarcodelib.images;

import android.app.Activity;
import android.text.TextUtils;

import com.ivan200.photobarcodelib.orientation.OrientationHelper;

import java.util.HashMap;

import androidx.exifinterface.media.ExifInterface;

/*
 * Created by Zakharovi on 10.08.2017.
 */

public class ExifData {
    public String fileFromPath;
    private boolean ex = false;
    private String[] attributes = new String[]{
            /*
              ExifInterface.IFD_TIFF_TAGS
            */
            ExifInterface.TAG_NEW_SUBFILE_TYPE,
            ExifInterface.TAG_SUBFILE_TYPE,
//            ExifInterface.TAG_IMAGE_WIDTH,
//            ExifInterface.TAG_IMAGE_LENGTH,
//            ExifInterface.TAG_BITS_PER_SAMPLE,
//            ExifInterface.TAG_COMPRESSION,
//            ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION,
            ExifInterface.TAG_IMAGE_DESCRIPTION,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_STRIP_OFFSETS,
            ExifInterface.TAG_ORIENTATION,
//            ExifInterface.TAG_SAMPLES_PER_PIXEL,
//            ExifInterface.TAG_ROWS_PER_STRIP,
//            ExifInterface.TAG_STRIP_BYTE_COUNTS,
            ExifInterface.TAG_X_RESOLUTION,
            ExifInterface.TAG_Y_RESOLUTION,
//            ExifInterface.TAG_PLANAR_CONFIGURATION,
            ExifInterface.TAG_RESOLUTION_UNIT,
            ExifInterface.TAG_TRANSFER_FUNCTION,
            ExifInterface.TAG_SOFTWARE,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_ARTIST,
            ExifInterface.TAG_WHITE_POINT,
            ExifInterface.TAG_PRIMARY_CHROMATICITIES,
            ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT,
            ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
            ExifInterface.TAG_Y_CB_CR_COEFFICIENTS,
//            ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING,
            ExifInterface.TAG_Y_CB_CR_POSITIONING,
            ExifInterface.TAG_REFERENCE_BLACK_WHITE,
            ExifInterface.TAG_COPYRIGHT,
            ExifInterface.TAG_RW2_SENSOR_TOP_BORDER,
            ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER,
            ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER,
            ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER,
            ExifInterface.TAG_RW2_ISO,
            ExifInterface.TAG_RW2_JPG_FROM_RAW,

            /*
            ExifInterface.IFD_EXIF_TAGS
            */
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_EXPOSURE_PROGRAM,
            ExifInterface.TAG_SPECTRAL_SENSITIVITY,
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterface.TAG_OECF,
            ExifInterface.TAG_EXIF_VERSION,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_COMPONENTS_CONFIGURATION,
            ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL,
            ExifInterface.TAG_SHUTTER_SPEED_VALUE,
            ExifInterface.TAG_APERTURE_VALUE,
            ExifInterface.TAG_BRIGHTNESS_VALUE,
            ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
            ExifInterface.TAG_MAX_APERTURE_VALUE,
            ExifInterface.TAG_SUBJECT_DISTANCE,
            ExifInterface.TAG_METERING_MODE,
            ExifInterface.TAG_LIGHT_SOURCE,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_SUBJECT_AREA,
            ExifInterface.TAG_MAKER_NOTE,
            ExifInterface.TAG_USER_COMMENT,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
            ExifInterface.TAG_FLASHPIX_VERSION,
            ExifInterface.TAG_COLOR_SPACE,
            ExifInterface.TAG_PIXEL_X_DIMENSION,
            ExifInterface.TAG_PIXEL_Y_DIMENSION,
            ExifInterface.TAG_RELATED_SOUND_FILE,
            ExifInterface.TAG_FLASH_ENERGY,
            ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE,
            ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION,
            ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION,
            ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT,
            ExifInterface.TAG_SUBJECT_LOCATION,
            ExifInterface.TAG_EXPOSURE_INDEX,
            ExifInterface.TAG_SENSING_METHOD,
            ExifInterface.TAG_FILE_SOURCE,
            ExifInterface.TAG_SCENE_TYPE,
            ExifInterface.TAG_CFA_PATTERN,
            ExifInterface.TAG_CUSTOM_RENDERED,
            ExifInterface.TAG_EXPOSURE_MODE,
            ExifInterface.TAG_WHITE_BALANCE,
            ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
            ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
            ExifInterface.TAG_SCENE_CAPTURE_TYPE,
            ExifInterface.TAG_GAIN_CONTROL,
            ExifInterface.TAG_CONTRAST,
            ExifInterface.TAG_SATURATION,
            ExifInterface.TAG_SHARPNESS,
            ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
            ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
            ExifInterface.TAG_IMAGE_UNIQUE_ID,
            ExifInterface.TAG_DNG_VERSION,
            ExifInterface.TAG_DEFAULT_CROP_SIZE,

            /*
              ExifInterface.IFD_GPS_TAGS
            */
            ExifInterface.TAG_GPS_VERSION_ID,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_GPS_SATELLITES,
            ExifInterface.TAG_GPS_STATUS,
            ExifInterface.TAG_GPS_MEASURE_MODE,
            ExifInterface.TAG_GPS_DOP,
            ExifInterface.TAG_GPS_SPEED_REF,
            ExifInterface.TAG_GPS_SPEED,
            ExifInterface.TAG_GPS_TRACK_REF,
            ExifInterface.TAG_GPS_TRACK,
            ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
            ExifInterface.TAG_GPS_IMG_DIRECTION,
            ExifInterface.TAG_GPS_MAP_DATUM,
            ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
            ExifInterface.TAG_GPS_DEST_LATITUDE,
            ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
            ExifInterface.TAG_GPS_DEST_LONGITUDE,
            ExifInterface.TAG_GPS_DEST_BEARING_REF,
            ExifInterface.TAG_GPS_DEST_BEARING,
            ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
            ExifInterface.TAG_GPS_DEST_DISTANCE,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_AREA_INFORMATION,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_DIFFERENTIAL
    };

    private HashMap<String,String> tagMap;



    public ExifData(String fileFrom) {
        this.fileFromPath = fileFrom;
        try {
            ExifInterface fileExif = new ExifInterface(fileFrom);
            tagMap = new HashMap<>();
            for (String attribute : attributes) {
                String value = fileExif.getAttribute(attribute);
                if(!TextUtils.isEmpty(value)){
                    tagMap.put(attribute, value);
                }
            }
        } catch (Exception e) {
            ex = true;
        }
    }

    public void setExif(String fileTo){
        if(!ex && tagMap.size()>0){
            try {
                ExifInterface file2Exif = new ExifInterface(fileTo);
                for (String key : tagMap.keySet()) {
                    file2Exif.setAttribute(key, tagMap.get(key));
                }
                file2Exif.saveAttributes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getOrientation(){
        String orientValue = tagMap.get(ExifInterface.TAG_ORIENTATION);
        int orientation = ExifInterface.ORIENTATION_UNDEFINED;
        if (orientValue != null) {
            try {
                orientation = Integer.parseInt(orientValue);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return orientation;
    }


    public FixOrientationMode fixOrientationMode = FixOrientationMode.NONE;
    int imgWidth;
    int imgHeight;
    Double rotation;
    Activity activity;
    int oldOrientation = ExifInterface.ORIENTATION_UNDEFINED;
    int newOrientation = ExifInterface.ORIENTATION_UNDEFINED;

    public boolean isFixed() {
        return isFixed;
    }

    boolean isFixed = false;

    public enum FixOrientationMode {
        ALL,        //Fix orientation in any case.
        UNDEFINED,  //Fix orientation only if not defined.
        NONE        //Don't fix the orientation.
    }

    //If the orientation of the image itself is not defined, then set the orientation by the phone angle
    public void fixExifOrientation(FixOrientationMode mode, int imgWidth, int imgHeight, Double rotation, Activity activity) {
        this.fixOrientationMode = mode;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.rotation = rotation;
        this.activity = activity;

        if(fixOrientationMode == FixOrientationMode.NONE || isFixed) {
            return;
        }
        oldOrientation = getOrientation();
        boolean rotationIsBlocked = OrientationHelper.isDeviceRotationLocked(activity);
        if(fixOrientationMode == FixOrientationMode.ALL || rotationIsBlocked || oldOrientation == ExifInterface.ORIENTATION_UNDEFINED) {
            newOrientation = getFixedOrientation(oldOrientation, rotationIsBlocked);
        }
        if (newOrientation != ExifInterface.ORIENTATION_UNDEFINED) {
            tagMap.put(ExifInterface.TAG_ORIENTATION, String.valueOf(newOrientation));
            isFixed = true;
        }
    }


    private int getFixedOrientation(int currentOrientation, boolean rotationIsBlocked) {
        if (rotation != null && imgHeight > 0 && imgWidth > 0) {
            boolean defaultLandscape = OrientationHelper.isDeviceDefaultOrientationLandscape(activity);

            if ((imgWidth <= imgHeight && !defaultLandscape) || (imgWidth >= imgHeight && defaultLandscape)) {
                if (rotation >= -45 && rotation <= 45) {
                    return ExifInterface.ORIENTATION_NORMAL;
                } else if (rotation > 45 && rotation <= 135) {
                    return ExifInterface.ORIENTATION_ROTATE_270;
                } else if (rotation >= -135 && rotation < -45) {
                    return ExifInterface.ORIENTATION_ROTATE_90;
                } else if (rotation > 135 || rotation < -135) {
                    return rotationIsBlocked ? ExifInterface.ORIENTATION_ROTATE_180 : currentOrientation;
                }
            } else {
                if (rotation >= -45 && rotation <= 45) {
                    return ExifInterface.ORIENTATION_ROTATE_90;
                } else if (rotation > 45 && rotation <= 135) {
                    return ExifInterface.ORIENTATION_NORMAL;
                } else if (rotation >= -135 && rotation < -45) {
                    return rotationIsBlocked ? ExifInterface.ORIENTATION_ROTATE_180 : currentOrientation;
                } else if (rotation > 135 || rotation < -135) {
                    return ExifInterface.ORIENTATION_ROTATE_270;
                }
            }
        }
        return currentOrientation;
    }

    public int getRotationDifference(){
        if(isFixed){
            int oldRotateAngle = getRotateAngleByExif(oldOrientation);
            int newRotateAngle = getRotateAngleByExif(newOrientation);
            return ((oldRotateAngle - newRotateAngle) + 360)%360;
        }
        return 0;
    }


    public static int getRotateAngleByExif(int exifOrientation){
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
        }
        return 0;
    }

}
