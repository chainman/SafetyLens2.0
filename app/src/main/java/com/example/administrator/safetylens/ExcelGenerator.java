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


    private double toMercatorLon(double lon)
    {
        return lon * 20037508.34 / 180;
    }

    private double toMercatorLat(double lat)
    {
        return Math.log(Math.tan((90 + lat) * Math.PI / 360)) / (Math.PI / 180)* 20037508.34 / 180;
    }
}
