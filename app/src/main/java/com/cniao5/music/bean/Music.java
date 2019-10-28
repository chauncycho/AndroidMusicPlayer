package com.cniao5.music.bean;


import java.io.Serializable;

/**
 * 音乐实体类
 */
public class Music implements Serializable{

    private static final long SerialVersionUID = 1L;

    private String name;//歌曲名
    private String author;//演唱者
    private String path;//路径
    private long duration;//时长

    public Music() {
    }

    public Music(String name, String author, String path, long duration) {
        this.name = name;
        this.author = author;
        this.path = path;
        this.duration = duration;
    }

    public static long getSerialVersionUID() {
        return SerialVersionUID;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
