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
    private long total;

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

    /**
     * 带分页信息
     * @param code
     * @param message
     * @param data
     * @param total
     */
    public HttpResult(int code,String message,Object data,long total) {
        this(code,message,data);
        this.total = total;
    }

    public HttpResult(Object data,long total) {
        this(data);
        this.total = total;
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

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
