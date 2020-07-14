package com.example.administrator.safetylens;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gekov on 20/02/2018.
 */

//GPS handler for application, used to allocate current location

public class  Gps implements LocationListener {

    private double lon,lat;
    private Location loc;
    private LocationManager locationManager;
    private String mprovider;
    private Criteria criteria;
    private MainActivity contextCompat;
    private LatLng latLng;


    public double getLon(){
        return lon;
    }
    public double getLat()
    {
        return lat;
    }
    public Location getLoc() {
        return loc;
    }
    public LatLng getLatLng() { return latLng; }


    //Requests permission from user, in case of GPS, it need to be requested programmatically.
    public Gps(MainActivity contextCompat1) {
        contextCompat = contextCompat1;

        locationManager = (LocationManager) contextCompat.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        mprovider = locationManager.getBestProvider(criteria, true);

        if (mprovider != null && !mprovider.equals("")) {
            if (ActivityCompat.checkSelfPermission(contextCompat, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(contextCompat, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation(mprovider);
            locationManager.requestLocationUpdates(mprovider, 0, 0, this);

            if (location != null)
                ;//onLocationChanged(location);
            else
                Toast.makeText(contextCompat.getBaseContext(), "No Location Found", Toast.LENGTH_SHORT).show();
        }
    }

    //Updates current location
    @Override
    public void onLocationChanged(Location location) {
        this.latLng = new LatLng(location.getLatitude(),location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Sets current polygon based on location, and notifies users
     * It is assumed that none overlap (test excluded)
     * If key is already set, passes, as will occur in most scenarios

    private void setCurrentPolygon(){
        if(customPolygonList.getMap().get(MainActivity.key).isIn(latLng))
            return;
        for(String set : customPolygonList.getMap().keySet())
            if(customPolygonList.getMap().get(set).isIn(latLng)){
                MainActivity.key = set;
                Toast.makeText(contextCompat,set,Toast.LENGTH_SHORT).show();
                return;
            }
        MainActivity.key = "";
    }*/
}
