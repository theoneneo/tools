package com.mybitcoin.wallet.net;

public class ConstNet {

	// ///////////////////////////////////////////////////////
	public final static int NETWORK_RETURN_RESULT = 1000;
	// ///////////////////////////////////////////////////////
	public final static String NETWORK_RETURN_STATUS = "network_return_status";

	public final static int NETWORK_RETURN_STATUS_SUCCESS = 2000;
	public final static int NETWORK_RETURN_STATUS_HTTP = 2001;
	public final static int NETWORK_RETURN_STATUS_ERROR = 2002;
	public final static int NETWORK_RETURN_STATUS_CANCEL = 2003;
	// ///////////////////////////////////////////////////////
	public final static String NETWORK_REQUEST_DATA_TYPE = "network_request_data_type";
	public final static int NETWORK_REQUEST_DATA_TYPE_JSON = 3000;
	public final static int NETWORK_REQUEST_DATA_TYPE_BYTE = 3001;
	// ///////////////////////////////////////////////////////
	public final static String NETWORK_RETURN_DATA = "network_return_data";

	// ///////////////////////////////////////////////////////

	public final static String NETWORK_REQUEST_URL = "network_request_url";
	public final static String NETWORK_REQUEST_DATA = "network_request_data";
	public final static String NETWORK_REQUEST_ID = "network_request_id";

	// ///////////////////////////////////////////////////////
	public final static int LOCAL_NETWORK_WIFI = 4000;
	public final static int LOCAL_NETWORK_MOBILE = 4001;
	public final static int LOCAL_NETWORK_NOT_CONNECT = 4002;

	public final static int Request_materials = 4999;

	// ?��??��?
	public final static int Request_registrations = 5000;
	public final static String Request_registrations_address = "/registrations";

	public final static int Request_login = 5001;
	public final static String Request_login_address = "/login";

	public final static int Request_register = 5001;
	


	public final static int Request_music_list = 5003;

}
