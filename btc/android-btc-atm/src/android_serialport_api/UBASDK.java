/*
 * author:	Caeson Guan
 * 
 * verson:	3.2
 * 
 * date:	2014/10/01
 * 
 * brief:	SDK for UBA	of JPY
 * 
 * change:	v2.0	add interface 
 * 					add class ComThread, add fuction to open COM and get iostream
 * 					delete class Application
 * 					delete choosing serialport
 * 			v2.1	datahandler
 * 			v3.0	libserial_port.so is changed
 * 			v3.1	inhibit after end
 * 			v3.2	changes interface
 * 
 * 
 */
package android_serialport_api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import com.mybitcoin.wallet.ui.NewReceiverCoinActivity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android_serialport_api.SerialPort;

public class UBASDK {

	// power up status
	byte powerup = 0x40;
	byte powerupacpt = 0x41;
	byte powerupstc = 0x42;

	// response to operation command
	byte ack = 0x50;
	byte invalidcmd = 0x4b;

	// status
	byte idling = 0x11;
	byte accepting = 0x12;
	byte escrow = 0x13;
	byte stacking = 0x14;
	byte vendvalid = 0x15;
	byte stacked = 0x16;
	byte rejecting = 0x17;
	byte returning = 0x18;
	byte holding = 0x19;
	byte inhibit = 0x1a;
	byte initialize = 0x1b;

	// error status
	byte stackerfull = 0x43;
	byte stackeropen = 0x44;
	byte jaminacpt = 0x45;
	byte jaminstck = 0x46;
	byte pause = 0x47;
	byte cheated = 0x48;
	byte failure = 0x49;
	byte commerror = 0x4a;

	// poll request
	byte enq = 0x05;

	byte[] temp = new byte[256]; // buffer锟斤拷锟剿猴拷锟斤拷锟斤拷锟斤拷锟芥当前buffer锟斤拷锟斤拷锟斤拷锟斤拷一锟斤拷buffer锟较诧拷
	int tsize = 0; // buffer锟斤拷锟剿ｏ拷锟斤拷前锟斤拷锟斤拷锟斤拷buffer锟斤拷size
	// int flag = 0; // 1锟斤拷示buffer锟斤拷锟斤拷
    public static boolean stopflag = false;
	
	ComThread mthread;
	protected SerialPort mSerialPort = null;
	protected OutputStream mOutputStream = null;
	protected InputStream mInputStream = null;
	private onUBAListener monUBAListener = null;

	public interface onUBAListener {

		// 锟斤拷锟斤拷锟诫钞锟斤拷锟�
		public void onCash(int num);
		
		//锟诫钞锟竭程斤拷锟斤拷
		public void onFinished();

		// 锟斤拷锟�?锟节讹拷锟斤拷锟斤拷锟斤拷锟�
		public void onComRead(byte[] rxbuffer, int size);

		// 锟斤拷始锟斤拷锟斤拷锟节达拷锟斤拷锟斤拷示
		public void onError(Exception e);
	}

	public void setOnUBAListener(onUBAListener onUBAListener) {
		this.monUBAListener = onUBAListener;
	}

	public UBASDK() {}

