package com.sunkaisens.skdroid.cpim;

public class CPIMMessage {
	
	public static final String MSGTYPE_IM = "IM";
	public static final String MSGTYPE_GM = "GM";
	
	public static final String MSGREPORT_YES = "YES";
	public static final String MSGREPORT_NO = "NO";
	
	public static final String CONTENTTYPE_TEXT_PLAIN = "text/plain";

	private String ns;
	
	private String msgType;
	
	private String msgReport;
	
	private String localMsgID;
	
	private String contentType;
	
	private String content;
	
	/*gzc 20140807 */
	private String submitTime;
	
	private String reportID;
	
	private String reportType;
	
	public String getNs() {
		return ns;
	}

	public void setNs(String ns) {
		this.ns = ns;
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

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}

	public String getReportID() {
		return reportID;
	}

	public void setReportID(String reportID) {
		this.reportID = reportID;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	
}
