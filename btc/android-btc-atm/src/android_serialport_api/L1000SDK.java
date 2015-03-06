/*
 * author:	Caeson Guan
 * 
 * verson:	5.1
 * 
 * date:	2014/08/25
 * 
 * brief:	SDK for L1000
 * 
 * change:	v2.0	add interface: onDispenseListener
 * 			v2.1	add function: dispense n pieces of notes
 * 			v3.0	add function: getSerialPort getiostream
 * 					comthread is terminated after dispensation
 * 			v3.1	delete Application.java
 * 					add function: getSerialPort .ets
 * 			v4.0	repair bugs
 * 			v4.1	dispense a bit amount of cash
 * 			V4.2	
 * 			v5.0	add interface: onNotesNearEnd
 * 					change the function of datahandler
 * 			v5.1	change max pieces of cash one time to 20
 * 
 */
package android_serialport_api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;


import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;
import android_serialport_api.SerialPort;

public class L1000SDK {

	// response
	byte ack = 0x06;
	byte nck = 0x15;
	byte rsppurge = 0x44;
	byte rspdispense = 0x45;
	byte rspstatus = 0x46;
	byte rspversion = 0x47;

	// status error code
	byte egood = 0x30;
	byte enmlstop = 0x31;

	byte[] temp = new byte[256]; 
	int tsize = 0; 
	
	// boolean success = true;

	ComThread mthread;
	int dspsnum;

	protected SerialPort mSerialPort = null;
	protected OutputStream mOutputStream = null;
	protected InputStream mInputStream = null;
	private onDispenseListener monDispenseListener = null;


    public void begin(int num) {            //开始出钞线程
        if(!init()) // 若初始化失败，则退出
            return;
        dspsnum = num;
        mthread = new ComThread();
        mthread.start();
    }


    public void end() {
        closeSerialPort();        //关闭串口
        mSerialPort = null;
        if (mthread != null && !mthread.isInterrupted())
            mthread.interrupt();
    }
	
	public interface onDispenseListener {
		
		//出钞完毕
		public void onDispenseFinished();
		
		//处理串口读到的数据
		public void onComRead(byte[] rxbuffer, int size);
		
		//初始化串口错误显示
		public void onError(Exception e);
		
		public void onNotesNearEnd();
	}

	public void setOnDispenseListener(onDispenseListener ondispenselistener) {
		monDispenseListener = ondispenselistener;
	}

	public L1000SDK() {
	}

	private boolean init() {
		String compath = "/dev/ttySAC1";
		int combaudrate = 9600;
		try {
			if (mSerialPort == null)
				mSerialPort = getSerialPort(compath, combaudrate); // 打开串口
			if (mOutputStream == null)
				mOutputStream = mSerialPort.getOutputStream(); // 打开输出流
			if (mInputStream == null)
				mInputStream = mSerialPort.getInputStream(); // 打开输入流
		} catch (SecurityException e) {
			monDispenseListener.onError(e);
			return false;
		} catch (IOException e) {
			monDispenseListener.onError(e);
			return false;
		} catch (InvalidParameterException e) {
			monDispenseListener.onError(e);
			return false;
		}

		return true;
	}

