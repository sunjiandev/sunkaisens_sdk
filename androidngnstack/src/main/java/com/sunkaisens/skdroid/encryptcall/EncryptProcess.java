package com.sunkaisens.skdroid.encryptcall;

import java.nio.ByteBuffer;

import org.doubango.ngn.services.impl.NgnSipService;
import org.doubango.utils.MyLog;

import android.util.Log;

public class EncryptProcess 
{
	public final static String ENCRYPT_BEGIN="EncryptBegin";
	public final static String ENCRYPT_REQUEST="EncryptRequest";
	public final static String ENCRYPT_KEY_DIS="EncryptKeyDis";
	public final static String ENCRYPT_SUCCESS="EncryptSuccess";
	public final static String NONE="NONE";
	
	private final static int RINDEX=16;
	
	private final static String TAG = EncryptProcess.class.getCanonicalName();
	
	private String encryptState;
	private boolean isCaller=true;
	
	public ByteBuffer process(byte[] content)
	{
		if(content[0]==(byte)0x03||content.length<RINDEX)
		{
			encryptState=NONE;
			return null;
		}
			
		if(content[RINDEX]==(byte)0x85)
		{
			MyLog.d(TAG,"receive a encrypt_info:8585858585");
			encryptState=ENCRYPT_BEGIN;
			return CcUssdRspMsg.encodeEncryptRequest((byte)0x86);
			
		}else if(content[RINDEX]==(byte)0x87)
		{
			MyLog.d(TAG,"receive a encrypt_info:878787878787");
			encryptState=ENCRYPT_KEY_DIS;
			return CcUssdRspMsg.encodeEncryptSuccess((byte)0x86);
		}else if(content[RINDEX]==(byte)0x88)
		{
			MyLog.d(TAG,"receive a encrypt_info:88888888888");
			isCaller=false;
			encryptState=ENCRYPT_BEGIN;
			return CcUssdRspMsg.encodeEncryptRequest((byte)0x89);
			
		}else if(content[RINDEX]==(byte)0x8A)
		{
			MyLog.d(TAG,"receive a encrypt_info:8a8a8a8a8a8a8a8");
			isCaller=false;
			encryptState=ENCRYPT_KEY_DIS;
			return CcUssdRspMsg.encodeEncryptSuccess((byte)0x89);
		}
		return null;
	}

	public String getEncryptState() {
		return encryptState;
	}

	public void setEncryptState(String encryptState) {
		this.encryptState = encryptState;
	}

	public boolean isCaller() {
		return isCaller;
	}

	public void setCaller(boolean isCaller) {
		this.isCaller = isCaller;
	}
	
	
	

}
