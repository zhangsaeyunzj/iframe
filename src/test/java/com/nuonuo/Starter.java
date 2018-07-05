package com.nuonuo;

import com.nuonuo.iframe.filter.CORSFilter;
import com.nuonuo.iframe.netty.IServer;
import com.nuonuo.iframe.netty.ServerSetting;

import java.io.File;

public class Starter {

    static {
        String path = Starter.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (path.contains("jar")) {
            String log4j2xml = new File(path).getParentFile().getParentFile().getPath()
                    + File.separator + "conf/log4j2.xml";
            System.out.println(String.format(" set log4j2.xml path is %s", log4j2xml));
            System.setProperty("log4j.configurationFile", log4j2xml);
        }
    }

    public static void main(String[] args) {
        try {
            ServerSetting.setFilter(ServerSetting.MAPPING_ALL,new CORSFilter());
            new IServer().start(ServerSetting.getPort());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
