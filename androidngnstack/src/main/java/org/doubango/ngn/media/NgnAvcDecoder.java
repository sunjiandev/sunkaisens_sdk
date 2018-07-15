package org.doubango.ngn.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.sunkaisens.skdroid.util.HexDump;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaCodec.BufferInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

public class NgnAvcDecoder {
	private static final String TAG = NgnAvcDecoder.class.getCanonicalName();
	private static NgnAvcDecoder instance = null;
	private  String mimeType = "video/avc";
	private MediaCodec mediaCodecDecoder;
	private ByteBuffer[] decoderInputBuffers = null;
	private ByteBuffer[] decoderOutputBuffers = null;
	private long timestamp =  0l;
	private File mVideoFile;
	private static int mFps;
	
	
	private int mWidth;
	private int mHeight;
	
	final int TIMEOUT_USEC = 10000;
	
	private int frameNum = 0;
    private FileOutputStream fostream = null;
    
	public NgnAvcDecoder()
	{
		
	}
	public static NgnAvcDecoder getInstance()
	{
		if(instance == null)
			instance = new NgnAvcDecoder();
		return instance;
	}
	public void setNgnAveDecoderParameter(int width,int height,int fps,Surface surface) {
		// TODO Auto-generated constructor stub
		Log.d(TAG, "create decoder...");
			mediaCodecDecoder = MediaCodec.createDecoderByType(mimeType);

		MediaFormat format = MediaFormat.createVideoFormat(mimeType, width, height);
		Log.d(TAG, "Configuring decoder with input format : " + format);
		mediaCodecDecoder.configure(format, 		// 	The format of the input data (decoder)
				surface, 				// 	a surface on which to render the output of this decoder.
				null, 					// 	a crypto object to facilitate secure decryption of the media data.
				0 						// 	configure the component as an decoder.
		);
		
		mediaCodecDecoder.start();
		decoderInputBuffers = mediaCodecDecoder.getInputBuffers();
		decoderOutputBuffers = mediaCodecDecoder.getOutputBuffers();
		mFps = fps;
		mWidth = width;
		mHeight = height;
		
		//createFile();
		
	}
	
