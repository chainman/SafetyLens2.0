package com.example.administrator.safetylens;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generates the excel sheet to write it out
 */
class ExcelGenerator {

    private String directory;
    File file;
    private FileOutputStream fos = null;
    private HSSFWorkbook workbook;

    /***
     * Generates new Excel spread sheet
     * @param left - left boundary
     * @param right - right boundary
     */
    @SuppressLint("SimpleDateFormat")
    ExcelGenerator(LatLng location, LatLng left, LatLng right){
        directory = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+MainActivity.timestamp;
        file = new File(directory, "Data"+".xls");
        workbook = new HSSFWorkbook();

        final HSSFSheet sheet = workbook.createSheet("קורדינטות");
        final HSSFRow row0=sheet.createRow(0), row1=sheet.createRow(1),row2 = sheet.createRow(2);

        row0.createCell((short) 0).setCellValue("סוג"); //Type of ammo
        row1.createCell((short) 0).setCellValue(MainActivity.data.type);

        //Make sure lat and lon are not mixed up
        row0.createCell((short) 1).setCellValue("מיקום"); //Location
        row1.createCell((short) 1).setCellValue(toMercatorLat(location.latitude));
        row2.createCell((short) 1).setCellValue(toMercatorLon(location.longitude));

        row0.createCell((short) 2).setCellValue("שמאל"); //Left
        row1.createCell((short) 2).setCellValue(toMercatorLat(left.latitude));
        row2.createCell((short) 2).setCellValue(toMercatorLon(left.longitude));

        row0.createCell((short) 3).setCellValue("ימין"); //Right
        row1.createCell((short) 3).setCellValue(toMercatorLat(right.latitude));
        row2.createCell((short) 3).setCellValue(toMercatorLon(right.longitude));

        row0.createCell((short) 4).setCellValue("הערות"); //Notes
        row1.createCell((short) 4).setCellValue(MainActivity.data.notes);

        try { //Write out file
            fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            fos = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    ExcelGenerator(LatLng location, LatLng target){
        directory = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+MainActivity.timestamp;
        file = new File(directory, "Data"+".xls");
        workbook = new HSSFWorkbook();

        final HSSFSheet sheet = workbook.createSheet("קורדינטות");
        final HSSFRow row0=sheet.createRow(0), row1=sheet.createRow(1),row2 = sheet.createRow(2);

        row0.createCell((short) 0).setCellValue("סוג"); //Type of ammo
        row1.createCell((short) 0).setCellValue(MainActivity.data.type);

        //Make sure lat and lon are not mixed up
        row0.createCell((short) 1).setCellValue("מיקום"); //Location
        row1.createCell((short) 1).setCellValue(toMercatorLat(location.latitude));
        row2.createCell((short) 1).setCellValue(toMercatorLon(location.longitude));

        row0.createCell((short) 2).setCellValue("מטרה"); //Left
        row1.createCell((short) 2).setCellValue(toMercatorLat(target.latitude));
        row2.createCell((short) 2).setCellValue(toMercatorLon(target.longitude));

        row0.createCell((short) 3).setCellValue("הערות"); //Notes
        row1.createCell((short) 3).setCellValue(MainActivity.data.notes);

        try { //Write out file
            fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            fos = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints all the funnels in one excel file
     * @param funnels
     */
    ExcelGenerator(List<Funnel> funnels){
        directory = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+MainActivity.timestamp;
        file = new File(directory, "Data"+".xls");
        workbook = new HSSFWorkbook();

        final HSSFSheet sheet = workbook.createSheet("קורדינטות");
        final HSSFRow row0=sheet.createRow(0), row1=sheet.createRow(1);
        HSSFRow firstRow,secondRow;
        int rowCount = 1;

        boolean target = MainActivity.targetVisible;

        row0.createCell((short) 0).setCellValue("סוג"); //Type of ammo
        row1.createCell((short) 0).setCellValue(MainActivity.data.type);
        row0.createCell((short) 1).setCellValue("מיקום"); //Location
        if(target){//This means motion or target
            row0.createCell((short) 2).setCellValue("מטרה"); //Target
            row0.createCell((short) 3).setCellValue("הערות"); //Notes
        }else{//Drill or Area
            row0.createCell((short) 2).setCellValue("שמאל"); //Left
            row0.createCell((short) 3).setCellValue("ימין"); //Right
            row0.createCell((short) 4).setCellValue("הערות"); //Notes
        }

        for(Funnel funnel:funnels){
            firstRow = sheet.createRow(rowCount);
            secondRow = sheet.createRow(rowCount+1);

            firstRow.createCell((short) 1).setCellValue(toMercatorLat(funnel.points.get("FiringPoint").latitude));
            secondRow.createCell((short) 1).setCellValue(toMercatorLon(funnel.points.get("FiringPoint").longitude));
            if(target){//Checks if it needs target or left and right
                firstRow.createCell((short) 2).setCellValue(toMercatorLat(funnel.points.get("Target").latitude));
                secondRow.createCell((short) 2).setCellValue(toMercatorLon(funnel.points.get("Target").longitude));
                firstRow.createCell((short) 3).setCellValue(funnel.getNote());
            }else{
                firstRow.createCell((short) 2).setCellValue(toMercatorLat(funnel.points.get("Left").latitude));
                secondRow.createCell((short) 2).setCellValue(toMercatorLon(funnel.points.get("Left").longitude));
                firstRow.createCell((short) 3).setCellValue(toMercatorLat(funnel.points.get("Right").latitude));
                secondRow.createCell((short) 3).setCellValue(toMercatorLon(funnel.points.get("Right").longitude));
                firstRow.createCell((short) 4).setCellValue(funnel.getNote());
            }
            rowCount += 3;
        }

        try { //Write out file
            fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            fos = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    HSSFSheet sheets;
    int rowCounter;

    /**
     * Created for drill, adds each new funnel to the sheet
     */
    public ExcelGenerator(){
        directory = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+MainActivity.name+File.separator+MainActivity.timestamp;
        file = new File(directory, "Data"+".xls");
        workbook = new HSSFWorkbook();

        sheets = workbook.createSheet("קורדינטות");
        final HSSFRow row0=sheets.createRow(0);

        row0.createCell((short) 0).setCellValue("סוג"); //Type of ammo
        //row1.createCell((short) 0).setCellValue(MainActivity.data.type);
        row0.createCell((short) 1).setCellValue("מיקום"); //Location
        row0.createCell((short) 2).setCellValue("שמאל"); //Left
        row0.createCell((short) 3).setCellValue("ימין"); //Right
        row0.createCell((short) 4).setCellValue("הערות"); //Notes

        rowCounter = 1;
    }

    public void addFunnel(Funnel funnel){
        HSSFRow firstRow = sheets.createRow(rowCounter),secondRow = sheets.createRow(rowCounter+1);

        firstRow.createCell((short) 0).setCellValue(MainActivity.data.getType());
        firstRow.createCell((short) 1).setCellValue(toMercatorLat(funnel.firingPoint.latitude));
        secondRow.createCell((short) 1).setCellValue(toMercatorLon(funnel.firingPoint.longitude));
        firstRow.createCell((short) 2).setCellValue(toMercatorLat(funnel.getCertainPoint("Left").latitude));
        secondRow.createCell((short) 2).setCellValue(toMercatorLon(funnel.getCertainPoint("Left").longitude));
        firstRow.createCell((short) 3).setCellValue(toMercatorLat(funnel.getCertainPoint("Right").latitude));
        secondRow.createCell((short) 3).setCellValue(toMercatorLon(funnel.getCertainPoint("Right").longitude));
        firstRow.createCell((short) 4).setCellValue(funnel.getNote());

        rowCounter += 3;
    }

    public void printFile(){
        try { //Write out file
            fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            fos = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Changes the lat and lon to Mercator coordinates
     */
    private double toMercatorLon(double lon)
    {
        return lon * 20037508.34 / 180;
    }

    private double toMercatorLat(double lat)
    {
        return Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180)* 20037508.34 / 180;
    }
}
