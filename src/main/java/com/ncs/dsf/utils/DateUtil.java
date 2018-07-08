package com.ncs.dsf.utils;

import org.apache.tools.ant.util.DateUtils;

import java.text.SimpleDateFormat;


public class DateUtil {
    public static String currentTime(){
        SimpleDateFormat dateFormat=   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d = dateFormat.format(new java.util.Date());
      return  d;
    }
}
