package com.sunkaisens.skdroid.bean;

import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;

public class PttInfor {

	// PTT.Type:Release
	// PTT.PhoneNumber:18852002203
	// PTT.IncludeNumber:
	// PTT.VideoIP:
	// PTT.VideoPort:
	// PTT.Action:Unknown
	// PTT.Result:OK
	private PTTState state;
	private String remoteNum;
	private String groupNum;

	public PttInfor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PttInfor(PTTState state, String remoteNum, String groupNum) {
		super();
		this.state = state;
		this.remoteNum = remoteNum;
		this.groupNum = groupNum;
	}

	public PTTState getState() {
		return state;
	}

	public void setState(PTTState state) {
		this.state = state;
	}

	public String getRemoteNum() {
		return remoteNum;
	}

	public void setRemoteNum(String remoteNum) {
		this.remoteNum = remoteNum;
	}

	public String getGroupNum() {
		return groupNum;
	}

	public void setGroupNum(String groupNum) {
		this.groupNum = groupNum;
	}

	@Override
	public String toString() {
		return "PttInfor [state=" + state + ", remoteNum=" + remoteNum
				+ ", groupNum=" + groupNum + "]";
	}

}
