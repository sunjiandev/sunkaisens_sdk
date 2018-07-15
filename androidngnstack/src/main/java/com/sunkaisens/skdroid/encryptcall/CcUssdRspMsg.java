package com.sunkaisens.skdroid.encryptcall;

import java.nio.ByteBuffer;

public class CcUssdRspMsg
{
	//private ByteBuffer rsp;
	//public static final int EncryptRequestLen=35;
	//public static final int EncryptSuccessLen=36;
	
	public static ByteBuffer encodeEncryptRequest(byte firstByte)
	{
		byte[] rspBytes={(byte) 0x02,
					  (byte) 0xA2,(byte)0x1F,//Invoke TL
				      (byte)0x2,(byte)0x1,(byte)0x0,//InvokeId TLV
				      (byte)0x30,(byte)0x1A,//Param TL
				      (byte) 0x2,(byte) 0x1,(byte) 0x3c,//OptCode TLV
				      (byte) 0x30,(byte) 0x15,//Data TL
				      (byte) 0x4,(byte) 0x1,(byte) 0x44,//Datacode TLV
				      (byte) 0x4,(byte) 0x13,//Ussd String TL
				      firstByte,
				      (byte) 0xE1,
				      (byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,(byte) 0x1,
				      (byte) 0x23};
		ByteBuffer rsp = ByteBuffer.allocateDirect(rspBytes.length);
		return rsp.put(rspBytes);
	}
	
	public static ByteBuffer encodeEncryptSuccess(byte firstByte)
	{
		byte[] rspBytes={(byte) 0x02,
					  (byte) 0xA2,(byte)0x13,//Invoke TL
				      (byte)0x2,(byte)0x1,(byte)0x1,//InvokeId TLV
				      (byte)0x30,(byte)0xE,//Param TL
				      (byte) 0x2,(byte) 0x1,(byte) 0x3c,//OptCode TLV
				      (byte) 0x30,(byte) 0x9,//Data TL
				      (byte) 0x4,(byte) 0x1,(byte) 0x44,//Datacode TLV
				      (byte) 0x4,(byte) 0x3,//Ussd String TL
				      firstByte,
				      (byte) 0xE1,
				      (byte) 0x1,
				      (byte) 0x23};
		ByteBuffer rsp = ByteBuffer.allocateDirect(rspBytes.length);
		return rsp.put(rspBytes);
	}
}
