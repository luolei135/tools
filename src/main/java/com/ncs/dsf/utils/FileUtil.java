package com.ncs.dsf.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by luolei on 9/27/2016.
 */
public class FileUtil {

    protected static MessageDigest messagedigest = null;
    static{
        try{
            messagedigest = MessageDigest.getInstance("MD5");
        }catch(NoSuchAlgorithmException nsaex){
            System.err.println(FileUtils.class.getName()+"init failed,MessageDigest not support MD5Util.");
//            StringUtil.printExceptionTrace(nsaex);
        }
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9','a', 'b', 'c', 'd', 'e', 'f' };

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }


    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = hexDigits[(bt & 0xf0) >> 4];
        char c1 = hexDigits[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    public static String getFileMD5String(String filePath){
        FileInputStream fis = null;
        StringBuffer sb = new StringBuffer();
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(filePath);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
            byte[] mdbytes = md.digest();

            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            System.out.println("Digest(in hex format):: " + sb.toString());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try{
                fis.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    public static void main(String[] args) throws IOException {
        File input = new File("C:\\DSF\\log\\weblog.log");
        List<String> strs = FileUtils.readLines(input);
        File output = new File("C:\\DSF\\log\\output.txt");
        FileUtils.write(output,"",false);
        for(String str:strs){
            if(str.indexOf("INSERT INTO")!=-1){
                FileUtils.write(output,str+"\r\n",true);
            }
        }
    }
}
