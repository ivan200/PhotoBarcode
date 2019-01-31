package com.ivan200.photobarcodelib.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;

import androidx.arch.core.util.Function;

public class OrientationProviderRotVectorG extends OrientationProviderBase {
    public static Function<SensorManager, OrientationProviderBase> providerData = OrientationProviderRotVectorG::new;

    private float[] m_lastRotVec;
    private Sensor sensor;

    public OrientationProviderRotVectorG(SensorManager sensorManager) {
        super(sensorManager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        }
        allowed = sensor != null;
    }

    @Override
    int getSensorType() {
        return SensorType.GAME_ROT_VECTOR;
    }

    @Override
    void registerListener() {
        if (allowed) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    void onBaseSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            m_lastRotVec = copyData(event.values, m_lastRotVec);
            setAngleByRotVec(m_lastRotVec);
        }
    }
}