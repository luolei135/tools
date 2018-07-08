package com.ncs.dsf.process;

import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.utils.DBUtil;
import com.ncs.dsf.vo.SyncVO;
import oracle.sql.TIMESTAMP;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by luolei on 2016/6/27.
 */
public class SyncDown {


    public final String[] selects = {
            "select a.* from DS_TRIGGERED_FORMS a join DS_PROPOSAL_FORM b on a.PROPOSAL_FORM_DTL_ID = b.PROPOSAL_FORM_DTL_ID left join DS_PROPOSAL c on b.PROPOSAL_ID = c.PROPOSAL_ID where c.CASE_ID='_caseId_' or b.PROPOSAL_ID='_caseId_' order by 1",
            "select a.* from DS_DOCUMENT_SIGN a join DS_CASE_DOCUMENTS b on a.DOCUMENT_ID = b.DOCUMENT_ID where b.CASE_ID='_caseId_' or b.PROPOSAL_ID='_caseId_' order by 1",
            "select a.* from DS_FILE a join DS_CASE_DOCUMENTS b on a.FILE_ID = b.FILE_ID where b.CASE_ID = '_caseId_' or b.PROPOSAL_ID='_caseId_' order by 1",
            "select a.* from DS_PROPOSAL_FORM a left join DS_PROPOSAL b on a.PROPOSAL_ID = b.PROPOSAL_ID where b.CASE_ID = '_caseId_' or a.PROPOSAL_ID='_caseId_' order by 1",
            "select a.* from DS_QUOTATION a join DS_CASE_DOCUMENTS b on a.PROPOSAL_ID = b.PROPOSAL_ID where b.CASE_ID = '_caseId_' order by 1",
            "select a.* from CM_TAG a join CM_CUST_TAG b on a.TAG_ID = b.TAG_ID where b.CUSTOMER_ID='_customerId_' order by 1",
            "select a.* from CM_INS_BENEFIT a join CM_INS_PLAN b on a.INS_PLAN_ID = b.INS_PLAN_ID where b.CUSTOMER_ID = '_customerId_' order by 1",
            "select * from SE_CUSTOMER_NEED a join DS_CASE b on a.CUSTOMER_ID = b.SE_CUSTOMER_ID where b.CASE_ID='_caseId_' order by 1",
            "select a.* from SE_CUSTOMER a join DS_CASE b on a.CUSTOMER_ID = b.SE_CUSTOMER_ID where b.CASE_ID='_caseId_' order by 1",
            "select a.* from CM_CUSTOMER a join CM_CUST_DEP b on a.CUSTOMER_ID = b.DEP_ID where b.CUSTOMER_ID='_customerId_' order by 1",
            "select * from CM_CUST_DEP where CUSTOMER_ID='_customerId_' order by 1",
            "select * from CM_CUST_DEP where CUSTOMER_ID='_customerId_' order by 1",
            "select * from DS_MFP_FORM where CASE_ID ='_caseId_' order by 1",
            "select * from CM_ENGAGEMENT_HIST where CASE_ID = '_caseId_' order by 1",
            "select * from SE_INS_PLAN  a join DS_CASE b on a.CUSTOMER_ID = b.SE_CUSTOMER_ID where b.CASE_ID='_caseId_' order by 1",
            "select * from CM_CUSTOMER where CUSTOMER_ID='_customerId_' order by 1",
            "select * from DS_CASE_DOCUMENTS where CASE_ID = '_caseId_' or PROPOSAL_ID='_caseId_' order by 1",
            "select * from DS_PROPOSAL where CASE_ID = '_caseId_' order by 1",
            "select * from DS_CASE where CASE_ID = '_caseId_' order by 1",
            "select * from DS_TASK where CUSTOMER_ID = '_customerId_' order by 1",
            "select * from CM_INS_PLAN where CUSTOMER_ID='_customerId_' order by 1",
            "select * from CM_CUST_TAG where CUSTOMER_ID='_customerId_' order by 1",
            "select * from SE_CASHFLOW_BUDGET a join DS_CASE b on a.CUSTOMER_ID = b.SE_CUSTOMER_ID where b.CASE_ID='_caseId_' order by 1",
            "select * from SE_ASSETS_DETAILS a join DS_CASE b on a.CUSTOMER_ID = b.SE_CUSTOMER_ID where b.CASE_ID='_caseId_' order by 1",
            "select * from DS_POLICY_SERVICING where CUSTOMER_ID ='_customerId_' order by 1",

    };

