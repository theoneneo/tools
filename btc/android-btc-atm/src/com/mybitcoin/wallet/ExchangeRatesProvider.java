/*
 * Copyright 2011-2014 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mybitcoin.wallet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.util.Log;

import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.ui.WalletActivityBase;
import com.mybitcoin.wallet.util.GenericUtils;
import com.mybitcoin.wallet.util.Io;

/**
 * @author Andreas Schildbach
 */
public class ExchangeRatesProvider extends ContentProvider
{
	public static class ExchangeRate
	{
		public ExchangeRate(@Nonnull final String currencyCode, @Nonnull final BigInteger rate, final String source)
		{
			this.currencyCode = currencyCode;
			this.rate = rate;
			this.source = source;
		}

		public final String currencyCode;
		public final BigInteger rate;
		public final String source;

		@Override
		public String toString()
		{
			return getClass().getSimpleName() + '[' + currencyCode + ':' + GenericUtils.formatDebugValue(rate) + ']';
		}
	}

	public static final String KEY_CURRENCY_CODE = "currency_code";
	public static final String KEY_RATE = "rate";
	private static final String KEY_SOURCE = "source";

	private Configuration config;
	private String userAgent;

	@CheckForNull
	private Map<String, ExchangeRate> exchangeRates = null;
	private long lastUpdated = 0;

	private static final URL BITCOINAVERAGE_URL = null;
	private static final String[] BITCOINAVERAGE_FIELDS = new String[] { "24h_avg", "last" };
	private static final String BITCOINAVERAGE_SOURCE = "BitcoinAverage.com";
	private static final URL BLOCKCHAININFO_URL = null;
	private static final String[] BLOCKCHAININFO_FIELDS = new String[] { "15m" };
	private static final String BLOCKCHAININFO_SOURCE = "blockchain.info";

    private static final Logger log = LoggerFactory.getLogger(ExchangeRatesProvider.class);

    
	// https://bitmarket.eu/api/ticker

	/*static
	{
		try
		{

			BITCOINAVERAGE_URL = new URL("https://api.bitcoinaverage.com/custom/abw");
			BLOCKCHAININFO_URL = new URL("https://blockchain.info/ticker");
		}
		catch (final MalformedURLException x)
		{
			throw new RuntimeException(x); // cannot happen
		}
	}*/

	private static final long UPDATE_FREQ_MS = 10 * DateUtils.MINUTE_IN_MILLIS;

	// AUTHOR: F, DATE: 2014.6.10
	SettingInfo mSettingInfo;

	@Override
	public boolean onCreate()
	{
		final Context context = getContext();

        this.config = new Configuration(PreferenceManager.getDefaultSharedPreferences(context));

		this.userAgent = WalletApplication.httpUserAgent(WalletApplication.packageInfoFromContext(context).versionName);
		
		// AUTHOR: F, DATE: 2014.6.10
		mSettingInfo = new SettingInfo(context);

		/*final ExchangeRate cachedExchangeRate = config.getCachedExchangeRate();
		if (cachedExchangeRate != null)
		{
			exchangeRates = new TreeMap<String, ExchangeRate>();
			exchangeRates.put(cachedExchangeRate.currencyCode, cachedExchangeRate);
		}*/

		return true;
	}

