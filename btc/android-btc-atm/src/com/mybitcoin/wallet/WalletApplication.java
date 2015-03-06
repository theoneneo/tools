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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.math.BigInteger;

import javax.annotation.Nonnull;

import org.bitcoinj.wallet.Protos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Resources;
import android.content.SharedPreferences;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.VersionMessage;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.store.UnreadableWalletException;
import com.google.bitcoin.store.WalletProtobufSerializer;
import com.google.bitcoin.utils.Threading;
import com.google.bitcoin.wallet.WalletFiles;
import com.google.bitcoin.core.Wallet.BalanceType;
import com.mybitcoin.wallet.environment.SettingInfo;
import com.mybitcoin.wallet.service.BlockchainService;
import com.mybitcoin.wallet.service.BlockchainServiceImpl;
import com.mybitcoin.wallet.ui.DialogBuilder;
import com.mybitcoin.wallet.util.CrashReporter;
import com.mybitcoin.wallet.util.Crypto;
import com.mybitcoin.wallet.util.Io;
import com.mybitcoin.wallet.util.Iso8601Format;
import com.mybitcoin.wallet.util.LinuxSecureRandom;
import com.mybitcoin.wallet.util.WalletUtils;
import com.mybitcoin.wallet.R;

import android_serialport_api.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * @author Andreas Schildbach
 */
public class WalletApplication extends Application
{
	private Configuration config;
	private ActivityManager activityManager;

	private Intent blockchainServiceIntent;
	private Intent blockchainServiceCancelCoinsReceivedIntent;
	private Intent blockchainServiceResetBlockchainIntent;

	private File walletFile;
	private Wallet wallet;
	private PackageInfo packageInfo;

	private static final int KEY_ROTATION_VERSION_CODE = 135;

    public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mPrintSerialPort = null,mScanSerialPort = null;


    private static final Logger log = LoggerFactory.getLogger(WalletApplication.class);

    private WakeLock wakeLock;
    private static WalletApplication app;

    public static  BigInteger MinAvaiableBitcoinAmount = null;//new BigInteger("100000");

    // AUTHOR: F, DATE: 2014.6.10
    SettingInfo mSettingInfo;
    
    
	@Override
	public void onCreate()
	{
		new LinuxSecureRandom(); // init proper random number generator

		initLogging();

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().permitDiskReads().permitDiskWrites().penaltyLog().build());

		Threading.throwOnLockCycles();

		log.info("configuration: " + (Constants.TEST ? "test" : "prod") + ", " + Constants.NETWORK_PARAMETERS.getId());

		super.onCreate();
		app = this;
		packageInfo = packageInfoFromContext(this);

		CrashReporter.init(getCacheDir());

		Threading.uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(final Thread thread, final Throwable throwable)
			{
				log.info("bitcoinj uncaught exception", throwable);
				CrashReporter.saveBackgroundTrace(throwable, packageInfo);
			}
		};

		config = new Configuration(PreferenceManager.getDefaultSharedPreferences(this));
		activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		blockchainServiceIntent = new Intent(this, BlockchainServiceImpl.class);
		blockchainServiceCancelCoinsReceivedIntent = new Intent(BlockchainService.ACTION_CANCEL_COINS_RECEIVED, null, this,
				BlockchainServiceImpl.class);
		blockchainServiceResetBlockchainIntent = new Intent(BlockchainService.ACTION_RESET_BLOCKCHAIN, null, this, BlockchainServiceImpl.class);

		walletFile = getFileStreamPath(Constants.WALLET_FILENAME_PROTOBUF);

		loadWalletFromProtobuf();
		wallet.autosaveToFile(walletFile, 1, TimeUnit.SECONDS, new WalletAutosaveEventListener());
