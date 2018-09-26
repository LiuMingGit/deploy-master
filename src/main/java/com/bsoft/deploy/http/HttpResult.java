package com.bsoft.deploy.http;


/**
 * desc
 * Created on 2018/8/8.
 *
 * @author yangl
 */
public class HttpResult {
    private int code;
    private String message;
    private Object data;

    public HttpResult() {
        this(20000,"success");
    }

    public HttpResult(Object data) {
        this(20000,"success", data);
    }

    public HttpResult(int code,String message) {
        this.code = code;
        this.message = message;
    }

    public HttpResult(int code,String message,Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
