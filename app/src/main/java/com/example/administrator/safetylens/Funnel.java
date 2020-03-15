package com.example.administrator.safetylens;

import android.graphics.Color;

import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.administrator.safetylens.MapUtils.MathUtil.SphericalUtil.computeDistanceBetween;

/**
 * Due to a need for a more dynamic funnels, this will be objected
 */

public class Funnel{

    LatLng firingPoint;//Bases for funnel
    Float leftAngle,rightAngle; //Angles, will be imported from Main
    //LatLng left,right,leftClose,rightClose;// Focal points of funnel
    //LatLng farthest, target; //Farthest and target point of funnel
    //PolylineOptions leftShort,rightShort,curve,leftOffset,rightOffset,rightArc,leftArc,leftLong,rightLong;
    Status status;
    String timestamp;
    //GoogleMap mMap;
    //Map<String,CustomPolygon> polygonList;
    //CustomPolygon currentPolygon;
    Map<String,LatLng> points;
    Map<String, PolylineOptions> lines;
    float angleTarget = 60;
    List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));

    /**
     * Constructor for Funnel,
     * @param FiringPoint - Where the funnel starts
     */
    public Funnel (LatLng FiringPoint){
        //mMap = Gmap;
        timestamp = MainActivity.timestamp;
        firingPoint = FiringPoint;
        leftAngle = MainActivity.data.leftAngle;
        rightAngle = MainActivity.data.rightAngle;
        //polygonList = customPolygonList;
        points = new HashMap<String,LatLng>();
        lines = new HashMap<String,PolylineOptions>();
        addBasicPoints(30);
        if(MainActivity.targetOrArea == FireType.AREA)
            addAreaPolygon(30);
        else
            addTargetPolygon(30);
    }

    /**
     * Creates a Polygon based upon the targeting option
     * @param step
     */
    private void addBasicPoints(float step) {
        points.put("FiringPoint",MainActivity.gps.getLatLng());
    }

    /**
     * Creates a Polygon based upon the targeting option
     * @param step
     */
    private void addTargetPolygon(float step) {
        float angle = MainActivity.data.getTargetAngle();
        points.put("Target",fullRangePoint(getFiringPoint(),MainActivity.targetDistance,angle));
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

        lines.put("LeftCurve",addCurvedLine(plus(angle,-36),plus(angle,-24),false,step));
        lines.put("Curve",addCurvedLine(plus(angle,-24),plus(angle,24),true,step));
        lines.put("RightCurve",addCurvedLine(plus(angle,24),plus(angle,36),false,step));

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
    private LatLng getCertainPoint(String key){return (LatLng)points.get(key);} //Return point based upon key value
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
     * Create all four focus points - location, left, farthest, right. Farthest is only used as reference, to be displayed on as part of radius
     * Create lines between them and then add curve.
     * After, validate if the area is ok to fire.
     */
    private void addAreaPolygon(float step){
        float focalAngle = calculateFarthest(); //The main angle the funnel is based upon
        float arcLength = Math.abs((rightAngle-leftAngle)%360); //Distance between Right and Left
        if(arcLength>180) arcLength-=180;

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

        lines.put("LeftCurve",addCurvedLine(plus(focalAngle,(float)-0.6*arcLength),plus(focalAngle, (float) (arcLength*-0.4)),false,step));
        lines.put("Curve",addCurvedLine(plus(focalAngle, (float) (arcLength*-0.4)),plus(focalAngle, (float) (arcLength*0.4)),true,step));
        lines.put("RightCurve",addCurvedLine(plus(focalAngle, (float) (arcLength*0.4)),plus(focalAngle, (float) (arcLength*0.6)),false,step));

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

    /*
    private void blah(float step) {
        left = callNewBoundary(z,c,MainActivity.data.leftAngle,true);//fullRangePoint(MainActivity.gps.getLatLng(),MainActivity.height,MainActivity.data.getLeftAngle());
        right = callNewBoundary(z,c,MainActivity.data.rightAngle,false);//fullRangePoint(MainActivity.gps.getLatLng(),MainActivity.height,MainActivity.data.getRightAngle());

        leftClose = fullRangePoint(MainActivity.gps.getLatLng(),z,MainActivity.data.getLeftAngle());
        rightClose = fullRangePoint(MainActivity.gps.getLatLng(),z,MainActivity.data.getRightAngle());

        farthest = fullRangePoint(MainActivity.gps.getLatLng(),MainActivity.height, calculateFarthest());

        List<LatLng> llList = new ArrayList<>();
        llList.add(left); llList.add(MainActivity.gps.getLatLng()); llList.add(right); llList.add(leftClose); llList.add(rightClose); //llList.add(farthest);
        ///////////
        leftShort = new PolylineOptions().add(MainActivity.gps.getLatLng(),leftClose).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap());
        rightShort = new PolylineOptions().add(MainActivity.gps.getLatLng(),rightClose).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap());
        leftLong = new PolylineOptions().add(leftClose,left).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap());
        rightLong = new PolylineOptions().add(rightClose,right).color(Color.BLACK).endCap(new RoundCap()).startCap(new RoundCap());
        mMap.addPolyline(leftShort); mMap.addPolyline(rightShort); mMap.addPolyline(leftLong); mMap.addPolyline(rightLong);
        curve = addCurvedLine(plus(MainActivity.data.leftAngle,-c/2),plus(MainActivity.data.rightAngle,c/2),true,step).startCap(new RoundCap()).endCap(new RoundCap());
        llList.addAll(curve.getPoints());
        //mMap.addMarker(new MarkerOptions().position(farthest));
        ///////////
        PolylineOptions[] offsets = addOffset(step);
        leftOffset = offsets[0];
        rightOffset = offsets[1];
        llList.addAll(leftOffset.getPoints()); llList.addAll(rightOffset.getPoints());llList.addAll(rightArc.getPoints());llList.addAll(leftArc.getPoints());
    }*/

    /**
     * @returns current firing area
     **/
    /*
    public CustomPolygon findCurrentArea() {
        for(CustomPolygon polygon:polygonList.values())
            if(polygon.isIn(firingPoint))
                return polygon;
            return null;
    }*/

    /**
     * Sets offsets and arcs for left and right, if they meet, it flips it to outer direction
     * Line1 is for the left & Line2 is for right
     * @param  - how many steps in arc
     * @return the left and right offset polyline
     */
    /*
    private PolylineOptions[] addOffset(float step){
        List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));
        float angle = Math.abs(MainActivity.data.leftAngle-MainActivity.data.rightAngle);
        if (angle>180)
            angle = 360 - angle;
        //float temp = MainActivity.data.leftAngle;
        //MainActivity.data.setLeftAngle(MainActivity.data.getRightAngle());
        //MainActivity.data.setRightAngle(temp);
        angle=angle/2;
        PolylineOptions line1 = new PolylineOptions(), line2 = new PolylineOptions();
        if(equals(plus(MainActivity.data.leftAngle,angle),plus(MainActivity.data.rightAngle,-angle),angle/step*5 )){
            line1.add(fullRangePoint(getFiringPoint(),MainActivity.height,plus(MainActivity.data.leftAngle,plus(-angle,c)))).add(getFiringPoint()).pattern(pattern);
            leftArc = addCurvedLine(plus(MainActivity.data.leftAngle,plus(-angle,c/2)),MainActivity.data.leftAngle,false,step);
            line2.add(fullRangePoint(getFiringPoint(),MainActivity.height,plus(MainActivity.data.rightAngle,plus(angle,-c)))).add(getFiringPoint()).pattern(pattern);
            rightArc = addCurvedLine(plus(MainActivity.data.rightAngle,plus(angle,-c/2)),MainActivity.data.rightAngle,false,step);
        }else {
            line1.add(fullRangePoint(getFiringPoint(),MainActivity.height,plus(MainActivity.data.leftAngle,plus(angle,-c)))).add(getFiringPoint()).pattern(pattern);
            leftArc = addCurvedLine(plus(MainActivity.data.leftAngle,plus(angle,-c/2)),MainActivity.data.leftAngle,false,step);
            line2.add(fullRangePoint(getFiringPoint(),MainActivity.height,plus(MainActivity.data.rightAngle,plus(-angle,c)))).add(getFiringPoint()).pattern(pattern);
            rightArc = addCurvedLine(plus(MainActivity.data.rightAngle,plus(-angle,c/2)),MainActivity.data.rightAngle,false,step);
        }
        mMap.addPolyline(line1);mMap.addPolyline(line2);
        return new PolylineOptions[]{line1,line2};
    }*/

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
    public PolylineOptions addCurvedLine(float left, float right,boolean solid, float lines){
        PolylineOptions options = new PolylineOptions();
        //options.add(start);
        for(float i:points(left,right,lines))
            options.add(fullRangePoint(getFiringPoint(),MainActivity.height,i));
        //options.add(end);
        if(!solid){
            List<PatternItem> pattern = Arrays.asList(new Dash(30), new Gap(20));
            options.pattern(pattern);}
        //mMap.addPolyline(options);
        return options;
    }

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


enum Status {GREEN,YELLOW,RED}

}