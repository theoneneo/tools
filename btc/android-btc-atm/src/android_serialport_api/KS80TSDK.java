/**
 * ATTENTION!! This version is designed with English!
 */

/*
 * author:	Caeson Guan
 * 
 * verson:	3.3
 * 
 * date:	2014/10/15
 * 
 * brief:	SDK for KS80T, initialize the printer and print QR code	
 * 
 * changes:	v2.0	add interface: onPrintListener
 * 					add function: getSerialPort getiostream
 * 					comthread is terminated after print
 * 					delete Application.java
 * 					add fuction: getSerialPort .ets
 * 			v2.1	add function for RMB QRcode
 * 			v3.0	repair bugs
 * 			v3.1	add comthread
 * 			v3.2	cancel ksCut function
 * 			v3.3	add param transresult
 * 			v4.0	*change printout, add English Version
 * 
 */

package android_serialport_api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android_serialport_api.SerialPort;

public class KS80TSDK {

	protected SerialPort mSerialPort = null;
	protected OutputStream mOutputStream = null;
	protected InputStream mInputStream = null;
	private onPrintListener monPrintListener = null;

	String address = null;
	String bitcoinAmount = null;
	String coinAmount = null;
	String tele = null;

	String strAddress = null;
	String strPrivateKey = null;
	Bitmap addressQRCodeBitmap = null;
	Bitmap pkAddressQRCodeBitmap = null;

	String rmbamount = null;
	String QRstring = null;
	Bitmap CashQR = null;
	String btcamount = null;

	boolean result;

	boolean flagTransInvoice = false;
	boolean flagPaperWallet = false;
	boolean flagCashQRSlip = false;
	boolean flagTransactionSlip = false;
	boolean flagCashOutSlip = false;

	ComThread mthread;

	public interface onPrintListener {

		// when printing is finished
		public void onFinished();

		// to handle the data from printer
		public void onComRead(byte[] rxbuffer, int size);

		// show errors when open serialport and iostream
		public void onError(Exception e);
	}

	public void setOnPrintListener(onPrintListener onprintlistener) {
		monPrintListener = onprintlistener;
	}

	public KS80TSDK() {

	}

	public void begin() { // 开始打印线程
		if (!init()) // 若初始化失败，则退出
			return;
		mthread = new ComThread();
		mthread.start();
	}

	private boolean init() {


		// 串口号，波特率，很重要请注意！
//		String compath = "/dev/ttyUSB0";
		 String compath = "/dev/ttySAC2";
		int combaudrate = 19200;
		try {
			if (mSerialPort == null)
				mSerialPort = getSerialPort(compath, combaudrate); // 打开串口
			if (mOutputStream == null)
				mOutputStream = mSerialPort.getOutputStream(); // 打开输出流
			if (mInputStream == null)
				mInputStream = mSerialPort.getInputStream(); // 打开输入流
		} catch (SecurityException e) {
			monPrintListener.onError(e);
			return false;
		} catch (IOException e) {
			monPrintListener.onError(e);
			return false;
		} catch (InvalidParameterException e) {
			monPrintListener.onError(e);
			return false;
		}

		return true;
	}

	public void end() {
		closeSerialPort();
		mSerialPort = null;
		mOutputStream = null;
		mInputStream = null;
		if (mthread != null && !mthread.isInterrupted())
			mthread.interrupt();
	}

