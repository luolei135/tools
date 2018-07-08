package com.ncs.dsf.service;

import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.utils.DBUtil;
import com.ncs.dsf.vo.AuditVO;
import com.ncs.dsf.vo.Entity;
import com.ncs.dsf.vo.SyncVO;

import java.util.Arrays;
import java.util.List;

public class SearchService extends CURDService{
    public List<AuditVO> searchAllAuditLogs(SyncVO vo) throws ToolException {
        String sql = "select * from audit_log";
        return new DBUtil().queryByNativeSql(sql, Arrays.asList(new Object[]{}),AuditVO.class,vo);
    }
}
