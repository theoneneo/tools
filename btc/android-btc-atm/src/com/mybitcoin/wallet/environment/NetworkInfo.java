/**
 *  AUTHOR: F
 *  DATE: 2014.6.9
 */

package com.mybitcoin.wallet.environment;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class NetworkInfo {
    public static final String INTERFACE = "interface";
    public static final String THROUGHPUT_RX = "throughput_rx";
    public static final String THROUGHPUT_TX = "throughput_tx";
    public static final String TIME_PERIOD = "time_period";

    public static final String INTERFACE_WIRED0 = "eth0";
    public static final String INTERFACE_WIRELESS0 = "wlan0";

    /**
     * validate the network interface *
     */
    private static boolean validateNwkType(String networkInterface) {
        if (!networkInterface.equals(INTERFACE_WIRED0)
                && !networkInterface.equals(INTERFACE_WIRELESS0))
            return false;

        return true;
    }

    /**
     * Return the first line of /sys/class/net/XXX/statistics/rx_bytes or null if failed.
     */
    private static String readNwkRxBytes(String networkInterface) {
        if (!validateNwkType(networkInterface))
            return null;

        RandomAccessFile reader = null;
        String output = null;

        try {
            reader = new RandomAccessFile("/sys/class/net/" + networkInterface + "/statistics/rx_bytes", "r");
            output = reader.readLine();

            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return output;
    }

    /**
     * Return the first line of /sys/class/net/XXX/statistics/tx_bytes or null if failed.
     */
    private static String readNwkTxBytes(String networkInterface) {
        if (!validateNwkType(networkInterface))
            return null;

        RandomAccessFile reader = null;
        String output = null;

        try {
            reader = new RandomAccessFile("/sys/class/net/" + networkInterface + "/statistics/tx_bytes", "r");
            output = reader.readLine();

            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return output;
    }

    /**
     * Return the throughput of the network interface during the interval time, in MB/s.
     * <p>
     * The call is blocking for the time specified by interval time.
     * </p>
     *
     * @param networkInterface the network interface, spcecified by the linux, such as "eth0", "wlan0", etc. you can use NetworkInfo.INTERFACE_WIRED0, NetworkInfo.INTERFACE_WIRELESS0, etc.
     * @param interval         the time in milliseconds between reads.
     * @return 6.32, 3.21, 10,12 for a throughput of 6.32MB/s as Tx(out) throughput, Rx(in) throughput and Tx+RX(in+out) throughput or -1 if the value is not
     * available.
     */
    public static float[] syncGetNetworkThroughput(@Nonnull String networkInterface, int interval) {
        String txBytesStr1 = readNwkTxBytes(networkInterface);
        String rxBytesStr1 = readNwkRxBytes(networkInterface);
        if (txBytesStr1 == null || rxBytesStr1 == null) {
            float errorArr[] = {-1.f, -1.f, -1.f};
            return errorArr;
        }

        try {
            Thread.sleep(interval);
        } catch (Exception e) {
            e.printStackTrace();
            float errorArr[] = {-1.f, -1.f, -1.f};
            return errorArr;
        }

        String txBytesStr2 = readNwkTxBytes(networkInterface);
        String rxBytesStr2 = readNwkRxBytes(networkInterface);
        if (txBytesStr2 == null || rxBytesStr2 == null) {
            float errorArr[] = {-1.f, -1.f, -1.f};
            return errorArr;
        }

        float txBytes1, txBytes2, rxBytes1, rxBytes2;
        try {
            txBytes1 = Float.parseFloat(txBytesStr1);
            txBytes2 = Float.parseFloat(txBytesStr2);
            rxBytes1 = Float.parseFloat(rxBytesStr1);
            rxBytes2 = Float.parseFloat(rxBytesStr2);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            float errorArr[] = {-1.f, -1.f, -1.f};
            return errorArr;
        }

        float sumTxMB = (txBytes2 - txBytes1) / 1024 / 1024;   // covert Byte to MByte
        float sumRxMB = (rxBytes2 - rxBytes1) / 1024 / 1024;   // covert Byte to MByte
        float intervalSec = interval / 1000;

        float txThroughtput = sumTxMB / intervalSec;
        float rxThroughtput = sumRxMB / intervalSec;
        float txRxThroughtput = txThroughtput + rxThroughtput;

        float throughtputArr[] = {txThroughtput, rxThroughtput, txRxThroughtput};

        return throughtputArr;
    }
}
