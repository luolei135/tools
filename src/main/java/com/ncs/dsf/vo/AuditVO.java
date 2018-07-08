package com.ncs.dsf.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditVO extends Entity {
    private int id;
    private String action;
    private String caseNo;
    private String type;
    private String customerName;
    private String syncStartTime;
    private String syncCompleteTime;
    private String remarks;
    private String createTime;
    private String lastUpdateTime;

    public PreparedStatement prepareInsert(PreparedStatement statement) throws SQLException {
        statement.setString(1,this.getAction());
        statement.setString(2,this.getCaseNo());
        statement.setString(3,this.getType());
        statement.setString(4,this.getCustomerName());
        statement.setString(5,this.getSyncStartTime());
        statement.setString(6,this.getSyncCompleteTime());
        statement.setString(7,this.getRemarks());
        statement.setString(8,this.getCreateTime());
        statement.setString(9,this.getLastUpdateTime());
        return statement;
    }

    @Override

    public PreparedStatement prepareUpdate(PreparedStatement statement) throws SQLException {
        statement.setString(1,this.getAction());
        statement.setString(2,this.getCaseNo());
        statement.setString(3,this.getType());
        statement.setString(4,this.getCustomerName());
        statement.setString(5,this.getSyncStartTime());
        statement.setString(6,this.getSyncCompleteTime());
        statement.setString(7,this.getRemarks());
        statement.setString(8,this.getLastUpdateTime());
        statement.setInt(9,this.getId());
        return statement;
    }


    @Override
    public int getPK() {
        return id;
    }

    @Override
    public void setPK(int id) {
        this.id=id;
    }

    public String insertSQL(){

        return "INSERT INTO AUDIT_LOG (ACTION, CASE_NO, TYPE, CUSTOMER_NAME, SYNC_START_TIME, SYNC_COMPLETE_TIME, REMARKS, CREATE_TIME, LAST_UPDATE_TIME)\n" +
                "VALUES (?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public String updateSQL() {
        return "update AUDIT_LOG set ACTION =?, CASE_NO=?, TYPE=?, CUSTOMER_NAME=?, SYNC_START_TIME=?, SYNC_COMPLETE_TIME=?, REMARKS=?, LAST_UPDATE_TIME =? where id=?\n";
    }

    @Override
    public String findByPKQL() {
        return "select * from AUDIT_LOG where id=?";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String caseNo) {
        this.caseNo = caseNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSyncStartTime() {
        return syncStartTime;
    }

    public void setSyncStartTime(String syncStartTime) {
        this.syncStartTime = syncStartTime;
    }

    public String getSyncCompleteTime() {
        return syncCompleteTime;
    }

    public void setSyncCompleteTime(String syncCompleteTime) {
        this.syncCompleteTime = syncCompleteTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
