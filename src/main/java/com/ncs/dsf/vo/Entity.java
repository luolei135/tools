package com.ncs.dsf.vo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class Entity {
    public abstract String insertSQL();
    public abstract String updateSQL();
    public abstract String findByPKQL();
    public abstract PreparedStatement prepareInsert(PreparedStatement statement) throws SQLException;
    public abstract PreparedStatement prepareUpdate(PreparedStatement statement) throws SQLException;
    public abstract int getPK();
    public abstract void setPK(int id);
    public abstract void setLastUpdateTime(String lastUpdateTime);
}
