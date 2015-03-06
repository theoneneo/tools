package com.mybitcoin.wallet.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import com.mybitcoin.wallet.util.Global;
import com.mybitcoin.wallet.util.Tools;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class HttpNet {

	private String TAG = "HttpNet";
	private HttpPost post = null;
	private HttpGet get = null;
	private int timeoutConnection = 60 * 1000;
	private int timeoutSocket = 60 * 1000;

	private boolean canceled;
	private static CookieStore cookieStore;

	public Bundle requestGet(String requestURL, int requestId,
			int requestDataType) {
		Bundle returnBundle = new Bundle();
//		SystemLog.debug("HttpNet.requestGet.requestURL", requestURL);
		try {
			get = new HttpGet();
			URI uri = new URI(requestURL);
			get.setURI(uri);

			HttpParams httpParams = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(httpParams,
					timeoutConnection);
			HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			if (cookieStore != null) {
				httpClient.setCookieStore(cookieStore);
			}
			if (Global.DEFAULT_PROXY == true) {
				httpClient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY,
						new HttpHost("172.17.18.80", 8080));
			}
			HttpResponse response = null;
			response = httpClient.execute(get);

			int responseCode = response.getStatusLine().getStatusCode();
//			SystemLog.debug("HttpNet", "http response code:" + responseCode);
			if (responseCode == 200) {
				cookieStore = ((AbstractHttpClient) httpClient)
						.getCookieStore();
				returnBundle.putInt(ConstNet.NETWORK_RETURN_STATUS,
						ConstNet.NETWORK_RETURN_STATUS_SUCCESS);
				returnBundle.putInt(ConstNet.NETWORK_REQUEST_ID, requestId);

				if (requestDataType == ConstNet.NETWORK_REQUEST_DATA_TYPE_JSON) {
					String data = EntityUtils.toString(response.getEntity(),
							"UTF-8");
					int ind = data.indexOf("{");
					if (ind >= 0)
						data = data.substring(ind);
					returnBundle.putString(ConstNet.NETWORK_RETURN_DATA, data);
//					SystemLog.debug("HttpNet.response", data);
				} else if (requestDataType == ConstNet.NETWORK_REQUEST_DATA_TYPE_BYTE) {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					InputStream in = response.getEntity().getContent();
					int readLen = -1;
					byte[] bdata = new byte[2048];
					while ((readLen = (in.read(bdata))) != -1) {
						output.write(bdata, 0, readLen);
					}
					byte[] data = output.toByteArray();
					returnBundle.putByteArray(ConstNet.NETWORK_RETURN_DATA,
							data);
//					SystemLog.debug("HttpNet.response", "byte data,len:"
//							+ data.length);
					output.close();
					in.close();
					output = null;
					in = null;
				}
			} else {
				returnBundle.putInt(ConstNet.NETWORK_RETURN_STATUS,
						ConstNet.NETWORK_RETURN_STATUS_HTTP);

				SystemLog.error("HttpNet.HttpError", responseCode);
			}

		} catch (SocketTimeoutException e) {
			returnBundle.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error("HttpNet.TimeOutError", e.toString());
		} catch (Exception e) {
			returnBundle.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error("HttpNet.Error", e.toString());
		} finally {
			if (get != null) {
				get = null;
			}
		}

		if (canceled) {
			returnBundle.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_CANCEL);
		}

		return returnBundle;
	}

	public Bundle requestPost(String requestURL, String requestData,
			int requestId, int requestDataType) {
		Log.i(TAG, "requestURL = " + requestURL);
		Log.i(TAG, "requestData = " + requestData);
		Bundle result = new Bundle();
		result.putInt(ConstNet.NETWORK_REQUEST_ID, requestId);
		try {
			post = new HttpPost();
			post.setHeader("Accept", "application/json");
			post.setHeader("Content-Type", "application/json");
			URI uri = new URI(requestURL);

			post.setURI(uri);

			if (requestData != null) {

				StringEntity entity = new StringEntity(requestData, "utf-8");

				post.setEntity(entity);
			}

			HttpParams httpParams = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(httpParams,
					timeoutConnection);

			HttpConnectionParams.setSoTimeout(httpParams, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
			if (cookieStore != null) {
				httpClient.setCookieStore(cookieStore);
			}
			HttpResponse response = null;

			response = httpClient.execute(post);

			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == 200) {
				cookieStore = ((AbstractHttpClient) httpClient)
						.getCookieStore();
				result.putInt(ConstNet.NETWORK_RETURN_STATUS,
						ConstNet.NETWORK_RETURN_STATUS_SUCCESS);
//				SystemLog.debug(TAG, "responseCode=" + responseCode);

				if (requestDataType == ConstNet.NETWORK_REQUEST_DATA_TYPE_JSON) {
					String data = EntityUtils.toString(response.getEntity(),
							"utf-8");
					int index = data.indexOf("{");
					if (index > 0) {
						data = data.substring(index);
					}
					result.putString(ConstNet.NETWORK_RETURN_DATA, data);
//					SystemLog.debug(TAG, "response json = " + data);
				} else if (requestDataType == ConstNet.NETWORK_REQUEST_DATA_TYPE_BYTE) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream is = response.getEntity().getContent();
					int readLen = -1;
					byte[] byteData = new byte[2048];
					while ((readLen = (is.read(byteData))) != -1) {
						baos.write(byteData, 0, readLen);
					}
					byte[] data = baos.toByteArray();
					result.putByteArray(ConstNet.NETWORK_RETURN_DATA, data);
//					SystemLog.debug(TAG, "response byte data length = "
//							+ data.length);
					baos.close();
					is.close();
					baos = null;
					is = null;
				}

			} else {
				result.putInt(ConstNet.NETWORK_RETURN_STATUS,
						ConstNet.NETWORK_RETURN_STATUS_HTTP);
				SystemLog.error(TAG, "responseCode=" + responseCode);
			}
		} catch (SocketTimeoutException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "SocketTimeoutException: " + e.getMessage());
		} catch (ClientProtocolException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "IOException: " + e.getMessage());
		} catch (URISyntaxException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "URISyntaxException: " + e.getMessage());
		} finally {
			if (post != null) {
				post = null;
			}
		}

		if (canceled) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_CANCEL);
		}

		return result;
	}

	public Bundle requestPostComment(Bundle bundle, String requestURL,
			int requestId, int requestDataType) {
		Log.i(TAG, "requestURL = " + requestURL);
		Bundle result = new Bundle();
		result.putInt(ConstNet.NETWORK_REQUEST_ID, requestId);
		String boundary = "---------------------------7dd31b261e0376";
		try {
			post = new HttpPost();
			post.setHeader("Connection", "Keep-Alive");
			// post.setHeader("Accept", "application/json");
			post.setHeader("Content-Type", "multipart/form-data; boundary="
					+ boundary);
			// post.setHeader("Referer", requestURL);
			post.setHeader("Charset", "UTF-8");

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int material_type = bundle.getInt("material_type");
			if (material_type == 8) {
				out.write((boundary
						+ "\r\nContent-Disposition:form-data;name=\""
						+ "material_type" + "\"\r\n\r\n  " + material_type + "  \r\n")
						.getBytes("utf-8"));
				out.write((boundary
						+ "\r\nContent-Disposition:form-data;name=\""
						+ "auth_key" + "\"\r\n\r\n  "
						+ bundle.getString("auth_key") + "  \r\n")
						.getBytes("utf-8"));
				out.write((boundary
						+ "\r\nContent-Disposition:form-data;name=\""
						+ "client_type" + "\"\r\n\r\n  "
						+ bundle.getString("client_type") + "  \r\n")
						.getBytes("utf-8"));

				byte[] jpg = null;
				Bitmap bitmap;

				Uri uri = bundle.getParcelable("file");
				bitmap = Tools.getBitmap(uri, 320);

				ByteArrayOutputStream out1 = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out1);
				jpg = out1.toByteArray();

				out.write((boundary
						+ "\r\nContent-Disposition:form-data;name=\"" + "file"
						+ "\";filename=\"" + bundle.getString("filename") + "\"\r\nContent-Type:image/jpg\r\n\r\n")
						.getBytes("utf-8"));
				out.write(jpg);
				out.write("\r\n".getBytes("utf-8"));
			}
			/*
			 * String key[] = { "material_type", "file" }; for (int i = 0; i <
			 * key.length; i++) { if ("file1".equals(key[i])) { if
			 * (bundle.getString(key[i]) != null) { byte[] jpg = null; Bitmap
			 * bitmap;
			 * 
			 * Uri uri = bundle.getParcelable(key[i]); bitmap =
			 * Tools.getBitmap(uri,320);
			 * 
			 * ByteArrayOutputStream out1 = new ByteArrayOutputStream();
			 * bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out1); jpg =
			 * out1.toByteArray();
			 * 
			 * out.write(("--" + boundary +
			 * "\r\nContent-Disposition:form-data;name=\"" + key[i] +
			 * "\";filename=\"" + bundle.getString("filename") +
			 * "\"\r\nContent-Type:application/octet-stream\r\n\r\n"
			 * ).getBytes("utf-8")); out.write(jpg);
			 * out.write("\r\n".getBytes("utf-8")); } else out.write(("--" +
			 * boundary + "\r\nContent-Disposition:form-data;name=\"" + key[i] +
			 * "\";filename=\"\"\r\nContent-Type:application/octet-stream\r\n\r\n"
			 * + "\r\n").getBytes("utf-8")); } else { if
			 * (bundle.getString(key[i]) != null) out.write(("--" + boundary +
			 * "\r\nContent-Disposition:form-data;name=\"" + key[i] +
			 * "\"\r\n\r\n" + bundle.getString(key[i]) +
			 * "\r\n").getBytes("utf-8")); else out.write(("--" + boundary +
			 * "\r\nContent-Disposition:form-data;name=\"" + key[i] +
			 * "\"\r\n\r\n" + "\r\n").getBytes("utf-8")); } }
			 */
			out.write(("--" + boundary + "--\r\n").getBytes("utf-8"));
			out.flush();
			ByteArrayEntity myEntity = new ByteArrayEntity(out.toByteArray());

			post.setEntity(myEntity);

			URI uri = new URI(requestURL);
			post.setURI(uri);

			HttpParams httpParams = new BasicHttpParams();

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);

			if (cookieStore != null) {
				// httpClient.setCookieStore(cookieStore);
			}

			HttpResponse response = null;

			response = httpClient.execute(post);

			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == 200) {
				cookieStore = httpClient.getCookieStore();
				result.putInt(ConstNet.NETWORK_RETURN_STATUS,
						ConstNet.NETWORK_RETURN_STATUS_SUCCESS);
				result.putInt(ConstNet.NETWORK_REQUEST_ID, requestId);
//				SystemLog.debug(TAG, "responseCode=" + responseCode);

				if (requestDataType == ConstNet.NETWORK_REQUEST_DATA_TYPE_JSON) {
					String data = EntityUtils.toString(response.getEntity(),
							"utf-8");
					int index = data.indexOf("{");
					if (index > 0) {
						data = data.substring(index);
					}
					result.putString(ConstNet.NETWORK_RETURN_DATA, data);
//					SystemLog.debug(TAG, "response json = " + data);
				}
			} else {
				result.putInt(ConstNet.NETWORK_RETURN_STATUS,
						ConstNet.NETWORK_RETURN_STATUS_HTTP);
				SystemLog.error(TAG, "responseCode=" + responseCode);
			}
		} catch (SocketTimeoutException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "SocketTimeoutException: " + e.getMessage());
		} catch (ClientProtocolException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "ClientProtocolException: " + e.getMessage());
		} catch (IOException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "IOException: " + e.getMessage());
		} catch (URISyntaxException e) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_ERROR);
			SystemLog.error(TAG, "URISyntaxException: " + e.getMessage());
		} catch (Exception e) {
			SystemLog.error(TAG, "Exception: " + e.getMessage());
		} finally {
			if (post != null) {
				post = null;
			}
		}

		if (canceled) {
			result.putInt(ConstNet.NETWORK_RETURN_STATUS,
					ConstNet.NETWORK_RETURN_STATUS_CANCEL);
		}

		return result;
	}

	public void stop() {
		canceled = true;
	}
}
