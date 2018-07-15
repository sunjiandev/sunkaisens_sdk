package com.sunkaisens.skdroid.bean;

import org.doubango.utils.MyLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class ModelFileTransport {

    public String type;
    public String url;
    public String status;
    public String name;
    public String savePath;
    public String localPath;

    public ModelFileTransport() {
    }

    public ModelFileTransport(String type, String url, String status,

                              String name, String savePath) {

        super();
        this.type = type;
        this.url = url;
        this.status = status;
        this.name = name;
        this.savePath = savePath;
    }

    @Override
    public String toString() {
        return "type:" + type + "\n" + "url:" + url + "\n" + "status:" + status
                + "\n" + "name:" + name + "\n";
    }

    public String toString_send() {
        return "type:" + type + "\n" + "url:" + url + "\n" + "status:" + status
                + "\n" + "name:" + name + "\n" + "local:" + localPath;
    }

    public String toString_recv() {
        return "type:" + type + "\n" + "url:" + url + "\n" + "status:" + status
                + "\n" + "name:" + name + "\n" + "savepath:" + savePath;
    }

    public ModelFileTransport parseFileContent(String ftcontent) {

        // MyLog.e("", "�ļ�����  ����content:"+ftcontent);

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
//				String path = SystemVarTools.downloadPath;
//
//				if (path.startsWith("null")) {
//
//					NgnEngine engine = NgnEngine.getInstance();
//					engine.getStorageService().initFilePath();
//					MyLog.init(engine.getStorageService().getSdcardDir());
//					SystemVarTools.initFiles(engine.getStorageService()
//							.getSdcardDir());
//					path = SystemVarTools.downloadPath;
//				}
//				File dir = new File(path);
//				if (!dir.exists()) {
//					dir.mkdirs();
//				}

//				this.savePath = path + this.name;
            }
            if (items[i].contains("local:")) {
                this.localPath = items[i].replace("local:", "");
            }
            if (items[i].contains("type:")) {
                this.type = items[i].replace("type:", "");
            }
            if (items[i].contains("status:")) {
                this.status = items[i].replace("status:", "");
            }
        }
        // MyLog.e("", "�ļ�����  ���� ok:"+this.toString());
        if (url.isEmpty() || shortname.isEmpty() || savepathname.isEmpty())
            return null;
        else {

            return this;
        }
    }

}
