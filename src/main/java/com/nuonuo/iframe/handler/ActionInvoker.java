package com.nuonuo.iframe.handler;

import com.nuonuo.iframe.action.Action;

public class ActionInvoker {
    private Action action;

    private String methodName;

    public ActionInvoker(Action action, String methodName) {
        this.action = action;
        this.methodName = methodName;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
