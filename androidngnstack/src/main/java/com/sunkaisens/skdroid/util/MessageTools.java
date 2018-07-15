package com.sunkaisens.skdroid.util;

public class MessageTools {
	
	/**
	* 操作符 << 的优先级比 & 高
	* intValue = (bytes[3] & 0xFF) << 24
	       | (bytes[2] & 0xFF) << 16
	       | (bytes[1] & 0xFF) <<  8
	       | (bytes[0] & 0xFF) <<  0
	* @param bytes
	* @return
	*/
	public static int bytesToInt (byte[] bytes){
	int length = Math.min(4,bytes.length);
	int intValue = 0;
        for (int i = 0; i < length; i++) {
         int offset = (length-1-i) * 8; 
         intValue |= (bytes[i] & 0xFF) << offset;
        }
       return intValue;
	}
	
	public static byte[] intToBytes(int value,int targlength){
		int length = Math.min(targlength,4);
		byte[] bytes = new byte[length];
		for (int i = 0; i <length; i++) {
		int offset = (length-1-i) * 8; //24, 16, 8
		bytes[i] = (byte) (value >> offset);
		}
		return bytes;
		}
	
	public static byte[] intToBytes16(int value,int targlength){
		int length = Math.min(targlength,4);
		byte[] bytes = new byte[length];
		for (int i = 0; i <length; i++) {
		int offset = (length-1-i) * 16; //24, 16, 8
		bytes[i] = (byte) (value >> offset);
		}
		return bytes;
		}
	
	
	public static String bytes2HexString(byte[] b) {
		String ret = ""; 
		for (int i = 0; i < b.length; i++) { 
		String hex = Integer.toHexString(b[i]&0xFF); 
		if (hex.length() == 1) {
		  hex = '0' + hex; 
		  } 
		  ret += hex.toUpperCase(); 
		  } 
		  return ret; 
		} 

	public static byte[] intToBytes4(int a){
		byte[] b = new byte[4];
		b[3] = (byte)((a>>24) & 0XFF);
		b[2] = (byte)((a>>16) & 0XFF);
		b[1] = (byte)((a>>8) & 0XFF);
		b[0] = (byte)( a & 0XFF);
		return b;
	}
	public static byte[] intToBytes2(int a){
		byte[] b = new byte[2];
		b[1] = (byte)((a>>8) & 0XFF);
		b[0] = (byte)( a & 0XFF);
		return b;
	}
	public static int bytesToInt4(byte[] b){
		int a;
		a = (int)((b[0] & 0xFF) | 
				((b[1] & 0xFF)<<8)) |
				((b[2] & 0xFF)<<16) |
				((b[3] & 0xFF)<<24);
		return a;
	}
	public static int bytesToInt2(byte[] b){
		int a;
		a = (int)((b[0] & 0xFF) | 
				((b[1] & 0xFF)<<8));
		return a;
	}

}
