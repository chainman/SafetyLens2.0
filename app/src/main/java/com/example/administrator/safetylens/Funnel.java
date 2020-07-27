package com.example.administrator.safetylens;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.administrator.safetylens.MapUtils.MathUtil.SphericalUtil.computeDistanceBetween;

/**
 * Funnel class, the shapes added to the maps displaying the users output
 *
 *   LeftCurve  Curve     Right Curve
 *    ----**************-----
 *     -   *          *    -
 *       -  *        *   -
 *         -  *     *   -
 *           - *   *  -
 *    left      -*-     Right
 *              Firing point
 */

public class Funnel{

    LatLng firingPoint;//Bases for funnel
    Float leftAngle,rightAngle; //Angles, will be imported from Main
    Status status;
    String timestamp;
    Map<String,LatLng> points; //All the points in the funnel
    Map<String, PolylineOptions> lines; //All the lines in the funnel
    float angleTarget = 60;
    List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20)); //Dotted lines for offsets
    String note = " ";
    int drillPolygonCounter;

    /**
     * Constructor for Funnel,
     * @param FiringPoint - Where the funnel starts
     */
    public Funnel (LatLng FiringPoint){
        //mMap = Gmap;
        firingPoint = FiringPoint;
        leftAngle = MainActivity.data.leftAngle;
        rightAngle = MainActivity.data.rightAngle;
        //polygonList = customPolygonList;
        points = new HashMap<String,LatLng>();
        lines = new HashMap<String,PolylineOptions>();
        timestamp = new SimpleDateFormat("dd MM yyyy hh:mm:ss").format(new Date());
        points.put("FiringPoint",firingPoint);
        if(!MainActivity.targetVisible)
            addAreaPolygon(30);
        else
            addTargetPolygon(30);

        drillPolygonCounter = 1;
    }

    @SuppressLint("SimpleDateFormat")
    public Funnel (LatLng FiringPoint, float bearing){
        firingPoint = FiringPoint;
        points = new HashMap<String,LatLng>();
        lines = new HashMap<String,PolylineOptions>();
        timestamp =  new SimpleDateFormat("dd MM yyyy hh:mm:ss").format(new Date());
        points.put("FiringPoint",firingPoint);
        addMotionPolygon(30, bearing ,firingPoint);
    }

    @SuppressLint("SimpleDateFormat")
    public Funnel (LatLng FiringPoint, LatLng target){
        firingPoint = FiringPoint;
        points = new HashMap<String,LatLng>();
        lines = new HashMap<String,PolylineOptions>();
        timestamp =  new SimpleDateFormat("dd MM yyyy hh:mm:ss").format(new Date());
        points.put("FiringPoint",firingPoint);
        addMotionPolygon(30, bearing(firingPoint,target) ,firingPoint);
    }

    /**
     * Creates a Polygon based upon the targeting option
     * @param step
     */
    private void addTargetPolygon(float step) {
        float angle = MainActivity.data.getTargetAngle();//Gets angle for target

        points.put("Target",fullRangePoint(getFiringPoint(),MainActivity.targetDistance,angle));
        MapsActivity.target = fullRangePoint(getFiringPoint(),MainActivity.targetDistance,angle); //Sets target for clicking polygons

        points.put("LeftShort",fullRangePoint(getFiringPoint(),(0.5/6371.0)*(180.0/Math.PI),plus(angle,-30)));
        points.put("RightShort",fullRangePoint(getFiringPoint(),(0.5/6371.0)*(180.0/Math.PI),plus(angle,30)));
        points.put("Left",fullRangePoint(getFiringPoint(),MainActivity.height,plus(angle,-24)));
        points.put("Right",fullRangePoint(getFiringPoint(),MainActivity.height,plus(angle,24)));
        points.put("FarLeft",fullRangePoint(getFiringPoint(),MainActivity.height,plus(angle,-36)));
        points.put("FarRight",fullRangePoint(getFiringPoint(),MainActivity.height,plus(angle,36)));

        lines.put("FarLeft",new PolylineOptions().add(getFiringPoint(),getCertainPoint("FarLeft")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));
        lines.put("Left",new PolylineOptions().add(getCertainPoint("LeftShort"),getCertainPoint("Left")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("LeftShort",new PolylineOptions().add(getFiringPoint(),getCertainPoint("LeftShort")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("FarRight",new PolylineOptions().add(getFiringPoint(),getCertainPoint("FarRight")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));
        lines.put("Right",new PolylineOptions().add(getCertainPoint("RightShort"),getCertainPoint("Right")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("RightShort",new PolylineOptions().add(getFiringPoint(),getCertainPoint("RightShort")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));

        lines.put("Target",new PolylineOptions().add(getFiringPoint(),getTarget()).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));

        lines.put("LeftCurve",addCurvedLine(plus(angle,-36),plus(angle,-24),false,step,false));
        lines.put("Curve",addCurvedLine(plus(angle,-24),plus(angle,24),true,step,false));
        lines.put("RightCurve",addCurvedLine(plus(angle,24),plus(angle,36),false,step,false));

        int i=0;
        for(LatLng point:lines.get("LeftCurve").getPoints()){
            points.put("LeftCurve"+i,point);
            i++;
        }i=0;
        for(LatLng point:lines.get("Curve").getPoints()) {
            points.put("Curve"+i, point);
            i++;
        }i=0;
        for(LatLng point:lines.get("RightCurve").getPoints()) {
            points.put("RightCurve"+i, point);
            i++;
        }
    }

    /**
     * Creates a Polygon based upon the targeting option
     * @param step
     */
    public void addMotionPolygon(float step, float angle, LatLng firingPoint){
        points.put("Target",MapsActivity.target);
        points.put("LeftShort",fullRangePoint(firingPoint,(0.5/6371.0)*(180.0/Math.PI),plus(angle,-30)));
        points.put("RightShort",fullRangePoint(firingPoint,(0.5/6371.0)*(180.0/Math.PI),plus(angle,30)));
        points.put("Left",fullRangePoint(firingPoint,MainActivity.height,plus(angle,-24)));
        points.put("Right",fullRangePoint(firingPoint,MainActivity.height,plus(angle,24)));
        points.put("FarLeft",fullRangePoint(firingPoint,MainActivity.height,plus(angle,-36)));
        points.put("FarRight",fullRangePoint(firingPoint,MainActivity.height,plus(angle,36)));

        lines.put("FarLeft",new PolylineOptions().add(firingPoint,getCertainPoint("FarLeft")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));
        lines.put("Left",new PolylineOptions().add(getCertainPoint("LeftShort"),getCertainPoint("Left")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("LeftShort",new PolylineOptions().add(firingPoint,getCertainPoint("LeftShort")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("FarRight",new PolylineOptions().add(firingPoint,getCertainPoint("FarRight")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));
        lines.put("Right",new PolylineOptions().add(getCertainPoint("RightShort"),getCertainPoint("Right")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("RightShort",new PolylineOptions().add(getFiringPoint(),getCertainPoint("RightShort")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));

        //lines.put("Target",new PolylineOptions().add(getFiringPoint(),getTarget()).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));

        lines.put("LeftCurve",addCurvedLine(plus(angle,-36),plus(angle,-24),false,step,false));
        lines.put("Curve",addCurvedLine(plus(angle,-24),plus(angle,24),true,step,false));
        lines.put("RightCurve",addCurvedLine(plus(angle,24),plus(angle,36),false,step,false));

        int i=0;
        for(LatLng point:lines.get("LeftCurve").getPoints()){
            points.put("LeftCurve"+i,point);
            i++;
        }i=0;
        for(LatLng point:lines.get("Curve").getPoints()) {
            points.put("Curve"+i, point);
            i++;
        }i=0;
        for(LatLng point:lines.get("RightCurve").getPoints()) {
            points.put("RightCurve"+i, point);
            i++;
        }
    }

    public LatLng getFiringPoint(){return firingPoint;} //Get firing point
    public LatLng getCertainPoint(String key){return (LatLng)points.get(key);} //Return point based upon key value
    public LatLng getTarget(){return (LatLng)points.get("Target");}

    public PolylineOptions[] getLines(){//Returns all Polyline Options
        PolylineOptions[] polylineOptions = new PolylineOptions[lines.values().toArray().length];
        for(int i = 0; i<polylineOptions.length;i++)
            polylineOptions[i] = (PolylineOptions) lines.values().toArray()[i];
        return polylineOptions;
    }

    public LatLng[] getPoints(){//Return all points
        LatLng[] latLngs = new LatLng[points.values().toArray().length];
        for(int i = 0; i<latLngs.length;i++)
            latLngs[i] = (LatLng) points.values().toArray()[i];
        return latLngs;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Sets status for this funnel:
     * Green - All in
     * Yellow - Some points in other custom polygon
     * Red - Some points outside custom polygon
     */
    public void setStatus(CustomPolygon currentPolygon,Map<String,CustomPolygon> map){
        setStatus(Funnel.Status.GREEN);
        //LatLng[] points = funnel.getPoints();
        for(LatLng ll : points.values())
            if(!currentPolygon.isIn(ll)){ //No status change
                //(Green) if doesn't go in,
                for(String key:map.keySet()){
                    if(!key.equals(MainActivity.key)){
                        if(map.get(key).isIn(ll)){
                            setStatus(Funnel.Status.YELLOW);
                            break;
                        }else
                        { //It wasn't yellow or in the fire zone. Therefore, it is red.
                            setStatus(Funnel.Status.RED);
                            break;
                        }
                    }
                }
                if(getStatus().equals(Funnel.Status.RED))//We know that it out of all boundaries, so no point in checking any farther
                    break;
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

    /**Based on same algorithm as before just with slight changes to the angle
     * Used for left and right boundaries, with one being positive, the other negative
     * @param currentLocation .
     * @param height .
     * @param offset .
     * @return location with offset
     */
    public static LatLng offsetPoint(LatLng currentLocation, double height, float angle, float offset){
        return fullRangePoint(currentLocation,height,angle+offset);
    }

    /**
     * Caller for all points, in farthest 0 degrees is called, otherwise +- offset
     * @param offset - offset
     * @return the location at specific angle
     */
    public static LatLng pointCalculator(float offset){
        return offsetPoint(MainActivity.gps.getLatLng(),MainActivity.height,CameraActivity.degrees,offset);
    }

    /**
     * Adds polygon to map, after the data is received from other classes. Creates it and styles it.
     * Create lines between them and then add curve.
     * After, validate if the area is ok to fire.
     */
    private float arcLength, focalAngle;
    private void addAreaPolygon(float step){
        focalAngle = plus(rightAngle,-plus(rightAngle,-leftAngle)/2);//calculateFarthest(); //The main angle the funnel is based upon
        arcLength = Math.abs((rightAngle-leftAngle)%360); //Distance between Right and Left
        //if(arcLength>180) arcLength-=180;

        points.put("LeftShort",fullRangePoint(getFiringPoint(),(0.5/6371.0)*(180.0/Math.PI),MainActivity.data.getLeftAngle()));
        points.put("RightShort",fullRangePoint(getFiringPoint(),(0.5/6371.0)*(180.0/Math.PI),MainActivity.data.getRightAngle()));
        points.put("Left",fullRangePoint(getFiringPoint(),MainActivity.height,plus(focalAngle, (float) (arcLength*-0.4))));
        points.put("Right",fullRangePoint(getFiringPoint(),MainActivity.height,plus(focalAngle, (float) (arcLength*0.4))));
        points.put("FarLeft",fullRangePoint(getFiringPoint(),MainActivity.height,plus(focalAngle,(float)-0.6*arcLength)));
        points.put("FarRight",fullRangePoint(getFiringPoint(),MainActivity.height,plus(focalAngle,(float)0.6*arcLength)));

        lines.put("FarLeft",new PolylineOptions().add(getFiringPoint(),getCertainPoint("FarLeft")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));
        lines.put("Left",new PolylineOptions().add(getCertainPoint("LeftShort"),getCertainPoint("Left")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("LeftShort",new PolylineOptions().add(getFiringPoint(),getCertainPoint("LeftShort")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("FarRight",new PolylineOptions().add(getFiringPoint(),getCertainPoint("FarRight")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()).pattern(pattern));
        lines.put("Right",new PolylineOptions().add(getCertainPoint("RightShort"),getCertainPoint("Right")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));
        lines.put("RightShort",new PolylineOptions().add(getFiringPoint(),getCertainPoint("RightShort")).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap()));

        lines.put("Curve",addCurvedLine(plus(focalAngle, (float) (arcLength*0.4)),plus(focalAngle, (float) (arcLength*-0.4)),true,step));
        if(arcLength<300){
            lines.put("LeftCurve",addCurvedLine(plus(focalAngle, (float) (arcLength*-0.4)),plus(focalAngle,(float)-0.6*arcLength),false,step));
            lines.put("RightCurve",addCurvedLine(plus(focalAngle, (float) (arcLength*0.6)),plus(focalAngle, (float) (arcLength*0.4)),false,step));
        }else{
            lines.put("RemainingCurve",addCurvedLine(leftAngle,rightAngle,false,step));
        }

        int i=0;
         for(LatLng point:lines.get("LeftCurve").getPoints()){
            points.put("LeftCurve"+i,point);
            i++;
        }i=0;
        for(LatLng point:lines.get("Curve").getPoints()) {
            points.put("Curve"+i, point);
            i++;
        }i=0;
        for(LatLng point:lines.get("RightCurve").getPoints()) {
            points.put("RightCurve"+i, point);
            i++;
        }

        if(MainActivity.fireType.equals(FireType.DRILL))
            for(PolylineOptions line:lines.values())
                line.color(0x500000ff);

        //if(MainActivity.targetOrArea.equals(FireType.DRILL_RESUME)) insideBoundaries();
    }

    private void insideBoundaries() {
        float originalRight = MapsActivity.originalRight, originalLeft = MapsActivity.originalLeft;
        float farRight = plus(focalAngle,(float)0.6*arcLength), farLeft = plus(focalAngle,(float)-0.6*arcLength);
        if(originalRight - originalLeft > 0){
            if(farRight < originalRight &&  farLeft > originalLeft)
                for(PolylineOptions line : lines.values())
                    line.color(Color.GREEN);
        }else{
            if((farRight > 0 && farRight < originalRight) && (farLeft <360 && farLeft > originalLeft))
                for(PolylineOptions line : lines.values())
                    line.color(Color.GREEN);
        }
    }

    private float calculateFarthest() {
        float a = MainActivity.data.getLeftAngle();
        float b = MainActivity.data.getRightAngle();
        if(Math.abs(a - b) > 180)
            return (a + b)/2 + 180;
        else
            return (a+b)/2;
    }

    /**
     * Creates and adds the curved line to the polygon
     * @param left - starting point
     * @param right - ending point
     * @param solid - if the polyline is solid
     * @param lines - how many lines we want in curve
     * @return the polylineOption so system can check if all points are in
     */
    public PolylineOptions addCurvedLine(float left, float right,boolean solid, float lines, boolean f){
        PolylineOptions options = new PolylineOptions();
        //options.add(start);
        for(float i:points(left,right,lines,false))
            options.add(fullRangePoint(getFiringPoint(),MainActivity.height,i));
        //options.add(end);
        if(!solid){
            List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));
            options.pattern(pattern);}
        //mMap.addPolyline(options);
        return options;
    }

    //Returns list of points on curve
    public List<Float> points(float p1, float p2, float num, boolean f){
        List<Float> list = new ArrayList<>();
        float distance = Math.abs(p1-p2);
        if(distance > 180 ) distance = 360 - distance;
        if(Math.abs((p1+distance)%360-p2)<0.1)//abs(p1+distance)=p2
            for(float angle=p1,step=distance/num,i=0;i<=num;angle+=step,i++)
            {
                if(angle>=360) angle-=360;
                list.add(angle);
            }
        else
            for(float angle=p1,step=distance/num,i=0;i<=num;angle-=step,i++)
            {
                if(angle<=0) angle+=360;
                list.add(angle);
            }
        return list;
    }


    /**
     * Creates and adds the curved line to the polygon
     * @param left - starting point
     * @param right - ending point
     * @param solid - if the polyline is solid
     * @param lines - how many lines we want in curve
     * @return the polylineOption so system can check if all points are in
     */
    public PolylineOptions addCurvedLine(float right, float left,boolean solid, float lines){
        PolylineOptions options = new PolylineOptions();
        //options.add(start);
        for(float i:points(right,left,lines))//Points go from right to left, therefore forgoing to decide which one came first
            options.add(fullRangePoint(getFiringPoint(),MainActivity.height,i));
        //options.add(end);
        if(!solid){
            List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));
            options.pattern(pattern);}
        //mMap.addPolyline(options);
        return options;
    }

    /*
    public List<Float> points(float p1, float p2, float num){
        List<Float> list = new ArrayList<>();
        float distance = Math.abs(p1-p2);
        if(distance > 180 ) distance = 360 - distance;
        if(Math.abs((p1+distance)%360-p2)<0.1)//abs(p1+distance)=p2
            for(float angle=p1,step=distance/num,i=0;i<=num;angle+=step,i++)
            {
                if(angle>=360) angle-=360;
                list.add(angle);
            }
        else
            for(float angle=p1,step=distance/num,i=0;i<=num;angle-=step,i++)
            {
                if(angle<=0) angle+=360;
                list.add(angle);
            }
        return list;
    }*/

    public List<Float> points(float p1, float p2, float num){
        List<Float> list = new ArrayList<>();
        float distance = (p1-p2)%360;
        for(float angle = p1, step = distance/num,i=0; i<=num; angle-=step,i++) {
            if(angle<=0) angle+=360;
            list.add(angle);
            }
        return list;
    }

    /**Calculate the length on which the new arc rests
     * @param current - where you are
     * @param side - to where
     * @return the length of the radius of the arc
     */
    public double arcLength(LatLng current, LatLng side){
        return computeDistanceBetween(current,side);
    }

    /**Calculate the length on which the new arc rests
     * @param tempLength - Shorter length
     * @param angle - Left\Right angle
     * @param tempAngle - Smaller angle
     * @return the length of the radius of the arc
     */
    public double arcLength(double tempLength, float angle, float tempAngle){
        double h = MainActivity.height;
        angle = plus(angle,tempAngle);
        return (h-tempLength)/Math.sin(Math.toRadians(angle)) * Math.sin(Math.toRadians(180 - angle - Math.asin(Math.toRadians(tempLength / (h-tempLength) * Math.sin(Math.toRadians(angle))))));
    }

    /** Calculates the smaller angle inside the polygon
     * @param left angle
     * @param right angle
     * @param percentage - how small it is in comparison
     * @return the new angle
     */
    public float calculateSmallerAngle(float left, float right, float percentage){
        float distance = Math.abs(left-right);
        if(distance > 180 ) distance = 360 - distance;
        return distance*percentage;
    }

    /**Caller for shorter boundaries. Created to ease calling of method
     * @param tempLength - Shorter length
     * @param tempAngle - Smaller angle
     * @param angle - The angle - left or right
     * @param positive - Based upon which direction firing is going - left- positive(true), right- negative(negative).
     * @return the new boundary.
     */
    public LatLng callNewBoundary(double tempLength, float tempAngle, float angle , boolean positive){
        float multiply = 1;
        if (!positive) multiply = -1;
        return newBoundaries(MainActivity.gps.getLatLng(), tempLength, 1, tempAngle*multiply, angle);
    }

    /**Calculate the new left/right of the polygon based upon format of polygons
     * @param latLng - Users current location
     * @param tempLength - Temporary length, a portion of length, <length
     * @param length - Distance of ammo
     * @param tempAngle - Temporary angle, a portion of angle, <(left+right)2
     * @param angle - Original firing angle
     * @return new Latlng of the points of the firing
     */
    public LatLng newBoundaries(LatLng latLng, double tempLength, double length, float tempAngle, float angle){
        double lat,lon;

        lat = latLng.latitude + tempLength * Math.cos(Math.toRadians(angle)) + (length - tempLength) * Math.cos(Math.toRadians(plus(angle , tempAngle)));
        lon = latLng.longitude + tempLength * Math.sin(Math.toRadians(angle)) + (length - tempLength) * Math.sin(Math.toRadians(plus(angle , tempAngle)));

        return new LatLng(lat,lon);
    }

    /**
     * @param meters - meters
     * @return meters converted to angles
     */
    public double metersToDegrees(double meters){
        return (meters/6371009.0) * (180.0/Math.PI);
    }

    /**
     * Calculate sum in circle based upon angles
     * @param f1 - first
     * @param f2 - second
     * @return sum based upon angle based upon circle
     */
    public float plus(float f1,float f2){
        float angle = (f1+f2)%360;
        if(angle<0) angle+=360;
        return angle;
    }

    /**
     * Checks if two angles are equal. Due to float uneven proximity, it is compared against a relatively small number
     * @param f1 - first
     * @param f2 - second
     * @param epsilon - small number, in most cases ~<0.1
     * @return if they are equal
     */
    public boolean equals(float f1, float f2, float epsilon){return Math.abs(f1-f2)<epsilon;}

    public LatLng targetFunnelFiringPoint(LatLng location,LatLng target){
        float theta = (float) Math.toRadians(bearing(location,target));

        double x = location.latitude + MainActivity.height * Math.cos(theta);
        double y = location.longitude + MainActivity.height * Math.sin(theta);

        return new LatLng(x,y);
    }

    //Returns bearing towards other location
    private float bearing(LatLng location, LatLng target){
        //return (float) Math.atan2(-(location.latitude-target.latitude),(location.longitude-target.longitude));
        Location l1 = new Location(""),l2 = new Location("");

        l1.setLatitude(location.latitude);
        l1.setLongitude(location.longitude);

        l2.setLatitude(target.latitude);
        l2.setLongitude(target.longitude);

        return l1.bearingTo(l2);
    }

    public String getNote(){return note;}

    public void setNotes(String toString) {
        note = toString;
    }

    /**
     * In drill resume, sets it to more of a background to distinguish between polygons
     */
    public void setColorsToSaveInDrill() {
        int color = calculateNextColor();
        for(PolylineOptions line: lines.values())
            line.color(color);
    }

    private int calculateNextColor(){
        int color;
        try {
            color = Color.argb(230 - drillPolygonCounter * 5,128 + drillPolygonCounter * 5,drillPolygonCounter * 5,128 + drillPolygonCounter * 5);
        }catch (Exception e){
            drillPolygonCounter = 1;
            color = Color.argb(230 - drillPolygonCounter * 5,128 + drillPolygonCounter * 5,drillPolygonCounter * 5,128 + drillPolygonCounter * 5);
        }
        drillPolygonCounter++;
        return color;
    }

    /**
     * When polygon is created, shows user which is the new one
     */
    public void setColorToNew() {
        for(PolylineOptions line: lines.values())
            line.color(0x500ff0ff);
    }

    /**
     * Types of statuses, see setStatus for breakdown
     */
    enum Status {GREEN,YELLOW,RED}

}