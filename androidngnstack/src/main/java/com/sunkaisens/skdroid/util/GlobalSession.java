package com.sunkaisens.skdroid.util;

import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnMediaSession.NgnMediaSessionState;

public class GlobalSession {
	public static NgnAVSession avSession;
	public static NgnMediaSession mediaSession;
	/*
	 * true: 大终端云台控制
	 */
	public static boolean bSocketService = false; //true/false 适配软件与业务软件接口服务开关
	//
	/**
	 * true: 服务版本
	 * false: 单机版本
	 */
	public static boolean isSocketServicePath = false; //true/false 适配软件与业务软件接口服务开关 区分库文件路径赋值 为和联芯公司调试而设置
	
	//public static boolean isBigDevice = false;
	
}
