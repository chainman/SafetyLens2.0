package com.example.administrator.safetylens;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements SensorEventListener {

    Camera camera; //Camera handler
    FrameLayout frameLayout;
    Button left,right,target;
    int count; //Check how many sides were clicked, after so move to next activity
    static String directory = Environment.getExternalStorageDirectory().getAbsolutePath(),direction=""; //+/temp
    static float degrees;
    SensorManager sensorManager;
    TextView angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        count = 0;
        /*OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener(this)
        {
            @Override
            public void onSwipeLeft() { goToMain(); } //Cycle to Main
                                                     //This cycle is Map-Main-Camera-Map-Main...
            @Override
            public void onSwipeRight() { goToMap(); }//Cycle to Map
        };

        findViewById(R.id.cameraLayout).setOnTouchListener(onSwipeTouchListener);*/
        frameLayout = findViewById(R.id.cameraLayout);
        angle = findViewById(R.id.direction);
        try {
            right = findViewById(R.id.buttonRight);
            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count++;
                    if (count == 1) { //Take picture, if pressed first
                        right.setVisibility(View.INVISIBLE);
                        MainActivity.data.setRightAngle(degrees);
                        direction = "right";
                        takePicture();
                    } else {
                        secondClickOptions();
                        MainActivity.data.setRightAngle(degrees);
                        direction = "right";
                        takePicture();
                    }
                }
            });
            left = findViewById(R.id.buttonLeft);
            left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    count++;
                    if (count == 1) {
                        left.setVisibility(View.INVISIBLE);
                        MainActivity.data.setLeftAngle(degrees);
                        direction = "left";
                        takePicture();
                    } else {
                        secondClickOptions();
                        MainActivity.data.setLeftAngle(degrees);
                        direction = "left";
                        takePicture();
                    }
                }
            });
        }catch (Exception e){Toast.makeText(this,"נסה שנית", Toast.LENGTH_SHORT).show();}

        target = findViewById(R.id.buttonTarget);
        target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.data.setTargetAngle(degrees);
                direction = "target";
                takePicture();
                targetDoubleCheck();
            }
        });

        right.bringToFront();
        left.bringToFront();
        target.bringToFront();

        if(MainActivity.targetOrArea==FireType.AREA)
            target.setVisibility(View.INVISIBLE);
        else{
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.x).bringToFront();
        camera = getCameraInstance();
        frameLayout.addView(new CameraPreview(this,camera));
    }

    //Confirming if angle is ok for target
    private void targetDoubleCheck() {
        target.setVisibility(View.INVISIBLE);
        left.setVisibility(View.VISIBLE);
        right.setVisibility(View.VISIBLE);

        left.setText("למפה");
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMap();
            }
        });

        right.setText("איפוס");
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.data.setTargetAngle(0f);
                target.setVisibility(View.VISIBLE);
                left.setVisibility(View.INVISIBLE);
                right.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void secondClickOptions() { //Return to original state/move on. Give the user the choice if he wants to reset or carry on
        right.setVisibility(View.VISIBLE);
        left.setVisibility(View.VISIBLE);
        try {
        left.setText("למפה");
        right.setText("איפוס");
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMap();
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //Reset camera option when added
                count=0;
                left.setText("שמאל");
                left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        count++;
                        if(count==1) {
                            left.setVisibility(View.INVISIBLE);
                            MainActivity.data.setLeftAngle(degrees);
                            direction="left";
                            takePicture();
                        }else {
                            secondClickOptions();
                            MainActivity.data.setLeftAngle(degrees);
                            direction="left";
                            takePicture();
                        }
                    }
                });
                right.setText("ימין");
                right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        count++;
                        if(count==1) { //Take picture, if pressed first
                            right.setVisibility(View.INVISIBLE);
                            MainActivity.data.setRightAngle(degrees);
                            direction="right";
                            takePicture();
                        } else {
                            secondClickOptions();
                            MainActivity.data.setRightAngle(degrees);
                            direction="right";
                            takePicture();
                        }
                    }
                });
            }
        });
        }catch (Exception e){Toast.makeText(this,"נסה שנית",Toast.LENGTH_SHORT).show();}
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length);
            try {
                FileOutputStream out = new FileOutputStream(directory+File.separator +direction+".jpg");
                picture.compress(Bitmap.CompressFormat.JPEG , 90, out);
                picture.recycle();
                camera.startPreview();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void takePicture() {
        if(Settings.pictures)
            camera.takePicture(null,null,mPicture);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        degrees = event.values[0];
        angle.setText(String.valueOf(degrees));
        //Log.i("Degrees",degrees+"");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void goToMap() { startActivity(new Intent(this,MapsActivity.class)); }

    private void goToMain() { startActivity(new Intent(this,MainActivity.class));}

    // Check if this device has a camera
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();} // attempt to get a Camera instance
        catch (Exception e) {e.getMessage();
        }// Camera is not available (in use or does not exist)
        return c; // returns null if camera is unavailable
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d("","Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here
            mCamera.setDisplayOrientation(90);
            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d("", "Error starting camera preview: " + e.getMessage());
            }
        }
    }

}
