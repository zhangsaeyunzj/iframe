package com.nuonuo.iframe.filter;

import com.nuonuo.iframe.handler.Request;
import com.nuonuo.iframe.handler.Response;
import io.netty.handler.codec.http.HttpHeaderNames;

/**
 * 允许跨域过滤器
 * <p>
 * created by zhangbl 2017-10-24
 */
public class CORSFilter implements Filter {

    @Override
    public boolean doFilter(Request request, Response response) {
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*");
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), "POST, GET, OPTI ONS, DELETE");
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString(), "3600");
        response.setHeader(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(), "Origin,x-requested-with,content-type");
        return true;
    }
}
