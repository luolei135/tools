package com.ncs.dsf.controller;

import com.google.gson.Gson;
import com.ncs.dsf.exception.ToolException;
import com.ncs.dsf.service.BusinessService;
import com.ncs.dsf.vo.SyncVO;

public class Controller {

    BusinessService service = new BusinessService();

    public String mounted(SyncVO data) throws ToolException {
        return service.mounted(data);
    }

    public String saveRemark(SyncVO data) throws ToolException {
        return new Gson().toJson(service.saveRemark(data));
    }

    public String syncup(SyncVO data) throws ToolException {
        return new Gson().toJson(service.syncup(data));
    }

    public String syncdown(SyncVO data) throws ToolException {
        return new Gson().toJson(service.syncdown(data));
    }

    public String changeRootFolder(SyncVO data) {
        return service.changeRootFolder(data);
    }


}
