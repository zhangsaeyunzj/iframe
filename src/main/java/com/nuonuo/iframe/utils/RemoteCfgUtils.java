package com.nuonuo.iframe.utils;


import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * @author wujz
 * @describe 云端配置文件加载类
 */
public class RemoteCfgUtils {

    /** AES加密 */
    public static String aesEncrypt(String str, String key) throws Exception {
        if (str == null || key == null) return null;
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
        byte[] bytes = cipher.doFinal(str.getBytes("utf-8"));
        return Hex.encodeHexString(bytes);
    }

    /** AES解密 */
    public static String aesDecrypt(String str, String key) throws Exception {
        if (str == null || key == null) return null;
        byte[] content = Hex.decodeHex(str.toCharArray());
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes("utf-8"), "AES"));
        return new String(cipher.doFinal(content), "utf-8");
    }

    /**
     * 发起网络请求, 从云端获取应用配置集合
     * @param  host 云端接口地址 appId 云端配置集合(应用)ID  appKey 云端配置随机码
     * @return
     */
    public static RemoteConfig loadRemoteCfg(String host,String appId,String appKey) throws Exception {
        // random_str 随机生成, 长度为8
        String random_str = MD5Util.toMD5(UUID.randomUUID().toString());
        random_str = random_str.substring(random_str.length()-8);
        String key = appKey + random_str;
        String data = appKey + random_str;
        String token = aesEncrypt(data, key);

        String url = "http://%s/api/en_kv_config?appId=%s&random_str="+random_str+"&token="+token;
        String content = aesDecrypt(loadRemoteCfg(String.format(url, host, appId)), key);

        RemoteConfig remoteCfg = JSON.parseObject(content.trim(),RemoteConfig.class);
        if (remoteCfg != null && remoteCfg.getCode() == 0) {
            // 云端数据请求成功
            return remoteCfg;
        } else {
            // 云端数据请求失败
            String errMsg = "loadRemoteCfg fail, remoteCfg is null\ncontent:"+content;
            if (remoteCfg != null) {
                errMsg = "loadRemoteCfg fail...\ncode:"+remoteCfg.getCode()+" msg:"+remoteCfg.getMessage();
            }
            throw new Exception(errMsg);
        }
    }

    /**
     * 请求云端配置
     * @param cfgUrl  请求路径
     * @return        文件内容
     */
    public static String loadRemoteCfg(String cfgUrl) throws Exception {
        URL url = new URL(cfgUrl.trim());
        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
        urlCon.setConnectTimeout(5000);
        urlCon.setReadTimeout(10000);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                urlCon.getInputStream(), "UTF-8"));

        String readLine = in.readLine();
        StringBuilder sb = new StringBuilder();
        while (readLine != null) {
            sb.append(readLine);
            readLine = in.readLine();
            //if (readLine != null) {
            //    sb.append("\r\n");
            //}
        }

        in.close();
        return sb.toString();
    }

    /**
     * 写临时文件
     * @param fileName 临时文件名称
     * @param content  待写入的内容
     * @return         File
     * @deprecated
     */
    public static File createFile(String fileName, String content) throws Exception {
        if (null==content ||"".equals(content)) {
            throw new Exception("Remote configuration load failed...");
        }

        String appPath = System.getProperty("user.dir");
        String absPath = String.format("%s%sconfig%s%s", appPath, File.separator, File.separator, fileName);
        File file = new File(absPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        PrintWriter pw = new PrintWriter(file);
        pw.write(content);
        pw.flush();
        pw.close();
        return file;
    }

    /**
     *
     * @param host host 云端接口地址
     * @param appId  appId 云端配置集合(应用)ID
     * @param appKey appKey 云端配置随机码
     * @param keys 远程文件key值
     */
    public static void loadRemoteCfg(String host,String appId,String appKey,String[] keys) throws Exception{
        RemoteConfig config = loadRemoteCfg(host,appId,appKey);
        if (null != ResourceBundle.loadMap) {
            ResourceBundle.loadMap.clear();//清空本地数据
        }
        for(int i=0;i<keys.length;i++){
            String value = config.findData(keys[i]);
            if(null !=value){
                parseStr(value);
            }
        }
    }

    private static void parseStr(String str) throws Exception{
        BufferedReader reader = new BufferedReader(new StringReader(str));
        try {
            String tempString = null;
            int line = 1;
            //一次读一行，读入null时文件结束
            while ((tempString = reader.readLine()) != null) {
                if(null!=tempString&&!"".equals(tempString)){
                    String[] temp = tempString.split("=");
                    if(temp.length==2){
                        ResourceBundle.loadMap.put(temp[0].trim(),temp[1].trim());
                    }
                }
                line++;
            }
            reader.close();
        } catch (IOException e) {
        }
    }
}