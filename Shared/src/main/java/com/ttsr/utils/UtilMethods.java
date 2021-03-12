package com.ttsr.utils;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public final class UtilMethods {
    public static ArrayList<String> getFileList(File directory){
        System.out.println("getFileList dir: " + directory);
        File[] files = directory.listFiles();
        ArrayList<String> fileList = new ArrayList<>();
        ArrayList<String> filesTemp = new ArrayList<>();
        ArrayList<String> directoriesTemp = new ArrayList<>();

        fileList.add(".. \n");
        if(files != null){
            for (File file : files) {
                StringBuilder sb = new StringBuilder();
                sb.append(file.getName()).append(" ");
                if(file.isDirectory()){
                    sb.append(File.separator);
                    directoriesTemp.add(sb.toString());
                }else {
                    sb.append(file.length()).append(" bytes.");
                    filesTemp.add(sb.toString());
                }
                sb.setLength(0);
            }
            fileList.addAll(directoriesTemp);
            fileList.addAll(filesTemp);
        }
        return fileList;
    }
    public static Boolean isFile(File file){
        return file.isFile();
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
