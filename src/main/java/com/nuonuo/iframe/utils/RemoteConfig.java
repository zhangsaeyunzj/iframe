package com.nuonuo.iframe.utils;

import java.io.Serializable;
import java.util.List;

/**
 * 云端配置集合
 * @author wujz
 */
public class RemoteConfig implements Serializable {
    public static class Data {
        private String key;  // 配置项KEY
        private String value;// 配置项VALUE

        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    private static final long serialVersionUID = 1L;
    private String message; // 回馈msg
    private int code;       // 回馈code
    private List<Data> data;// 配置项集合

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public List<Data> getData() { return data; }
    public void setData(List<Data> data) { this.data = data; }

    public String findData(String key) {
        if (data != null) {
            for (Data t : data) {
                if (t.key.equals(key)) {
                    return t.value;
                }
            }
        }
        return null;
    }
}