    public final String FROM = "FROM ";

    private String getTable(String selectSQL) {
        selectSQL = selectSQL.toUpperCase();
        int start = selectSQL.indexOf(FROM) + FROM.length();
        int end = selectSQL.substring(start).indexOf(" ") + start;
        return selectSQL.substring(start, end);
    }

    private void prepareCaseIdAndCustomerId(SyncVO vo) throws ClassNotFoundException, SQLException {
        String sql = "select case_id, CM_CUSTOMER_ID from ds_case where case_no = ? union all select POL_SERV_ID,CUSTOMER_ID from DS_POLICY_SERVICING  where POL_SERV_NO=?  ";
        Connection connection = new DBUtil().getConn(vo.getSyncDownServer());
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, vo.getSyncDownCase());
        statement.setString(2, vo.getSyncDownCase());
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            vo.setCaseId(resultSet.getString(1));
            vo.setCustomerId(resultSet.getString(2));
        }
    }


    public SyncVO process(SyncVO vo) throws IOException, ClassNotFoundException, SQLException, ToolException {

        System.out.println("start...");
        long start = System.currentTimeMillis();
        System.out.println("prepare caseId and customerId...");
        prepareCaseIdAndCustomerId(vo);
        if(vo.getCustomerId()==null){
            throw new ToolException("Case No not found in "+vo.getSyncDownServer());
        }

        FileUtils.write(vo.syncDownTargetFile(), "", false);


        for (String sql : selects) {
            sql = sql.replaceAll("_caseId_", vo.getCaseId()).replaceAll("_customerId_", vo.getCustomerId());
            prepareInsertScripts(sql, vo);
        }

        long end = System.currentTimeMillis();
        vo.setErrorMessage("Sync down successfully:" + vo.getSyncDownCase());
        System.out.println("Sync down complete: " + (end - start) + "ms");
        return vo;
    }


    private Map<String, String> getCaseInfoByCaseNo(String caseNo) {
        return null;
    }

    public List<String> prepareInsertScripts(String selectSQL, SyncVO vo) throws ClassNotFoundException, SQLException, IOException {
        int count = 0;
        List<String> result = new ArrayList<String>();
        String table = getTable(selectSQL);
        System.out.println("selectSQL==>" + selectSQL);

        String sql = selectSQL;
        selectSQL = selectSQL.substring(0, 20);

        PreparedStatement pstmt;
        ResultSet resultSet;
        Connection connection = new DBUtil().getConn(vo.getSyncDownServer());
        pstmt = connection.prepareStatement(sql);
        resultSet = pstmt.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int cols_len = metaData.getColumnCount();
        int size = 0;
        while (resultSet.next()) {
            StringBuilder sb = new StringBuilder();
            String delete = prepareDeleteStr(table, resultSet, metaData, cols_len, sb);
            result.add(sb.toString());
            sb = new StringBuilder();
            selectSQL = prepareInsertStr(table, resultSet, metaData, cols_len, sb, vo);
            result.add(sb.toString());
            if (result.size() != 0 && result.size() % 10000 == 0) {
                size = size + 1;
                System.out.println(new Date() + ": write " + size + " records for " + table);
                FileUtils.writeLines(vo.syncDownTargetFile(), result, true);
                result.clear();
            }
        }
        size = size + result.size();
        System.out.println(new Date() + ": write " + size + " records for " + table);
        FileUtils.writeLines(vo.syncDownTargetFile(), result, true);

        return result;
    }

    private String prepareDeleteStr(String table, ResultSet resultSet, ResultSetMetaData metaData, int cols_len, StringBuilder sb) throws SQLException {
        sb.append("delete from ");
        table = mappingTableName(table);
        sb.append(table);
        sb.append(" where ");
        sb.append(metaData.getColumnName(1));
        sb.append("=");
        sb.append("\'");
        sb.append(resultSet.getString(1));
        sb.append("\';");
        return sb.toString();
    }

    private String prepareInsertStr(String table, ResultSet resultSet, ResultSetMetaData metaData, int cols_len, StringBuilder sb, SyncVO vo) throws SQLException {
        sb.append("insert into ");
        table = mappingTableName(table);
        sb.append(table);


        sb.append(" (");
        for (int i = 0; i < cols_len; i++) {
            String cols_name = metaData.getColumnName(i + 1);
            cols_name = mappingColumn(table, cols_name);
            sb.append(cols_name);
            if ((i + 1) < cols_len) {
                sb.append(",");
            }

        }
        sb.append(" ) values (");
        for (int i = 0; i < cols_len; i++) {
            String cols_name = metaData.getColumnName(i + 1);
            Object cols_value = resultSet.getObject(cols_name);

            int type = metaData.getColumnType(i + 1);
            type = manualSetColumnType(table, cols_name, type);

            if (type == Types.NULL) {
//                    sb.append("'");
                sb.append("null");
//                    sb.append("'");
            } else if (type == Types.INTEGER || Types.NUMERIC == type) {
//                    sb.append("'");
                sb.append(cols_value);
//                    sb.append("'");
            } else if (type == Types.VARCHAR || type == Types.NVARCHAR) {
                sb.append("'");
                if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("CREATED_BY")) {
                    sb.append("SYSTEM");
                } else if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("LAST_UPDATED_BY")) {
                    sb.append("SYSTEM");
                } else if (table.equals("DS_PARAM_TYPE") && cols_name.equals("CREATED_BY")) {
                    sb.append("SYSTEM");
                } else if (table.equals("DS_PARAM_TYPE") && cols_name.equals("LAST_UPDATED_BY")) {
                    sb.append("SYSTEM");
                } else if (table.equals("DS_PARAM_TYPE") && cols_name.equals("ISDELETED")) {
                    sb.append("N");
                } else if (cols_value != null) {
                    sb.append(checkNull(replace((String) cols_value), cols_name, table));
                } else {
                    sb.append(checkNull(replace((String) " "), cols_name, table));
                }

                sb.append("'");
            } else if (type == Types.DATE) {
                if (cols_value == null) {
                    sb.append("null");
                } else {
                    if ("LAST_UPDATED_TIME".equals(cols_name) || "CREATED_TIME".equals(cols_name)) {
                        prepareDate(table, sb, cols_name, cols_value);
                    } else {
                        prepareDate(table, sb, cols_name, cols_value);
                    }

                }

            } else if (type == Types.TIMESTAMP) {
                if (cols_value == null) {
                    sb.append("null");
                } else {
                    if ("LAST_UPDATED_TIME".equals(cols_name) || "CREATED_TIME".equals(cols_name)) {
                        prepareTimeStamp(sb, (Serializable) cols_value);
                    } else {
                        prepareTimeStamp(sb, (Serializable) cols_value);
                    }
                }
            } else if (type == Types.CLOB) {

                String val = oracleClob2Str((Clob) cols_value);
                try {
                    FileUtils.write(new File(vo.syncDownTargetFolder() + "File__" + resultSet.getObject(1)), val, false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sb.append("\'File__");
                sb.append(resultSet.getObject(1));
                sb.append("\'");
            } else {
                System.err.println("unknown type " + table + "." + cols_name + ";" + type);
            }

            if ((i + 1) < cols_len) {
                sb.append(",");
            }
        }
        sb.append(" );");
        return table;
    }

    private void prepareDate(String table, StringBuilder sb, String cols_name, Object cols_value) {
        sb.append("to_date('");
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("CREATED_TIME")) {
            sb.append("2016-01-01 00:00:00");
        } else if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("LAST_UPDATED_TIME")) {
            sb.append("2016-01-01 00:00:00");
        } else if (table.equals("DS_PARAM_TYPE") && cols_name.equals("CREATED_TIME")) {
            sb.append("2016-01-01 00:00:00");
        } else if (table.equals("DS_PARAM_TYPE") && cols_name.equals("LAST_UPDATED_TIME")) {
            sb.append("2016-01-01 00:00:00");
        } else if (table.equals("DS_PARAM")) {
            sb.append("2016-01-01 00:00:00");
        } else if (cols_value instanceof TIMESTAMP) {
            sb.append(((TIMESTAMP) cols_value).toString().substring(0, 19));
        } else if (formatDate((String) cols_value).length() >= 21) {
            sb.append(formatDate((String) cols_value).substring(0, 19));
        } else {
            sb.append(formatDate((String) cols_value));
        }

        sb.append("','yyyy-MM-dd HH24:MI:ss')");
    }

    private void prepareTimeStamp(StringBuilder sb, Serializable cols_value) {
        sb.append("to_timestamp('");
        try {
            sb.append(((TIMESTAMP) cols_value).timestampValue());
        } catch (Exception e) {
            try {
                sb.append(((Timestamp) cols_value).toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        sb.append("','yyyy-MM-dd HH24:MI:ss.FF')");
    }

    public String oracleClob2Str(Clob clob) {
        try {
            return (clob != null ? clob.getSubString(1, (int) clob.length()) : null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String mappingTableName(String table) {
        if (table.equals("DS_MFPFORM")) {
            table = "DS_MFP_FORM";
        }
        return table;
    }

    private int manualSetColumnType(String table, String cols_name, int type) {
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("CREATED_TIME")) {
            type = Types.DATE;
        }
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("LAST_UPDATED_TIME")) {
            type = Types.DATE;
        }
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("BIRTHDAY")) {
            type = Types.DATE;
        }
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("INSERT_TIME")) {
            type = Types.DATE;
        }
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("UPDATE_TIME")) {
            type = Types.DATE;
        }
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("ROWINSERTDATE")) {
            type = Types.DATE;
        }
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("RSTIMESTAMP")) {
            type = Types.DATE;
        }
        if (table.equals("DS_AGENT_INFO_DMGT") && cols_name.equals("REC_UPDATE_TIME")) {
            type = Types.DATE;
        }
        if ((table.indexOf("DS_POSTCODE") != -1) && cols_name.equals("CREATED_TIME")) {
            type = Types.DATE;
        }
        if ((table.indexOf("DS_POSTCODE") != -1) && cols_name.equals("LAST_UPDATED_TIME")) {
            type = Types.DATE;
        }


        return type;
    }

    private String mappingColumn(String table, String cols_name) {
        if (table.equals("DS_FILE") && cols_name.equals("FILE")) {
            cols_name = "\"FILE\"";
        }
        if (table.equals("CM_CUST_DEP") && cols_name.equals("RELATION_CDE")) {
            cols_name = "RELATION_CODE";
        }
        if (table.equals("CM_CUSTOMER") && cols_name.equals("POSTEL_CODE")) {
            cols_name = "POSTAL_CODE";
        }
        if (table.equals("CM_CUSTOMER") && cols_name.equals("ADDR1BLK")) {
            cols_name = "ADDR1_BLK";
        }
        if (table.equals("CM_CUSTOMER") && cols_name.equals("ADDR1UNT")) {
            cols_name = "ADDR1_UNT";
        }
        if (table.equals("CM_CUSTOMER") && cols_name.equals("ADDR1DESC")) {
            cols_name = "ADDR1_DESC";
        }
        if (table.equals("CM_CUSTOMER") && cols_name.equals("ADDR2DESC")) {
            cols_name = "ADDR2_DESC";
        }
        if (table.equals("CM_CUSTOMER") && cols_name.equals("ADDR3DESC")) {
            cols_name = "ADDR3_DESC";
        }
        if (table.equals("CM_CUSTOMER") && cols_name.equals("ADDR4DESC")) {
            cols_name = "ADDR4_DESC";
        }
        return cols_name;
    }


    private String formatDate(String str) {
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (c == 'T') {
                sb.append(' ');
            } else {
                sb.append(c);
            }

        }
        return sb.toString();
    }

    private String replace(String str) {
        char[] chars = str.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (c == '\'') {
                sb.append("''");
            } else {
                sb.append(c);
            }

        }
        return sb.toString();
    }

    private String checkNull(String str, String columnName, String table) {
        if ("PRODLIB_PROMOTION".equals(table) && "DIST_ID".equals(columnName)) {
            if (str == null || "".equals(str)) {
                return " ";
            }
        }
        return str;
    }
}
