package com.ncs.dsf.process;

import com.ncs.dsf.constants.Constants;
import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.utils.DBUtil;
import com.ncs.dsf.utils.StringUtils;
import com.ncs.dsf.vo.SyncVO;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Date;

/**
 * Created by luolei on 2016/6/27.
 */
public class SyncUp {


    public static final String indicator = "__";
    public String delete = "delete from DS_TRIGGERED_FORMS a where a.PROPOSAL_FORM_DTL_ID in (select b.PROPOSAL_FORM_DTL_ID from DS_PROPOSAL_FORM b join DS_PROPOSAL c on b.PROPOSAL_ID = c.PROPOSAL_ID where c.CASE_ID='_caseId_');\n" +
            "delete from DS_DOCUMENT_SIGN a  where a.DOCUMENT_ID in (select b.DOCUMENT_ID from DS_CASE_DOCUMENTS b where b.CASE_ID='_caseId_');\n" +
            "delete from DS_FILE a where a.FILE_ID in (select b.FILE_ID from DS_CASE_DOCUMENTS b where b.CASE_ID = '_caseId_');\n" +
            "delete from DS_PROPOSAL_FORM a where a.PROPOSAL_ID in (select b.PROPOSAL_ID from DS_PROPOSAL b where b.CASE_ID = '_caseId_');\n" +
            "delete from DS_QUOTATION a where a.PROPOSAL_ID in (select b.PROPOSAL_ID from DS_PROPOSAL b where b.CASE_ID = '_caseId_');\n" +
            "delete from CM_TAG a where a.TAG_ID in (select b.TAG_ID from CM_CUST_TAG b  where b.CUSTOMER_ID='_customerId_');\n" +
            "delete from CM_INS_BENEFIT a where a.INS_PLAN_ID in (select b.INS_PLAN_ID from CM_INS_PLAN b where b.CUSTOMER_ID = '_customerId_' );\n" +
            "delete  from SE_CUSTOMER_NEED a where a.CUSTOMER_ID in (select b.SE_CUSTOMER_ID from DS_CASE b where b.CASE_ID='_caseId_');\n" +
            "delete from SE_CUSTOMER a where a.CUSTOMER_ID in (select b.SE_CUSTOMER_ID from DS_CASE b where b.CASE_ID='_caseId_');\n" +
            "delete from CM_CUSTOMER a where a.CUSTOMER_ID in (select b.DEP_ID from CM_CUST_DEP b  where b.CUSTOMER_ID='_customerId_');\n" +
            "delete  from CM_CUST_DEP where CUSTOMER_ID='_customerId_';\n" +
            "delete  from CM_CUST_DEP where CUSTOMER_ID='_customerId_';\n" +
            "delete  from DS_MFP_FORM where CASE_ID ='_caseId_';\n" +
            "delete  from CM_ENGAGEMENT_HIST where CASE_ID = '_caseId_';\n" +
            "delete  from SE_INS_PLAN  a where a.CUSTOMER_ID in (select b.SE_CUSTOMER_ID from DS_CASE b  where b.CASE_ID='_caseId_');\n" +
            "delete  from CM_CUSTOMER where CUSTOMER_ID='_customerId_';\n" +
            "delete  from DS_CASE_DOCUMENTS where CASE_ID = '_caseId_' or PROPOSAL_ID='_caseId_';\n" +
            "delete  from DS_PROPOSAL where CASE_ID = '_caseId_';\n" +
            "delete  from DS_CASE where CASE_ID = '_caseId_';\n" +
            "delete  from DS_TASK where CUSTOMER_ID = '_customerId_';\n" +
            "delete  from CM_INS_PLAN where CUSTOMER_ID='_customerId_';\n" +
            "delete  from CM_CUST_TAG where CUSTOMER_ID='_customerId_';\n" +
            "delete  from SE_CASHFLOW_BUDGET a where a.CUSTOMER_ID in (select b.SE_CUSTOMER_ID from DS_CASE b where b.CASE_ID='_caseId_');\n" +
            "delete  from SE_ASSETS_DETAILS a where a.CUSTOMER_ID in (select b.SE_CUSTOMER_ID from DS_CASE b  where b.CASE_ID='_caseId_');\n" +
            "delete  from DS_POLICY_SERVICING where CUSTOMER_ID ='_customerId_';";

