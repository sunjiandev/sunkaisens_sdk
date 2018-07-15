package com.sunkaisens.skdroid.groupcall;

import java.nio.ByteBuffer;

import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.util.MessageTools;

import android.R.integer;
import android.util.Log;

public class PTTInfoMsg {	
	 
	public static final String PTT_TYPE="PTT.Type";
	public static final String PTT_PHONE_NUMBER="PTT.PhoneNumber";
	public static final String PTT_INCLUDE_NUMBER="PTT.IncludeNumber";
	public static final String PTT_VIDEO_IP="PTT.VideoIP";
	public static final String PTT_VIDEO_PORT="PTT.VideoPort";
	public static final String PTT_ACTION="PTT.Action";
	public static final String PTT_RESULT="PTT.Result";
	
	private String mPTTType="";
	private String mPTTPhoneNumber="";
	private String mPTTIncludeNumber="";
	private String mPTTVideoIP="0";
	private String mPTTVideoPort="0";
	private String mPTTAction="";
	private String mPTTResult="";
	
	private int mPTTType2;	
	private int mPTTAction2;
	private int mPTTResult2;
	 
	public PTTInfoMsg(String PTTType)
	{
		this.mPTTType=PTTType;
	}
	public PTTInfoMsg(int PTTType)
	{
		this.mPTTType2=PTTType;
	}
	 
	public PTTInfoMsg(byte[] infoContent)
	 {
		 if(infoContent != null){
			String type = new String(infoContent).substring(0,3);
			MyLog.d("", "IsPTTBinary:"+type);
			if(type.equals("PTT")){
				 final String values[] = new String(infoContent).split("\r\n");
				for(String value : values){
					if(value == null) continue;
					final String kvp[] = value.split(":");
						if(kvp.length == 2){
							if(NgnStringUtils.equals(kvp[0], PTT_TYPE, true)){
								this.setPTTType(kvp[1]);
							}else if(NgnStringUtils.equals(kvp[0], PTT_PHONE_NUMBER, true)){
								this.setPTTPhoneNumber(kvp[1]);
							}else if(NgnStringUtils.equals(kvp[0], PTT_INCLUDE_NUMBER, true)){
								this.setPTTIncludeNumber(kvp[1]);
							}else if(NgnStringUtils.equals(kvp[0], PTT_VIDEO_IP, true)){
								this.setPTTVideoIP(kvp[1]);
							}else if(NgnStringUtils.equals(kvp[0],PTT_VIDEO_PORT, true)){
								this.setPTTVideoPort(kvp[1]);
							}else if(NgnStringUtils.equals(kvp[0], PTT_ACTION, true)){
								this.setPTTAction(kvp[1]);
							}else if(NgnStringUtils.equals(kvp[0], PTT_RESULT, true)){
								this.setPTTResult(kvp[1]);
							}
						}
					}
				}else {
					byte[] pttType = new byte[2];
					System.arraycopy(infoContent, 0, pttType, 0, 1);
					this.mPTTType2 = MessageTools.bytesToInt2(pttType);
					byte[] phoneNum = new byte[31]; 
					System.arraycopy(infoContent, 2, phoneNum, 0, 30);
					this.mPTTPhoneNumber = new String(phoneNum);
					byte[] includeNum = new byte[32]; 
					System.arraycopy(infoContent, 33, includeNum, 0, 31);
					this.mPTTIncludeNumber = new String(includeNum);
					byte[] PTTVideoIP = new byte[4]; 
					System.arraycopy(infoContent, 65, PTTVideoIP, 0, 3);
					
					byte[] PTTVideoPORT = new byte[2]; 
					System.arraycopy(infoContent, 69, PTTVideoPORT, 0, 1);
					byte PTTAction2 = infoContent[71];
					byte PTTResult2 = infoContent[72];
				}
		 }
	 }
	 
