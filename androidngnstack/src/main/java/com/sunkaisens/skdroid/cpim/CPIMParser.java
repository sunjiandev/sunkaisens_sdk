package com.sunkaisens.skdroid.cpim;

import org.doubango.utils.MyLog;

import android.R.integer;
import android.nfc.Tag;
import android.util.Log;

public class CPIMParser {
	
	private static String TAG = CPIMParser.class.getCanonicalName();

	public static CPIMMessage parse(String content){

		CPIMMessage cpimMessage = new CPIMMessage();
				
		String endSign = "";//cpim头结束标记
		
		// NS
		if (content.indexOf("NS:") >= 0) {
			int nsStart = content.indexOf("NS:") + "NS:".length();
			int nsEnd = content.indexOf("\n");
			
			String ns = content.substring(nsStart, nsEnd+1);
			
			//检测短信cpim头的结束标记
			if(ns.indexOf("\r\n") >= 0){
				endSign = "\r\n";
			}else if(ns.indexOf("\n") >= 0){
				endSign = "\n";
			}
			
			MyLog.d(TAG, "endSign:"+endSign);
			
			cpimMessage.setNs(ns.trim());

			content = content.substring(nsEnd + 1);
		}

		// 离线消息的递送报告存在两条NS字段数据行
		// NS
		if (content.indexOf("NS:") >= 0) {
			int nsStart = content.indexOf("NS:") + "NS:".length();
			int nsEnd = content.indexOf(endSign);

			String ns = content.substring(nsStart, nsEnd).trim();
			cpimMessage.setNs(ns);

			content = content.substring(nsEnd + 1);
		}
			
		String contentTemp = content;// gzc 20140808
		try {			
			/*gzc 20140812修改cpim解析方式 */
			String heads = null;
			String msgContent =  null;
			if(contentTemp.indexOf("\n\r\n") >= 0){
				heads = contentTemp.substring(0,contentTemp.indexOf("\n\r\n"));
				msgContent =  contentTemp.substring((contentTemp.indexOf("\n\r\n")+("\n\r\n").length()));
			}else if(contentTemp.indexOf((endSign+endSign)) >= 0){
				heads = contentTemp.substring(0,contentTemp.indexOf(endSign+endSign));
				msgContent =  contentTemp.substring((contentTemp.indexOf(endSign+endSign)+(endSign+endSign).length()));
			}else{
				heads = contentTemp;				
			}					
			
			if(heads == null){
				MyLog.d(TAG, "Invalid cpimMsg");
				return null;
			}
			
			MyLog.d(TAG, "heads:"+heads+"|msgContent:"+msgContent);
			
			String[] cpimHeads = heads.split(endSign);			
			if (cpimHeads.length > 0) {
				for (int i = 0; i < cpimHeads.length; i++) {
					if (cpimHeads[i].contains("msgType:")) {
						int s = cpimHeads[i].indexOf("msgType:")+"msgType:".length();
						cpimMessage.setMsgType(cpimHeads[i].substring(s).trim());
					}
					if (cpimHeads[i].contains("msgReport:")) {
						int s = cpimHeads[i].indexOf("msgReport:")+"msgReport:".length();
						cpimMessage.setMsgReport(cpimHeads[i].substring(s).trim());
					}
					if (cpimHeads[i].contains("localMsgID:")) {
						int s = cpimHeads[i].indexOf("localMsgID:")+"localMsgID:".length();
						cpimMessage.setLocalMsgID(cpimHeads[i].substring(s).trim());
					}
					if (cpimHeads[i].contains("Content-Type:")) {
						int s = cpimHeads[i].indexOf("Content-Type:")+"Content-Type:".length();
						cpimMessage.setContentType(cpimHeads[i].substring(s).trim());
					}
					if (cpimHeads[i].contains("submitTime:")) {
						int s = cpimHeads[i].indexOf("submitTime:")+"submitTime:".length();
						cpimMessage.setSubmitTime(cpimHeads[i].substring(s).trim());
					}
					if (cpimHeads[i].contains("reportID:")) {
						int s = cpimHeads[i].indexOf("reportID:")+"reportID:".length();
						cpimMessage.setReportID(cpimHeads[i].substring(s).trim());
					}
					if (cpimHeads[i].contains("reportType:")) {
						int s = cpimHeads[i].indexOf("reportType:")+"reportType:".length();
						cpimMessage.setReportType(cpimHeads[i].substring(s).trim());
					}					
				}				
			}
			
			if(msgContent != null){
				cpimMessage.setContent(msgContent);
			}			
		
			return cpimMessage;
		} catch (Exception e) {
			//gzc 20140808
			MyLog.d(TAG, "cpimparse Exception:"+e.getMessage());
			return null;
		}	
//		return cpimMessage;
	}
	
	public static void main(String[] args){
		
		String content = "NS: MsgExt<http://www.message.com/msgExtensions/>\n" +
					 	 "MsgExt.msgType: GM\n" +
					 	 "MsgExt.msgReport: NO\n" +
					 	 "MsgExt.localMsgID: \n" + 
					 	 "Content-Type: text/plain\n\n" +
					 
					 	 "123\n";
		
		CPIMMessage cpimMessage = CPIMParser.parse(content);
		
//		System.out.println(cpimMessage.getNs());
//		System.out.println(cpimMessage.getMsgType());
//		System.out.println(cpimMessage.getMsgReport());
//		System.out.println(cpimMessage.getLocalMsgID());
//		System.out.println(cpimMessage.getContentType());
//		System.out.println(cpimMessage.getContent());
		
	}
}
