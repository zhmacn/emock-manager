package com.mzh.emock.manager.po;

import com.mzh.emock.core.type.object.method.EMMethodInvoker;

public class EMInvokerCreateResult<T> {
    private boolean success;
    private String message;
    private EMMethodInvoker<T> invoker;

    public EMInvokerCreateResult(boolean success, String message, EMMethodInvoker<T> invoker) {
        this.success = success;
        this.message = message;
        this.invoker = invoker;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public EMMethodInvoker<T> getInvoker() {
        return invoker;
    }

    public void setInvoker(EMMethodInvoker<T> invoker) {
        this.invoker = invoker;
    }
}
