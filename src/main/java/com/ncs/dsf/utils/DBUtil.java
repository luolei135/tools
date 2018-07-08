package com.ncs.dsf.utils;

import com.ncs.dsf.constants.Constants;
import com.ncs.dsf.core.RequestContextHolder;
import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.vo.SyncVO;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBUtil {

    public static void main(String[] args) throws ToolException {
        SyncVO vo = new SyncVO();
        vo.setRootFolder("D:\\DSF\\log\\DSFUAT");
        new DBUtil().init(vo);
    }

    public void init(SyncVO vo) throws ToolException {
        List list = queryByNativeSql("SELECT count(*) FROM sqlite_master",new ArrayList<Object>(), Integer.class,vo);
        LogUtil.info(list.get(0)+" found in database before init");
        if((int)list.get(0)==0){
            String createTable="CREATE TABLE AUDIT_LOG\n" +
                    "(\n" +
                    "    ID INTEGER PRIMARY KEY autoincrement,\n" +
                    "    ACTION TEXT,\n" +
                    "    CASE_NO TEXT,\n" +
                    "    TYPE TEXT,\n" +
                    "    CUSTOMER_NAME TEXT,\n" +
                    "    SYNC_START_TIME TEXT,\n" +
                    "    SYNC_COMPLETE_TIME TEXT,\n" +
                    "    REMARKS TEXT,\n" +
                    "    CREATE_TIME TEXT,\n" +
                    "    LAST_UPDATE_TIME TEXT\n" +
                    ")";
            Connection connection = getMetaConn(vo);
            try {
               Statement statement =  connection.createStatement();
               statement.execute(createTable);
               statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
         list = queryByNativeSql("SELECT count(*) FROM sqlite_master",new ArrayList<Object>(), Integer.class,vo);
        LogUtil.info(list.get(0)+" found in database after init");

    }
    public <T> List<T> queryByNativeSql(String sql, List<Object> params,
                                               Class<T> cls,SyncVO vo) throws ToolException {

        long start = System.currentTimeMillis();
        List<T> list = new ArrayList<T>();
        try {

            PreparedStatement pstmt =null;
            ResultSet resultSet;
            int index = 1;

            Connection connection = this.getMetaConn(vo);

            pstmt = connection.prepareStatement(sql);
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    //convert java.util.Date to java.sql.Date when we use jdbc PreparedStatement
                    Object temp = params.get(i);
                    if (temp instanceof java.util.Date) {
                        temp = new java.sql.Date(((java.util.Date) temp).getTime());
                    }
                    pstmt.setObject(index++, temp);
                }
            }
            resultSet = pstmt.executeQuery();// missing in or out parameter
            ResultSetMetaData metaData = resultSet.getMetaData();

            int cols_len = metaData.getColumnCount();
            while (resultSet.next()) {
                T resultObject = null;
                if(cls == String.class){
                    resultObject = (T) resultSet.getString(1);
                }else if( cls == Integer.class){
                    resultObject = (T) Integer.valueOf(resultSet.getInt(1));
                }else{
                    resultObject = cls.newInstance();
                    resultSetToVO(resultSet, metaData, cols_len, resultObject);
                }
                list.add(resultObject);
            }
        } catch (Exception e) {
            LogUtil.error("Exception queryByNativeSql" + StringUtils.printExceptionTrace(e));
            throw new ToolException(e);
        }
        return list;
    }

    private <T> void resultSetToVO(ResultSet resultSet, ResultSetMetaData metaData, int cols_len, T resultObject) throws SQLException, IllegalAccessException, IOException {
        Field[] fields = resultObject.getClass().getDeclaredFields();
        Field[] parentFields = null;
        if (resultObject.getClass() != null && resultObject.getClass().getSuperclass() != null) {
            parentFields = resultObject.getClass().getSuperclass().getDeclaredFields();
        }
        for (int i = 0; i < cols_len; i++) {
            String cols_name = metaData.getColumnName(i + 1);
            Object cols_value = resultSet.getObject(cols_name);
            boolean setInd = false;

            //set BaseVO fields
            setInd = setVOFields(resultObject, parentFields, cols_name, cols_value);

            if (!setInd) {
                setInd = setVOFields(resultObject, parentFields, StringUtils.convertColumnNameToVariableName(cols_name), cols_value);
            }
            //set currentVO fields
            if (!setInd) {
                setInd = setVOFields(resultObject, fields, cols_name, cols_value);
            }
            //set Properties fields
            if (!setInd) {
                try {

                    setProperty(resultObject, cols_name, cols_value);

                    setInd = true;
                } catch (Exception e) {
                    LogUtil.error(StringUtils.printExceptionTrace(e));
                    setInd = false;
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private static <T> void setProperty(T resultObject, String cols_name, Object cols_value) {
        String fieldName = StringUtils.convertColumnNameToVariableName(cols_name);

        try {
            Field field = resultObject.getClass().getDeclaredField(fieldName);
            if (field == null) {
                LogUtil.info("fieldName not found in " + resultObject.getClass().getName());
            }

            field.setAccessible(true);
            field.set(resultObject, cols_value);
        } catch (NoSuchFieldException e) {
            LogUtil.error(StringUtils.printExceptionTrace(e));
        } catch (IllegalAccessException e) {
            LogUtil.error(StringUtils.printExceptionTrace(e));
        }

    }

    private static <T> boolean setVOFields(T resultObject, Field[] parentFields, String cols_name, Object cols_value) throws IllegalAccessException, SQLException, IOException {
        for (Field f : parentFields) {
            if (cols_name.equalsIgnoreCase(f.getName())) {
                f.setAccessible(true);
                f.set(resultObject, cols_value);
                return true;
            }

        }
        return false;
    }
    public Connection getMetaConn(SyncVO vo) {
        return getMetaConn(vo,false);
    }

    public Connection getMetaConn(SyncVO vo,boolean newCon) {

        if(newCon){
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Connection connection = null;
            if (connection == null) {
                try {
                    connection = DriverManager.getConnection("jdbc:sqlite:" + vo.getRootFolder() + Constants.META_DB);
                    return connection;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("connection is " + connection);
            return connection;
        }else{
           return RequestContextHolder.getDSFRequestContext().getConnection();
        }

    }

    public Connection getConn(String env) throws ClassNotFoundException {
//        Class.forName("org.sqlite.JDBC");
        Class.forName("oracle.jdbc.OracleDriver");
        Connection connection = null;
        if (connection == null) {
            try {
                switch (env) {
                    case Constants.DatabaseServers.CD:
                        connection = DriverManager.getConnection("jdbc:oracle:thin:@172.31.170.95:1521:DSFDEV", "dsfdev", "Dsfdev1");
                        break;
                    case Constants.DatabaseServers.SIT:
                        connection = DriverManager.getConnection("jdbc:oracle:thin:@10.22.3.36:1521/dsfdevt", "dsfdev", "Dsfdev123");
                        break;
                    case Constants.DatabaseServers.local:
                        connection = DriverManager.getConnection("jdbc:oracle:thin:@//localhost:1521/dsfdev", "dsfdev", "Dsfdev1");
                        break;
                    case Constants.DatabaseServers.UAT:
                        connection = DriverManager.getConnection("jdbc:oracle:thin:@10.22.3.37:1521/dsfweb", "dsfuat2", "Dsfuat2");
                        break;
                }
                return connection;
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
        System.out.println("connection is " + connection);
        return connection;
    }


}
