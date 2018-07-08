package com.ncs.dsf.service;

import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.utils.DBUtil;
import com.ncs.dsf.utils.DateUtil;
import com.ncs.dsf.vo.Entity;
import com.ncs.dsf.vo.SyncVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class CURDService {
    public <T extends Entity> T  insertEntity(SyncVO vo, T entity) throws ToolException {
        String sql = entity.insertSQL();
        Connection connection = new DBUtil().getMetaConn(vo);
        try {
           PreparedStatement statement =  connection.prepareStatement(sql);
            entity.prepareInsert(statement);
            int result = statement.executeUpdate();
            entity.setPK(statement.getGeneratedKeys().getInt(1));
            return entity;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ToolException(e);
        }
    }

    public <T extends Entity> boolean  updateEntity(SyncVO vo,T entity) throws ToolException {
        String sql = entity.updateSQL();
        Connection connection = new DBUtil().getMetaConn(vo);
        try {
            PreparedStatement statement =  connection.prepareStatement(sql);
            entity.setLastUpdateTime(DateUtil.currentTime());
            entity.prepareUpdate(statement);

            return statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ToolException(e);
        }
    }

    public <T extends Entity> T searchByPK(SyncVO vo,T t) throws ToolException {
        List<T> result = (List<T>) new DBUtil().queryByNativeSql(t.findByPKQL(), Arrays.asList(new Object[]{t.getPK()}),t.getClass(),vo);
        return result.get(0);
    }
}
