package com.bitocean.atm.service;
/**
 * @author bing.liu
 * 
 */
public class ATMBroadCastEvent {
	public final static int BASE_EVENT = 0;

	public final static int EVENT_GOHOME = BASE_EVENT + 1;
	public final static int EVENT_UPDATE_TIMER = BASE_EVENT + 2;
	public final static int EVENT_NETWORK_STATUS = BASE_EVENT + 3;
	public final static int EVENT_ADMIN_LOGIN_SUCCESS = BASE_EVENT + 4;
	public final static int EVENT_ADMIN_LOGIN_FAIL = BASE_EVENT + 5;
	public final static int EVENT_USER_LOGIN_SUCCESS = BASE_EVENT + 6;
	public final static int EVENT_USER_LOGIN_FAIL = BASE_EVENT + 7;
	public final static int EVENT_USER_REGISTER_SUCCESS = BASE_EVENT + 8;
	public final static int EVENT_USER_REGISTER_FAIL = BASE_EVENT + 9;
	public final static int EVENT_VERIFY_CODE_SUCCESS = BASE_EVENT + 10;
	public final static int EVENT_VERIFY_CODE_FAIL = BASE_EVENT + 11;	
	public final static int EVENT_GET_RATE_LIST_SUCCESS = BASE_EVENT + 12;
	public final static int EVENT_GET_RATE_LIST_FAIL = BASE_EVENT + 13;	
	//摄像头扫描===============
	public final static int EVENT_QR_AUTO_FOCUS = BASE_EVENT + 14;
	public final static int EVENT_QR_RESTART_PREVIEW = BASE_EVENT + 15;
	public final static int EVENT_QR_DECODE_SUCCEEDED = BASE_EVENT + 16;
	public final static int EVENT_QR_DECODE_FAILED = BASE_EVENT + 17;
	public final static int EVENT_QR_RETURN_SCAN_RESULT = BASE_EVENT + 18;
	public final static int EVENT_QR_LAUNCH_PRODUCT_QUERY = BASE_EVENT + 19;
	public final static int EVENT_QR_DECODE = BASE_EVENT + 20;
	public final static int EVENT_QR_QUIT = BASE_EVENT + 21;
	//======================
	private int type;
	private Object obj;
	
	public ATMBroadCastEvent(int type) {
		this.type = type;
	}

	public ATMBroadCastEvent(int type, Object obj) {
		this.type = type;
		this.obj = obj;
	}

	public int getType() {
		return type;
	}

	public Object getObject() {
		return obj;
	}
}
