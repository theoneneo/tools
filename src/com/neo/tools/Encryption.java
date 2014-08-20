package com.neo.tools;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


public class Encryption {
	
//	private void init() {
//		File load = new File("/mnt/sdcard/test.png");
//		File loaddest = new File("/mnt/sdcard/test_e.jpg");
//		File loadd = new File("/mnt/sdcard/test_d.jpg");
//		try {
//			encrImg(load, loaddest);
//			DecrImg(loaddest, loadd);
//			encrAssetsFile(fis, load);
//			fis.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
	
	public static void encrAssetsFile(InputStream fis, File dest, int key) throws Exception {
//		InputStream fis = getClass().getResourceAsStream("/assets/test/test.jpg");
		OutputStream fos = new FileOutputStream(dest);
		int read;
		while ((read = fis.read()) > -1) {
			fos.write(read ^ key);
		}
		fos.flush();
		fos.close();
		fis.close();
	}
	
	public static void encrFile(File src, File dest, int key) throws Exception {
		InputStream fis = new FileInputStream(src);
		OutputStream fos = new FileOutputStream(dest);
		int read;
		while ((read = fis.read()) > -1) {
			fos.write(read ^ key);
		}
		fos.flush();
		fos.close();
		fis.close();
	}
	
	public static void DecrFile(File src, File dest, int key) throws Exception {
		InputStream fis = new FileInputStream(src);
		OutputStream fos = new FileOutputStream(dest);
		int read;
		while ((read = fis.read()) > -1) {
			fos.write(read ^ key);
		}
		fos.flush();
		fos.close();
		fis.close();
	}
}
