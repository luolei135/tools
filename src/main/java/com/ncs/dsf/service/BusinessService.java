package com.ncs.dsf.service;

import com.google.gson.Gson;
import com.ncs.dsf.constants.Constants;
import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.process.SyncDown;
import com.ncs.dsf.process.SyncUp;
import com.ncs.dsf.utils.DBUtil;
import com.ncs.dsf.utils.DateUtil;
import com.ncs.dsf.utils.StringUtils;
import com.ncs.dsf.vo.AuditVO;
import com.ncs.dsf.vo.Entity;
import com.ncs.dsf.vo.SyncVO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sun.text.resources.CollationData_nl;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class BusinessService extends SearchService{
    SyncDown syncDown = new SyncDown();
    SyncUp syncUp = new SyncUp();
    public SyncVO syncdown(SyncVO vo) throws ToolException {
        try {
            AuditVO audit = new AuditVO();
            audit.setSyncStartTime(DateUtil.currentTime());
            audit.setAction("Sync Down");
            audit.setCreateTime(DateUtil.currentTime());
            audit.setLastUpdateTime(DateUtil.currentTime());
            audit.setCaseNo(vo.getSyncDownCase());
            insertEntity(vo,audit);
            audit=searchByPK(vo,audit);
            vo.setNewId(audit.getId()+"");
            vo = syncDown.process(vo);
            audit.setSyncCompleteTime(DateUtil.currentTime());
            audit.setLastUpdateTime(DateUtil.currentTime());
            updateEntity(vo,audit);
            vo.setAudits(super.searchAllAuditLogs(vo));

        } catch (ToolException e) {
            throw new ToolException(e);

        }catch (Exception e){
            e.printStackTrace();
            throw new ToolException("Sync down unknown exception.");
        }
        return vo;
    }
    public SyncVO syncup(SyncVO vo) throws ToolException {

            AuditVO audit = new AuditVO();
            audit.setSyncStartTime(DateUtil.currentTime());
            audit.setAction("Sync Up");
            audit.setSyncCompleteTime(DateUtil.currentTime());
            audit.setCaseNo(vo.getSyncUpCase());
            audit.setCreateTime(DateUtil.currentTime());
            audit.setLastUpdateTime(DateUtil.currentTime());
            audit.setRemarks(vo.getSelectAudit().getId()+"");
            insertEntity(vo,audit);
            vo = syncUp.process(vo);
            audit.setSyncCompleteTime(DateUtil.currentTime());
            vo.setAudits(super.searchAllAuditLogs(vo));
            updateEntity(vo,audit);

        return vo;
    }

    public SyncVO saveRemark(SyncVO data) throws ToolException {
        AuditVO vo = searchByPK(data,data.getSelectAudit());
        vo.setRemarks(data.getSelectAudit().getRemarks());
        updateEntity(data,vo);
        data.setAudits(searchAllAuditLogs(data));
        return data;
    }
    public String changeRootFolder(SyncVO vo){
        JSONArray jsonArray = new JSONArray();
        String rootFolder = vo.getRootFolder();
        if(StringUtils.isNotBlank(rootFolder)){
            File root = new File(rootFolder);
            if(!root.exists()){
                root.mkdir();
            }
            File[] cases = root.listFiles();

            Set set = new HashSet();

            for(File cs:cases){
                if(!cs.getName().equals(Constants.META_DB))
                set.add(cs.getName().substring(0,cs.getName().indexOf("-")));
            }

            jsonArray.addAll(set);
        }
        return jsonArray.toString();
    }

    public String mounted(SyncVO data) throws ToolException {
        SyncVO vo = new SyncVO();
        new DBUtil().init(vo);
        Gson gson = new Gson();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("selectedCase", "");
        jsonObject.put("selectedServer", "CD");
        try {
            jsonObject.put("audits", new JSONParser().parse(gson.toJson(searchAllAuditLogs(vo))));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ToolException(e);
        }
        prepareCases(jsonObject, vo);
        prepareServers(jsonObject);
        jsonObject.put("rootFolder", vo.getRootFolder());
        jsonObject.put("selectedAudit", gson.toJson(new AuditVO()));

        return jsonObject.toString();
    }

    private void prepareServers(JSONObject jsonObject) {
        JSONArray servers = new JSONArray();
        servers.add(Constants.DatabaseServers.CD);
        servers.add(Constants.DatabaseServers.SIT);
        servers.add(Constants.DatabaseServers.local);
        servers.add(Constants.DatabaseServers.UAT);

        jsonObject.put("servers", servers);
    }

    private void prepareCases(JSONObject jsonObject, SyncVO vo) throws ToolException {

        try {
            jsonObject.put("cases", new JSONParser().parse(this.changeRootFolder(vo)));
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ToolException(e);
        }
    }

}