	public SerialPort getSerialPort(String path, int baudrate)
			throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Check parameters */
			if ((path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, 0, 1, 0);
		}
		return mSerialPort;
	}

	public void closeSerialPort() { // close serial port
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	private class ComThread extends Thread {
		@Override
		public void run() {

			// 打印手机钱包或者纸钱包
			if (flagTransInvoice) {
				Log.i("printTransInvoice", "start");
				try {
					ksClearCNmode();
					ksCenter();
					ksText("BITOCEAN");
					ksLF();
					ksLF();
					ksText("ATM CUSTOMER ADVICE");
					ksLF();
					ksLF();
					ksLeft();
					long sysTime = System.currentTimeMillis();
					String sysTimerStr = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(sysTime);
					ksText("Date and time:");
					ksText(sysTimerStr);
					ksLF();
					ksLF();
					ksText("Transaction type: Buy bitcoin");
					ksLF();
					ksLF();

					if (flagPaperWallet) { // 纸钱包？
						ksText("Transaction mode: Paper Wallet");// 纸钱包
						ksLF();
						ksLF();
					} else {
						ksText("Transaction mode: Mobile Phone Wallet");// 手机钱包
						ksLF();
						ksLF();
					}

					if (result == true) {
						ksText("Bitcoins amount:");
						ksText(bitcoinAmount);
						ksLF();
						ksLF();
						ksText("Cash amount:");
						ksText(coinAmount);
						ksText(" JPY");
						ksLF();
						ksLF();

						if (flagPaperWallet) { // if纸钱包 then打印公钥私钥二维码
							Log.i("printPaperWalletQRBitmap", "start");
							ksSetCNmode();
							ksCNdef();
							ksLeft();
							ksText("Public key:");
							ksText(strAddress);
							ksLF();
							ksClearCNmode();
							ksCenter();
							ksQRdef(addressQRCodeBitmap);
							ksQRprt();
							ksQRclr();
							ksLF();
							ksSetCNmode();
							ksCNdef();
							ksLeft();
							ksText("Private key:");
							ksText(strPrivateKey);
							ksLF();
							ksClearCNmode();
							ksCenter();
							ksQRdef(pkAddressQRCodeBitmap);
							ksQRprt();
							ksQRclr();
							ksLF();
							ksLF();
							Log.i("printPaperWalletQRBitmap", "finished");
						}

					} else {
						ksText("Transaction Failed");
						ksLF();
						ksLF();
						ksText("Please contact customer service immediately");
						ksLF();
						ksLF();
					}
					ksText("Customer service hotline: ");
					ksText("03-6666-9037");
					ksLF();
					ksLF();
					ksText("Welcome to use again");
					ksLF();
					ksLF();
					ksLF();
					ksLF();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				flagTransInvoice = false;
				flagPaperWallet = false;
				Log.i("printTransInvoice", "finished");
			}

			// 打印提款二维码凭条
			if (flagTransactionSlip) {
				try {
					Log.i("printCashQR", "start");
					ksClearCNmode();
					ksCenter();
					ksText("BITOCEAN");
					ksLF();
					ksLF();
					ksText("ATM CUSTOMER ADVICE");
					ksLF();
					ksLF();
					ksLeft();
					long sysTime = System.currentTimeMillis();
					String sysTimerStr = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(sysTime);
					ksText("Date and time:");
					ksText(sysTimerStr);
					ksLF();
					ksLF();
					ksText("Transaction type: Sell bitcoin ");
					ksLF();
					ksLF();
					if (result == true) {
						ksText("Need acknowledgement (about 15 minutes)");
						ksLF();
						ksLF();
						ksText("Bitcoins amount:");
//						if(btcamount != null){
//						String amount = btcamount.substring(4);
//						amount=amount+" mBTC";
//						ksText(amount);
//						}
						
						ksText(btcamount + " BTC");
						ksLF();
						ksLF();
						ksText("Cash amount:");
						ksText(rmbamount);
						ksText(" JPY");
						ksLF();
						ksLF();
						ksText("Drawing QR code:");
						ksText(QRstring);
						ksLF();
						ksLF();
						ksClearCNmode();
						ksQRdef(CashQR);
						ksQRprt();
						ksQRclr();
						ksLF();
						ksLF();
					} else {
						ksText("Transaction Failed");
						ksLF();
						ksLF();
						ksText("Please contact customer service immediately");
						ksLF();
						ksLF();
					}
					ksText("Customer service hotline: ");
					ksText("03-6666-9037");
					ksLF();
					ksLF();
					ksText("Welcome to use again");
					ksLF();
					ksLF();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				flagTransactionSlip = false;
				Log.i("printCashQRSlip", "finished");
			}

			// 打印人民币提款凭条
			if (flagCashOutSlip) {
				try {
					Log.i("printCashOutSlip", "start");
					ksClearCNmode();
					ksCenter();
					ksText("BITOCEAN");
					ksLF();
					ksLF();
					ksText("ATM CUSTOMER ADVICE");
					ksLF();
					ksLF();
					ksLeft();
					long sysTime = System.currentTimeMillis();
					String sysTimerStr = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(sysTime);
					ksText("Date and time:");
					ksText(sysTimerStr);
					ksLF();
					ksLF();
					ksText("Transaction type: Sell bitcoin ");
					ksLF();
					ksLF();
					if (result == true) {
//						ksCNprint("卖出比特币数量:");
//						ksText(btcamount);
//						ksLF();
//						ksLF();
						ksText("Cash amount:");
						ksText(rmbamount);
						ksText(" JPY");
						ksLF();
						ksLF();
					} else {
						ksText("Transaction Failed");
						ksLF();
						ksLF();
						ksText("Please contact customer service immediately");
						ksLF();
						ksLF();
					}
					ksText("Customer service hotline: ");
					ksText("03-6666-9037");
					ksLF();
					ksLF();
					ksText("Welcome to use again");
					ksLF();
					ksLF();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				flagCashOutSlip = false;
				Log.i("printCashOutSlip", "finished");
			}

			// cut paper
			try {
				ksLF();
				ksLF();
				ksLF();
				ksLF();
				ksLF();
				ksLF();
				ksLF();
				ksLF();
				ksLF();
				ksLF();
				ksCut();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			monPrintListener.onFinished();
		}
	}

	// 打印手机钱包/纸钱包——买入
	public void printTransInvoice(String address, String bitcoinAmount,
			String coinAmount, String tele, boolean result) {
		this.address = address;
		this.bitcoinAmount = bitcoinAmount;
		this.coinAmount = coinAmount;
		this.tele = tele;
		this.result = result;
		this.flagTransInvoice = true;

	}

	// 打印纸钱包二维码——买入
	public void printAddressAndPrivateKeyQRImage(String strAddress,
			String strPrivateKey, String tele, Bitmap addressQRCodeBitmap,
			Bitmap pkAddressQRCodeBitmap) {
		this.addressQRCodeBitmap = myResample(addressQRCodeBitmap);
		this.pkAddressQRCodeBitmap = myResample(pkAddressQRCodeBitmap);
		this.flagPaperWallet = true;
		this.strAddress = strAddress;
		this.strPrivateKey = strPrivateKey;
		this.tele = tele;
	}

	// 打印提款二维码凭条 ——卖出
	public void printCashQRSlip(String rmbamount, String QRstring,
			Bitmap CashQR, String btcamount) {
		this.CashQR = myResample(CashQR);
		this.rmbamount = rmbamount;
		this.QRstring = QRstring;
		this.btcamount = btcamount;
	}

	// 打印提款二维码凭条的交易结果 ——卖出
	public void printTransactionSlip(String rmbamount, String btcamount,
			boolean result) {
		this.result = result;
		this.flagTransactionSlip = true;
	}

	// 打印人民币提款凭条设置
	public void printCashOutSlip(String rmbamount, boolean result) {
		this.rmbamount = rmbamount;
//		this.btcamount = btcamount;
		this.result = result;
		this.flagCashOutSlip = true;
	}

	// 调整位图大小为256*256
	private Bitmap myResample(Bitmap bitmap) {
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();
		if (height != 256 || width != 256) {
			int newheight = 256;
			int newwidth = 256;
			float heightScale = ((float) newheight) / height;
			float widthScale = ((float) newwidth) / width;
			Matrix matrix = new Matrix();
			matrix.postScale(heightScale, widthScale);
			Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, height, width,
					matrix, true);
			Log.i("myResample", Integer.toString(newbitmap.getHeight()));
			Log.i("myResample", Integer.toString(newbitmap.getWidth()));
			return newbitmap;
		} else
			return bitmap;
	}

	/*
	 * basic function
	 */

	// new line
	public void ksLF() throws IOException {
		mOutputStream.write((byte) 0x0A);
	}

	// set the text "center"
	public void ksCenter() throws IOException {
		mOutputStream.write(hexStringToBytes("1B6101"));
	}

	// set the text "left"
	public void ksLeft() throws IOException {
		mOutputStream.write(hexStringToBytes("1B6100"));
	}

	// print text
	public void ksText(String string) throws IOException {
		mOutputStream.write(string.getBytes());
	}

	// cut paper
	public void ksCut() throws IOException {
		mOutputStream.write(hexStringToBytes("1D5601"));
	}

	/*
	 * Chinese
	 */

	// set Chinese mode
	public void ksSetCNmode() throws IOException {
		mOutputStream.write(hexStringToBytes("1C26"));
	}

	// clear Chinese mode
	public void ksClearCNmode() throws IOException {
		mOutputStream.write(hexStringToBytes("1C2E"));
	}

	// define Chinese characters
	public void ksCNdef() throws IOException {
		mOutputStream.write(hexStringToBytes("1C2100"));
	}

	// print Chinese
	public void ksCNprint(String string) throws IOException {
		mOutputStream.write(string.getBytes(Charset.forName("GB18030")));
	}

	/*
	 * QR code
	 */

	// QRcode definition
	public void ksQRdef(Bitmap bitmap) throws IOException {
		mOutputStream.write(getBytesFormBitmap(bitmap));
	}

	// QRcode print
	public void ksQRprt() throws IOException {
		mOutputStream.write(hexStringToBytes("1D2F03"));
	}

	// QRcode clear
	public void ksQRclr() throws IOException {
		mOutputStream.write(hexStringToBytes("1B40"));
	}

	// 将位图转化为bytes以被打印机识别
	private byte[] getBytesFormBitmap(Bitmap bitmap) {
		int[] pixels_old = new int[bitmap.getWidth() * bitmap.getHeight()];
		int[] pixels_new = new int[bitmap.getWidth() * bitmap.getHeight()];
		byte[] bytes_new = new byte[bitmap.getWidth() * bitmap.getHeight() / 8
				+ 4];
		bytes_new[0] = (byte) 0x1D;
		bytes_new[1] = (byte) 0x2A;
		bytes_new[2] = (byte) 0x20;
		bytes_new[3] = (byte) 0x20;

		bitmap.getPixels(pixels_old, 0, bitmap.getWidth(), 0, 0,
				bitmap.getWidth(), bitmap.getHeight());
		for (int i = 0; i < pixels_old.length; i++) {
			if (pixels_old[i] == Color.BLACK)
				pixels_new[i] = 1;
			else
				pixels_new[i] = 0;
		}

		int sum = 0;
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				// log.info("2^(7-j%8) is :"+(2 << (6-j%8)));
				int element = (pixels_new[j * 256 + i]) * (2 << (6 - j % 8));
				// log.info("element is :"+ element);
				sum += element;
				// log.info("sum is "+sum);
				if (j % 8 == 7) {
					bytes_new[i * 32 + (j - 7) / 8 + 4] = (byte) sum;
					// log.info("bytes_new["+i*32+(j-7)/8+4+"] is :"+bytes_new[i*32+(j-7)/8+4]);
					sum = 0;
				}
			}
		}
		return bytes_new;
	}

	public byte[] hexStringToBytes(String hexString) {
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

	private byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public String str2Hex(String str) {
		if (str == null)
			return "";
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < str.length(); i++) {
			byte[] ba = str.substring(i, i + 1).getBytes();
			String tmpHex = Integer.toHexString(ba[0] & 0xFF);
			sb.append("0x" + tmpHex.toUpperCase() + " ");
			if (ba.length == 2) {
				tmpHex = Integer.toHexString(ba[1] & 0xff);
				sb.append("0x" + tmpHex.toUpperCase() + " ");
			}
		}
		return sb.toString();
	}

	public String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString().toUpperCase();
	}
}