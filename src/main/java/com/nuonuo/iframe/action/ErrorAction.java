package com.nuonuo.iframe.action;


import com.nuonuo.iframe.annotation.Route;
import com.nuonuo.iframe.error.IError;
import com.nuonuo.iframe.handler.Request;
import com.nuonuo.iframe.handler.Response;
import com.nuonuo.iframe.netty.ServerSetting;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.StaticLog;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 错误堆栈Action类
 *
 * @author Looly
 */
@Route(ServerSetting.MAPPING_ERROR)
public class ErrorAction implements Action,IError {
    private static final Log log = StaticLog.get();

    public final static String ERROR_PARAM_NAME = "_e";

    @Override
    public void doAction(Request request, Response response) {
        Object eObj = request.getObjParam(ERROR_PARAM_NAME);
        if (eObj == null) {
            response.sendError(HttpResponseStatus.NOT_FOUND,
                    "404 File not found!");
            return;
        }
    }
}