	public SerialPort getSerialPort(String path, int baudrate)
			throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Check parameters */
			if ((path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, 0 , 1, 2);//停止位1  偶校锟斤拷
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	public void mystart() { // 锟斤拷始锟诫钞锟竭筹拷
        if(!init()) // 锟斤拷锟斤拷始锟斤拷失锟杰ｏ拷锟斤拷锟剿筹拷
            return;
		mthread = new ComThread();
		mthread.start();
	}

	public void myfinish() {
        closeSerialPort();        //锟截闭达拷锟斤拷
        mSerialPort = null;
        if (mthread != null && !mthread.isInterrupted())
            mthread.interrupt();
	}

	private boolean init() {
		//String compath = "/dev/ttySAC1";
		String compath = "/dev/ttySAC3";
		int combaudrate = 9600;
		try {
			if (mSerialPort == null)
				mSerialPort = getSerialPort(compath, combaudrate); // 锟津开达拷锟斤拷
			if (mOutputStream == null)
				mOutputStream = mSerialPort.getOutputStream(); // 锟斤拷锟斤拷锟斤拷锟�
			if (mInputStream == null)
				mInputStream = mSerialPort.getInputStream(); // 锟斤拷锟斤拷锟斤拷锟斤拷
		} catch (SecurityException e) {
			monUBAListener.onError(e);
			return false;
		} catch (IOException e) {
			monUBAListener.onError(e);
			return false;
		} catch (InvalidParameterException e) {
			monUBAListener.onError(e);
			return false;
		}

		return true;
	}
	
	public class ComThread extends Thread {
		boolean vend = false;
		int moneycount = 0;
		int size = 0;
		byte[] readbuffer = new byte[256];

		@Override
		public void run() {
			try {
				Log.i("ComThread", "start");
				//mOutputStream.flush();
				//mInputStream.read();
				ubaStatusRequest();
				super.sleep(100);
				size = mInputStream.read(readbuffer);

				
				Log.i("ComThread", readbuffer.toString());
				
				
				datahandler(readbuffer, size);
				super.sleep(100);
				ubaReset();
				super.sleep(100);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(100);
				ubaSetEnable();
				super.sleep(100);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(100);
				ubaSetSecurity();
				super.sleep(100);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(100);
				ubaSetOpFunction();
				super.sleep(100);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(100);
				ubaSetInhibit(false);
				super.sleep(100);
				size = mInputStream.read(readbuffer);
				datahandler(readbuffer, size);
				super.sleep(100);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			while (stopflag) {
				try {
					Log.i("ComThread", "polling");
					ubaStatusRequest();
					super.sleep(100);
					// byte[] readbuffer = new byte[256];
					size = mInputStream.read(readbuffer);
					datahandler(readbuffer, size);
					super.sleep(100);
					if (readbuffer[2] == escrow) {
						int tempmoneycount = moneycount;
						if (readbuffer[3] == 0x64){
							tempmoneycount += 1000;
						}
						if (readbuffer[3] == 0x65){
							tempmoneycount += 2000;
						}
						if (readbuffer[3] == 0x66){
							tempmoneycount += 5000;
						}
						if (readbuffer[3] == 0x67){
							tempmoneycount += 10000;
						}
						ubaStack1();
						super.sleep(100);
						while (!vend) {
							ubaStatusRequest();
							super.sleep(100);
							size = mInputStream.read(readbuffer);
							datahandler(readbuffer, size);
							super.sleep(100);
							if (readbuffer[2] == 0x15) {
								ubaAck();
								super.sleep(100);
								vend = true;
								moneycount = tempmoneycount;
								monUBAListener.onCash(moneycount);
							}
						}
						vend = false;
					}
					super.sleep(100);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			try {
				ubaSetInhibit(true);			//锟结不锟斤拷锟斤拷锟矫伙拷锟斤拷锟饺ワ拷锟絘ctivity锟酵斤拷锟斤拷锟剿ｏ拷
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("ComThread", "finished");
			monUBAListener.onFinished();
		}

	}

	private void datahandler(byte[] buffer, int size) {
		try{
		temp = getMergeBytes(temp, tsize, buffer, size);
		tsize = tsize + size;
		while (tsize >= 2) { 		//锟斤拷锟斤拷buffer[1]
			if (tsize >= temp[1]) {// fc锟斤拷头锟斤拷一锟斤拷锟斤拷锟斤拷锟斤拷荩锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷
				monUBAListener.onComRead(temp, temp[1]);
				tsize = tsize - temp[1];
				temp = restBytes(temp);
			}
			else break;
		}
		}catch(Exception e){
		}
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

	public byte[] restBytes(byte[] totalbytes) {
		int n = totalbytes[1];
		byte[] b = new byte[totalbytes.length - n];
		for (int i = 0; i < totalbytes.length - n; i++) {
			b[i] = totalbytes[i + n];
		}
		return b;
	}

	public void ubaAck() throws IOException {
		byte[] sendbuffer = hexStringToBytes("FC0550AA05");
		mOutputStream.write(sendbuffer);
	}

	public void ubaReset() throws IOException {
		byte[] sendbuffer = hexStringToBytes("FC05402B15");
		mOutputStream.write(sendbuffer);
	}

	public void ubaStatusRequest() throws IOException {
		byte[] sendbuffer = hexStringToBytes("FC05112756");
		mOutputStream.write(sendbuffer);
	}

	public void ubaSetEnable() throws IOException { // must be sent?
		byte[] sendbuffer = hexStringToBytes("FC07C000002DB5");
		mOutputStream.write(sendbuffer);
	}

	public void ubaSetInhibit(boolean flag) throws IOException { // true->inhibit
		byte[] sendbuffer;
		if (flag)
			sendbuffer = hexStringToBytes("FC06C3018DC7"); // inhibit
		else
			sendbuffer = hexStringToBytes("FC06C30004D6");

		mOutputStream.write(sendbuffer);
	}

	public void ubaStack1() throws IOException {
		byte[] sendbuffer = hexStringToBytes("FC0541A204");
		mOutputStream.write(sendbuffer);
	}

	public void ubaSetSecurity() throws IOException {
		byte[] sendbuffer = hexStringToBytes("FC07C10000F1EF");
		mOutputStream.write(sendbuffer);
	}

	public void ubaSetOpFunction() throws IOException {
		byte[] sendbuffer = hexStringToBytes("FC07C50000908C");
		mOutputStream.write(sendbuffer);
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
