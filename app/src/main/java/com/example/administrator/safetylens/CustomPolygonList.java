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

        List<LatLng> l = new ArrayList<>();
        l.add(new LatLng(32.039427, 34.830236));//A few more of those
        l.add(new LatLng(32.016727, 34.823075));
        l.add(new LatLng(32.017295, 34.899066));
        l.add(new LatLng(32.028951, 34.877971));
        CustomPolygon var = new CustomPolygon(l,"סביון");
        map.put("סביון",var);

        List<LatLng> l2 = new ArrayList<>();
        l2.add(new LatLng(32.084190, 34.798950));//A few more of those
        l2.add(new LatLng(32.066775, 34.934309));
        l2.add(new LatLng(32.029036, 34.879771));
        l2.add(new LatLng(32.036973, 34.830234));
        CustomPolygon var2 = new CustomPolygon(l2,"תל השומר");
        map.put("תל השומר",var2);

        List<LatLng> l3 = new ArrayList<>();
        l3.add(new LatLng(35.077642, 32.304898));//A few more of those
        l3.add(new LatLng(35.546932, 34.256857));
        l3.add(new LatLng(34.985236, 34.034785));
        l3.add(new LatLng(34.581295, 33.003987));
        CustomPolygon var3 = new CustomPolygon(l3,"כפריסין");
        map.put("כפריסין",var3);

        List<LatLng> l4 = new ArrayList<>();
        l4.add(new LatLng(33.233092, 35.680523));//A few more of those
        l4.add(new LatLng(33.213463, 35.725306));
        l4.add(new LatLng(33.177652, 35.726576));
        l4.add(new LatLng(33.140742, 35.711806));
        l4.add(new LatLng(33.146373, 35.674392));
        CustomPolygon var4 = new CustomPolygon(l4,"רמת הגולן");
        map.put("רמת הגולן",var4);

        List<LatLng> l5 = new ArrayList<>();
        l5.add(new LatLng(32.287669, 34.898053));
        l5.add(new LatLng(32.297559, 35.010054));
        l5.add(new LatLng(32.245506, 35.020794));
        l5.add(new LatLng(32.176218, 34.956547));
        l5.add(new LatLng(32.248263, 34.801202));
        CustomPolygon var5 = new CustomPolygon(l5,"מרכז");
        map.put("מרכז",var5);

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
