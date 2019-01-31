package com.ivan200.photobarcodelib.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.arch.core.util.Function;

public class OrientationProviderAccel extends OrientationProviderBase {
    public static Function<SensorManager, OrientationProviderBase> providerData = OrientationProviderAccel::new;

    private float[] m_lastAccels;
    private Sensor sensor;

    public OrientationProviderAccel(SensorManager sensorManager) {
        super(sensorManager);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        allowed = sensor != null;
    }

    @Override
    int getSensorType() {
        return SensorType.ACC;
    }

    @Override
    void registerListener() {
        if (allowed) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    void onBaseSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            m_lastAccels = lowPass(event.values, m_lastAccels);
            setAngleByAccel(m_lastAccels);
        }
    }
}
