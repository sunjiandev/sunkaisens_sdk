
//author  duhaitao
package com.sunkaisens.skdroid.bean;


public class MessageBodyInfo {
	private String NS = null;
	// private String EXT = "MsgExt";
	private String msgType = null;
	private String msgReport = null;
	private String localMsgID = null;
	private String conType;
	private String mSubmitTime = "";

	public MessageBodyInfo(String NS, String msgType, String msgReport,
			String localMsgID, String conType) {
		this.NS = NS;
		this.msgType = msgType;
		this.msgReport = msgReport;
		this.localMsgID = localMsgID;
		this.conType = conType;
	}

	public MessageBodyInfo(String NS, String msgType, String msgReport,
			String localMsgID, String conType, String submitTime) {
		this.NS = NS;
		this.msgType = msgType;
		this.msgReport = msgReport;
		this.localMsgID = localMsgID;
		this.conType = conType;
		this.mSubmitTime = submitTime;
	}
	
	public void setSubmitTime(String submitTime){
		this.mSubmitTime = submitTime;
	}
	public String getConType() {
		return conType;
	}

	public void setConType(String conType) {
		this.conType = conType;
	}

	public String getNS() {
		return NS;
	}

	public void setNS(String nS) {
		NS = nS;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsgReport() {
		return msgReport;
	}

	public void setMsgReport(String msgReport) {
		this.msgReport = msgReport;
	}

	public String getLocalMsgID() {
		return localMsgID;
	}

	public void setLocalMsgID(String localMsgID) {
		this.localMsgID = localMsgID;
	}

	@Override
	public String toString() {
		return "NS: " + NS + "\nMsgExt.msgType: " + msgType + "\nMsgExt.msgReport: " + msgReport
				+ "\nMsgExt.localMsgID: " + localMsgID + "\nContent-Type: " + conType;
		
	}
	public String toAdhocMsgString(){
		return "NS: " + NS + "\nMsgExt.msgType: " + msgType + "\nMsgExt.msgReport: " + msgReport
				+ "\nMsgExt.localMsgID: " + localMsgID + "\nContent-Type: " + conType +
				"\nMsgExt.submitTime: " + mSubmitTime;
	}

}
