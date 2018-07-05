package com.nuonuo.iframe.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceBundle {

    private static final Logger log = LogManager.getLogger(ResourceBundle.class.getName());

    public static final PropertiesLoader PROPERTIES_LOADER;

    public static final String baseDir;

    private static final List<String> FILE_SET = new ArrayList<>();

    public static Map<String,Object> loadMap = new HashMap<>();

    static {
        String jvmPath = System.getProperty("java.class.path");
        log.info("jvm.path is {}", jvmPath);
        File basedir = null;
        if (!jvmPath.contains(";")) {
            File f = new File(jvmPath);
            basedir = new File(new File(f.getAbsolutePath()).getParentFile().getParentFile().getPath() + File.separator + "conf");
        } else {
            String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            basedir = new File(new File(path).getParentFile().getPath() + File.separator + "resources");
        }
        baseDir = basedir.getAbsolutePath();
        log.info("config.base.dir is {}", baseDir);
        findProperFilesInPath(basedir);

        PROPERTIES_LOADER = new PropertiesLoader(FILE_SET.toArray(new String[FILE_SET.size()]));
        loadMap = PROPERTIES_LOADER.getAllKeyValue();
    }

    private static void findProperFilesInPath(File baseDir) {
        File[] fs = baseDir.listFiles();
        if(null != fs){
            for (File f : fs) {
                if (f.isDirectory()) {
                    findProperFilesInPath(f);
                } else {
                    if (f.getName().endsWith(".properties")) {
                        FILE_SET.add(baseDir + File.separator + f.getName());
                    }
                }
            }
        }
    }
}
