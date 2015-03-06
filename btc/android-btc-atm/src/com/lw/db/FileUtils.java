package com.lw.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;

public class FileUtils {
    
    public static void write(String filename,String content){
        File sdCardDir = Environment.getExternalStorageDirectory();
        File saveFile = new File(sdCardDir,filename);
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(saveFile);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    
    public static void append(String filename,String content){
        File sdCardDir = Environment.getExternalStorageDirectory();
        File saveFile = new File(sdCardDir,filename);
        if(!saveFile.exists()){
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(saveFile,true);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}