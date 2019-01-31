package com.ivan200.photobarcodelib.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.arch.core.util.Function;

public class OrientationProviderGravity extends OrientationProviderBase {
    public static Function<SensorManager, OrientationProviderBase> providerData = OrientationProviderGravity::new;

    private float[] m_lastGrav;
    private Sensor sensor;

    public OrientationProviderGravity(SensorManager sensorManager) {
        super(sensorManager);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        allowed = sensor != null;
    }

    @Override
    int getSensorType() {
        return SensorType.GRAVITY;
    }

    @Override
    void registerListener() {
        if (allowed) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    void onBaseSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
            m_lastGrav = lowPass(event.values, m_lastGrav);
            setAngleByAccel(m_lastGrav);
        }
    }
}
