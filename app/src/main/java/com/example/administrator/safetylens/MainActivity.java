package com.example.administrator.safetylens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{

    static boolean dangerCircleNeeded;
    static Gps gps;
    static Data data;
    Spinner spinnerArea,spinnerAmmo;
    String[] keys;
    private String[] ammos; //List of ammo names,to be filled in manually here
    static String key;
    static CustomPolygonList customPolygonList;
    AmmoList ammoList;
    static double height;
    Button start,target;
    static String timestamp,folderDirectory,name = "Safety";
    static FireType targetOrArea;
    static double targetDistance;
    EditText targetText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask for permission to access files, gps and camera
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.CAMERA);

        /**OnSwipeTouchListener onSwipeTouchListener = new OnSwipeTouchListener(this) //Adds option to swipe between activities
        *{
         * @Override
         * public void onSwipeLeft() { goToMap(); } //Cycles left for map
          *                                         //This cycle is Map-Main-Camera-Map-Main...
          *@Override
          *public void onSwipeRight() { goToCamera(); } //Cycles right for camera
        *};
         *
        *findViewById(R.id.mainLayout).setOnTouchListener(onSwipeTouchListener); //Create swiper for dynamic usability */
        //Set up data to store all calculation and output data
        data = new Data();

        //Button for next activity, will notify user if ammo type is not selected
        start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(data.type != null){
                    targetOrArea = FireType.AREA;
                    goToCamera();
                }
                else
                    Toast.makeText(MainActivity.this,"בחר סוג תחמושת",Toast.LENGTH_SHORT).show();
            }
        });
        targetText = findViewById(R.id.editText2);
        target = findViewById(R.id.target);
        target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    targetDistance = (Double.parseDouble(targetText.getText().toString()))/6371.0*(180.0/Math.PI);
                    if(targetDistance>height)
                        Toast.makeText(MainActivity.this, "הזן מרחק קטן יותר", Toast.LENGTH_SHORT).show();
                    else{
                        targetOrArea = FireType.TARGET;
                        goToCamera();
                    }
                }catch (Exception e){ Toast.makeText(MainActivity.this,"הזן מטרה",Toast.LENGTH_SHORT).show();}
            }
        });

        //Create list of custom polygons, and store keys
        customPolygonList = new CustomPolygonList();
        keys = customPolygonList.getMap().keySet().toArray(new String[customPolygonList.getMap().keySet().size()]);

        //Set the spinners and adapters. These display the area or ammo and updates for when moving between activities.
        spinnerArea = findViewById(R.id.spinnerArea);
        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                key = keys[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        gps = new Gps(this,customPolygonList); //GPS for usage

        ArrayAdapter<String> adapterArea =  new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,keys);
        adapterArea.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(adapterArea);
        //Insert here the names of the ammo types:
        ammos = new String[]{"לאו","9 ממ","מפצח אגוזים - 1.5","לאו A4","M-855","M-193","M-24","מפצח אגוזים - 2.5","M-193 3.0","40ממ","ברק 3.4","בארט","7.62 M-24","6.72 מאג","ברק - 4.4","0.5 אינצ"};
        ammoList = new AmmoList(this,android.R.layout.simple_spinner_item,ammos);
        spinnerAmmo = findViewById(R.id.spinnerAmmo);
        spinnerAmmo.setAdapter(ammoList);
        spinnerAmmo.setOnItemSelectedListener(ammoList.onItemClicker);

        //goToMap();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),name);
        if(!file.exists())
            file.mkdir();
    }

    @Override
    protected void onResume() {
        super.onResume();

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),name);
        if(!file.exists())
            file.mkdir();
    }

    private void goToCamera() { startActivity(new Intent(this,CameraActivity.class)); }

    private void goToMap() {
        startActivity(new Intent(this,MapsActivity.class));
        data.setRightAngle(0f);
        data.setLeftAngle(0f);
        data.setTargetAngle(0f);
    }

    //Asks user for permission
    //Being storage manipulation, gps, camera,  ect...
    private void requestPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{permission}, 101);
    }

    //Get current key of selective area
    public static String getKey() {
        return key;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        MenuItem item = menu.add("למפה");
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                goToMap();
                return true;
            }
        });
        MenuItem item1 = menu.add("להגדרות");
        item1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                goToSettings();
                return true;
            }
        });
        return true;
    }

    private void goToSettings() { startActivity(new Intent(this,Settings.class)); }

    /**
     * Creates folder to store Excel sheet and both photo of both left and right
     */
    @SuppressLint("SimpleDateFormat")
    static void setFileName(){
        timestamp = new SimpleDateFormat("dd MM yyyy hh:mm:ss").format(new Date());
        folderDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+timestamp;
    }
}

enum FireType {TARGET,AREA}