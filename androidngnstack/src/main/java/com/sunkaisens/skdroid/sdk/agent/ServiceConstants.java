package com.sunkaisens.skdroid.sdk.agent;

public interface ServiceConstants {

	public static enum LoginFailedRes {
		LOGINFAILED_SENDFAIL, // 发送失败
		LOGINFAILED_TIMEOUT, // 登录超时
		LOGINFAILED_SYSTEM_ERR, // 系统错误
		LOGINFAILED_USER_NOT_EXIST, // 用户不存在
		LOGINFAILED_USER_PWD_ERR, // 用户名或密码错误
		LOGINFAILED_SERVER_NOT_ACHIEVE, // 服务器不可达
		LOGINFAILED_CHECK_CONFIG, // 检测服务器配置
		LOGINFAILED_NO_REASON;// 未知原因
	}
	
	public static enum FILE_TYPE{
		
		FILE,
		IMAGE,
		AUDIO,
		VIDEO;
	}
	
	public static enum EX_RST_EN{
		ER_SUCCESS,
        ER_FAILED;
	}
}
