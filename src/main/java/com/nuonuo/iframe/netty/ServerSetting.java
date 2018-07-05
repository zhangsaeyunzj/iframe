package com.nuonuo.iframe.netty;


import com.nuonuo.iframe.action.Action;
import com.nuonuo.iframe.annotation.Mapping;
import com.nuonuo.iframe.annotation.Route;
import com.nuonuo.iframe.error.IError;
import com.nuonuo.iframe.exception.ServerSettingException;
import com.nuonuo.iframe.filter.Filter;
import com.nuonuo.iframe.handler.ActionInvoker;
import com.nuonuo.iframe.utils.ReflectUtils;
import com.nuonuo.iframe.utils.ResourceBundle;
import com.xiaoleilu.hutool.lang.Singleton;
import com.xiaoleilu.hutool.util.FileUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局设定文件
 *
 * @author xiaoleilu
 */
public class ServerSetting {
    private static final Logger log = LogManager.getLogger(ServerSetting.class
            .getName());

    // -------------------------------------------------------- Default value
    // start
    /**
     * 默认的字符集编码
     */
    public final static String DEFAULT_CHARSET = "utf-8";

    public final static String MAPPING_ALL = "/*";

    public final static String MAPPING_ERROR = "/_error";
    // -------------------------------------------------------- Default value
    // end

    /**
     * 字符编码
     */
    private static String charset = DEFAULT_CHARSET;
    /**
     * 端口
     */
    private static int port = 8090;
    /**
     * 根目录
     */
    private static File root;
    /**
     * Filter映射表
     */
    private static Map<String, Filter> filterMap;
    /**
     * Action映射表
     */
    private static Map<String, Action> actionMap;
    /**
     * ActionInvoker映射表
     */
    private static Map<String, ActionInvoker> invokerMap;
    /**
     * Error映射表
     */
    private static Map<String, IError> errorMap;

    static {
        filterMap = new ConcurrentHashMap<String, Filter>();

        actionMap = new ConcurrentHashMap<String, Action>();

        invokerMap = new ConcurrentHashMap<String, ActionInvoker>();

        errorMap = new ConcurrentHashMap<String, IError>();

        String actionPaths = (String) ResourceBundle.loadMap.get("action.root.path");
        for (String path : actionPaths.split(";")) {
            loadAction(path);
        }
        port = Integer.parseInt((String)ResourceBundle.loadMap.get("port"));
    }

    /**
     * @return 获取编码
     */
    public static String getCharset() {
        return charset;
    }

    /**
     * @return 字符集
     */
    public static Charset charset() {
        return Charset.forName(getCharset());
    }

    /**
     * 设置编码
     *
     * @param charset 编码
     */
    public static void setCharset(String charset) {
        ServerSetting.charset = charset;
    }

    /**
     * @return 监听端口
     */
    public static int getPort() {
        return port;
    }

    /**
     * 设置监听端口
     *
     * @param port 端口
     */
    public static void setPort(int port) {
        ServerSetting.port = port;
    }

    // -----------------------------------------------------------------------------------------------
    // Root start

    /**
     * @return 根目录
     */
    public static File getRoot() {
        return root;
    }

    /**
     * @return 根目录
     */
    public static boolean isRootAvailable() {
        if (root != null && root.isDirectory() && root.isHidden() == false
                && root.canRead()) {
            return true;
        }
        return false;
    }

    /**
     * @return 根目录
     */
    public static String getRootPath() {
        return FileUtil.getAbsolutePath(root);
    }

    /**
     * 根目录
     *
     * @param root 根目录绝对路径
     */
    public static void setRoot(String root) {
        ServerSetting.root = FileUtil.mkdir(root);
        log.debug("Set root to [{}]", ServerSetting.root.getAbsolutePath());
    }

    /**
     * 根目录
     *
     * @param root 根目录绝对路径
     */
    public static void setRoot(File root) {
        if (root.exists() == false) {
            root.mkdirs();
        } else if (root.isDirectory() == false) {
            throw new ServerSettingException(StrUtil.format(
                    "{} is not a directory!", root.getPath()));
        }
        ServerSetting.root = root;
    }

    // -----------------------------------------------------------------------------------------------
    // Root end

    // -----------------------------------------------------------------------------------------------
    // Filter start

    /**
     * @return 获取FilterMap
     */
    public static Map<String, Filter> getFilterMap() {
        return filterMap;
    }

    /**
     * 获得路径对应的Filter
     *
     * @param path 路径，为空时将获得 根目录对应的Action
     * @return Filter
     */
    public static Filter getFilter(String path) {
        if (StrUtil.isBlank(path)) {
            path = StrUtil.SLASH;
        }
        return getFilterMap().get(path.trim());
    }

    /**
     * 设置FilterMap
     *
     * @param filterMap FilterMap
     */
    public static void setFilterMap(Map<String, Filter> filterMap) {
        ServerSetting.filterMap = filterMap;
    }

    /**
     * 设置Filter类，已有的Filter类将被覆盖
     *
     * @param path   拦截路径（必须以"/"开头）
     * @param filter Action类
     */
    public static void setFilter(String path, Filter filter) {
        if (StrUtil.isBlank(path)) {
            path = StrUtil.SLASH;
        }

        if (null == filter) {
            log.warn("Added blank action, pass it.");
            return;
        }
        // 所有路径必须以 "/" 开头，如果没有则补全之
        if (false == path.startsWith(StrUtil.SLASH)) {
            path = StrUtil.SLASH + path;
        }

        ServerSetting.filterMap.put(path, filter);
    }