	public SerialPort getSerialPort(String path, int baudrate) throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Check parameters */
			if ( (path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, 0,0);
		}
		return mSerialPort;
	}
	

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
	
	/**
	 * 串口线程，串口输出命令控制出钞机，以及处理接收到的数据
	 * 
	 * 出钞完毕后线程结束
	 * 
	 */
	public class ComThread extends Thread {

		boolean vend = false;
		int moneycount = 0;
		byte[] readbuffer = new byte[256];
		int size = 0;

		@Override
		public void run() {
			try {
				Log.i("comthread", "try");
				l1000status();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				l1000status();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				l1000status();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				l1000purge();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				
				while (dspsnum > 20) {
					l1000dispense(20);
					super.sleep(1500);
					size = mInputStream.read(readbuffer);
					datahandler(readbuffer, size);
					super.sleep(500);
					l1000status();
					super.sleep(500);
					size = mInputStream.read(readbuffer);
					datahandler(readbuffer, size);
					super.sleep(500);
					l1000status();
					super.sleep(500);
					size = mInputStream.read(readbuffer);
					datahandler(readbuffer, size);
					super.sleep(500);
					l1000status();
					super.sleep(500);
					size = mInputStream.read(readbuffer);
					datahandler(readbuffer, size);
					super.sleep(500);
					l1000status();
					super.sleep(500);
					size = mInputStream.read(readbuffer);
					datahandler(readbuffer, size);
					super.sleep(500);
					l1000status();
					super.sleep(500);
					size = mInputStream.read(readbuffer);
					datahandler(readbuffer, size);
					super.sleep(500);
					dspsnum -= 20;
					Log.i("dispense", "20");
				}
				l1000dispense(dspsnum);
				super.sleep(1500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				l1000status();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				l1000status();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				l1000status();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				l1000status();
				super.sleep(500);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(500);
				Log.i("dispense", "1");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.i("comthread", "IOException");
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.i("comthread", "InterruptedException");
				e.printStackTrace();
			}
			monDispenseListener.onDispenseFinished();
			Log.i("comthread", "finished");
		}

	}
	
	//串口输出purge命令，清楚出钞机原有的数据
	public void l1000purge() throws IOException {
		byte[] sendbuffer = hexStringToBytes("06045002440311");
		mOutputStream.write(sendbuffer);
	}

	//串口输出status命令，查询出钞机的状态
	public void l1000status() throws IOException {
		byte[] sendbuffer = hexStringToBytes("06045002460313");
		mOutputStream.write(sendbuffer);
	}

	//串口输出version命令，查询出钞机的版本
	public void l1000version() throws IOException {
		byte[] sendbuffer = hexStringToBytes("06045002470312");
		mOutputStream.write(sendbuffer);
	}
	
	//串口输出dispense命令，出钞
	public void l1000dispense(int notenum) throws IOException {
		if (notenum >= 1 && notenum <= 20) {
			byte[] sendbuffer = new byte[9];
			sendbuffer[0] = ack;
			sendbuffer[1] = (byte) 0x04;
			sendbuffer[2] = (byte) 0x50;
			sendbuffer[3] = (byte) 0x02;
			sendbuffer[4] = (byte) 0x45;
			int notenumUnits = notenum % 10;
			int notenumTens = (notenum - notenumUnits) / 10;
			sendbuffer[5] = (byte) (notenumTens + 0x30);
			sendbuffer[6] = (byte) (notenumUnits + 0x30);
			sendbuffer[7] = (byte) 0x03;
			sendbuffer[8] = XOR(sendbuffer);
			mOutputStream.write(sendbuffer);
		}
	}

	//出钞命令最后一个校验byte的生成
	public byte XOR(byte[] buffer) {
		int ans = buffer[1];
		for (int i = 2; i < buffer.length - 1; i++) {
			ans ^= buffer[i];
		}
		return (byte) ans;
	}

	private void datahandler(byte[] buffer, int size) {
		temp = getMergeBytes(temp, tsize, buffer, size);
		tsize = tsize + size;
		while (true) {
			int num1 = findBytes(temp, (byte) 0x50);
			int num2 = findBytes(temp, (byte) 0x03);
			if (num1 > 0 && num2 > num1) {
				byte[] sendbuffer = new byte[num2 - num1 + 3];
				for (int i = num1 - 1; i < num2 + 2; i++) {
					sendbuffer[i - num1 + 1] = temp[i];
				}
				monDispenseListener.onComRead(sendbuffer, sendbuffer.length);
				temp = restBytes(temp, num2);
				tsize = temp.length;
				
				if (sendbuffer[3] == 0x45) {
					if (sendbuffer[9] == 0x31)
						monDispenseListener.onNotesNearEnd();
				}
				
			} else
				break;
		}
	}
	
	public int findBytes(byte[] buffer, byte dst) {
		int num = -1;
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] == dst) {
				num = i;
				break;
			}
		}
		return num;
	}
	
	
	public byte[] getMergeBytes(byte[] pByteA, int numA, byte[] pByteB, int numB) {

		byte[] b = new byte[numA + numB];
		for (int i = 0; i < numA; i++) {
			b[i] = pByteA[i];
		}
		for (int i = 0; i < numB; i++) {
			b[numA + i] = pByteB[i];
		}
		return b;
	}
	
	public byte[] restBytes(byte[] totalbytes, int num) {
		byte[] b = new byte[totalbytes.length - num - 2];
		for (int i = 0; i < totalbytes.length - num - 2; i++) {
			b[i] = totalbytes[i + num + 2];
		}
		return b;
	}
	
	/**
	 * Convert hex string to byte[]
	 * 
	 * @param hexString
	 *            the hex string
	 * @return byte[]
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;

	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
}
