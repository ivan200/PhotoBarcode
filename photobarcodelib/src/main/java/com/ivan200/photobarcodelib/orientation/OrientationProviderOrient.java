package com.ivan200.photobarcodelib.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import androidx.arch.core.util.Function;

public class OrientationProviderOrient extends OrientationProviderBase {
    public static Function<SensorManager, OrientationProviderBase> providerData = OrientationProviderOrient::new;

    private float[] m_lastOrient;
    private Sensor sensor;

    public OrientationProviderOrient(SensorManager sensorManager) {
        super(sensorManager);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        allowed = sensor != null;
    }

    @Override
    int getSensorType() {
        return SensorType.ORIENTATION;
    }

    @Override
    void registerListener() {
        if (allowed) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    void onBaseSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            m_lastOrient = copyData(event.values, m_lastOrient);
            setAngleByOrient();
        }
    }

    //Расчёт угла поворота по сенсору ориентации
    private void setAngleByOrient() {
        if (m_lastOrient != null && m_lastOrient.length >= 3) {
            float azimuth = m_lastOrient[0];    //направление на северный полюс
            float pitch = m_lastOrient[1];      //0 = экраном вверх, +-180 = экраном вниз, >0 = кверх ногами, <0 = нормальная ориентация
            float roll = m_lastOrient[2];       //0 = вертикально (или вверх ногами), 90 = поворот налево, -90 = поворот направо
            inclination = (double)(Math.abs(pitch) * -1 + 90);

            //Данный угол считается не очень корректно. Eесли положить телефон на стол, то при наклоне вбок, угол поворота будет считаться только после 45 градусов)
            if (pitch > 0){
                sensorAngle = (double) (180 - Math.abs(roll)) * Math.signum(roll);
            } else {
                sensorAngle = (double) roll;
            }
        } else {
            sensorAngle = 0.0;
            inclination = 0.0;
        }
    }
}