//        log.info("local btc's amount is :"+wallet.getBalance(BalanceType.AVAILABLE).floatValue());
		// clean up spam
		wallet.cleanup();

		config.updateLastVersionCode(packageInfo.versionCode);

		if (config.versionCodeCrossed(packageInfo.versionCode, KEY_ROTATION_VERSION_CODE))
		{
			log.info("detected version jump crossing key rotation");
			wallet.setKeyRotationTime(System.currentTimeMillis() / 1000);
		}

		ensureKey();

		migrateBackup();
        final Resources res = getResources();

        exportPrivateKeys(res.getString(com.mybitcoin.wallet.R.string.crypto_password));
        wakelockScreen();
        
        // AUTHOR: F, DATE: 2014.6.10
        mSettingInfo = new SettingInfo(getApplicationContext());
	}
	
    public static WalletApplication getStaticWalletApplication(){
    	return app;
    }

    private void exportPrivateKeys(@Nonnull final String password)
    {
        try
        {
            Constants.EXTERNAL_WALLET_BACKUP_DIR.mkdirs();
            final DateFormat dateFormat = Iso8601Format.newDateFormat();
            dateFormat.setTimeZone(TimeZone.getDefault());
            final File file = new File(Constants.EXTERNAL_WALLET_BACKUP_DIR, Constants.EXTERNAL_WALLET_KEY_BACKUP + "-"
                    + dateFormat.format(new Date()));
            String cipherText = config.getCryptoPrivateKey();
            if(cipherText == null || ("").equals(cipherText)){
//                log.info("cipherText is "+cipherText);
                final List<ECKey> keys = new LinkedList<ECKey>();
                for (final ECKey key : wallet.getKeys())
                    if (!wallet.isKeyRotating(key))
                        keys.add(key);
//                log.info("keys'size is "+keys.size());
                final StringWriter plainOut = new StringWriter();
                WalletUtils.writeKeys(plainOut, keys);
                plainOut.close();
                final String plainText = plainOut.toString();
//                log.info("plainText is "+plainText);
                cipherText = Crypto.encrypt(plainText, password.toCharArray());
//                log.info(" cipherText is "+cipherText);
                if(cipherText != null && !("").equals(cipherText)){
                    config.setCryptoPrivateKey(cipherText);
//                    log.info("config's ciphertext is "+config.getCryptoPrivateKey());
                }
            }
            if(!file.exists()){
            	file.createNewFile();
            }
            
            final Writer cipherOut = new OutputStreamWriter(new FileOutputStream(file), Constants.UTF_8);
            cipherOut.write(cipherText);
            cipherOut.close();

//			log.info("exported " + keys.size() + " private keys to " + file);
            log.info("exported  private keys to " + file);
        }
        catch (final IOException x)
        {
            /*final DialogBuilder dialog = DialogBuilder.warn(this, R.string.import_export_keys_dialog_failure_title);
            dialog.setMessage(getString(R.string.export_keys_dialog_failure, x.getMessage()));
            dialog.singleDismissButton(null);
            dialog.show();*/

            log.error("problem writing private keys", x);
        }
    }
    private void wakelockScreen(){
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getPackageName() + "application lock");
        wakeLock.acquire();
        log.info("lock is locaked.");
    }

    public void onDestory(){
        wakeLock.release();
        log.info("lock is released.");
//        super.onDestory();
    }

	private void initLogging()
	{
		final File logDir = getDir("log", Constants.TEST ? Context.MODE_WORLD_READABLE : MODE_PRIVATE);
		final File logFile = new File(logDir, "wallet.log");

		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
		filePattern.setContext(context);
		filePattern.setPattern("%d{HH:mm:ss.SSS} [%thread] %logger{0} - %msg%n");
		filePattern.start();

		final RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
		fileAppender.setContext(context);
		fileAppender.setFile(logFile.getAbsolutePath());

		final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
		rollingPolicy.setContext(context);
		rollingPolicy.setParent(fileAppender);
		rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/wallet.%d.log.gz");
		rollingPolicy.setMaxHistory(7);
		rollingPolicy.start();

		fileAppender.setEncoder(filePattern);
		fileAppender.setRollingPolicy(rollingPolicy);
		fileAppender.start();

		final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
		logcatTagPattern.setContext(context);
		logcatTagPattern.setPattern("%logger{0}");
		logcatTagPattern.start();

		final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
		logcatPattern.setContext(context);
		logcatPattern.setPattern("[%thread] %msg%n");
		logcatPattern.start();

		final LogcatAppender logcatAppender = new LogcatAppender();
		logcatAppender.setContext(context);
		logcatAppender.setTagEncoder(logcatTagPattern);
		logcatAppender.setEncoder(logcatPattern);
		logcatAppender.start();

		final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
		log.addAppender(fileAppender);
		log.addAppender(logcatAppender);
		log.setLevel(Level.INFO);
	}

	private static final class WalletAutosaveEventListener implements WalletFiles.Listener
	{
		@Override
		public void onBeforeAutoSave(final File file)
		{
		}

		@Override
		public void onAfterAutoSave(final File file)
		{
			// make wallets world accessible in test mode
			if (Constants.TEST)
				Io.chmod(file, 0777);
		}
	}

	public Configuration getConfiguration()
	{
		return config;
	}

	public Wallet getWallet()
	{
		return wallet;
	}

	private void loadWalletFromProtobuf()
	{
		if (walletFile.exists())
		{
			final long start = System.currentTimeMillis();

			FileInputStream walletStream = null;

			try
			{
				walletStream = new FileInputStream(walletFile);

				wallet = new WalletProtobufSerializer().readWallet(walletStream);

				log.info("wallet loaded from: '" + walletFile + "', took " + (System.currentTimeMillis() - start) + "ms");
			}
			catch (final FileNotFoundException x)
			{
				log.error("problem loading wallet", x);

		        TextView text = new TextView(WalletApplication.this);
		        text.setText(x.getClass().getName());
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        Toast toast = new Toast(WalletApplication.this);
		        toast.setView(text);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
				wallet = restoreWalletFromBackup();
			}
			catch (final UnreadableWalletException x)
			{
				log.error("problem loading wallet", x);

		        TextView text = new TextView(WalletApplication.this);
		        text.setText(x.getClass().getName());
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        Toast toast = new Toast(WalletApplication.this);
		        toast.setView(text);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
				wallet = restoreWalletFromBackup();
			}
			finally
			{
				if (walletStream != null)
				{
					try
					{
						walletStream.close();
					}
					catch (final IOException x)
					{
						// swallow
					}
				}
			}

			if (!wallet.isConsistent())
			{
		        TextView text = new TextView(this);
		        text.setText("inconsistent wallet: " + walletFile);
		        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		        Toast toast = new Toast(this);
		        toast.setView(text);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
				wallet = restoreWalletFromBackup();
			}

			if (!wallet.getParams().equals(Constants.NETWORK_PARAMETERS))
				throw new Error("bad wallet network parameters: " + wallet.getParams().getId());
		}
		else
		{
			wallet = new Wallet(Constants.NETWORK_PARAMETERS);

			log.info("new wallet created");
		}

		// this check is needed so encrypted wallets won't get their private keys removed accidently
		for (final ECKey key : wallet.getKeys())
			if (key.getPrivKeyBytes() == null)
				throw new Error("found read-only key, but wallet is likely an encrypted wallet from the future");
            else if (!wallet.isKeyRotating(key))
                log.info("private key is "+key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS).toString());
	}

	private Wallet restoreWalletFromBackup()
	{
		InputStream is = null;

		try
		{
			is = openFileInput(Constants.WALLET_KEY_BACKUP_PROTOBUF);

			final Wallet wallet = new WalletProtobufSerializer().readWallet(is);

			if (!wallet.isConsistent())
				throw new Error("inconsistent backup");

			resetBlockchain();
			
	        TextView text = new TextView(this);
	        text.setText(R.string.toast_wallet_reset);
	        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
	        Toast toast = new Toast(this);
	        toast.setView(text);
	        toast.setGravity(Gravity.CENTER, 0, 0);
	        toast.show();

			log.info("wallet restored from backup: '" + Constants.WALLET_KEY_BACKUP_PROTOBUF + "'");

			return wallet;
		}
		catch (final IOException x)
		{
			throw new Error("cannot read backup", x);
		}
		catch (final UnreadableWalletException x)
		{
			throw new Error("cannot read backup", x);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch (final IOException x)
			{
				// swallow
			}
		}
	}

	private void ensureKey()
	{
		for (final ECKey key : wallet.getKeys())
			if (!wallet.isKeyRotating(key))
				return; // found

		log.info("wallet has no usable key - creating");
		addNewKeyToWallet();
	}

	public void addNewKeyToWallet()
	{
		wallet.addKey(new ECKey());
        //crypto private key
        final Resources res = getResources();
        cryptoKey(res.getString(com.mybitcoin.wallet.R.string.crypto_password));

		backupWallet();

		config.armBackupReminder();
	}
    public void cryptoKey(String password){
        try {
            final List<ECKey> keys = new LinkedList<ECKey>();
            String privateKey = "";
            for (final ECKey key : wallet.getKeys())
                if (!wallet.isKeyRotating(key)){
                    keys.add(key);
                    log.info("private key is :"+ key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS).toString());
                }
                    log.info("keys'size is "+keys.size());
//            log.info("private key is :"+ key.getPrivateKeyEncoded(Constants.NETWORK_PARAMETERS).toString());
            final StringWriter plainOut = new StringWriter();
            WalletUtils.writeKeys(plainOut, keys);
            plainOut.close();
            final String plainText = plainOut.toString();
//            log.info("plainText is "+plainText);
            final String cipherText = Crypto.encrypt(plainText, password.toCharArray());
//            log.info(" cipherText is "+cipherText);
            if(cipherText != null && !("").equals(cipherText)){
                config.setCryptoPrivateKey(cipherText);
//                log.info("config's ciphertext is "+config.getCryptoPrivateKey());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
	public void saveWallet()
	{
		try
		{
			protobufSerializeWallet(wallet);
		}
		catch (final IOException x)
		{
			throw new RuntimeException(x);
		}
	}

	private void protobufSerializeWallet(@Nonnull final Wallet wallet) throws IOException
	{
		final long start = System.currentTimeMillis();

		wallet.saveToFile(walletFile);

		// make wallets world accessible in test mode
		if (Constants.TEST)
			Io.chmod(walletFile, 0777);

//		log.debug("wallet saved to: '" + walletFile + "', took " + (System.currentTimeMillis() - start) + "ms");
	}

	private void backupWallet()
	{
		final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(wallet).toBuilder();

		// strip redundant
		builder.clearTransaction();
		builder.clearLastSeenBlockHash();
		builder.setLastSeenBlockHeight(-1);
		builder.clearLastSeenBlockTimeSecs();
		final Protos.Wallet walletProto = builder.build();

		OutputStream os = null;

		try
		{
			os = openFileOutput(Constants.WALLET_KEY_BACKUP_PROTOBUF, Context.MODE_PRIVATE);
			walletProto.writeTo(os);
		}
		catch (final IOException x)
		{
			log.error("problem writing key backup", x);
		}
		finally
		{
			try
			{
				os.close();
			}
			catch (final IOException x)
			{
				// swallow
			}
		}

		try
		{
			final String filename = String.format(Locale.US, "%s.%02d", Constants.WALLET_KEY_BACKUP_PROTOBUF,
					(System.currentTimeMillis() / DateUtils.DAY_IN_MILLIS) % 100l);
			os = openFileOutput(filename, Context.MODE_PRIVATE);
			walletProto.writeTo(os);
		}
		catch (final IOException x)
		{
			log.error("problem writing key backup", x);
		}
		finally
		{
			try
			{
				os.close();
			}
			catch (final IOException x)
			{
				// swallow
			}
		}
	}

	private void migrateBackup()
	{
		if (!getFileStreamPath(Constants.WALLET_KEY_BACKUP_PROTOBUF).exists())
		{
			log.info("migrating automatic backup to protobuf");

			// make sure there is at least one recent backup
			backupWallet();

			// remove old backups
			for (final String filename : fileList())
				if (filename.startsWith(Constants.WALLET_KEY_BACKUP_BASE58))
					new File(getFilesDir(), filename).delete();
		}
	}

	public Address determineSelectedAddress()
	{
		final String selectedAddress = config.getSelectedAddress();

		Address firstAddress = null;
		for (final ECKey key : wallet.getKeys())
		{
			if (!wallet.isKeyRotating(key))
			{
				final Address address = key.toAddress(Constants.NETWORK_PARAMETERS);

				if (address.toString().equals(selectedAddress))
					return address;

				if (firstAddress == null)
					firstAddress = address;
			}
		}

		return firstAddress;
	}

	public void startBlockchainService(final boolean cancelCoinsReceived)
	{
		if (cancelCoinsReceived)
			startService(blockchainServiceCancelCoinsReceivedIntent);
		else
			startService(blockchainServiceIntent);
	}

	public void stopBlockchainService()
	{
		stopService(blockchainServiceIntent);
	}

	public void resetBlockchain()
	{
		// actually stops the service
		startService(blockchainServiceResetBlockchainIntent);
	}

	public void broadcastTransaction(@Nonnull final Transaction tx)
	{
		final Intent intent = new Intent(BlockchainService.ACTION_BROADCAST_TRANSACTION, null, this, BlockchainServiceImpl.class);
		intent.putExtra(BlockchainService.ACTION_BROADCAST_TRANSACTION_HASH, tx.getHash().getBytes());
		startService(intent);
	}

	public static PackageInfo packageInfoFromContext(final Context context)
	{
		try
		{
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		}
		catch (final NameNotFoundException x)
		{
			throw new RuntimeException(x);
		}
	}

	public PackageInfo packageInfo()
	{
		return packageInfo;
	}

	public final String applicationPackageFlavor()
	{
		final String packageName = getPackageName();
		final int index = packageName.lastIndexOf('_');

		if (index != -1)
			return packageName.substring(index + 1);
		else
			return null;
	}

	public static String httpUserAgent(final String versionName)
	{
		final VersionMessage versionMessage = new VersionMessage(Constants.NETWORK_PARAMETERS, 0);
		versionMessage.appendToSubVer(Constants.USER_AGENT, versionName, null);
		return versionMessage.subVer;
	}

	public String httpUserAgent()
	{
		return httpUserAgent(packageInfo().versionName);
	}

	public int maxConnectedPeers()
	{
		final int memoryClass = activityManager.getMemoryClass();
		if (memoryClass <= Constants.MEMORY_CLASS_LOWEND)
			return 4;
		else
			return 6;
	}

	public static void scheduleStartBlockchainService(@Nonnull final Context context)
	{
		final Configuration config = new Configuration(PreferenceManager.getDefaultSharedPreferences(context));
		final long lastUsedAgo = config.getLastUsedAgo();

		// apply some backoff
		final long alarmInterval;
		if (lastUsedAgo < Constants.LAST_USAGE_THRESHOLD_JUST_MS)
			alarmInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
		else if (lastUsedAgo < Constants.LAST_USAGE_THRESHOLD_RECENTLY_MS)
			alarmInterval = AlarmManager.INTERVAL_HALF_DAY;
		else
			alarmInterval = AlarmManager.INTERVAL_DAY;

		log.info("last used {} minutes ago, rescheduling blockchain sync in roughly {} minutes", lastUsedAgo / DateUtils.MINUTE_IN_MILLIS,
				alarmInterval / DateUtils.MINUTE_IN_MILLIS);

		final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent alarmIntent = PendingIntent.getService(context, 0, new Intent(context, BlockchainServiceImpl.class), 0);
		alarmManager.cancel(alarmIntent);

		// workaround for no inexact set() before KitKat
		final long now = System.currentTimeMillis();
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, now + alarmInterval, AlarmManager.INTERVAL_DAY, alarmIntent);
	}


    public SerialPort getPrintSerialPort() throws SecurityException, IOException, InvalidParameterException {
			/* Read serial port parameters */
            /*SharedPreferences sp = getSharedPreferences("com.mybitcoin.wallet_preferences", MODE_PRIVATE);
            String path = sp.getString("DEVICE", "");
            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));

			*//* Check parameters *//*
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }*/
            String path = "/dev/ttySAC2";
            int baudrate = 38400;
			/* Open the serial port */
            mPrintSerialPort = new SerialPort(new File(path), baudrate, 0, 0, 0);
        return mPrintSerialPort;
    }

    public SerialPort getScanSerialPort() throws SecurityException, IOException, InvalidParameterException {
			/* Read serial port parameters */
            /*SharedPreferences sp = getSharedPreferences("com.mybitcoin.wallet_preferences", MODE_PRIVATE);
            String path = sp.getString("DEVICE", "");
            int baudrate = Integer.decode(sp.getString("BAUDRATE", "-1"));

			*//* Check parameters *//*
            if ( (path.length() == 0) || (baudrate == -1)) {
                throw new InvalidParameterException();
            }*/
            String path = "/dev/ttySAC1";
            int baudrate = 9600;
			/* Open the serial port */
            mScanSerialPort = new SerialPort(new File(path), baudrate, 0, 2, 0);

        return mScanSerialPort;
    }

    public void closePrintSerialPort() {
        if (mPrintSerialPort != null) {
            mPrintSerialPort.close();
            mPrintSerialPort = null;
        }

    }
    public void closeScanSerialPort() {
        if (mScanSerialPort != null) {
            mScanSerialPort.close();
            mScanSerialPort = null;
        }

    }
    public void sendMessage(String result){

        /*String message_url = "http://utf8.sms.webchinese.cn/";
        String username = "btcatm";
        String key ="dae55ec097981a932a87";*/
    	
    	// AUTHOR: F, DATE: 2014.6.10
        String url = mSettingInfo.getSmsPlatformUrl();
        
        
        String content = "&smsText=有一笔交易发生";
        final Resources res = getResources();
        String mobile1 = res.getString(com.mybitcoin.wallet.R.string.msgmobile1);//"18201584183""18604766766";"18611147179";//;//&smsText=明天上午开会";有一笔交易发生
        String mobile2 = res.getString(com.mybitcoin.wallet.R.string.msgmobile2);
        StringBuilder sb = new StringBuilder(url);
        sb.append(mobile1).append(content);
            /*.append("交易时间").append(new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(datetime))
                .append("交易类型").append(type)
                .append("转出地址").append(address)
                .append("转出比特币数量").append(bitcoinAount)
                .append("花费金额").append(coinAmount).append("元")
                .append("交易结果").append(result);*/
        String post1 = sb.toString();
        sb = new StringBuilder(url);
        sb.append(mobile2).append(content);
        String post2 = sb.toString();
        log.info("smsmessage1 is :"+post1);
        log.info("smsmessage2 is :"+post2);


        try{
            HttpClient client  = new DefaultHttpClient();
            HttpPost httpPost1 = new HttpPost(post1);
            HttpPost httpPost2 = new HttpPost(post2);

            client.execute(httpPost1);
            client.execute(httpPost2);
//            PostMethod post = new PostMethod(message_url);
//            log.info("The result is :begin1!");
            //GetMethod get = new GetMethod();
            //post.setRequestHeader("Content-type","application/x-www-form-urlencoded;charset=utf8");
            /*get.setRequestHeader("Content-type","application/x-www-form-urlencoded;charset=utf8"
            NameValuePair[] data = {new NameValuePair("Uid",username),new NameValuePair("Key",key),
                    new NameValuePair("smsMob","18201584183"),new NameValuePair("smsText","胡总")};
            //post.setRequestBody(data);
*/
//            log.info("The result is :begin2!");
            // client.executeMethod(post);

//            HttpResponse response = client.execute(httpPost1);
//            int statuscode = response.getStatusLine().getStatusCode();
            log.info("The result is :success!");
            /*Header[] headers = post.getResponseHeaders();
            int statusCode = post.getStatusCode();
            log.info("statusCode is :"+statusCode);
            for(Header h:headers){
                log.info("header is :"+h.toString());
            }
            String result = new String(post.getResponseBodyAsString().getBytes("utf8"));
            log.info("The result is :"+result);*/
        /*}catch(UnsupportedEncodingException exception){
            //log.debug("Send message had excpeiton :"+exception.printStackTrace());
        }catch(IOException ioe){*/
            //log.debug("Send message had excpeiton :"+ioe.printStackTrace());
        }catch(Exception e){

        }
    }
}