	public void releaseDecoder() 
	{
		if (mediaCodecDecoder != null) 
		{
			try 
			{
				mediaCodecDecoder.stop();
				mediaCodecDecoder.release();
				instance = null;
				
				//closeFile();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	@SuppressLint("WrongConstant")
	public int offerDecoderSurface(byte[] datarecv, int recvLen)
	{
		if(instance == null||recvLen <=0
				|| decoderInputBuffers == null
				|| decoderOutputBuffers == null
				|| mediaCodecDecoder == null) 
			return 0;
		
		//if(recvLen > 0) writeToFile(datarecv,recvLen);
		//decoderInputBuffers = mediaCodecDecoder.getInputBuffers();
		int outIndex = -1;
		try
		{
			int inputBufferIndex = mediaCodecDecoder.dequeueInputBuffer(TIMEOUT_USEC);//TIMEOUT_USEC);
		
			Log.d("NgnAvcDecoder",String.format("NgnAvcDecoder inputBufferIndex = %d", inputBufferIndex));
			if (inputBufferIndex >= 0) {
	
				ByteBuffer inputBuffer = decoderInputBuffers[inputBufferIndex];  
				inputBuffer.clear(); 
				inputBuffer.put(datarecv);
				if(frameNum++ >= Integer.MAX_VALUE)
					frameNum = 0;
				long ptsUsec = computePresentationTime(frameNum);
				mediaCodecDecoder.queueInputBuffer(inputBufferIndex, 0,recvLen, ptsUsec, 0);
			}
			
			// get a outputBuffer memory index
			BufferInfo bufferInfo = new BufferInfo();
			// if inputBuffer is null ,this fun will be blocking
			outIndex = mediaCodecDecoder.dequeueOutputBuffer(bufferInfo,0);
			//Log.d(TAG, "dequeueOutputBuffer index " + outIndex);
			
			switch (outIndex) {
		    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
		      Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
		      decoderOutputBuffers = mediaCodecDecoder.getOutputBuffers();
		      break;
		    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
		      Log.d("DecodeActivity", "New format " + mediaCodecDecoder.getOutputFormat());
		      break;
		    case MediaCodec.INFO_TRY_AGAIN_LATER:
		      Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
		      break;
		    default:
		      ByteBuffer buf = decoderOutputBuffers[outIndex];
		     // Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + buf);
		      //Log.d("",String.format("NgnAvcDecoder buf.capacity=[%d,%d] buf=[%d,%d]",decoderOutputBuffers[outIndex].capacity(), buf.capacity(),bufferInfo.offset,bufferInfo.size));
				
				mediaCodecDecoder.releaseOutputBuffer(outIndex, true);
				break;
			}
		} catch (Throwable t) {
	        t.printStackTrace();
	    }
		
		return outIndex;
	}
	  
	public int offerDecoder(byte[] datarecv, byte[] output,int recvLen) 
	{
		if(instance == null
				||decoderInputBuffers == null
				||mediaCodecDecoder==null
				||decoderOutputBuffers == null)
			return -1;
//		writeToFile(datarecv,recvLen);
		int inputBufferIndex = mediaCodecDecoder.dequeueInputBuffer(-1);
		Log.d("NgnAvcDecoder",String.format("NgnAvcDecoder inputBufferIndex = %d", inputBufferIndex));
		if (inputBufferIndex >= 0) {

			ByteBuffer inputBuffer = decoderInputBuffers[inputBufferIndex];  
			Log.d("",String.format("NgnAvcDecoder inbuf.capacity=%d recvLen=%d", inputBuffer.capacity(),recvLen));
			inputBuffer.clear(); 
			inputBuffer.put(datarecv);
			if(frameNum++ >= Integer.MAX_VALUE)
				frameNum = 0;
			long ptsUsec = computePresentationTime(frameNum);
			mediaCodecDecoder.queueInputBuffer(inputBufferIndex, 0,recvLen, ptsUsec, 0);
			
		}
		
		/** ouput ***/
		// get a outputBuffer memory index
		BufferInfo bufferInfo = new BufferInfo();
		// if inputBuffer is null ,this fun will be blocking
		int outIndex = mediaCodecDecoder.dequeueOutputBuffer(bufferInfo,TIMEOUT_USEC);
		
		switch (outIndex) {
	    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
	      Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
	      decoderOutputBuffers = mediaCodecDecoder.getOutputBuffers();
	      break;
	    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
	      Log.d("DecodeActivity", "New format " + mediaCodecDecoder.getOutputFormat());
	      break;
	    case MediaCodec.INFO_TRY_AGAIN_LATER:
	      Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
	      break;
	    default:
	      ByteBuffer buf = decoderOutputBuffers[outIndex];
	     // Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + buf);
	      //Log.d("",String.format("NgnAvcDecoder buf.capacity=[%d,%d] buf=[%d,%d]",decoderOutputBuffers[outIndex].capacity(), buf.capacity(),bufferInfo.offset,bufferInfo.size));
			buf.position(bufferInfo.offset);
			buf.limit(bufferInfo.offset+bufferInfo.size);
			
			byte [] tempByte = new byte [bufferInfo.size];
			buf.get(tempByte);
		
			mediaCodecDecoder.releaseOutputBuffer(outIndex, true);
			
			YUV420SP2YUV420(tempByte,output,mWidth,mHeight);
			break;
		}

		return outIndex;
		
//		if (outIndex >= 0) 
//		{  
//			ByteBuffer buf = decoderOutputBuffers[outIndex];
			
//			Log.d("",String.format("NgnAvcDecoder buf.capacity=[%d,%d] buf=[%d,%d]",decoderOutputBuffers[outIndex].capacity(), buf.capacity(),bufferInfo.offset,bufferInfo.size));
//			buf.position(bufferInfo.offset);
//			buf.limit(bufferInfo.offset+bufferInfo.size);
//			
//			byte [] tempByte = new byte [bufferInfo.size];
//			buf.get(tempByte);
//		
//			mediaCodecDecoder.releaseOutputBuffer(outIndex, true);
//			
//			YUV420SP2YUV420(tempByte,output,mWidth,mHeight);
//			try {
//				fostream.write(output);
//				//fostream.flush();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	//	} 
//		return outIndex;
	}
	 private static long computePresentationTime(int frameIndex) {
	    	return frameIndex * 1000000 / mFps;
	 }
	 
	 private static void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height)
	 {
		    if (yuv420sp == null ||yuv420 == null||yuv420sp.length==0)
		    return;
		    int framesize = width*height;
		    int i = 0, j = 0;
		    //copy y
		    for (i = 0; i < framesize; i++)
		    {
		    	yuv420[i] = yuv420sp[i];
		    }
		    i = 0;
		    for(j = 0; j < framesize/2;j+=2)
		    {
		    	yuv420[i+framesize] = yuv420sp[j+framesize];
		    	i++;
		    }
		    i = 0;
		    for (j = 1; j < framesize/2; j+=2)
		    {
		    	yuv420[i + framesize*5/4] = yuv420sp[j+framesize];
		    	i++;
		    }
		   
	}
	private void createFile()
    {
        mVideoFile = new File(Environment.getExternalStorageDirectory() + File.separator + "videoRecv.264");
       
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
        }else{
        	mVideoFile.delete();
        }
        try
        {
        	fostream = new FileOutputStream(mVideoFile);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
	private void writeToFile(byte [] output, int len)
	{
		 try {
			 fostream.write(output,0,len);
			 fostream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	private void closeFile()
	{
		try {
			fostream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
}
