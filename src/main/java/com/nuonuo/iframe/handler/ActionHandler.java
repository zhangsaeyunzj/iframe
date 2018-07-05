package com.nuonuo.iframe.handler;


import com.nuonuo.iframe.action.Action;
import com.nuonuo.iframe.action.ErrorAction;
import com.nuonuo.iframe.action.FileAction;
import com.nuonuo.iframe.error.IError;
import com.nuonuo.iframe.filter.Filter;
import com.nuonuo.iframe.netty.ServerSetting;
import com.xiaoleilu.hutool.lang.Singleton;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.timeout.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Action处理单元
 *
 * @author Looly
 */
public class ActionHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log = LogManager.getLogger(ActionHandler.class.getName());

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                FullHttpRequest fullHttpRequest) throws Exception {

        final Request request = Request.build(ctx, fullHttpRequest);
        final Response response = Response.build(ctx, request);
        // 判断请求方法
        String method = request.getMethod();
        log.debug("invoke http method: " + method);
        try {
            // do filter
            boolean isPass = this.doFilter(request, response);
            if (isPass) {
                // do action
                this.doAction(request, response);
            }
        } catch (Exception e) {
            log.error("action调用发生异常", e);
//            Action errorAction = ServerSetting.getAction(ServerSetting.MAPPING_ERROR);
            request.putParam(ErrorAction.ERROR_PARAM_NAME, e);
//            errorAction.doAction(request, response);
            this.doError(request, response);
        }
        // 如果发送请求未被触发，则触发之，否则跳过。UtilException URISyntaxException SimpleChannelInboundHandler
        if (false == response.isSent()) {
            response.send().channel().close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (cause instanceof IOException) {
            log.warn("{}", cause);
        } else if (cause instanceof TimeoutException) {
            log.warn("{}", cause);
        } else {
            super.exceptionCaught(ctx, cause);

        }
        ctx.close();
    }

    // ----------------------------------------------------------------------------------------
    // Private method start

    /**
     * 执行过滤
     *
     * @param request  请求
     * @param response 响应
     */
    private boolean doFilter(Request request, Response response) {
        // 全局过滤器
        Filter filter = ServerSetting.getFilter(ServerSetting.MAPPING_ALL);
        if (null != filter) {
            if (false == filter.doFilter(request, response)) {
                return false;
            }
        }

        // 自定义Path过滤器
        filter = ServerSetting.getFilter(request.getPath());
        if (null != filter) {
            if (false == filter.doFilter(request, response)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 执行Action
     *
     * @param request  请求对象
     * @param response 响应对象
     */
    private void doAction(Request request, Response response) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String url = request.getPath();
        Action action = ServerSetting.getAction(url);
        if (null != action) {
            action.doAction(request, response);
        }

        ActionInvoker invoker = ServerSetting.getActionInvoker(url);
        if (null != invoker) {
            Class clazz = invoker.getAction().getClass();
            Method method = clazz.getDeclaredMethod(invoker.getMethodName(), new Class[]{Request.class, Response.class});
            method.invoke(Singleton.get(invoker.getAction().getClass()), request, response);
        } else if (null == action) {
            // 查找匹配所有路径的Action
            action = ServerSetting.getAction(ServerSetting.MAPPING_ALL);
            if (null == action) {
                // 非Action方法，调用静态文件读取com.alibaba.fastjson.JSONException
                action = Singleton.get(FileAction.class);
            }
            action.doAction(request, response);
        }
    }
    // ----------------------------------------------------------------------------------------
    // Private method start

    private void doError(Request request, Response response) {
        IError error = ServerSetting.getError(request.getPath());
        error.doAction(request, response);
    }
}
