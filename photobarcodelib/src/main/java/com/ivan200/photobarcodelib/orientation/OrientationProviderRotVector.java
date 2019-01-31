package com.ivan200.photobarcodelib.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.arch.core.util.Function;

public class OrientationProviderRotVector extends OrientationProviderBase {
    public static Function<SensorManager, OrientationProviderBase> providerData = OrientationProviderRotVector::new;

    private float[] m_lastRotVec;
    private Sensor sensor;

    public OrientationProviderRotVector(SensorManager sensorManager) {
        super(sensorManager);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        allowed = sensor != null;
    }

    @Override
    int getSensorType() {
        return SensorType.ROTATION_VECTOR;
    }

    @Override
    void registerListener() {
        if (allowed) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    void onBaseSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            m_lastRotVec = copyData(event.values, m_lastRotVec);
            setAngleByRotVec(m_lastRotVec);
        }
    }
}
