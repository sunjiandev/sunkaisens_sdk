package com.sunkaisens.skdroid.session;

public interface SessionType {
	public final static int Unknown=-1;
	public final static int AudioCall=0;
	public final static int VideoCall=1;
	public final static int GroupAudioCall=2;
	public final static int GroupVideoCall=3;
	public final static int EncryptCall=4;
	public final static int VideoMonitor=5;
	public final static int VideoTransmit=6;
	public final static int AudioEvn=7;
	public final static int GroupVideoMonitor=8;
	public final static int VideoUaMonitor=9;
	public final static int VideoSurveilMonitor=10;//调度台发起的视频回传（被动视频回传）
	public final static int Directcall=11;// PTT直通
}
