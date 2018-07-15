package com.sunkaisens.skdroid.groupcall;

public class PTTTypes {
	public static final String PTT_TYPE_UNKNOWN = "Unknown";
	public static final String PTT_TYPE_REQUEST = "Request";
	public static final String PTT_TYPE_GRANT = "Grant";
	public static final String PTT_TYPE_REJECT = "Reject";
	public static final String PTT_TYPE_RELEASE = "Release";
	public static final String PTT_TYPE_RELEASE_ACK = "Release Ack";
	public static final String PTT_TYPE_INCLUDE = "Include";
	public static final String PTT_TYPE_INCLUDE_ACK = "Include Ack";
	public static final String PTT_TYPE_INCLUDE_I_ACK = "Include Individual Ack";
	public static final String PTT_TYPE_INTERRUPT = "Interrupt";
	public static final String PTT_TYPE_INTERRUPT_ACK = "Interrupt Ack";
	public static final String PTT_TYPE_SUBSCRIBE = "Subscribe";
	public static final String PTT_TYPE_SUBSCRIBE_ACK = "Subscribe Ack";
	public static final String PTT_TYPE_CANCEL = "Cancel";
	public static final String PTT_TYPE_CANCEL_ACK = "Cancel Ack";
	public static final String PTT_TYPE_REPORT = "Report";
	public static final String PTT_TYPE_INQ = "Inquire";
	public static final String PTT_TYPE_VIDEO_ADDR = "VideoAddr";
	public static final String PTT_TYPE_COUNT = "";
	
	/**
	 *强插号码变更通知 
	 */
	public static final String PTT_TYPE_FLASH = "Flash Number";
	
	public static final String PTT_TYPE_INQUIRE = "Inquire";

	
	//add by Gongle
	public static final String PTT_TYPE_CONTROL = "Control";
	public static final String PTT_TYPE_GET_AUDIO = "Get Audio";

	public static final int PTT_EXT_TYPE_REQUEST = 0x01;
	public static final int PTT_EXT_TYPE_GRANT = 0x02;
	public static final int PTT_EXT_TYPE_REJECT = 0x03;
	public static final int PTT_EXT_TYPE_RELEASE = 0x04;
	public static final int PTT_EXT_TYPE_RELEASE_ACK =	0x05;
	public static final int PTT_EXT_TYPE_INCLUDE =	0x06;
	public static final int PTT_EXT_TYPE_INCLUDE_ACK =	0x07;
	public static final int PTT_EXT_TYPE_INCLUDE_I_ACK = 0x08;
	public static final int PTT_EXT_TYPE_INTERRUPT = 0x09;
	public static final int PTT_EXT_TYPE_INTERRUPT_ACK = 0x0A;
	public static final int PTT_EXT_TYPE_SUBSCRIBE	= 0x0B;//订阅视频
	public static final int PTT_EXT_TYPE_SUBSCRIBE_ACK	= 0x0C;
	public static final int PTT_EXT_TYPE_CANCEL	= 0x0D;//取消订阅
	public static final int PTT_EXT_TYPE_CANCEL_ACK = 0x0E;
	public static final int PTT_EXT_TYPE_REPORT	= 0x0F;//PTT报告
	public static final int PTT_EXT_TYPE_INQ = 	0x10;
	public static final int PTT_EXT_TYPE_VIDEO_ADDR  = 	0x11;//视频单呼地址		
	
}