    /**
     * 设置Filter类，已有的Filter类将被覆盖
     *
     * @param path        拦截路径（必须以"/"开头）
     * @param filterClass Filter类
     */
    public static void setFilter(String path,
                                 Class<? extends Filter> filterClass) {
        setFilter(path, Singleton.get(filterClass));
    }

    // -----------------------------------------------------------------------------------------------
    // Filter end

    // -----------------------------------------------------------------------------------------------
    // Action start

    /**
     * @return 获取ActionMap
     */
    public static Map<String, Action> getActionMap() {
        return actionMap;
    }

    /**
     * 获得路径对应的Action
     *
     * @param path 路径，为空时将获得 根目录对应的Action
     * @return Action
     */
    public static Action getAction(String path) {
        if (StrUtil.isBlank(path)) {
            path = StrUtil.SLASH;
        }
        return getActionMap().get(path.trim());
    }

    /**
     * 设置ActionMap
     *
     * @param actionMap ActionMap
     */
    public static void setActionMap(Map<String, Action> actionMap) {
        ServerSetting.actionMap = actionMap;
    }

    /**
     * 设置Action类，已有的Action类将被覆盖
     *
     * @param path   拦截路径（必须以"/"开头）
     * @param action Action类
     */
    public static void setAction(String path, Action action) {
        if (StrUtil.isBlank(path)) {
            path = StrUtil.SLASH;
        }

        if (null == action) {
            log.warn("Added blank action, pass it.");
            return;
        }
        // 所有路径必须以 "/" 开头，如果没有则补全之
        if (false == path.startsWith(StrUtil.SLASH)) {
            path = StrUtil.SLASH + path;
        }
        log.info("{} load", path);
        ServerSetting.actionMap.put(path, action);
    }

    /**
     * 设置Action类，已有的Action类将被覆盖
     *
     * @param path    拦截路径（必须以"/"开头）
     * @param invoker ActionInvoker类
     */
    public static void setActionInvoker(String path, ActionInvoker invoker) {
        if (StrUtil.isBlank(path)) {
            path = StrUtil.SLASH;
        }

        if (null == invoker) {
            log.warn("Added blank action, pass it.");
            return;
        }
        // 所有路径必须以 "/" 开头，如果没有则补全之
        if (false == path.startsWith(StrUtil.SLASH)) {
            path = StrUtil.SLASH + path;
        }
        log.info("{} load", path);
        ServerSetting.invokerMap.put(path, invoker);
    }

    /**
     * 增加Action类，已有的Action类将被覆盖<br>
     * 所有Action都是以单例模式存在的！
     *
     * @param path        拦截路径（必须以"/"开头）
     * @param actionClass Action类
     */
    public static void setAction(String path,
                                 Class<? extends Action> actionClass) {
        setAction(path, Singleton.get(actionClass));
    }

    /**
     * 增加Action类，已有的Action类将被覆盖<br>
     * 自动读取Route的注解来获得Path路径
     *
     * @param action 带注解的Action对象
     */
    public static void setAction(Action action) {
        final Route route = action.getClass().getAnnotation(Route.class);
        Class clazz = action.getClass();


        //加载默认的action方法
        if (route != null) {
            final String path = route.value();
            final Class routeException = route.exception();
            if (StrUtil.isNotBlank(path)) {
                setAction(path, action);
                setError(path, routeException);
            }

            //对指定mapping的方法进行url映射
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                final Mapping mapping = method.getAnnotation(Mapping.class);
                if (mapping == null) {
                    continue;
                }
                final String uri = mapping.value();
                if (uri.equals(""))
                    throw new RuntimeException("URI can not be null, please input the uri.");

                final String url = path + uri;
                if (StrUtil.isNotBlank(url)) {
                    ActionInvoker invoker = new ActionInvoker(action, method.getName());
                    setActionInvoker(url, invoker);
                    setError(url, routeException);
                }
            }
        }
//        throw new ServerSettingException(
//                "Can not find Route annotation,please add annotation to Action class!");
    }

    /**
     * 增加Action类，已有的Action类将被覆盖<br>
     * 所有Action都是以单例模式存在的！
     *
     * @param actionClass 带注解的Action类
     */
    public static void setAction(Class<? extends Action> actionClass) {
        setAction(Singleton.get(actionClass));
    }


    /**
     * 加载pkg下面所有的action到actionMap
     *
     * @param pkg 包名
     */
    public static <T extends Action> void loadAction(String pkg) {
        try {
            Set<T> classes = ReflectUtils.getAllClassInPKG(pkg);
            for (T clazz : classes) {
                setAction(clazz);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据URL返回相对应的异常处理类
     *
     * @param path URL
     * @return
     */
    public static IError getError(String path) {
        return ServerSetting.errorMap.get(path);
    }

    /**
     * 根据URL返回相对应的actionInvoker
     *
     * @param url
     * @return
     */
    public static ActionInvoker getActionInvoker(String url) {
        return ServerSetting.invokerMap.get(url);
    }

    /**
     * 设置对应的异常处理类
     *
     * @param path      URL
     * @param exception 异常处理类 需要 extends {@link IError}
     */
    private static void setError(String path, Class<? extends IError> exception) {
        IError error = Singleton.get(exception);

        if (StrUtil.isBlank(path)) {
            path = StrUtil.SLASH;
        }

        // 所有路径必须以 "/" 开头，如果没有则补全之
        if (false == path.startsWith(StrUtil.SLASH)) {
            path = StrUtil.SLASH + path;
        }
        log.info("{} exception class load {}", path, exception);
        ServerSetting.errorMap.put(path, error);
    }
    // -----------------------------------------------------------------------------------------------
    // Action start

}