	public String toString()
	{
		StringBuffer sb=new StringBuffer(PTT_TYPE);
		sb.append(":");
		sb.append(mPTTType);
		sb.append("\r\n");
		
		sb.append(PTT_PHONE_NUMBER);
		sb.append(":");
		sb.append(mPTTPhoneNumber);
		sb.append("\r\n");
		
		sb.append(PTT_INCLUDE_NUMBER);
		sb.append(":");
		sb.append(mPTTIncludeNumber);
		sb.append("\r\n");
		
		sb.append(PTT_VIDEO_IP);
		sb.append(":");
		sb.append(mPTTVideoIP);
		sb.append("\r\n");
		
		sb.append(PTT_VIDEO_PORT);
		sb.append(":");
		sb.append(mPTTVideoPort);
		sb.append("\r\n");
		
		sb.append(PTT_ACTION);
		sb.append(":");
		sb.append(mPTTAction);
		sb.append("\r\n");
		
		sb.append(PTT_RESULT);
		sb.append(":");
		sb.append(mPTTResult);
		sb.append("\r\n");
		
		return sb.toString();
		
	}
	
	public byte[] getBinaryBytes(){
		byte[] buf = new byte[72];
		byte[] pttType = MessageTools.intToBytes2(this.mPTTType2);
		System.arraycopy(pttType, 0, buf, 0, 2);
		byte[] phoneNum = mPTTPhoneNumber.getBytes(); 
		System.arraycopy(phoneNum, 0, buf, 2, phoneNum.length);
		byte[] includeNum = mPTTIncludeNumber.getBytes(); 
		System.arraycopy(includeNum, 0, buf, 33, includeNum.length);
		byte[] PTTVideoIP = MessageTools.intToBytes4(Integer.valueOf(mPTTVideoIP)); 
		System.arraycopy(PTTVideoIP, 0, buf, 65, PTTVideoIP.length);
		byte[] PTTVideoPORT = MessageTools.intToBytes2(Integer.valueOf(mPTTVideoPort)); 
		System.arraycopy(PTTVideoPORT, 0, buf, 69, PTTVideoPORT.length);
		byte PTTAction2 = (byte)(mPTTAction2);
		buf[710] = PTTAction2;
		byte PTTResult2 = (byte)(mPTTResult2);
		buf[71] = PTTResult2;
		return buf;
		
	}
	
	public int getmPTTType2() {
		return mPTTType2;
	}
	public void setmPTTType2(int mPTTType2) {
		this.mPTTType2 = mPTTType2;
	}	
	public int getmPTTAction2() {
		return mPTTAction2;
	}
	public void setmPTTAction2(int mPTTAction2) {
		this.mPTTAction2 = mPTTAction2;
	}
	public int getmPTTResult2() {
		return mPTTResult2;
	}
	public void setmPTTResult2(int mPTTResult2) {
		this.mPTTResult2 = mPTTResult2;
	}	
	public String getPTTType() {
		return mPTTType;
	}
	public void setPTTType(String pTTType) {
		mPTTType = pTTType;
	}
	public String getPTTPhoneNumber() {
		return mPTTPhoneNumber;
	}
	public void setPTTPhoneNumber(String pTTPhoneNumber) {
		mPTTPhoneNumber = pTTPhoneNumber;
	}
	public String getPTTIncludeNumber() {
		return mPTTIncludeNumber;
	}
	public void setPTTIncludeNumber(String pTTIncludeNumber) {
		mPTTIncludeNumber = pTTIncludeNumber;
	}
	public String getPTTVideoIP() {
		return mPTTVideoIP;
	}
	public void setPTTVideoIP(String pTTVideoIP) {
		mPTTVideoIP = pTTVideoIP;
	}
	public String getPTTVideoPort() {
		return mPTTVideoPort;
	}
	public void setPTTVideoPort(String pTTVideoPort) {
		mPTTVideoPort = pTTVideoPort;
	}
	public String getPTTAction() {
		return mPTTAction;
	}
	public void setPTTAction(String pTTAction) {
		mPTTAction = pTTAction;
	}
	public String getPTTResult() {
		return mPTTResult;
	}
	public void setPTTResult(String pTTResult) {
		mPTTResult = pTTResult;
	}

}
