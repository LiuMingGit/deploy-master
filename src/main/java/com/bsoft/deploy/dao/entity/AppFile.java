package com.bsoft.deploy.dao.entity;

import com.bsoft.deploy.utils.FileUtils;

import java.io.*;

/**
 * 传输的文件对象
 * Created on 2018/8/9.
 *
 * @author yangl
 */
public class AppFile implements Serializable {
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件相对路径
     */
    private String relative;
    /**
     * 文件长度
     */
    private long length;
    /**
     * 文件二进制数据
     */
    private byte[] content;

    public AppFile() {
    }

    public AppFile(String filename, String basePath) {
        this(new File(filename), basePath);
    }

    public AppFile(File file, String basePath) {
        if (file != null) {
            this.name = file.getName();
            this.relative = FileUtils.getRelativePath(basePath, file.getAbsolutePath());
            this.length = file.length();
            this.content = FileUtils.getBytes(file);
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getRelative() {
        return relative;
    }

    public void setRelative(String relative) {
        this.relative = relative;
    }

    @Override
    public String toString() {
        return "文件名:" + name + ",相对路径为:" + relative + ",文件长度:" + content.length;
    }
}
