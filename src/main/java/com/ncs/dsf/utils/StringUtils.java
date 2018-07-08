package com.ncs.dsf.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luolei on 7/12/2017.
 */
public class StringUtils {
    public static String convertColumnNameToVariableName(String columnName) {
        if (columnName == null || "".equals(columnName) || columnName.length() <= 1){
            return columnName;
        }
        if(columnName.charAt(0)>=97)return columnName;

        char[] chars = columnName.toLowerCase().toCharArray();
        char[] _after = new char[chars.length];
        int j=0;
        for(int i=0;i<chars.length;i++){
            if(((i-1)>0)&&chars[i-1]=='_'){
                _after[j]=(char)(chars[i]-32);
                j++;
            }else if(chars[i]=='_'){
                //remove _
            }else{
                _after[j] = chars[i];
                j++;
            }
        }
        return String.valueOf(_after,0,j);
    }
    public static String printExceptionTrace(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw, true));
        System.out.println(sw.toString());
        return sw.toString();
    }

    public static String printExceptionTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw, true));
        System.out.println(sw.toString());
        StringBuilder sb = new StringBuilder();
        String exStr = sw.toString();
        exStr = exStr.replaceAll("\n", "\n" + sb.toString());
        return sb.toString() + " " + exStr;
    }
    public static boolean isBlank(String str){
        if(str==null) return true;
        if(str.trim().equals("")) return true;
        return false;
    }
    public static boolean isNotBlank(String str){
      return !isBlank(str);
    }
}
