package com.ncs.dsf.vo;

import com.ncs.dsf.constants.Constants;

import java.io.File;
import java.util.List;

public class SyncVO {

    public static final String UNIX_SEPARATOR = "/";
    public static final String WIN_SEPARATOR = "\\";
    private String syncDownCase;
    private File file;
    private String syncUpCase;
    private String syncDownServer;
    private String syncUpServer;
    private String rootFolder="D:/DSF/sync";
    private String customerId;
    private String caseId;
    private String errorMessage;
    private AuditVO selectAudit;
    private List<AuditVO> audits;
    private String newId;

    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }

    public List<AuditVO> getAudits() {
        return audits;
    }

    public void setAudits(List<AuditVO> audits) {
        this.audits = audits;
    }

    public AuditVO getSelectAudit() {
        return selectAudit;
    }

    public void setSelectAudit(AuditVO selectAudit) {
        this.selectAudit = selectAudit;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getSyncDownCase() {
        return syncDownCase;
    }

    public void setSyncDownCase(String syncDownCase) {
        this.syncDownCase = syncDownCase;
    }

    public String getSyncUpCase() {
        return syncUpCase;
    }

    public void setSyncUpCase(String syncUpCase) {
        this.syncUpCase = syncUpCase;
    }

    public String getSyncDownServer() {
        return syncDownServer;
    }

    public void setSyncDownServer(String syncDownServer) {
        this.syncDownServer = syncDownServer;
    }

    public String getSyncUpServer() {
        return syncUpServer;
    }

    public void setSyncUpServer(String syncUpServer) {
        this.syncUpServer = syncUpServer;
    }

    public String getRootFolder() {
        if (rootFolder != null && (!(rootFolder.endsWith(WIN_SEPARATOR) || rootFolder.endsWith(UNIX_SEPARATOR)))) {
            rootFolder = rootFolder + UNIX_SEPARATOR;
        }
        return rootFolder;
    }

    public void setRootFolder(String rootFolder) {

        if (rootFolder != null && (!(rootFolder.endsWith(WIN_SEPARATOR) || rootFolder.endsWith(UNIX_SEPARATOR)))) {
            rootFolder = rootFolder + UNIX_SEPARATOR;
        }
        this.rootFolder = rootFolder;

    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String syncDownTargetFolder() {
        return getRootFolder()  + syncDownCase+"-"+newId + UNIX_SEPARATOR;
    }

    public String syncUpTargetFolder() {
        return getRootFolder() + syncUpCase+"-"+selectAudit.getId() + UNIX_SEPARATOR;
    }

    public File syncDownTargetFile() {
        if (file == null)
            file = new File(rootFolder + UNIX_SEPARATOR + syncDownCase +"-"+newId+ UNIX_SEPARATOR + caseId + Constants.indicator + customerId);
        return file;
    }
}
