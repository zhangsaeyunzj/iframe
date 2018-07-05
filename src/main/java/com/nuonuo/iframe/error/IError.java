package com.nuonuo.iframe.error;

import com.nuonuo.iframe.handler.Request;
import com.nuonuo.iframe.handler.Response;

/**
 * 用以处理不用情况下的exception
 * <p>
 * create by zhangbl 2017-10-23
 */
public interface IError {
    public void doAction(Request request, Response response);

}
