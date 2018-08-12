package com.bsoft.deploy.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 * Created on 2018/8/10.
 *
 * @author yangl
 */
public class DateUtils {
    /**
     * 获取当前时间字符串
     * @param format 指定解析的日期格式
     * @return
     */
    public static String getNow(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar c = Calendar.getInstance(Locale.CHINA);
        c.setTime(new Date());
        return sdf.format(c.getTime());
    }

    public static void main(String[] args) {
        System.out.println(DateUtils.getNow("yyyy-MM-dd"));
    }
}
