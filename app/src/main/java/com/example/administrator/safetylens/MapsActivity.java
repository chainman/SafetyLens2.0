package com.example.administrator.safetylens;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Map<String,CustomPolygon> map;
    CustomPolygon currentPolygon;
    boolean status = true;
    Button finish, show, notes;
    static List<PolylineOptions[]> polylineOptionsList = new ArrayList<>();
    static final List<String> timestamps = new ArrayList<>();
    static List<Boolean> booleanList = new ArrayList<>();
    RecyclerView recyclerView;
    PolygonOptionsListAdapter polygonOptionsListAdapter;
    private AlertDialog alertDialog,noteDialog;
    Funnel funnel;
    TextView statusBar;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        statusBar = findViewById(R.id.statusBar);
        statusBar.setBackgroundColor(Color.TRANSPARENT);
        statusBar.bringToFront();

        recyclerView = new RecyclerView(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        polygonOptionsListAdapter = new PolygonOptionsListAdapter(timestamps,mMap,polylineOptionsList);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(polygonOptionsListAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                    if(booleanList.get(position)){
                        ArrayList<Polyline> lines = new ArrayList<>();
                        for (PolylineOptions option:polylineOptionsList.get(position))
                            lines.add(mMap.addPolyline(option));
                        for (Polyline line : lines) line.remove();
                        lines.clear();
                    }
                    else {
                            for (PolylineOptions option:polylineOptionsList.get(position))
                            mMap.addPolyline(option);
                         }
                    booleanList.set(position,!booleanList.get(position));
                }

            @Override
            public void onLongClick(View view, int position) {
                //openFile(position);
            }
        }));

        finish = findViewById(R.id.finish);
        finish.setVisibility(View.INVISIBLE);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printInfo();
                finish.setVisibility(View.INVISIBLE);
                notes.setVisibility(View.INVISIBLE);
            }
        });

        show = findViewById(R.id.show);
        show.setVisibility(View.INVISIBLE);
        alertDialog = alertDialog();
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        notes = findViewById(R.id.notes);
        notes.setVisibility(View.INVISIBLE);
        noteDialog = noteDialog();
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDialog.show();
            }
        });
    }

    private AlertDialog alertDialog() {
       return new AlertDialog.Builder(this).setTitle("רשימת פוליגונים").setNegativeButton("הסר הכל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMap.clear();
                for(String key:map.keySet())
                    mMap.addPolygon(map.get(key).getPolygon());
                for (int i = 0; i < booleanList.size(); i++)
                    booleanList.set(i, false);
            }
        }).setNeutralButton("מחק הכל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMap.clear();
                for(String key:map.keySet())
                    mMap.addPolygon(map.get(key).getPolygon());
                polylineOptionsList.clear();
                timestamps.clear();
                polygonOptionsListAdapter.notifyDataSetChanged();
                booleanList.clear();
            }
        }).setPositiveButton("הוסף הכל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (PolylineOptions[] options : polylineOptionsList)
                    for (PolylineOptions option : options)
                        mMap.addPolyline(option);
            }
        }).setView(recyclerView).create();
    }

    EditText noteText;

    private AlertDialog noteDialog(){
        noteText = new EditText(this);
        noteText.setText(MainActivity.data.notes);
        return new AlertDialog.Builder(this).setTitle("הוסף הערות")
                .setView(noteText)
                .setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.data.setNotes(noteText.getText().toString());
                    }
                }).create();
    }

    private void openFile(int position){
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+timestamps.get(position)+"Data.xls");
        Uri path = Uri.fromFile(file);
        Intent openintent = new Intent(Intent.ACTION_VIEW);
        openintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openintent.setDataAndType(path, "application/vnd.ms-excel");
        try {
            startActivity(openintent);
        }
        catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json))); //Set the style to white oriented

        //Get map of static polygons and add to map
        try {
            map = MainActivity.customPolygonList.getMap();
        }catch (NullPointerException e){goToMain();}
        for(String key : map.keySet()) {
            map.get(key).setMap(mMap);
            mMap.addPolygon(map.get(key).getPolygon());
        }
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Create new Funnel to display the Polygon

        try {//Won't work on first instance, will succeed afterwards
            if(MainActivity.data.getLeftAngle()!=0f || MainActivity.data.getRightAngle()!=0f || MainActivity.data.targetAngle!=0f)
                addNewFunnel(); //Step goes in here
            //else {
                //for (String key : map.keySet())
                    //mMap.addPolygon(map.get(key).getPolygon());
                try {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() { show.setVisibility(View.VISIBLE);  finish.setVisibility(View.VISIBLE); notes.setVisibility(View.VISIBLE);
                        }
                    },1000*Settings.seconds);//Wait x seconds until set to visible, 1000*x ,1000 is conversion and x is seconds, called from settings
                } catch (Exception e) { e.printStackTrace(); }
           // }
        }catch (NullPointerException n){n.printStackTrace(); }
    }

    private void addNewFunnel() {
        funnel = new Funnel(MainActivity.gps.getLatLng());
        //currentPolygon = findCurrentArea();
        //if(currentPolygon==null)
        currentPolygon = MainActivity.customPolygonList.getMap().get(MainActivity.key);
        for(PolylineOptions options:funnel.getLines())
            mMap.addPolyline(options);
        addDangerCircle();
        checkFunnelStatus(funnel.getPoints());
        if(funnel.status == Funnel.Status.GREEN)
            statusBar.setBackgroundColor(Color.GREEN);
        else if (funnel.status == Funnel.Status.YELLOW)
            statusBar.setBackgroundColor(Color.YELLOW);
        else
            statusBar.setBackgroundColor(Color.RED);
    }

    /**
     * If it is a target, and a circle of 300m is needed, add one
     */
    private void addDangerCircle() {
        if(MainActivity.targetOrArea == FireType.TARGET && MainActivity.dangerCircleNeeded)
            mMap.addCircle(new CircleOptions().center(funnel.getTarget()).radius(300).fillColor(Color.MAGENTA));
    }

    private void checkFunnelStatus(LatLng[] points) {
        funnel.setStatus(Funnel.Status.GREEN);
        //LatLng[] points = funnel.getPoints();
        for(LatLng ll : points)
            if(!currentPolygon.isIn(ll)){ //No status change
                //(Green) if doesn't go in,
                for(String key:map.keySet()){
                    if(!key.equals(MainActivity.key)){
                        if(map.get(key).isIn(ll)){
                            funnel.setStatus(Funnel.Status.YELLOW);
                            break;
                        }else
                        { //It wasn't yellow or in the fire zone. Therefore, it is red.
                            funnel.setStatus(Funnel.Status.RED);
                            break;
                        }
                    }
                }
                if(funnel.getStatus() == Funnel.Status.RED)//We know that it out of all boundaries, so no point in checking any farther
                    break;
            }
    }

    private void goToMain() {startActivity(new Intent(this,MainActivity.class));}

    private void updateLocationUI() {
        if (mMap == null) { return; }
        try {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.gps.getLatLng(), 12));
        } catch(NullPointerException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void printInfo() {
        MainActivity.setFileName();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+MainActivity.timestamp);
        if(!file.exists())
            file.mkdir();
        if(MainActivity.targetOrArea == FireType.AREA)
            new ExcelGenerator(funnel.getFiringPoint(), funnel.points.get("Left"), funnel.points.get("Right"));
        else
            new ExcelGenerator(funnel.getFiringPoint(), funnel.points.get("Target"));
        if(Settings.pictures)
            movePhotos();
        Toast.makeText(this,"נוצר קובץ אקסל",Toast.LENGTH_SHORT).show();
        /*Add all polylineOptions to list to save, the order is left line, right line, main curve, left offset line, right offset line, left offset arc and finally right offset arc
          These can then be called on pop-up alert and be placed or removed from map.
          of course, it will only be saved if the user presses finish in activity*/
        polylineOptionsList.add(funnel.getLines());
        //polylineOptionsList.add(new PolylineOptions[]{p1,p2,curve,leftOffset,rightOffset,rightArc,leftArc});
        timestamps.add(MainActivity.timestamp);
        booleanList.add(true);
        polygonOptionsListAdapter.notifyDataSetChanged();
    }

    /**
     * @return current firing area
     */
    public CustomPolygon findCurrentArea() {
        for(CustomPolygon polygon:MainActivity.customPolygonList.getMap().values())
            if(polygon.isIn(MainActivity.gps.getLatLng()))
                return polygon;
            return null;
    }

    /**
     * Moves files from general storage to timestamp photo
     */
    private void movePhotos() {
        String from = CameraActivity.directory,directory = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+MainActivity.timestamp;
        if(MainActivity.targetOrArea == FireType.AREA) {
            File f1 = new File(from,"right.jpg");
            File f2 = new File(directory,"right.jpg");
            f1.renameTo(f2);
            f1 = new File(from,"left.jpg");
            f2 = new File(directory,"left.jpg");
            f1.renameTo(f2); }
        else {
            File f1 = new File(from,"target.jpg");
            File f2 = new File(directory,"target.jpg");
            f1.renameTo(f2);
        }
    }

}