/**
 *  AUTHOR: F
 *  DATE: 2014.6.9
 */

package com.mybitcoin.wallet.environment;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.RandomAccessFile;

public final class MemoryInfo {
    public static final String MEMORY_USAGE = "memory_usage";
    public static final String TIME_PERIOD = "time_period";

    /**
     * Return the first and second line of /proc/meminfo or null if failed.
     */
    private static String readSystemMeminfo() {

        RandomAccessFile reader = null;
        String memTotalStr = null;
        String memFreeStr = null;

        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            memTotalStr = reader.readLine(); // the first line: MemTotal
            memFreeStr = reader.readLine();  // the second line: MemFree

            memTotalStr = memTotalStr.replace('\t', ' ');
            memFreeStr = memFreeStr.replace('\t', ' ');

            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return memTotalStr + " " + memFreeStr;
    }

    /**
     * Return the memory usage of the system, in percent.
     *
     * @return 30.25 for a memory usage of 30.25% or -1 if the value is not
     * available.
     */
    public static float getSystemMemUsage() {
        try {
            String[] strArr = readSystemMeminfo().split("\\s+");

            String totalMemStr = strArr[1];
            String freeMemStr = strArr[4];

            float totalMem = Integer.valueOf(totalMemStr).floatValue();
            float freeMem = Integer.valueOf(freeMemStr).floatValue();

            return (totalMem - freeMem) / totalMem * 100;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Return the memory used by the system, in MB.
     *
     * @return 100.23 for a memory used 100.23MB of system total memory or -1 if the value is not
     * available.
     */
    public static float getSystemMemUsed() {
        try {
            String[] strArr = readSystemMeminfo().split("\\s+");

            String totalMemStr = strArr[1];
            String freeMemStr = strArr[4];

            float totalMem = Integer.valueOf(totalMemStr).floatValue();
            float freeMem = Integer.valueOf(freeMemStr).floatValue();

            return (totalMem - freeMem) / 1024;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Return the memory used by the process, in MB.
     *
     * @return 20.31 for a memory used 20.31MB
     */
    public static float getProcessMemUsed(@Nonnull Context context, int pid) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        int[] pids = {pid};
        android.os.Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(pids);

        return memoryInfoArray[0].getTotalPss() / 1024; // covert KB to MB
    }


    /**
     * Return the memory usage by the current process, in MB.
     *
     * @return 20.31 for a memory used 20.31MB
     */
    public static float getCurrentProcessMemUsed(@Nonnull Context context) {
        return getProcessMemUsed(context, Process.myPid());
    }

    /**
     * Return the memory usage of the process, in percent.
     *
     * @return 30.25 for a memory usage of 30.25% or -1 if the value is not
     * available.
     */
    public static float getProcessMemUsage(@Nonnull Context context, int pid) {
        try {
            String[] strArr = readSystemMeminfo().split("\\s+");

            String totalMemStr = strArr[1];

            float totalMem = Integer.valueOf(totalMemStr).floatValue() / 1024;

            float processMem = getProcessMemUsed(context, pid);

            return processMem / totalMem * 100;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Return the memory usage of the current process, in percent.
     *
     * @return 30.25 for a memory usage of 30.25% or -1 if the value is not
     * available.
     */
    public static float getCurrentProcessMemUsage(@Nonnull Context context) {
        return getProcessMemUsage(context, Process.myPid());
    }
}
