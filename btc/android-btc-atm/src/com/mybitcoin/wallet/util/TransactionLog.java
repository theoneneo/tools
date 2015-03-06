package com.mybitcoin.wallet.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.os.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by zhuyun on 14-4-20.
 */
public class TransactionLog {


    private static String MYLOG_PATH_DIR="TransactionLog";// 日志文件在sdcard中的路径
    private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
    private static String MYLOGFILEName = "Trans_log_";// 本类输出的日志文件名称
    private static SimpleDateFormat myLogSdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");// 日志的输出格式
    private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式
    private static Logger log = LoggerFactory.getLogger(TransactionLog.class);

    /**
     * 打开日志文件并写入日志
     *
     * @return
     * **/
    public  static void writeLogtoFile(String type, String address, String coinAmount,String bitcoinAount, String result) {
        Date nowtime = new Date();
        String needWriteFile = logfile.format(nowtime);
        StringBuilder sb = new StringBuilder();
        sb.append("交易时间：").append(myLogSdf.format(nowtime)).append("    " )
                .append("交易类型：").append((type.equals("0")?"扫描二维码":"打印纸线包")).append("    " )
                .append("转出地址：").append(address).append("    " )
                .append("转出比特币数量：").append(bitcoinAount).append("    " )
                .append("花费金额：").append(coinAmount).append("元").append("    " )
                .append("交易结果：").append(result);
        File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator+MYLOG_PATH_DIR+File.separator+MYLOGFILEName+needWriteFile+".log");
        try {log.info("file is :"+file.getAbsolutePath());
            if(!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            log.info("path is :"+file.getParentFile().getAbsolutePath());
            FileWriter filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
            BufferedWriter bufWriter = new BufferedWriter(filerWriter);
            bufWriter.write(sb.toString());
            bufWriter.newLine();
            bufWriter.close();
            filerWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 删除制定的日志文件
     * */
    public static void delFile() {// 删除日志文件
        String needDelFile = logfile.format(getDateBefore());
        File file = new File(Environment.getExternalStorageDirectory().toString()+File.separator+MYLOG_PATH_DIR+File.separator+MYLOGFILEName+needDelFile+".log");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
     * */
    private static Date getDateBefore() {
        Date nowtime = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowtime);
        now.set(Calendar.DATE, now.get(Calendar.DATE)
                - SDCARD_LOG_FILE_SAVE_DAYS);
        return now.getTime();
    }
}
