package com.ivan200.photobarcodelib.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

abstract public class OrientationProviderBase implements SensorEventListener {
    interface SensorChangedObserver {
        void onSensorChanged(Double sensorAngle, Double inclination);
    }


    protected SensorManager mSensorManager;
    public OrientationProviderBase(SensorManager sensorManager) {
        this.mSensorManager = sensorManager;
    }

    SensorChangedObserver sensorChangedObserver;
    public void setSensorChangedObserver(SensorChangedObserver sensorChangedObserver) {
        this.sensorChangedObserver = sensorChangedObserver;
    }

    abstract int getSensorType();
    abstract void registerListener();
    abstract void onBaseSensorChanged(SensorEvent event);

    volatile double sensorAngle = 0.0;
    volatile double inclination = 0.0;

    protected boolean allowed = false;

    public boolean isAllowed(){
        return allowed;
    }

    interface SensorType {
        int ROTATION_VECTOR = 1;
        int GAME_ROT_VECTOR = 2;
        int ACC_MAG = 3;
        int ACC = 4;
        int GRAVITY = 5;
        int ORIENTATION = 6;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            return;
        }

        onBaseSensorChanged(event);
        if(sensorChangedObserver!= null) {
            sensorChangedObserver.onSensorChanged(sensorAngle, inclination);
        }
    }
    void unregisterListener(){
        mSensorManager.unregisterListener(this);
    }

    //The calculation of the rotation angle of the rotation vector
    protected void setAngleByRotVec(float[] m_lastRotVec) {
        if (m_lastRotVec!= null && m_lastRotVec.length >= 3) {
            float[] rotationMatrix = new float[9];
            float[] adjustedRotationMatrix = new float[9];
            float[] orientation = new float[3];

            SensorManager.getRotationMatrixFromVector(rotationMatrix, m_lastRotVec);
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, adjustedRotationMatrix);
            SensorManager.getOrientation(adjustedRotationMatrix, orientation);

            inclination = Math.toDegrees((double) orientation[1]);
            sensorAngle = Math.toDegrees((double) orientation[2]) * -1;
        } else {
            sensorAngle = 0.0;
            inclination = 0.0;
        }
    }

    //Calculation of the rotation angle of the acceleration sensor
    protected void setAngleByAccel(float[] m_lastAccels) {
        if (m_lastAccels != null && m_lastAccels.length >= 3) {
            float aX = m_lastAccels[0];
            float aY = m_lastAccels[1];
            float aZ = m_lastAccels[2];

            double xAngle = Math.atan2((double)aX, (double)aZ);
            double yAngle = Math.atan2((double)aY, (double)aZ);
            double zAngle = Math.atan2((double)aX, (double)aY);

            inclination = Math.toDegrees(Math.atan(1 / Math.sqrt(Math.pow(Math.tan(xAngle), 2.0) + Math.pow(Math.tan(yAngle), 2.0)))) * Math.signum(aZ);

            sensorAngle = Math.toDegrees(zAngle);
        } else {
            sensorAngle = 0.0;
            inclination = 0.0;
        }
    }

    private float ALPHA = 0.50f;

    protected float[] lowPass(float[] input, float[] output) {
        if (input == null) {
            return null;
        }
        float[] fields = new float[input.length];
        System.arraycopy(input, 0, fields, 0, input.length);

        if (output == null) return fields;

        for (int i = 0; i < output.length; i++) {
            output[i] = output[i] + ALPHA * (fields[i] - output[i]);
        }
        return output;
    }

    protected float[] copyData(float[] input, float[] output) {
        if (input == null) {
            return output;
        }
        if (output == null) {
            float[] fields = new float[input.length];
            System.arraycopy(input, 0, fields, 0, input.length);
            return fields;
        } else {
            System.arraycopy(input, 0, output, 0, input.length);
            return output;
        }
    }
}
