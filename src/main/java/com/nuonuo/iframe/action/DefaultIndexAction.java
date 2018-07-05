package com.nuonuo.iframe.action;


import com.nuonuo.iframe.annotation.Route;
import com.nuonuo.iframe.handler.Request;
import com.nuonuo.iframe.handler.Response;
import com.nuonuo.iframe.netty.ServerSetting;

/**
 * 默认的主页Action，当访问主页且没有定义主页Action时，调用此Action
 *
 * @author Looly
 */
@Route(ServerSetting.MAPPING_ALL)
public class DefaultIndexAction implements Action {

    @Override
    public void doAction(Request request, Response response) {
        response.setContent("Welcome to LoServer.");
    }

}