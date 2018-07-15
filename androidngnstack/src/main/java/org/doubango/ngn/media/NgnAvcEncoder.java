package org.doubango.ngn.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import org.doubango.ngn.NgnApplication;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;

import com.sunkaisens.skdroid.util.HexDump;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;



public class NgnAvcEncoder 
{
	private static final String TAG = NgnAvcEncoder.class.getCanonicalName();
	
	private static NgnAvcEncoder instance;

	private MediaCodec mediaCodecEncoder;
	private ByteBuffer[] inputBuffers ;
	private ByteBuffer[] outputBuffers ;
	int m_width;
	int m_height;
	byte[] m_info = null;

	private static int frameRate;
	private static int numInputFrames = 0;
	private int colorFormat ;
	
	private long startTime;
	private File mVideoFile;
    private FileOutputStream fos = null;
    private  String mimeType = "video/avc";

	private byte[] yuv420 = null; 
	private byte[] yuv420Temp = null; 
	private int mRotation = 0;
	private boolean mFlip = false;
	
	public NgnAvcEncoder()
	{
		mFlip = false;
	}
	public void setNgnAvcRotation(int rotation)
	{
		mRotation = rotation;
	}
	public void setNgnAvcFlip(boolean flip)
	{
		mFlip = flip;
	}
	public  void setNgnAvcEncoderParameter(int width, int height, int framerate)
	{
		//if(instance != null) return ;
		//if(instance != null) return ;
		/*int bitrate = 100000;
		if(width == 352 || width == 320)
				bitrate = 400000;
		else if(width == 352 <<1 ) 
				bitrate = 1500000;
		else if(width == 640 || width==720)
				bitrate = 1100000;
		else if(width == 1280)
			bitrate = 14500000;
		else if (width >=1920)
			bitrate = 19000000; //10M	*/
					

		int bitrate =(int)((width*height*framerate*2*0.06/100000)*100000);
		m_width  = width;
		m_height = height;
		yuv420 = new byte[width*height*3/2];
		yuv420Temp = new byte[width*height*3/2];

			mediaCodecEncoder = MediaCodec.createEncoderByType(mimeType);
		colorFormat = getColorFormat(mimeType) ;
		MediaFormat mediaFormat ;
		if(mRotation == 90 || mRotation == 180)
			mediaFormat = MediaFormat.createVideoFormat(mimeType, height, width);
		else
			mediaFormat = MediaFormat.createVideoFormat(mimeType, m_width, m_height);
	    mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,bitrate);
	    mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
	   // mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);//COLOR_FormatYUV420SemiPlanar);//
	   // mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);// 
	    mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,colorFormat); 
	    mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
