package com.sunkaisens.skdroid.util;


import org.doubango.utils.MyLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class ModelFileTransport {

    public String type;
    public String url;
    public String status;
    public String name;

    public ModelFileTransport() {
    }

    public ModelFileTransport(String type, String url, String status,

                              String name) {

        super();
        this.type = type;
        this.url = url;
        this.status = status;
        this.name = name;
    }

    @Override
    public String toString() {
        return "type:" + type + "\n" + "url:" + url + "\n" + "status:" + status
                + "\n" + "name:" + name + "\n";
    }

    public String toString_recv() {
        return "type:" + type + "\n" + "url:" + url + "\n" + "status:" + status
                + "\n" + "name:" + name;
    }


    public ModelFileTransport parseFileContent(String ftcontent) {

        MyLog.e("", "开始文件解析content:" + ftcontent);

        if (ftcontent == null)
            return null;

        String[] items = ftcontent.split("\n");
        String url = "";
        String shortname = "";
        String savepathname = "";
        for (int i = 0; i < items.length; i++) {
            if (items[i].startsWith("url:")) {
                this.url = items[i].replace("url:", "");
                String[] strs = this.url.split("/");

                try {
                    strs[strs.length - 1] = URLEncoder.encode(
                            strs[strs.length - 1], "UTF-8");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                this.url = "";

                for (String str : strs) {
                    this.url += str + "/";
                }
                this.url = this.url.substring(0, this.url.length() - 1);
            }
            if (items[i].startsWith("name:")) {
                this.name = items[i].replace("name:", "");
                try {
                    MyLog.d("ywh", "this.name-->" + this.name);
                    this.name = URLDecoder.decode(this.name, "UTF-8");
                    MyLog.d("ywh", "this.name-->" + this.name);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


            }

        }
        MyLog.e("", "文件传输  解析 ok:" + this.toString());
        if (url.isEmpty() || shortname.isEmpty() || savepathname.isEmpty())
            return null;
        else {

            return this;
        }
    }

}
