package com.example.administrator.safetylens;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.data.kml.KmlContainer;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.data.kml.KmlPlacemark;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.administrator.safetylens.MainActivity.ammos;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Map<String,CustomPolygon> map;
    CustomPolygon currentPolygon;
    Button finish, show, notes;
    static List<PolylineOptions[]> polylineOptionsList = new ArrayList<>();
    static final List<String> timestamps = new ArrayList<>();
    static List<Boolean> booleanList = new ArrayList<>();
    List<Funnel> funnelList = new ArrayList<>();
    RecyclerView recyclerView;
    PolygonOptionsListAdapter polygonOptionsListAdapter;
    private AlertDialog alertDialog,noteDialog;
    Funnel funnel;
    TextView statusBar;
    static float originalLeft,originalRight;
    static LatLng originalFiringPoint, target;
    Funnel drillFunnel;
    AmmoList ammoList;
    Spinner spinnerAmmo;
    ExcelGenerator generator;
    CustomPolygonList customPolygonList;
    String key;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // Hiding the title bar has to happen before the view is created
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDrillResume()){
                    funnelList.add(drillFunnel);
                    generator.addFunnel(MainActivity.data.type,drillFunnel);
                    generator.printFile();
                }
                else
                    printInfo();
            }
        });

        notes = findViewById(R.id.notes);
        noteDialog = noteDialog();
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDialog.show();
            }
        });

        show = findViewById(R.id.show);
        alertDialog = alertDialog();
        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        ammoList = new AmmoList(this,android.R.layout.simple_spinner_item,ammos);
        spinnerAmmo = new Spinner(this);
        spinnerAmmo.setAdapter(ammoList);
        spinnerAmmo.setOnItemSelectedListener(ammoList.onItemClicker);
    }

    @Override
    public void onResume(){
        super.onResume();

        if(MainActivity.fireType.equals(FireType.MOTION_RESUME)) statusBar.setVisibility(View.INVISIBLE);
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
                       funnel.setNotes(noteText.getText().toString());
                    }
                }).create();
    }

    private AlertDialog drillDialog(){
        return new AlertDialog.Builder(this).setTitle("לשמור פוליגון לתרגיל? ניתן גם לשנות סוג תחמושת")
                .setView(spinnerAmmo)
                .setPositiveButton("שמור לתרגיל", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        drillFunnel.setColorsToSaveInDrill();
                        polylineOptionsList.add(drillFunnel.getLines());
                        funnelList.add(drillFunnel);
                        generator.addFunnel(MainActivity.data.type,drillFunnel);
                        goToCamera();
                    }
                }).setNeutralButton("חזרה למפה",null)
                .setNegativeButton("לא לשמור", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToCamera();
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
        if(Settings.maps)//Map type, might be changed later on to fit demands
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        /*Get map of static polygons and add to map
        try {
            map = MainActivity.customPolygonList.getMap();
        }catch (NullPointerException e){goToMain();}
        for(String key : map.keySet()) {
            map.get(key).setMap(mMap);
            mMap.addPolygon(map.get(key).getPolygon());
        }*/
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        addFiringAreas();

        currentPolygon = findCurrentPolygon();

        // Create new Funnel to display the Polygon
        try {//Won't work on first instance, will succeed afterwards
            if(!MainActivity.fireType.equals(FireType.NONE)) {
                try {
                    addNewFunnel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
           // }
        }catch (NullPointerException n){n.printStackTrace(); }

        mMap.setOnMyLocationClickListener(new GoogleMap.OnMyLocationClickListener(){
            @Override
            public void onMyLocationClick(@NonNull Location location) {
                if(MainActivity.targetVisible) {
                    Location targetLocation = new Location("");
                    targetLocation.setLongitude(target.longitude);
                    targetLocation.setLatitude(target.latitude);
                    onLocationClick(location.bearingTo(targetLocation));
                }
            }
        });

        if(MainActivity.fireType.equals(FireType.DRILL)){
            originalLeft = MainActivity.data.leftAngle;
            originalRight = MainActivity.data.rightAngle;
            originalFiringPoint = MainActivity.gps.getLatLng();
        }

        if(MainActivity.fireType.equals(FireType.DRILL_RESUME)){//Will only be called if drill was started, prints all of the previous ones
            for (PolylineOptions[] options : polylineOptionsList)
                for (PolylineOptions option : options)
                    mMap.addPolyline(option);
            drillFunnel = new Funnel(originalFiringPoint);
            //drillFunnel.setColorToNew();
            for(PolylineOptions options:drillFunnel.getLines())
                mMap.addPolyline(options);
        }

        //In the event it is not a single fire(Motion or Drill), all will be displayed regardless
        //Therefore, no need for show, but rather to start next stage
        if(MainActivity.fireType.equals(FireType.DRILL) || MainActivity.fireType.equals(FireType.DRILL_RESUME)){
            show.setText("הוסף");
            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isDrillResume())
                        drillDialog().show();
                    else {
                        MainActivity.fireType = FireType.DRILL_RESUME;
                        polylineOptionsList.add(funnel.getLines());
                        funnelList.add(funnel);
                        generator = new ExcelGenerator();
                        generator.addFunnel(MainActivity.data.type,funnel);
                        timestamps.add(funnel.timestamp);
                        booleanList.add(true);
                        polygonOptionsListAdapter.notifyDataSetChanged();
                        goToCamera();
                    }
                }
            });
        }
        if(MainActivity.fireType.equals(FireType.MOTION) || MainActivity.fireType.equals(FireType.MOTION_RESUME)){
            MainActivity.fireType = FireType.MOTION_RESUME;
            show.setText("הוסף");
            show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToCamera();
                }
            });
        }

         if(MainActivity.fireType.equals(FireType.MOTION_RESUME))
             addMotionPolyline();
    }

    private void addNewFunnel() {
        if(isDrillResume()) funnel = new Funnel(originalFiringPoint);
        else //if (Motion Resume))
            funnel = new Funnel(MainActivity.gps.getLatLng());
        //currentPolygon = findCurrentArea();
        //if(currentPolygon==null)
        currentPolygon = findCurrentPolygon();
        for(PolylineOptions options:funnel.getLines())
            mMap.addPolyline(options);
        addDangerCircle();
        if(currentPolygon == null){
            funnel.status = Funnel.Status.RED;
            statusBar.setBackgroundColor(Color.RED);
            return;
        }
        checkFunnelStatus(funnel.getPoints(),funnel);
        //setStatusImage(funnel);
        if(funnel.status == Funnel.Status.GREEN)
            statusBar.setBackgroundColor(Color.GREEN);
        else if (funnel.status == Funnel.Status.YELLOW)
            statusBar.setBackgroundColor(Color.YELLOW);
        else
            statusBar.setBackgroundColor(Color.RED);
        if (!funnel.status.equals(Funnel.Status.RED))
            funnelList.add(funnel);
    }

    /**
     * If it is a target, and a circle of 300m is needed, add one
     */
    private void addDangerCircle() {
        if(MainActivity.targetVisible && MainActivity.dangerCircleNeeded)
            mMap.addCircle(new CircleOptions().center(funnel.getTarget()).radius(300).fillColor(Color.MAGENTA));
    }

    private void checkFunnelStatus(LatLng[] points, Funnel funnel) {
        if(currentPolygon == null){
            funnel.status = Funnel.Status.RED;
            return;
        }
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
        if(isDrillResume() && funnel.getStatus().equals(Funnel.Status.RED))
            for(PolylineOptions line : funnel.lines.values())
                line.color(Color.RED);

    }

    //Check if status is Motion_Resume
    private boolean isDrillResume() {
        return MainActivity.fireType.equals(FireType.DRILL_RESUME);
    }

    private void addMotionPolyline() {
        PolygonOptions motionLine = new PolygonOptions();
        motionLine.add(MainActivity.gps.getLatLng()).add(fullRangePoint(MainActivity.gps.getLatLng(),CameraActivity.motionLength,CameraActivity.motionAngle)).fillColor(Color.DKGRAY);
        mMap.addPolygon(motionLine);

        for(int i=1; i<=CameraActivity.motionCount; i++){
            LatLng newFiringPoint = fullRangePoint(MainActivity.gps.getLatLng(),(double)i*CameraActivity.motionLength/CameraActivity.motionCount,CameraActivity.motionAngle);
            Funnel motionFunnel = new Funnel(newFiringPoint,MapsActivity.target);
            motionFunnel.setStatus(currentPolygon,map);
            if(!motionFunnel.status.equals(Funnel.Status.RED)){
                funnelList.add(motionFunnel);
                for(PolylineOptions options:motionFunnel.lines.values())
                    mMap.addPolyline(options);
                timestamps.add(motionFunnel.timestamp);
                booleanList.add(true);
                polygonOptionsListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void goToMain() {startActivity(new Intent(this,MainActivity.class));}

    private void goToCamera() { startActivity(new Intent(this,CameraActivity.class)); }

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

    //When location is clicked, add funnel towards target
    private void onLocationClick(float bearing) {
        Funnel f = new Funnel(MainActivity.gps.getLatLng(), bearing);
        for(PolylineOptions options:f.getLines())
            checkFunnelStatus(f.getPoints(),f);
            if(!f.getStatus().equals(Funnel.Status.RED)) {
                funnelList.add(f);
                polylineOptionsList.add(f.getLines());
                timestamps.add(f.timestamp);
                booleanList.add(true);
                polygonOptionsListAdapter.notifyDataSetChanged();
            }
    }

    public void printInfo() {
        MainActivity.setFileName();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+MainActivity.timestamp);
        if(!file.exists())
            file.mkdir();
        /*if(MainActivity.targetOrArea == FireType.AREA)
            new ExcelGenerator(funnel.getFiringPoint(), funnel.points.get("Left"), funnel.points.get("Right"));
        else
            new ExcelGenerator(funnel.getFiringPoint(), funnel.points.get("Target"));*/
        try {
            new ExcelGenerator(funnelList);
        }catch (Exception e){e.printStackTrace();}
        if(Settings.pictures)
            movePhotos();
        Toast.makeText(this,"נוצר קובץ אקסל",Toast.LENGTH_SHORT).show();
        /*Add all polylineOptions to list to save
          These can then be called on pop-up alert and be placed or removed from map.
          of course, it will only be saved if the user presses add in activity*/
        if(!(MainActivity.fireType.equals(FireType.DRILL_RESUME) || MainActivity.fireType.equals(FireType.DRILL) || MainActivity.fireType.equals(FireType.MOTION) || MainActivity.fireType.equals(FireType.MOTION_RESUME))) {
            //TODO: When UI is redrawn, and plus button is selected, change these lines to the plus button
            polylineOptionsList.add(funnel.getLines());
            timestamps.add(funnel.timestamp);
            booleanList.add(true);
            polygonOptionsListAdapter.notifyDataSetChanged();
            /*
            if(!isDrillResume())
                goToCamera();
            else
                drillDialog().show();
             */
        }
    }

    /**
     * Moves files from general storage to timestamp photo
     */
    private void movePhotos() {
        String from = CameraActivity.directory,directory = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+funnel.timestamp;
        if(MainActivity.targetVisible) {
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

    /**
     * Get the point at farthest range, which is a straight line from where you are
     * Find using the formula:
     * lat = currentLocation.latitude + (height*Math.cos(angle));
     * lon = currentLocation.longitude + (height*Math.sin(angle));
     * Angles need conversion to radians
     * @param currentLocation
     * @param height .
     * @param angle angle
     * @return Location of farthest point
     */
    public static LatLng fullRangePoint(LatLng currentLocation, double height,float angle){
        double lat,lon;

        lat = currentLocation.latitude + (height * Math.cos(Math.toRadians(angle)));
        lon = currentLocation.longitude + (height * Math.sin(Math.toRadians(angle)));

        return new LatLng(lat,lon);
    }

    public void addFiringAreas(){
        customPolygonList = new CustomPolygonList();
        try {
            KmlLayer layer = new KmlLayer(mMap, R.raw.firing_areas, this);
            layer.addLayerToMap();
            for(KmlContainer container:layer.getContainers())
                for(KmlContainer nestedContainer : container.getContainers()){
                    if(nestedContainer.hasPlacemarks())
                        for(KmlPlacemark placemark:nestedContainer.getPlacemarks()){
                            try {
                                //mMap.addPolygon(new PolygonOptions().addAll((ArrayList<LatLng>) placemark.getGeometry().getGeometryObject()).fillColor(Color.BLUE).strokeColor(Color.BLACK));
                                customPolygonList.addCustomPolygon(placemark.getProperty("name"),(ArrayList<LatLng>) placemark.getGeometry().getGeometryObject());
                            }catch (Exception e){e.printStackTrace();}
                        }
                }
            layer.removeLayerFromMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for(CustomPolygon polygon:customPolygonList.getMap().values())
            mMap.addPolygon(polygon.getPolygon());
    }

    public CustomPolygon findCurrentPolygon(){
        CustomPolygon findPolygon = null;
        try{
            for(CustomPolygon polygon:customPolygonList.getMap().values())
                if(polygon.isIn(MainActivity.gps.getLatLng())) {
                    findPolygon = polygon;
                    key = findPolygon.key;
                    break;
                }
        }catch (Exception e){e.printStackTrace(); Toast.makeText(this,"מיקום לא נמצא",Toast.LENGTH_SHORT).show();}

        return findPolygon;
    }

    public void setStatusImage(Funnel fun){
        //statusView.setVisibility(View.Visible);
        if(fun.status.equals(Funnel.Status.GREEN))
            ;//statusView.setimage(R.drawable.ic_greenstatus);
        else if (fun.status.equals(Funnel.Status.YELLOW))
            ;//statusView.setimage(R.drawable.ic_yellowstatus);
        else
            ;//statusView.setimage(R.drawable.ic_redstatus);
    }

    /*
    @Override
    public void onBackPressed() {//Returns to camera OR main. Removes all created polygons unless saved first
        new AlertDialog.Builder(this)
                .setTitle("לחזור אחור?")
                .setMessage("כל המידע הלא נשמר ימחק")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        MapsActivity.super.onBackPressed();
                    }
                }).create().show();
    }*/
}