	public static Uri contentUri(@Nonnull final String packageName)
	{
		return Uri.parse("content://" + packageName +"_test." + "exchange_rates");
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
	{

        final Context context = getContext();
        final Resources resources = context.getResources();
        
        // AUTHOR: F, DATE: 2014.6.10
//        String[] exchangeRateUrls = resources.getStringArray(R.array.exchage_rate_urls);
        String urls = mSettingInfo.getExchangeRateUrl();
        String[] exchangeRateUrls = urls.split(",");

//        final long now = System.currentTimeMillis();

		if (lastUpdated == 0)// || now - lastUpdated > UPDATE_FREQ_MS)
		{
			Map<String, ExchangeRate> newExchangeRates = null;
            int i =0;
            try {
                while(newExchangeRates == null && i< exchangeRateUrls.length){

                    if(exchangeRateUrls[i] !=null && !("").equals(exchangeRateUrls[i])){
                        log.info("exchange_rate_urls [" + i + "] is "+exchangeRateUrls[i]);
                        URL  url = new URL(exchangeRateUrls[i]);
                        newExchangeRates = requestExchangeRates(url, userAgent, BITCOINAVERAGE_SOURCE, BITCOINAVERAGE_FIELDS);
                    }
                    ++i;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
			/*if (newExchangeRates == null)
				newExchangeRates = requestExchangeRates(BITCOINAVERAGE_URL, userAgent, BITCOINAVERAGE_SOURCE, BITCOINAVERAGE_FIELDS);
			if (newExchangeRates == null)
				newExchangeRates = requestExchangeRates(BLOCKCHAININFO_URL, userAgent, BLOCKCHAININFO_SOURCE, BLOCKCHAININFO_FIELDS);
            */
			if (newExchangeRates != null)
			{
				exchangeRates = newExchangeRates;
				//lastUpdated = now;

				/*final ExchangeRate exchangeRateToCache = bestExchangeRate(config.getExchangeCurrencyCode());
				if (exchangeRateToCache != null)
					config.setCachedExchangeRate(exchangeRateToCache);*/
			}
		}

		if (exchangeRates == null)
			return null;

		final MatrixCursor cursor = new MatrixCursor(new String[] { BaseColumns._ID, KEY_CURRENCY_CODE, KEY_RATE, KEY_SOURCE });

		if (selection == null)
		{
			for (final Map.Entry<String, ExchangeRate> entry : exchangeRates.entrySet())
			{
				final ExchangeRate rate = entry.getValue();
				cursor.newRow().add(rate.currencyCode.hashCode()).add(rate.currencyCode).add(rate.rate.longValue()).add(rate.source);
			}
		}
		else if (selection.equals(KEY_CURRENCY_CODE))
		{
			final ExchangeRate rate = bestExchangeRate(selectionArgs[0]);
			if (rate != null)
				cursor.newRow().add(rate.currencyCode.hashCode()).add(rate.currencyCode).add(rate.rate.longValue()).add(rate.source);
		}

		return cursor;
	}

	private ExchangeRate bestExchangeRate(final String currencyCode)
	{
		ExchangeRate rate = currencyCode != null ? exchangeRates.get(currencyCode) : null;
		if (rate != null)
			return rate;

		final String defaultCode = defaultCurrencyCode();
		rate = defaultCode != null ? exchangeRates.get(defaultCode) : null;

		if (rate != null)
			return rate;

		return exchangeRates.get(Constants.DEFAULT_EXCHANGE_CURRENCY);
	}

	private String defaultCurrencyCode()
	{
		try
		{
			return Currency.getInstance(Locale.getDefault()).getCurrencyCode();
		}
		catch (final IllegalArgumentException x)
		{
			return null;
		}
	}

	public static ExchangeRate getExchangeRate(@Nonnull final Cursor cursor)
	{
		final String currencyCode = cursor.getString(cursor.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_CURRENCY_CODE));
		final BigInteger rate = BigInteger.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_RATE)));
		final String source = cursor.getString(cursor.getColumnIndexOrThrow(ExchangeRatesProvider.KEY_SOURCE));

