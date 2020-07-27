package com.example.administrator.safetylens;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Separate class for manual input of polygons, only function is return Map of CustomPolygons, meant to be called during onCreate for setting
 * or on sudden entry by GPS refreshing or turning on from settings
 */

public class CustomPolygonList {

    private Map<String,CustomPolygon> map ;
    private Map<String, PolygonOptions> data;
    //Brackets just to simplify coding area, no connection to previous statement
    CustomPolygonList()
    {
        //data = new FiringAreaData(context,mMap).getFiringData();
        /*
          //Variable input in format of:
          List<LatLng> l = new ArrayList<>();
          l.add(new LatLng(lat,lon));//A few more of those
          CustomPolygon var = new CustomPolygon(l,key);
          map.put(key,var);
          */

        map = new HashMap<>();

        /*
        List<LatLng> l2 = new ArrayList<>();
        l2.add(new LatLng(32.084190, 34.798950));//A few more of those
        l2.add(new LatLng(32.066775, 34.934309));
        l2.add(new LatLng(32.029036, 34.879771));
        l2.add(new LatLng(32.036973, 34.830234));
        CustomPolygon var2 = new CustomPolygon(l2,"תל השומר");
        map.put("תל השומר",var2);*/

        //Put all the firing data into the map
        //for(String key:data.keySet())
        //    map.put(key, new CustomPolygon(data.get(key),key));
    }

    //Returns the list of static locations
    Map<String,CustomPolygon> getMap(){
        return map;
    }

    public void addCustomPolygon(String key, ArrayList<LatLng> list){
        map.put(key, new CustomPolygon(list,key));
    }

    public List<LatLng> doubleToLatlng(double[] list){
        List<LatLng> ll = new ArrayList<>();
        double value = -0.1, temp = value;
        for(double point: list){
            if(temp == value)
                temp = point;
            else
            {
                ll.add(new LatLng(temp,point));
                temp = value;
            }
        }
        return ll;
    }

}
