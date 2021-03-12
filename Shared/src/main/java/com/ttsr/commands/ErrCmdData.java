package com.ttsr.commands;

import java.io.Serializable;

public class ErrCmdData implements Serializable {


    private final String errMsg;

    public ErrCmdData(String errMsg) {
        this.errMsg = errMsg;
    }

    public String getErrMsg() {
        return errMsg;
    }
}
