package com.nuonuo.iframe.utils;

import com.nuonuo.iframe.action.Action;
import com.xiaoleilu.hutool.lang.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ReflectUtils {

    private static final Logger log = LogManager.getLogger(ReflectUtils.class.getName());

    private static final String FILTER_REGEX = "^[A-Za-z]+Action.class$";

    /**
     * 获取到一个package下所有的action.Class集合
     *
     * @param pkg package名称
     * @return {@link Set<Class>} action.Class集合
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T extends Action> Set<T> getAllClassInPKG(String pkg) throws IOException, ClassNotFoundException {
        Set classes = new HashSet();
        String basedir = pkg.replace(".", "/");
        Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(basedir);
        while (dirs.hasMoreElements()) {
            URL url = dirs.nextElement();

            String protocol = url.getProtocol();
            if (protocol.equals("file")) {
                File file = new File(url.getPath());
                final String[] subFiles =
                        file.list(new java.io.FilenameFilter() {
                            public boolean accept(final File dir, final String name) {
                                return name.matches(FILTER_REGEX);
                            }
                        });
                for (String s : subFiles) {
                    String className = pkg + "." + s.replace(".class", "");
                    //这里为了保证在此项目中所有action为Singleton，调用Singleton.get()方法
                    classes.add(Singleton.get(className));
                }
            } else if (protocol.equals("jar")) {
                String jarFilePath = url.getFile();
                jarFilePath = jarFilePath.substring(0,jarFilePath.indexOf("!"));
                URL jarUrl = new URL(jarFilePath);
                JarFile jarFile = null;

                try {
                    jarFile = new JarFile(jarUrl.getFile());
                    Enumeration<JarEntry> ee = jarFile.entries();
                    while (ee.hasMoreElements()) {
                        JarEntry entry = (JarEntry) ee.nextElement();
                        // 过滤我们出满足我们需求的东西
                        String pn = entry.getName();
                        String spn = pn.substring(pn.lastIndexOf("/") + 1, pn.length());
                        if (pn.startsWith(basedir) && spn.matches(FILTER_REGEX)) {
                            String className = pkg + "." + spn.replace(".class", "");
                            //这里为了保证在此项目中所有action为Singleton，调用Singleton.get()方法
                            classes.add(Singleton.get(className));
                        }
                    }
                } finally {
                    if (null != jarFile) {
                        jarFile.close();
                    }
                }
            }
        }
        return classes;
    }
}
