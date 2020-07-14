package com.example.administrator.safetylens;

import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements SensorEventListener {

    Camera camera; //Camera handler
    FrameLayout frameLayout;
    ImageButton left,right,target;
    static String directory = Environment.getExternalStorageDirectory().getAbsolutePath(),direction="";
    static float degrees, motionAngle;
    static double motionLength;
    static int motionCount;
    SensorManager sensorManager;
    TextView angle;
    AlertDialog motionDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.camera_page);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        frameLayout = findViewById(R.id.cameraLayout1);
        angle = findViewById(R.id.direction1);
        try {
            right = findViewById(R.id.buttonRight1);
            right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.data.setRightAngle(degrees);
                    direction = "right";
                    left.setVisibility(View.VISIBLE);
                    takePicture();
                    rightToReset();
                }
            });
            left = findViewById(R.id.buttonLeft1);
            left.setImageResource(R.drawable.left);
            left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.data.setLeftAngle(degrees);
                    direction="left";
                    takePicture();
                    leftToMap();
                }
            });
        }catch (Exception e){Toast.makeText(this,"נסה שנית", Toast.LENGTH_SHORT).show();}

        target = findViewById(R.id.buttonTarget1);
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

        if(MainActivity.fireType == FireType.TARGET || MainActivity.fireType == FireType.MOTION) {
            target.setVisibility(View.VISIBLE);
            right.setVisibility(View.INVISIBLE);
            left.setVisibility(View.INVISIBLE);
        } else{
            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.VISIBLE);
            target.setVisibility(View.INVISIBLE);
        }

        findViewById(R.id.x1).bringToFront();
        camera = getCameraInstance();
        frameLayout.addView(new CameraPreview(this,camera));
    }

    public void rightToReset(){
        right.setImageResource(R.drawable.return_button);
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.data.setRightAngle(0f);
                MainActivity.data.setLeftAngle(0f);
                left.setVisibility(View.INVISIBLE);
                right.setImageResource(R.drawable.right);
                right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.data.setRightAngle(degrees);
                        direction="right";
                        left.setVisibility(View.VISIBLE);
                        takePicture();
                        rightToReset();
                    }
                });
                left.setImageResource(R.drawable.left);
                left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.data.setLeftAngle(degrees);
                        direction="left";
                        takePicture();
                        leftToMap();
                    }
                });
            }
        });
    }

    public void leftToMap(){
        left.setImageResource(R.drawable.ic_tomap);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMap();
            }
        });
    }

    //Confirming if angle is ok for target
    private void targetDoubleCheck() {
        target.setVisibility(View.INVISIBLE);
        left.setVisibility(View.VISIBLE);
        right.setVisibility(View.VISIBLE);

        leftToMap();

        right.setImageResource(R.drawable.return_button);
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

        if(MainActivity.fireType.equals(FireType.MOTION_RESUME)){

            motionAngle = 0f;

            left.setVisibility(View.INVISIBLE);
            right.setVisibility(View.INVISIBLE);
            target.setVisibility(View.VISIBLE);

            //target.setText("תמרון");
            target.setOnClickListener(motionAction());
        }
    }

    public View.OnClickListener motionAction() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motionAngle = degrees;

                left.setVisibility(View.VISIBLE);
                right.setVisibility(View.VISIBLE);
                target.setVisibility(View.INVISIBLE);
                left.setImageResource(R.drawable.ic_tomap);
                right.setImageResource(R.drawable.return_button);

                left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        motionDialog = motionDialog();
                        motionDialog.show();
                    }
                });
                right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        motionReset();
                    }
                });
            }
        };
    }

    private void motionReset() {
        left.setVisibility(View.INVISIBLE);
        right.setVisibility(View.INVISIBLE);
        target.setVisibility(View.VISIBLE);

        //target.setText("תמרון");
        target.setOnClickListener(motionAction());
    }

    EditText countText, countLength;
    private AlertDialog motionDialog(){
        LinearLayout layout = motionDialogLayout();
        return new AlertDialog.Builder(this).setTitle("הוסף הערות")
                .setView(layout)
                .setPositiveButton("אישור ולמפה", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!countLength.getText().toString().equals("") && !countLength.getText().toString().equals("")){
                            try {
                                motionCount = Integer.parseInt(countText.getText().toString());
                                motionLength = Double.parseDouble(countLength.getText().toString()) / 6371.0 * (180.0 / Math.PI);
                                goToMap();
                            }catch (Exception e){Toast.makeText(CameraActivity.this,"הזן כמות במספר שלם ומרחק במספר",Toast.LENGTH_SHORT).show();}
                        }else
                            Toast.makeText(CameraActivity.this,"הזן נתונים",Toast.LENGTH_SHORT).show();
                    }
                }).create();
    }

    /**
     * Fills a LinearLayout with information for motionDialog
     * @return linearLayout filled with views
     */
    private LinearLayout motionDialogLayout() {
        LinearLayout layout = new LinearLayout(this); //Set up layout
        layout.setOrientation(LinearLayout.VERTICAL);//In descending order
        layout.setBackgroundColor(333333);
        countText = new EditText(this);
        countLength = new EditText(this);
        TextView askLength = new TextView(this),askCount = new TextView(this);
        askCount.setText("כמה פוליגונים"); //Text here with questions
        askLength.setText("הגדר מסילת תנועה בקמ");
        layout.addView(askLength);//Fill layout with four views
        layout.addView(countLength);
        layout.addView(askCount);
        layout.addView(countText);
        return layout; //Return filled container
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
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
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
