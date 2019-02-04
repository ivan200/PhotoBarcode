package com.ivan200.photobarcodelib.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.arch.core.util.Function;

public class OrientationProviderAccMag extends OrientationProviderBase {
    public static Function<SensorManager, OrientationProviderBase> providerData = OrientationProviderAccMag::new;

    private float[] mLastAccels;
    private float[] mLastMagFields;

    private Sensor sensorAcc;
    private Sensor sensorMag;

    public OrientationProviderAccMag(SensorManager sensorManager) {
        super(sensorManager);
        sensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        allowed = (sensorAcc != null && sensorMag!= null);
    }

    @Override
    int getSensorType() {
        return SensorType.ACC_MAG;
    }

    @Override
    void registerListener() {
        if (allowed) {
            mSensorManager.registerListener(this, sensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    void onBaseSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                mLastAccels = lowPass(event.values, mLastAccels);
                setAngleByAccMag();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mLastMagFields = lowPass(event.values, mLastAccels);
                setAngleByAccMag();
                break;
        }
    }

    //Calculation of the angle of rotation of the acceleration sensor and magnetometer.
    private void setAngleByAccMag() {
        if (mLastAccels != null && mLastMagFields!= null) {
            float[] accMagMatrix = new float[9];
            float[] adjustedRotationMatrix = new float[9];
            float[] orientation = new float[3];

            SensorManager.getRotationMatrix(accMagMatrix, null, mLastAccels, mLastMagFields);
            SensorManager.remapCoordinateSystem(accMagMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);

            inclination = Math.toDegrees((double)orientation[1]);
            sensorAngle = Math.toDegrees((double)orientation[2]) * -1;
        } else {
            sensorAngle = 0.0;
            inclination = 0.0;
        }
    }
}
