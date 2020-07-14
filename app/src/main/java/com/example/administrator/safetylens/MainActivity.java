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
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {

    static boolean dangerCircleNeeded;
    static Gps gps;
    static Data data;
    Spinner spinnerArea,spinnerAmmo,spinnerFiringType;
    String[] keys;
    static String[] ammos; //List of ammo names,to be filled in manually here
    static String key;
    CustomPolygonList customPolygonList;
    AmmoList ammoList;
    static double height;
    Button start, automate;
    static String timestamp,folderDirectory,name = "Safety";
    static FireType fireType;
    static double targetDistance;
    EditText targetText,nameText;
    static boolean targetVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.home_page);

        //Ask for permission to access files, gps and camera
        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestPermission(Manifest.permission.CAMERA);

        //Set up data to store all calculation and output data
        data = new Data();

        targetText = findViewById(R.id.editText);

        //Button for next activity, will notify user if ammo type is not selected
        start = findViewById(R.id.startBtn);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(data.type != null) {
                    if (targetVisible)
                        try {
                            targetDistance = (Double.parseDouble(targetText.getText().toString())) / 6371.0 * (180.0 / Math.PI);
                            if (targetDistance > height)
                                Toast.makeText(MainActivity.this, "הזן מרחק קטן יותר", Toast.LENGTH_SHORT).show();
                            else {
                                if(nameText.getText()!=null)
                                    timestamp = nameText.getText().toString();
                                goToCamera();
                            }
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "הזן מטרה", Toast.LENGTH_SHORT).show();
                        }
                    else if(!fireType.equals(FireType.NONE)){
                        if(nameText.getText()!=null)
                            timestamp = nameText.getText().toString();
                        goToCamera();
                    }
                    else
                        Toast.makeText(MainActivity.this,"בחר סוג ירי",Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(MainActivity.this,"בחר סוג תחמושת",Toast.LENGTH_SHORT).show();
            }
        });
        targetText.setVisibility(View.INVISIBLE);
        findViewById(R.id.targetDistance).setVisibility(View.INVISIBLE);
        //Create list of custom polygons, and store keys
        customPolygonList = new CustomPolygonList();
        keys = customPolygonList.getMap().keySet().toArray(new String[customPolygonList.getMap().keySet().size()]);

        timestamp = "";
        nameText = findViewById(R.id.place);

        /*Set the spinners and adapters. These display the area or ammo and updates for when moving between activities.
        spinnerArea = findViewById(R.id.place);
        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                key = keys[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        automate = findViewById(R.id.automate); //Automates where the firing area is, removes need to search
        automate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    boolean f = true;
                    for(CustomPolygon polygon:customPolygonList.getMap().values())
                        if(polygon.isIn(gps.getLatLng())) {
                            key = polygon.getKey();
                            Toast.makeText(MainActivity.this,"שטח אש: "+key,Toast.LENGTH_SHORT).show();
                            f = false;
                            break;
                        }
                    if (f) Toast.makeText(MainActivity.this,"לא נמצא בתוך שטח אש",Toast.LENGTH_SHORT).show();
                }catch (Exception e){e.printStackTrace(); Toast.makeText(MainActivity.this,"מיקום לא נמצא",Toast.LENGTH_SHORT).show();}
            }
        });*/

        gps = new Gps(this); //GPS for usage

        /*
        ArrayAdapter<String> adapterArea =  new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,keys);
        adapterArea.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerArea.setAdapter(adapterArea);*/
        //Insert here the names of the ammo types:
        ammos = new String[]{"לאו","9 ממ","מפצח אגוזים - 1.5","לאו A4","M-855","M-193","M-24","מפצח אגוזים - 2.5","M-193 3.0","40ממ","ברק 3.4","בארט","7.62 M-24","6.72 מאג","ברק - 4.4","0.5 אינצ"};
        ammoList = new AmmoList(this,android.R.layout.simple_spinner_item,ammos);
        spinnerAmmo = findViewById(R.id.ammoPlace);
        spinnerAmmo.setAdapter(ammoList);
        spinnerAmmo.setOnItemSelectedListener(ammoList.onItemClicker);

        targetVisible = false;
        spinnerFiringType = findViewById(R.id.location2);
        final String[] firingTypeKeys = new String[]{"ג.ג","מטרה","תרגיל","תמרון"};
        ArrayAdapter<String> adapterFiringType = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,firingTypeKeys);
        adapterFiringType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiringType.setAdapter(adapterFiringType);
        spinnerFiringType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (firingTypeKeys[position]) {
                    case "ג.ג":
                        MainActivity.fireType = FireType.AREA;
                        targetVisible = false;
                        break;
                    case "מטרה":
                        MainActivity.fireType = FireType.TARGET;
                        targetVisible = true;
                        break;
                    case "תרגיל":
                        MainActivity.fireType = FireType.DRILL;
                        targetVisible = false;
                        break;
                    case "תמרון":
                        MainActivity.fireType = FireType.MOTION;
                        targetVisible = true;
                        break;
                    default:
                        Log.i("Firing Type:","Didn't click");
                }
                if(targetVisible){
                    findViewById(R.id.targetDistance).setVisibility(View.VISIBLE);
                    findViewById(R.id.txtLine).setVisibility(View.VISIBLE);
                    targetText.setVisibility(View.VISIBLE);
                }
                else{
                    findViewById(R.id.targetDistance).setVisibility(View.INVISIBLE);
                    findViewById(R.id.txtLine).setVisibility(View.INVISIBLE);
                    targetText.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

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

        if(fireType != null && fireType.equals(FireType.MOTION_RESUME))
            fireType = FireType.MOTION;
        if(fireType != null && fireType.equals(FireType.DRILL_RESUME))
            fireType = FireType.DRILL;
    }

    private void goToCamera() { startActivity(new Intent(this,CameraActivity.class)); }

    private void goToMap() {
        startActivity(new Intent(this,MapsActivity.class));
        data.setRightAngle(0f);
        data.setLeftAngle(0f);
        data.setTargetAngle(0f);
        fireType = FireType.NONE;
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
        if(timestamp.equals(""))
            timestamp = new SimpleDateFormat("dd MM yyyy hh:mm:ss").format(new Date());
        folderDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+timestamp;
    }

}

enum FireType {TARGET,AREA,DRILL,MOTION,DRILL_RESUME,MOTION_RESUME,NONE}