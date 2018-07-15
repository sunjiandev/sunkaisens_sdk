package com.sunkaisens.skdroid.util;

import java.util.Date;

import org.doubango.ngn.utils.NgnConfigurationEntry;

public class GlobalVar {

	public static boolean bADHocMode = false;
	public static String displayname = "";
	public static String account = "";

	public static boolean bBackOrSwitch = false; // 在视频通话中，按下回退按钮或者是切换界面标志

	public static boolean isVideoDisp = false; // 在视频通话中，界面显示了

	public static boolean isLandscap = true;

	public static boolean mSendVideo = true;

	public static boolean mLogout = false;

	public static String mLocalNum;

	public static String videoMonitorPrefix = "815";

	public static boolean mCameraIsUsed = false;

	public static int mMyPid;

	// 有序广播开关
	public static boolean orderedbroadcastSign = false;

	public static String pcscfIp;

	public static String mCurrIp = "";

	// 程序启动时间
	public static Date mAppStartTime;

	public static boolean PTTHasLongClickedDown = false;

	public static final String ASSERTED = "X-REST-Asserted-Identity:sip:administrator@test.com";
	public static final String CONTEN_TYPE = "Content-type:application/json";

	public static final String UPLOAD_FILE_CON = "Connection:keep-alive";
	public static final String UPLOAD_FILE_TYPE = "Content-Type:application/octet-stream;multipart/form-data";
}
