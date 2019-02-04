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

    //Calculation of the rotation angle of the sensor orientation
    private void setAngleByOrient() {
        if (m_lastOrient != null && m_lastOrient.length >= 3) {
            float azimuth = m_lastOrient[0];    //direction to the North pole
            float pitch = m_lastOrient[1];      //0 = screen up, +-180 = screen down, >0 = upside down, <0 = normal orientation
            float roll = m_lastOrient[2];       //0 = vertical (or upside down), 90 = turn left, -90 = turn right
            inclination = (double)(Math.abs(pitch) * -1 + 90);

            //This angle is not considered correct. If you put the phone on the table, when tilted sideways,
            // the angle of rotation will be considered only after 45 degrees)
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
