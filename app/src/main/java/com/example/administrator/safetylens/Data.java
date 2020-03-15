package com.example.administrator.safetylens;

import android.util.Log;

/**
 * Class to handle all saved/passed information and eventually write it out.
 */
public class Data {
    String type; //Taken from main
    float leftAngle,rightAngle,targetAngle; //Taken from camera, center need calculation formula
    String notes;//taken from maps

    /**
     *
     */
    Data(){
        nullify();
    }

    private void nullify() {
        type = null;
        leftAngle = 0f;
        rightAngle = 0f;
        targetAngle = 0f;
        notes = "";
    }

    public void nullNotes(){notes = null;}

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void clear(){
        nullify();
    }

    public String getType() {
        return type;
    }

    public float getLeftAngle() {
        return leftAngle;
    }

    public float getRightAngle() {
        return rightAngle;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLeftAngle(float leftAngle) {
        Log.i("Left",leftAngle+"");
        this.leftAngle = leftAngle;
    }

    public void setRightAngle(float rightAngle) {
        Log.i("Right",rightAngle+"");this.rightAngle = rightAngle;
    }

    public float getTargetAngle() {
        return targetAngle;
    }

    public void setTargetAngle(float targetAngle) {
        this.targetAngle = targetAngle;
    }

}
