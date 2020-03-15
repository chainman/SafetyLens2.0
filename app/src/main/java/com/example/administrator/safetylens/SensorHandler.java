package com.example.administrator.safetylens;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorHandler implements SensorEventListener{

    private float degrees;
    private SensorManager mSensorManager;
    private Context context;

    public SensorHandler(SensorManager sensorManager,Context context){
        this.context = context;
        mSensorManager = sensorManager;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        degrees = event.values[0];
        Log.i("Degrees",degrees+"");

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*public void onResume() {
        sensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
    }*/

    public float getDegrees() {
        return degrees;
    }
}
