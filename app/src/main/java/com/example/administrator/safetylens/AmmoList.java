package com.example.administrator.safetylens;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

/**
 * Immutable class for handling the type of ammo
 * Requirements are: Name of ammo - String
 *                   Distance - Double
 *                   Offset - Float. This is burst/angling effect radius
 */

public class AmmoList extends ArrayAdapter<String>{
    private String[] ammos; //List of ammo names,to be filled in manually here
    private double[] distance;//Corresponding to ammos, every index has to be the same, basically avoiding complex try/catch. Distance the ammo can travel
    private boolean[] dangerCircle; //If needs radius of 300m around it

    AmmoList(Context context, int resource, String[] array) {
        super(context, resource,array);
        ammos = array;
        //Insert here the distance of th e ammo. Keep in mind that they are correspondence
        distance = new double[]{1.0,1.5,1.5,1.7,2.3,2.3,2.3,2.5,3.0,3.0,3.4,3.4,3.4,3.9,4.1,4.4,5.5};
        dangerCircle = new boolean[]{false,false,false,true,false,false,false,true,false,true,false,false,false,false,false,false,false};
        //Convert all the distances to degrees, scale must be in kilometers
        //Functionality is so: (Distance/Re)*(180°/PI)
        //Where D = The distance of the ammo, Re = Radius of the earth = 6371 km
        for(int i=0;i<distance.length;i++)
            distance[i]=(distance[i]/6371.0)*(180.0/Math.PI);
    }

    /**
     * Same as CustonPolygonList, made to be created and stay the same
     */
    @Override
    public int getCount() //Length of array in adapter, which are equal for both ammo and distance
    {return ammos.length;}

    @Override
    public String getItem(int i)//Gets item at index, shouldn't be Object when know data type, also should be converted to double.
    {return ammos[i];}

    @Override
    public long getItemId(int i)//Returns Id of selected item
    {return i;}


    AdapterView.OnItemSelectedListener onItemClicker = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            MainActivity.height = distance[position];
            MainActivity.data.setType(ammos[position]);
            MainActivity.dangerCircleNeeded = dangerCircle[position];
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

}
