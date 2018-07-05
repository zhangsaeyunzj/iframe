package com.nuonuo.iframe.action;


import com.nuonuo.iframe.annotation.Mapping;
import com.nuonuo.iframe.annotation.Route;
import com.nuonuo.iframe.handler.Request;
import com.nuonuo.iframe.handler.Response;

@Route("/v1/hello")
public class HelloAction implements Action {
    @Override
    public void doAction(Request request, Response response) {
        response.setContent("hello world");
    }

    @Mapping(value = "/sayHello.do")
    public void sayHello(Request request, Response response) {
        response.setContent("hello world!!!");
    }
}