		return new ExchangeRate(currencyCode, rate, source);
//		String uriAPI = "http://dashboard.bitocean.com:8081/exchange_rate/" + WalletActivityBase.publiKeyForOuter;
//		DefaultHttpClient client = new DefaultHttpClient();  
//        HttpGet httpGet = new HttpGet(uriAPI);  
//        ExchangeRate er = null;
//        HttpResponse httpResponse;
//        try {  
//            httpResponse = client.execute(httpGet);  
//  
//  
//            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
//                // 第3步：使用getEntity方法获得返回结果  
//                String strResult = EntityUtils.toString(httpResponse.getEntity());  
//                try {
//					JSONObject jsonResult = new JSONObject(strResult);
//					
//					er = new ExchangeRate("CNY",new BigInteger(jsonResult.getString("rate")),"Server");
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} 
//  
//  
//            }  
//        } catch (ClientProtocolException e) {  
//            // TODO Auto-generated catch block  
//            e.printStackTrace();  
//        } catch (IOException e) {  
//            // TODO Auto-generated catch block  
//            e.printStackTrace();  
//        }  
//		return er;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getType(final Uri uri)
	{
		throw new UnsupportedOperationException();
	}

	private static Map<String, ExchangeRate> requestExchangeRates(final URL url, final String userAgent, final String source, final String... fields)
	{
		//从自己服务器上拿数据
		String uriAPI = "http://dashboard.bitocean.com:8081/exchange_rate/" + WalletActivityBase.publiKeyForOuter;
		DefaultHttpClient client = new DefaultHttpClient();  
        HttpGet httpGet = new HttpGet(uriAPI);  
        HttpResponse httpResponse;
        final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
        try {  
            httpResponse = client.execute(httpGet);  
  
  
            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
                // 第3步：使用getEntity方法获得返回结果  
                String strResult = EntityUtils.toString(httpResponse.getEntity());  
                try {
					JSONObject jsonResult = new JSONObject(strResult);
					log.info(jsonResult.toString());
					String rateStr = jsonResult.getString("price");
					log.info(rateStr);
					final BigInteger rate = GenericUtils.toNanoCoins(rateStr, 0);
//					
					ExchangeRate er = new ExchangeRate("CNY",rate,"Server");
					rates.put("CNY", er);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
  
  
            }  
        } catch (ClientProtocolException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        } catch (IOException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }
        log.info(String.valueOf(rates.size()));
		return rates;
		
		
		//原来的请求方法，不是从自己服务器上获取	
//		final long start = System.currentTimeMillis();
//
//		HttpURLConnection connection = null;
//		Reader reader = null;
//
//		try
//		{
//			connection = (HttpURLConnection) url.openConnection();
//
//			connection.setInstanceFollowRedirects(false);
//			connection.setConnectTimeout(Constants.HTTP_TIMEOUT_MS);
//			connection.setReadTimeout(Constants.HTTP_TIMEOUT_MS);
//			connection.addRequestProperty("User-Agent", userAgent);
//			connection.addRequestProperty("Accept-Encoding", "gzip");
//			connection.connect();
//
//			final int responseCode = connection.getResponseCode();
//			if (responseCode == HttpURLConnection.HTTP_OK)
//			{
//				final String contentEncoding = connection.getContentEncoding();
//
//				InputStream is = new BufferedInputStream(connection.getInputStream(), 1024);
//				if ("gzip".equalsIgnoreCase(contentEncoding))
//					is = new GZIPInputStream(is);
//
//				reader = new InputStreamReader(is, Constants.UTF_8);
//				final StringBuilder content = new StringBuilder();
//				final long length = Io.copy(reader, content);
//
//				final Map<String, ExchangeRate> rates = new TreeMap<String, ExchangeRate>();
//                final String currencyCode = "CNY";
//				final JSONObject head = new JSONObject(content.toString());
//                final JSONObject o = head.getJSONObject("ticker");
//                final String rateStr = o.optString("last", null);log.info("rateStr is :"+rateStr);
//                if (rateStr != null)
//                {
//                    try
//                    {
//                        final BigInteger rate = GenericUtils.toNanoCoins(rateStr, 0);
//
//                        if (rate.signum() > 0)
//                        {
//                            rates.put(currencyCode, new ExchangeRate(currencyCode, rate, source));
//                            log.info("currency_code:" + currencyCode);
//                            log.info("source:" + source);
//
//                        }
//                    }
//                    catch (final ArithmeticException x)
//                    {
//                        log.warn("problem fetching {} exchange rate from {} ({}): {}", currencyCode, url, contentEncoding, x.getMessage());
//                    }
//                }
//
//				/*for (final Iterator<String> i = head.keys(); i.hasNext();)
//				{
//					final String currencyCode = i.next();
//					if (!"timestamp".equals(currencyCode))
//					{
//						final JSONObject o = head.getJSONObject(currencyCode);
//
//						for (final String field : fields)
//						{
//							final String rateStr = o.optString(field, null);
//
//							if (rateStr != null)
//							{
//								try
//								{
//									final BigInteger rate = GenericUtils.toNanoCoins(rateStr, 0);
//
//									if (rate.signum() > 0)
//									{
//										rates.put(currencyCode, new ExchangeRate(currencyCode, rate, source));
//										break;
//									}
//								}
//								catch (final ArithmeticException x)
//								{
//									log.warn("problem fetching {} exchange rate from {} ({}): {}", currencyCode, url, contentEncoding, x.getMessage());
//								}
//							}
//						}
//					}
//				}*/
//
//				log.info("fetched exchange rates from {} ({}), {} chars, took {} ms", url, contentEncoding, length, System.currentTimeMillis()
//						- start);
//
//				return rates;
//			}
//			else
//			{
//				log.warn("http status {} when fetching {}", responseCode, url);
//			}
//		}
//		catch (final Exception x)
//		{
//			log.warn("problem fetching exchange rates from " + url, x);
//		}
//		finally
//		{
//			if (reader != null)
//			{
//				try
//				{
//					reader.close();
//				}
//				catch (final IOException x)
//				{
//					// swallow
//				}
//			}
//
//			if (connection != null)
//				connection.disconnect();
//		}
//
//		return null;
	}
}