    public SyncVO process(SyncVO vo) throws ToolException {
        File file = new File(vo.syncUpTargetFolder());
        if(file==null||file.listFiles()==null){
            throw new ToolException("Case data not found in local, please check again");
        }
        File[] files = file.listFiles();
        for(File f:files){
            if(!f.getName().startsWith("File_")&&!f.getName().startsWith("null")){
                vo.setCaseId(f.getName().split(Constants.indicator)[0]);
                vo.setCustomerId(f.getName().split(Constants.indicator)[1]);
                break;
            }
        }


        delete = delete.replaceAll("_caseId_", vo.getCaseId()).replaceAll("_customerId_", vo.getCustomerId());
        System.out.println(delete);
        Connection connection = null;
        try {
            connection = new DBUtil().getConn(vo.getSyncUpServer());
            connection.setAutoCommit(false);
            Statement s = connection.createStatement();
            String[] _delete = delete.split(";");
            for (String d : _delete) {
                s.addBatch(d);
            }
            s.executeBatch();
            s.close();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ToolException("Class not found:"+e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ToolException("SQLException:"+e.getMessage());
        }


        System.out.println("delete completed");

        long start = System.currentTimeMillis();
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (!f.getName().startsWith("File__")) {
                try {
                    loadSql(f, vo);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    throw new ToolException("ClassNotFoundException:"+e.getMessage());
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new ToolException("SQLException:"+e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new ToolException("IOException:"+e.getMessage());
                }
            }
        }
        vo.setErrorMessage("Sync Up successfuly "+vo.getSyncUpCase());
        long end = System.currentTimeMillis();
        System.out.print("Cost:" + (end - start) + "ms");
        return vo;
    }

    private void loadSql(File file, SyncVO vo) throws ClassNotFoundException, SQLException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        Connection connection = new DBUtil().getConn(vo.getSyncUpServer());
        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();


        String sql;
        int i = 0;
        while ((sql = reader.readLine()) != null) {

            if (StringUtils.isNotBlank(sql)) {
                System.out.println(sql);
                boolean isFile = false;
                String callFile = null;
                if (sql.indexOf("'File__") != -1) {
                    int start = sql.indexOf("'File__");
                    System.out.println("start==>" + start);
                    int end = start + sql.substring(start + 1).indexOf("'") + 1;
                    System.out.println("end==>" + end);
                    callFile = sql.substring(start + 1, end);

                    if (FileUtils.sizeOf(new File(vo.syncUpTargetFolder() + callFile)) > 0) {
                        isFile = true;
                    }
                }
                if (isFile) {
                    String callSql = sql.replace("\'" + callFile + "\'", "?");
                    System.out.println("callSQl:" + callSql);
                    CallableStatement call = connection.prepareCall(callSql.substring(0, callSql.length() - 1));
                    call.setClob(1, new FileReader(new File(vo.syncUpTargetFolder() + callFile)));
                    call.execute();
                    try {
                        call.close();
                    } catch (Exception e) {
                        e.printStackTrace();
//                        System.exit(1);
                    }

                } else {
                    if (i == 0) {
                        System.out.println(new Date() + ": write " + i + " record " + file.getName());
                    }
                    if (!sql.startsWith("--")) {
                        i++;
                        statement.addBatch(sql.substring(0, sql.length() - 1));

                    }


                    if (i % 1 == 0) {
                        System.out.println(new Date() + ": write " + i + " record " + file.getName());
                        processStatement(file, connection, statement);
                    }
                }
            }
        }
    }

    private void processStatement(File file, Connection connection, Statement statement) {
        try {
            statement.executeBatch();
            connection.commit();
            statement.clearBatch();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error:" + file.getName());
            try {
                connection.commit();
                statement.clearBatch();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

}
