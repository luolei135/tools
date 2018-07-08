package com.ncs.dsf.exception;

public class ToolException extends Exception {
    public ToolException(Exception e){
        super(e);
    }
    public ToolException(String msg){
        super(msg);
    }
}
