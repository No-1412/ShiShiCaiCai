/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTPower;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 方便的时间转换类
 *
 * @author jerry
 */
public class RTdate {

    /**
     * 时间转换时间戳
     *
     * @param TimeString 时间
     * @param TypeString yyyy-MM-dd HH:mm:ss 要处理的格式
     * @return 时间戳 返回秒的时间戳
     */
    public static long TimeToStemp(String TimeString, String TypeString) {
        long BackStemp = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TypeString);
        Date date;
        try {
            date = simpleDateFormat.parse(TimeString);
            BackStemp = (long) date.getTime() / 1000;
        } catch (ParseException ex) {
//           RTLog.d("时间转换时间戳失败" + TimeString);
            System.err.println("时间转换时间戳失败" + TimeString);
        }

        return BackStemp;
    }

    /**
     * 时间戳转换为时间
     *
     * @param StempInt 时间戳
     * @param TypeString yyyy-MM-dd HH:mm:ss 要处理的格式
     * @return
     */
    public static String StempToTime(long StempInt, String TypeString) {
        String BackTimeString;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TypeString);

        Date date = new Date(StempInt * 1000);
        BackTimeString = simpleDateFormat.format(date);
        return BackTimeString;
    }

    /**
     * 获得当前秒级别时间戳
     *
     * @return 返回秒的时间戳
     */
    public static long GetNowStemp() {
        long NowStemp;
        long Stemptime = System.currentTimeMillis();
        NowStemp = Stemptime / 1000;//获得截止秒的时间戳
        return NowStemp;
    }

    /**
     * 获得指定格式的时间字符
     *
     * @param TypeString "yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static String GetNowTime(String TypeString) {
        SimpleDateFormat df = new SimpleDateFormat(TypeString);//设置日期格式
        String now_time = df.format(new Date());
        return now_time;
    }
}
