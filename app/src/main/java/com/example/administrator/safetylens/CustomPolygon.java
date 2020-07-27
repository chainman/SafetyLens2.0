package com.example.administrator.safetylens;

import com.example.administrator.safetylens.MapUtils.PolyUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

/**
 * This class serves two purposes in the map feature, one laying static polygons of designated areas
 * The second is a movable and mutable based upon users decisions in the Main, which also serves as a setting tab
 * Another class with a long list of set areas will be created manually by the coder (cough,cough)
 * The static ones should have a listener if they are crossed via GPS, while the movable needs to be flexible and obtain circular boundaries
 */

public class CustomPolygon {

    private List<LatLng> latLngs; //List of coordinates
    private PolygonOptions polygon;
    GoogleMap map;
    private Polygon polygons;
    String key; //Name of firing area

    //Set of coordinates and state of the polygon
    //Make sure received list is array list
    public CustomPolygon(List<LatLng> ll, String key) {
        latLngs = ll;
        polygon = new PolygonOptions();
        for(LatLng l : ll)
            polygon.add(l);
        polygon.fillColor(0x9900ff00).strokeWidth(0);
        polygon.zIndex(1);
        this.key = key;
    }

    public PolygonOptions getPolygon() {
        return polygon;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    //Checks if a coordinate is in the Custom Polygon
    public boolean isIn(LatLng latLng){
        return PolyUtil.containsLocation(latLng,latLngs,false);
    }

    public void add(){
        map.clear();
        polygons = map.addPolygon(polygon);
    }

    public void remove(){
        polygons.remove();
    }

    public String getKey() { return key;}
}