//	    mediaFormat.setInteger("stride", m_width);
//	    mediaFormat.setInteger("slice-height", m_height);
	    mediaCodecEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
	    mediaCodecEncoder.start();
	    frameRate = framerate;	
	  
	   // createFile();
	   
	}
	
	public static NgnAvcEncoder getInstance()
	{
		if(instance == null)
			instance = new NgnAvcEncoder();
		return instance;
	}

	
	public void releaseEncoder() {
		if(mediaCodecEncoder == null)
			return;
	    try {
	        mediaCodecEncoder.stop();
	        mediaCodecEncoder.release();
	       
	        mFlip = false;
	        instance = null;
	        numInputFrames = 0;
	        
	      //  closeFile();
	    } catch (Exception e){ 
	        e.printStackTrace();
	    }
//	    try {
//			fos.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public int offerEncoder(byte[] input, byte[] output) 
	{	
		//System.arraycopy(input, 0, yuv420, 0, input.length);
		if(instance == null
				||mediaCodecEncoder == null
				||yuv420Temp == null) 
			return -1;
		Log.d(TAG, "NgnAvcEncoder Color format : " + colorFormatName(colorFormat)+", SDKVersion: "+NgnApplication.getSDKVersion()+"Rotation: "+mRotation);
		
 		if(colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
 		{
 			swapNV21toNV12(input, yuv420, m_width, m_height);
 			if(mRotation == 90)
 			{
 				//System.arraycopy(yuv420, 0,  yuv420Temp, 0, yuv420.length);
 				if(mFlip)
 					YUV420spRotateNegative90(yuv420,yuv420Temp, m_width, m_height); //逆时针旋转90度
 				else
 					rotateYUV420SP90(yuv420,yuv420Temp, m_width, m_height);
 			}
 		}
		else if(colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar)
		{
			YUV420SPtoYUV420(input, yuv420, m_width, m_height);
			
			if(mRotation == 90)
			{
				//System.arraycopy(yuv420, 0,  yuv420Temp, 0, yuv420.length);
				if(mFlip)
					YUV420PRotateNegative90(yuv420,yuv420Temp, m_width, m_height); //逆时针旋转90度
				else
					rotateYUV420P90(yuv420,yuv420Temp, m_width, m_height);
			}
		}
		else //if(NgnApplication.getSDKVersion() > 19)
		{
			//Log.d(TAG, "Unknown Color format : " + colorFormatName(colorFormat));
			if(yuv420 != null)
				YUV420SPtoYUV420(input, yuv420, m_width, m_height);
			else 
				return 0;
		}
//		if(mRotation == 90)
//		{
//			//System.arraycopy(yuv420, 0,  yuv420Temp, 0, yuv420.length);
//			if(mFlip)
//				YUV420spRotateNegative90(yuv420,yuv420Temp, m_width, m_height); //逆时针旋转90度
//			else
//				rotateYUV420Degree90(yuv420,yuv420Temp, m_width, m_height);
//		}
		int dataLen = input.length;
		int pos = 0;
	
	    try {
	    	inputBuffers = mediaCodecEncoder.getInputBuffers();
	  	    outputBuffers   = mediaCodecEncoder.getOutputBuffers();
	    	int inputBufferIndex = mediaCodecEncoder.dequeueInputBuffer(-1);
	        if (inputBufferIndex >= 0) 
	        {
	            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
	            inputBuffer.clear();
	            if(mRotation == 90)
	            	inputBuffer.put(yuv420Temp);
	            else
	            	inputBuffer.put(yuv420);
	           
	            long ptsUsec = computePresentationTime();
	            mediaCodecEncoder.queueInputBuffer(inputBufferIndex, 0,dataLen ,ptsUsec , 0);
	        }

	        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
	        int outputBufferIndex = mediaCodecEncoder.dequeueOutputBuffer(bufferInfo,0);
	        Log.d("",String.format("NgnAvcEncoder frameNum %d = %d",numInputFrames,outputBufferIndex));
//	        switch (outputBufferIndex ) 
//	        {
//	        case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
//	  	      Log.d(TAG, "MediaCodec Encode INFO_OUTPUT_BUFFERS_CHANGED");
//	  	      outputBuffers = mediaCodecEncoder.getOutputBuffers();
//	  	      break;
//	  	    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
//	  	      Log.d(TAG, "MediaCodec Encode New format changed." );
//	  	      break;
//	  	    case MediaCodec.INFO_TRY_AGAIN_LATER:
//	  	      Log.d(TAG, "MediaCodec Encode dequeueOutputBuffer timed out!");
//	  	      break;
//	  	    default:
	  	    if(outputBufferIndex >= 0){
	            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
	            byte[] outData = new byte[bufferInfo.size];
	            outputBuffer.get(outData);
	           Log.d("",String.format("NgnAvcEncoder (default) frame %d = %X-%X-%X-%X-%X", numInputFrames,outData[0],outData[1],outData[2],outData[3],outData[4]));
	        	
	            if(m_info != null)
	            {            	
	            	System.arraycopy(outData, 0,  output, pos, outData.length);
	 	            pos += outData.length;
	 	           Log.d("",String.format("NgnAvcEncoder (m_info)frame %d = %X-%X-%X-%X-%X", numInputFrames,output[0],output[1],output[2],output[3],output[4]));
		        	
	            }
	            else
	            {
	            	 ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);  
	            	// Log.d("",String.format("SPSPPS %X--Profile= %X", output[4],output[5]));
	               //  if (spsPpsBuffer.getInt() == 0x00000001) 
	            	 if(outData[4] == 0x67 || outData[4] == 0x27)
	                 {  
	                	 m_info = new byte[outData.length];
	                	 System.arraycopy(outData, 0, m_info, 0, outData.length);
	                	 Log.d("",String.format("NgnAvcEncoder (m_info=null)frame %d = %X-%X-%X-%X-%X", numInputFrames,m_info[0],m_info[1],m_info[2],m_info[3],m_info[4]));
	 		        	
	                	// System.arraycopy(outData, 0, output, 0, outData.length);
//	                	pos += outData.length;
//	                	Log.d("","MediaCodec Enc m_info Start:");
//	                	 for(int i = 0; i < m_info.length; ++i)
//	                		 Log.d("",String.format("MediaCodec Enc m_info[%d]=%X",i,m_info[i]));
//	                	 Log.d("","MediaCodec Enc m_info END!");
	                 } 
	                 else 
	                 {  
	                	 Log.e(TAG,"NgnAvcEncoder Error: something is miss?");
	                     return -1;
	                 } 
	                 	
	            }
	            mediaCodecEncoder.releaseOutputBuffer(outputBufferIndex, false);
	          //  outputBufferIndex = mediaCodecEncoder.dequeueOutputBuffer(bufferInfo, 0);
	        }else {
	        	Log.e(TAG,"outputBufferIndex < 0");
				return -1;
			}
	        if(output[4] == 0x65 || output[4] == 0x25) //key frame
	        {
	        	long end = new Date().getTime();
	        	if(startTime == 0)
	        		startTime = end;
	        	Log.d("",String.format("NgnAvcEncoder IDR %d = %X-%X-%X-%X-%X;%d", numInputFrames,output[0],output[1],output[2],output[3],output[4],end-startTime));
	        	System.arraycopy(output, 0,  yuv420, 0, pos);
	        	System.arraycopy(m_info, 0,  output, 0, m_info.length);
	        	System.arraycopy(yuv420, 0,  output, m_info.length, pos);
	        	pos += m_info.length;
	        	startTime = end;
	        }
	        
	    } catch (Throwable t) {
	        t.printStackTrace();
	    }
	  /*  Log.d("","MediaCodec Enc output Start:");
	    Log.d("",String.format("MediaCodec Enc Dump:%s", HexDump.dump(output, 0, pos)));
   	 	for(int i = 0; i < pos; ++i)
   	 	{
   	 		Log.d("","MediaCodec Enc output i = "+i);
   	 		Log.d("",String.format("MediaCodec Enc output[%d]=%X",i,output[i]));
   	 	} 
   	 	Log.d("","MediaCodec Enc output END!");*/
//	    if(pos > 0)
//	    	writeToFile(output,pos);
	    return pos;
	}
	private void writeToFile(byte [] output, int len)
	{
		 try {
				fos.write(output,0,len);
				fos.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private void closeFile()
	{
		try {
			fos.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*Sprivate void YUV420spRotate90(byte [] src,byte [] dst,  int srcWidth, int srcHeight)
	{
	    int nWidth = 0, nHeight = 0;
	    int wh = 0;
	    int uvHeight = 0;
	    if(srcWidth != nWidth || srcHeight != nHeight)
	    {
	        nWidth = srcWidth;
	        nHeight = srcHeight;
	        wh = srcWidth * srcHeight;
	        uvHeight = srcHeight >> 1;//uvHeight = height / 2
	    }

	    //旋转Y
	    int k = 0;
//	    for(int i = 0; i < srcWidth; i++) {
//	    	int nPos = 0;
//	        for(int j = 0; j < srcHeight; j++) {
//	        	dst[k] = src[nPos + i];
//	            k++;
//	            nPos += srcWidth;
//	        }
//	    }

	    for(int i = 0; i < srcWidth; i++) {
	    	int nPos = srcWidth-1;
	        for(int j = 0; j < srcHeight; j++) {
	        	dst[k] = src[nPos + i];
	            k++;
	            nPos -= srcWidth;
	        }
	    }
	    for(int i = 0; i < srcWidth; i+=2){
	        int nPos = wh;
	        for(int j = 0; j < uvHeight; j++) {
	            dst[k] = src[nPos + i];
	            dst[k + 1] = src[nPos + i + 1];
	            k += 2;
	            nPos += srcWidth;
	        }
	    }
	   
	}*/
	//逆时针旋转90度：

	void YUV420spRotateNegative90(byte [] src, byte [] dst, int srcWidth, int srcHeight)
	{
	    int nWidth = 0, nHeight = 0;
	    int wh = 0;
	    int uvHeight = 0;
	    if(srcWidth != nWidth || srcHeight != nHeight)
	    {
	        nWidth = srcWidth;
	        nHeight = srcHeight;
	        wh = srcWidth * srcHeight;
	        uvHeight = srcHeight >> 1;//uvHeight = height / 2
	    }

	    //旋转Y
	    int k = 0;
	    for(int i = 0; i < srcWidth; i++){
	        int nPos = srcWidth - 1;
	        for(int j = 0; j < srcHeight; j++)
	        {
	            dst[k] = src[nPos - i];
	            k++;
	            nPos += srcWidth;
	        }
	    }

	    for(int i = 0; i < srcWidth; i+=2){
	        int nPos = wh + srcWidth - 1;
	        for(int j = 0; j < uvHeight; j++) {
	            dst[k] = src[nPos - i - 1];
	            dst[k + 1] = src[nPos - i];
	            k += 2;
	            nPos += srcWidth;
	        }
	    }

	    
	}

    private void swapYV12toI420(byte[] yv12bytes, byte[] i420bytes, int width, int height) 
    {   
    	System.arraycopy(yv12bytes, 0, i420bytes, 0,width*height);
    	System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height,width*height/4);
    	System.arraycopy(yv12bytes, width*height, i420bytes, width*height+width*height/4,width*height/4); 
    	
    	//System.arraycopy(yv12bytes, width*height+width*height/4, i420bytes, width*height+width*height/4,width*height/4);
    	//System.arraycopy(yv12bytes, width*height, i420bytes, width*height,width*height/4); 
    } 
    private static void swapNV21toNV12(byte[] nv21, byte[] nv12, int width, int height)//yuv420sp
    {
	    if (nv21 == null ||nv12 == null)
	    return;
	    int framesize = width*height;
	    int frameLen = (framesize*3)>>1;
	    int j = 0;
	    //copy y
	    System.arraycopy(nv21, 0, nv12, 0,framesize);
	  
	    //switch u/v
	    for(j = framesize; j < frameLen;j+=2)
	    {
	    	nv12[j] = nv21[j+1];
	    	nv12[j+1] = nv21[j];
	    }
	    
    }
    private static void YUV420SPtoYUV420(byte[] yuv420sp, byte[] yuv420, int width, int height)
    {
	    if (yuv420sp == null ||yuv420 == null)
	    return;
	    int framesize = width*height;
	    int frameLen = framesize*3>>1;
	    int times = framesize>>1;
	    int i = 0, j = 0;
	    System.arraycopy(yuv420sp, 0, yuv420, 0,framesize);
//	    byte temp;
	    //switch u/v
//	    for(j = framesize; j < frameLen;j+=2)
//	    {
//	    	temp = yuv420sp[j];
//	    	yuv420sp[j] = yuv420sp[j+1];
//	    	yuv420sp[j+1] = temp;
//	    }
	    i = 0;
	    for(j = 1; j < times;j+=2)
	    {
	    	yuv420[i+framesize] = yuv420sp[j+framesize];
	    	++i;
	    }
	    i = 0;
	    int step = (framesize*5)>>2;
	    Log.d("",String.format("FRAMELEN = %d --- %d", frameLen, step));
	    for (j = 0; j < times; j+=2)
	    {
	    	yuv420[i + step] = yuv420sp[j+framesize];
	    	++i;
	    }
	   
    }
    public static void rotateNV21(byte[] input, byte[] output, int width, int height, int rotation) {
    
        boolean swap = (rotation == 90 || rotation == 270);
        boolean flip = (rotation == 90 || rotation == 180);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int xo = x, yo = y;
                int w = width, h = height;
                int xi = xo, yi = yo;
                if (swap) {
                    xi = w * yo / h;
                    yi = h * xo / w;
                }
                if (flip) {
                    xi = w - xi - 1;
                    yi = h - yi - 1;
                }
                output[w * yo + xo] = input[w * yi + xi];
                int fs = w * h;
                int qs = (fs >> 2);
                xi = (xi >> 1);
                yi = (yi >> 1);
                xo = (xo >> 1);
                yo = (yo >> 1);
                w = (w >> 1);
                h = (h >> 1);
                // adjust for interleave here
                /*int ui = fs + (w * yi + xi) * 2;
                 int uo = fs + (w * yo + xo) * 2;
                // and here
                 int vi = ui + 1;
                 int vo = uo + 1;
                 output[uo] = input[ui]; 
                 output[vo] = input[vi]; */
              /*  int ui = fs + w * yi + xi;
    			int uo = fs + w * yo + xo;
    			int vi = qs + ui;
    			int vo = qs + uo;
    			output[uo] = input[vi]; 
    			output[vo] = input[ui]; */

                int ui = fs + (w * yi + xi) * 2;
                int uo = fs + w * yo + xo;
                int vi = ui + 1;
                int vo = qs + uo;
                output[uo] = input[vi]; 
                output[vo] = input[ui]; 
            }
        }
    }
    private  void rotateYUV420SP90(byte[] data,byte [] yuv, int imageWidth, int imageHeight) 
    {
        //byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)                               
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components 
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)                                
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
       
        Log.d("",String.format("NgnAvcEncoder: rotation=%d, flipe=%s",mRotation,mFlip));
       
    }

    private  void rotateYUV420P90(byte[] data,byte [] yuv, int imageWidth, int imageHeight) 
    {
        //byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)                               
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        int step = imageWidth*imageHeight;
        int halfW = imageWidth >>1;
        int halfH = imageHeight >>1;
        i = step;
        for(int x = 0;x < halfW;x++)
        {
            for(int y = halfH-1;y >= 0;y--)                               
            {
                yuv[i++] = data[y*halfW+x+step];
            }
        }
        i = imageWidth*imageHeight*5>>2;
        step = i;
        for(int x = 0;x < halfW;x++)
        {
            for(int y = halfH-1;y >= 0;y--)                               
            {
                yuv[i++] = data[y*halfW+x+step];
            }
        }
        Log.d("",String.format("NgnAvcEncoder : rotation=%d, flipe=%s",mRotation,mFlip));
       
    }
    void YUV420PRotateNegative90(byte [] src, byte [] dst, int imageWidth, int imageHeight) //flip
	{
    	 int i = 0;
         for(int x = imageWidth;x>0;--x)
         {
             for(int y = 0;y < imageHeight;++y)                               
             {
                 dst[i++] = src[y*imageWidth+x];
             }
         }
         // Rotate the U and V color components
         int step = imageWidth*imageHeight;
         int halfW = imageWidth >>1;
         int halfH = imageHeight >>1;
         i = step;
         for(int x = halfW-1;x >= 0;--x)
         {
             for(int y = 0 ;y < halfH;++y)                               
             {
                 dst[i++] = src[y*halfW+x+step];
             }
         }
         i = imageWidth*imageHeight*5>>2;
         step = i;
         for(int x = halfW-1;x >= 0;--x)
         {
             for(int y = 0 ;y < halfH;++y)                              
             {
                 dst[i] = src[y*halfW+x+step];
                 ++i;
             }
         }
	    
	}
    private static long computePresentationTime() {
    	 if(++numInputFrames >= Integer.MAX_VALUE)
         	numInputFrames = 0;
    	return numInputFrames * 1000000 / frameRate;
    }
    private void createFile()
    {
        mVideoFile = new File(Environment.getExternalStorageDirectory() + File.separator + "videoSend.h264");
       
        if (!mVideoFile.exists())
        {
            try
            {
                mVideoFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }else
        {
        	mVideoFile.delete();
        }
        try
        {
            fos = new FileOutputStream(mVideoFile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
   
	private int getColorFormat(String mimeType) 
	{
	
		int numCodecs = MediaCodecList.getCodecCount();
		Log.d(TAG, "numCodecs : " + numCodecs);
		MediaCodecInfo codecInfo = null;
		for (int i = 0; i < numCodecs && codecInfo == null; i++) 
		{
			MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
			Log.d(TAG, "Codec : " + info.getName());
			if (!info.isEncoder()) 
			{
				Log.d(TAG, "not encoder");
				continue;
			}

			String[] types = info.getSupportedTypes();
			boolean found = false;
			for (int j = 0; j < types.length && !found; j++) 
			{
				if (types[j].equals(mimeType))
				{
					Log.d(TAG, types[j] + " found!!!");
					found = true;
				} 
				else 
				{
					Log.d(TAG, types[j]);
				}
			}
			
			if (!found)
				continue;
			
			codecInfo = info;
		}

		if (codecInfo == null) 
		{
			Log.d(TAG, "Encoder not found");
			return -1;
		}
		
		Log.d(TAG, "Using codec : " + codecInfo.getName() + "     supporting: "+ mimeType);		

		int colorFormat = 0;

		MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
		for (int i = 0; i < capabilities.colorFormats.length ; i++) 
		{
			int format = capabilities.colorFormats[i];
			Log.d(TAG, "Color format : " + colorFormatName(format));
			switch (format) 
			{
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
				if (colorFormat == 0)
					colorFormat = format;
				break;
			default:
				break;
			}
		}
		
		if (colorFormat == 0) 
		{
			Log.d(TAG, "No supported color format");
			return -1;
		}
		Log.d(TAG, "Using color format : " + colorFormatName(colorFormat));

		return colorFormat;
	}
    
    private String colorFormatName(int format)
	{
		String name;
		switch (format) 
		{
		case MediaCodecInfo.CodecCapabilities.COLOR_Format12bitRGB444:
			name = "COLOR_Format12bitRGB444";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB1555:
			name = "COLOR_Format16bitARGB1555";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitARGB4444:
			name = "COLOR_Format16bitARGB4444";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitBGR565:
			name = "COLOR_Format16bitBGR565";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format16bitRGB565:
			name = "COLOR_Format16bitRGB565";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format18BitBGR666:
			name = "COLOR_Format18BitBGR666";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format18bitARGB1665:
			name = "COLOR_Format18bitARGB1665";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format18bitRGB666:
			name = "COLOR_Format18bitRGB666";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format19bitARGB1666:
			name = "COLOR_Format19bitARGB1666";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24BitABGR6666:
			name = "COLOR_Format24BitABGR6666";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24BitARGB6666:
			name = "COLOR_Format24BitARGB6666";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24bitARGB1887:
			name = "COLOR_Format24bitARGB1887";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24bitBGR888:
			name = "COLOR_Format24bitBGR888";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888:
			name = "COLOR_Format24bitRGB888";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format25bitARGB1888:
			name = "COLOR_Format25bitARGB1888";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format32bitARGB8888:
			name = "COLOR_Format32bitARGB8888";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format32bitBGRA8888:
			name = "COLOR_Format32bitBGRA8888";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_Format8bitRGB332:
			name = "COLOR_Format8bitRGB332";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatCbYCrY:
			name = "COLOR_FormatCbYCrY";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatCrYCbY:
			name = "COLOR_FormatCrYCbY";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL16:
			name = "COLOR_FormatL16";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL2:
			name = "COLOR_FormatL2";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL24:
			name = "COLOR_FormatL24";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL32:
			name = "COLOR_FormatL32";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL4:
			name = "COLOR_FormatL4";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatL8:
			name = "COLOR_FormatL8";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatMonochrome:
			name = "COLOR_FormatMonochrome";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer10bit:
			name = "COLOR_FormatRawBayer10bit";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bit:
			name = "COLOR_FormatRawBayer8bit";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatRawBayer8bitcompressed:
			name = "COLOR_FormatRawBayer8bitcompressed";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYCbYCr:
			name = "COLOR_FormatYCbYCr";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYCrYCb:
			name = "COLOR_FormatYCrYCb";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411PackedPlanar:
			name = "COLOR_FormatYUV411PackedPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV411Planar:
			name = "COLOR_FormatYUV411Planar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
			name = "COLOR_FormatYUV420PackedPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
			name = "COLOR_FormatYUV420PackedSemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
			name = "COLOR_FormatYUV420Planar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
			name = "COLOR_FormatYUV420SemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedPlanar:
			name = "COLOR_FormatYUV422PackedPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422PackedSemiPlanar:
			name = "COLOR_FormatYUV422PackedSemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422Planar:
			name = "COLOR_FormatYUV422Planar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV422SemiPlanar:
			name = "COLOR_FormatYUV422SemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV444Interleaved:
			name = "COLOR_FormatYUV444Interleaved";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar:
			name = "COLOR_QCOM_FormatYUV420SemiPlanar";
			break;
		case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
			name = "COLOR_TI_FormatYUV420PackedSemiPlanar";
			break;
		default:
			name = "???";
		}
		name += "(" + format + ")";
		return name;
	}
}


