package com.sunkaisens.skdroid.sdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.sdk.agent.ServiceConstants.FILE_TYPE;

public class FileFmartCheck {

	private static final String TAG = FileFmartCheck.class.getCanonicalName();

	public  String name;
	public  String uri;
	private static FileFmartCheck fileFmart;
	private static final String picReg = ".+(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png)$";
	private static final String audioReg = ".+(.mp3|.wav|.wma|.ogg|.ape|.acc|.amr)";
	private static final String videoReg = ".+(.swf|.flv|.mp4|.rmvb|.avi|.mpeg|.ra|.ram|.mov|.wmv)";

	private FileFmartCheck() {
	};

	public static synchronized FileFmartCheck getInstance() {

		if (fileFmart == null) {
			return new FileFmartCheck();
		} else {
			return fileFmart;
		}
	}

	public FILE_TYPE fileFmart(String fileName) {
		String split = fileName.split("\\.")[1];
		if (dataCheck(picReg, split)) {
			return FILE_TYPE.IMAGE;
		} else if (dataCheck(audioReg, split)) {
			return FILE_TYPE.AUDIO;
		} else if (dataCheck(videoReg, split)) {
			return FILE_TYPE.VIDEO;
		} else {
			return FILE_TYPE.FILE;
		}
	}

	public boolean dataCheck(String picReg, String fmart) {
		Pattern pattern = Pattern.compile(picReg);
		Matcher matcher = pattern.matcher(fmart.toLowerCase());
		return matcher.find();
	}

	// type:file
	// url:192.168.1.10:8010/files/c076f5c9-3812-42d1-9f6c-8b091630c0d9
	// status:receive
	// name:Default_picture_5.jpg

	public FileFmartCheck parseFileContent(String content) {
		if (content == null) {
			return null;
		} else {
			String[] step1 = content.split("\n");
			for (int i = 0; i < step1.length; i++) {
				MyLog.d(TAG, "step1:" + step1[i]);
				if (step1[1] != null && step1[1].startsWith("url:")) {
					String[] step2 = step1[1].split(":");
					uri = step2[1];
					MyLog.d(TAG, "step2[1]:" + step2[1]);
					return fileFmart;
				}
				if (step1[3] != null && step1[2].startsWith("name:")) {
					name = step1[3].split(":")[1];
					MyLog.d(TAG, "step1[2]:" + step1[3].split(":")[1]);
					return fileFmart;
				}

			}
			return fileFmart;

		}
	}

}
