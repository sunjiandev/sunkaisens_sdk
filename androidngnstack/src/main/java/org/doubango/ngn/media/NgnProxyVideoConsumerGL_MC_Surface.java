/* Copyright (C) 2012, Doubango Telecom <http://www.doubango.org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.ngn.media;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.events.NgnMediaPluginEventArgs;
import org.doubango.ngn.events.NgnMediaPluginEventTypes;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.tinyWRAP.ProxyVideoConsumer;
import org.doubango.tinyWRAP.ProxyVideoConsumerCallback;
import org.doubango.tinyWRAP.ProxyVideoFrame;

//import com.sunkaisens.skdroid.util.MyLog;

import org.doubango.utils.MyLog;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

/**
 * Video consumer using OpenGL ES 2.0
 */
public class NgnProxyVideoConsumerGL_MC_Surface extends NgnProxyVideoConsumer{
	private static final String TAG = NgnProxyVideoConsumerGL_MC_Surface.class.getCanonicalName();
	private static final int DEFAULT_VIDEO_WIDTH = 176;
	private static final int DEFAULT_VIDEO_HEIGHT = 144;
	private static final int DEFAULT_VIDEO_FPS = 15;
	
	private final NgnProxyVideoConsumerGLCallback mCallback;
	private final ProxyVideoConsumer mConsumer;
//	private static ProxyVideoConsumer mPVC;
	private ByteBuffer mVideoFrame;
	private Context mContext;
	private NgnProxyVideoConsumerGLPreviewSurface mPreviewSurface;
	private int mWidth;
	private int mHeight;
	private int mFps;
//	private static boolean mIsFirst = true;
	
	private byte[] dataOut;
	private int recvLen;
	private static Surface mDecoderSurface ;
	

	protected NgnProxyVideoConsumerGL_MC_Surface(BigInteger id, ProxyVideoConsumer consumer){
    	super(id, consumer);
    	mConsumer = consumer;
//    	//解决视频通话时，图像宽高比首次不正确问题
//    	mPVC = consumer;
    	mCallback = new NgnProxyVideoConsumerGLCallback(this);
    	mConsumer.setCallback(mCallback);

    	// Initialize video stream parameters with default values
    	mWidth = NgnProxyVideoConsumerGL_MC_Surface.DEFAULT_VIDEO_WIDTH;
    	mHeight = NgnProxyVideoConsumerGL_MC_Surface.DEFAULT_VIDEO_HEIGHT;
    	mFps = NgnProxyVideoConsumerGL_MC_Surface.DEFAULT_VIDEO_FPS;
    }
    
    @Override
    public void invalidate(){
    	super.invalidate();
    	mVideoFrame = null;
    	System.gc();
    }
    
    @Override
    public void setContext(Context context){
    	mContext = context;
    }
    
    @Override
    public final View startPreview(Context context){
    	synchronized(this){
	    	mContext = context == null ? mContext : context;
	    	if(mContext != null){
		    		if(mPreviewSurface == null || mPreviewSurface.isDestroyed()){
		    			mPreviewSurface = new NgnProxyVideoConsumerGLPreviewSurface(mContext,super.mFullScreenRequired,  mWidth, mHeight,mFps);
		    		}		    		
	    	}
	    	return mPreviewSurface;			   
    	}
    }
    
    @Override
	public final View startPreview(){
		return startPreview(null);
	}
    
    private int prepareCallback(int width, int height, int fps){
    	synchronized(this){
	    	Log.d(TAG, "prepareCallback("+width+","+height+","+fps+")");
	    	
	    	// Update video stream parameters with real values (negotiated)
			mWidth = width;
			mHeight = height;
			mFps = fps;
			mVideoFrame = ByteBuffer.allocateDirect((mWidth * mHeight * 3) >> 1);
			mConsumer.setConsumeBuffer(mVideoFrame, mVideoFrame.capacity());			
			dataOut = new byte[(mWidth * mHeight * 3) >> 1];
			super.mPrepared = true;
			return 0;
    	}
    }
    
    private int startCallback(){
    	synchronized(this){
	    	Log.d(NgnProxyVideoConsumerGL_MC_Surface.TAG, "startCallback");
	    	super.mStarted = true;
	    	return 0;
    	}
    }

    private int bufferCopiedCallback(long nCopiedSize, long nAvailableSize) {
    	if(!super.mValid){
			Log.e(TAG, "Invalid state");
			return -1;
		}
		if(mPreviewSurface == null || !mPreviewSurface.isReady()){
			// Not on the top
			return 0;
		}

		if(mVideoFrame != null )
		{
//			synchronized(mPreviewSurface){
			 synchronized (mVideoFrame) {
				recvLen = (int)nCopiedSize;
				Log.d("",String.format("NgnAvcDecoder: [%d,%d] %d %d", mWidth,mHeight,(int)nCopiedSize,(int)nAvailableSize));
				
				mVideoFrame.rewind();
				mVideoFrame.get(dataOut,0,recvLen);
				
//				if(mPreviewSurface.startDecodeFrame == false)
//				{
//					mPreviewSurface.decodeFramePreview.start();
//				}
				if(dataOut != null){
					NgnAvcDecoder.getInstance().offerDecoderSurface(dataOut, recvLen);
				}else {
					MyLog.d(TAG, "dataOut is null.");
				}
			}
		}
		
		return 0;
    }
    
    private int consumeCallback(ProxyVideoFrame _frame){
		if(!super.mValid){
			Log.e(TAG, "Invalid state");
			return -1;
		}
		if(mPreviewSurface == null || !mPreviewSurface.isReady()){
			// Not on the top
			return 0;
		}
		
		// Get video frame content from native code
		_frame.getContent(mVideoFrame, mVideoFrame.capacity());
		
		mPreviewSurface.requestRender();
		
		return 0;
    }

    private int pauseCallback(){
    	synchronized(this){
	    	Log.d(TAG, "pauseCallback");
	    	super.mPaused = true;
	    	return 0;
    	}
    }
    
    private synchronized int stopCallback(){
    	synchronized(this){
	    	Log.d(TAG, "stopCallback");
	    	super.mStarted = false;
	    	
	    	mPreviewSurface = null;
	    	NgnAvcDecoder.getInstance().releaseDecoder();
	    	return 0;
    	}
    }
	
	/**
	 * NgnProxyVideoConsumerGLCallback
	 */
	static class NgnProxyVideoConsumerGLCallback extends ProxyVideoConsumerCallback
    {
        final NgnProxyVideoConsumerGL_MC_Surface mConsumer;

        public NgnProxyVideoConsumerGLCallback(NgnProxyVideoConsumerGL_MC_Surface consumer){
        	super();
        	mConsumer = consumer;
        }
        
        @Override
        public int prepare(int width, int height, int fps){
            int ret = mConsumer.prepareCallback(width, height, fps);
            NgnMediaPluginEventArgs.broadcastEvent(new NgnMediaPluginEventArgs(mConsumer.mId, NgnMediaType.Video, 
            		ret == 0 ? NgnMediaPluginEventTypes.PREPARED_OK : NgnMediaPluginEventTypes.PREPARED_NOK));
            return ret;
        }
        
        @Override
        public int start(){
            int ret = mConsumer.startCallback();
            NgnMediaPluginEventArgs.broadcastEvent(new NgnMediaPluginEventArgs(mConsumer.mId, NgnMediaType.Video, 
            		ret == 0 ? NgnMediaPluginEventTypes.STARTED_OK : NgnMediaPluginEventTypes.STARTED_NOK));
            return ret;
        }

        @Override
        public int consume(ProxyVideoFrame frame){
            return mConsumer.consumeCallback(frame);
        }        
        
        @Override
		public int bufferCopied(long nCopiedSize, long nAvailableSize) {
			return mConsumer.bufferCopiedCallback(nCopiedSize, nAvailableSize);
		}

		@Override
        public int pause(){
            int ret = mConsumer.pauseCallback();
            NgnMediaPluginEventArgs.broadcastEvent(new NgnMediaPluginEventArgs(mConsumer.mId, NgnMediaType.Video, 
            		ret == 0 ? NgnMediaPluginEventTypes.PAUSED_OK : NgnMediaPluginEventTypes.PAUSED_NOK));
            return ret;
        }
        
        @Override
        public int stop(){
            int ret = mConsumer.stopCallback();
            NgnMediaPluginEventArgs.broadcastEvent(new NgnMediaPluginEventArgs(mConsumer.mId, NgnMediaType.Video, 
            		ret == 0 ? NgnMediaPluginEventTypes.STOPPED_OK : NgnMediaPluginEventTypes.STOPPED_NOK));
            return ret;
        }
    }
	
	
	 class NgnProxyVideoConsumerGLPreviewSurface extends GLSurfaceView implements GLSurfaceView.Renderer,SurfaceTexture.OnFrameAvailableListener {//implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener { 
		

		private  String TAG = "NgnProxyVideoConsumerGLPreviewSurface"; 
		 
		private  final int FLOAT_SIZE_BYTES = 4; 
		private  final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 *  FLOAT_SIZE_BYTES; 
		private  final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0; 
		private  final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3; 
		 private  final float[] TRIANFLE_VERTICES_DATA = {
	    		1, -1, 0, 1, 1,
	    	    1, 1, 0, 1, 0,
	    	    -1, 1, 0, 0, 0,
	    	    -1, -1, 0, 0, 1
	    	 };
		private final float[]  mTriangleVerticesData = { 
		// X, Y, Z, U,  V 
		-1.0f, -1.0f, 0, 0.f, 0.f, 
		1.0f,  -1.0f, 0, 1.f, 0.f, 
		-1.0f, 1.0f, 0, 0.f, 1.f, 
		1.0f, 1.0f, 0, 1.f, 1.f, 
		}; 
		 
		private FloatBuffer mTriangleVertices; 
		 
		private final String mVertexShader = "uniform mat4 uMVPMatrix;\n" + 
		"uniform mat4 uSTMatrix;\n" + "attribute vec4 aPosition;\n" + 
		"attribute vec4 aTextureCoord;\n" + "varying vec2 vTextureCoord;\n" + 
		"void main() {\n" + " gl_Position = uMVPMatrix * aPosition;\n" + 
		" vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" + "}\n"; 
		 
		private final String mFragmentShader = "#extension GL_OES_EGL_image_external : require\n" + 
		"precision mediump float;\n" + "varying vec2 vTextureCoord;\n" + 
		"uniform samplerExternalOES sTexture;\n" + "void main() {\n" + 
		" gl_FragColor = texture2D(sTexture, vTextureCoord);\n" + "}\n";
		
		private static final String VERTEX_SHADER_SOURCE =
		        "attribute vec4 aPosition;\n" +
		        "attribute vec2 aTextureCoord;\n" +
		        "varying vec2 vTextureCoord;\n" +
		        "void main() {\n" +
		        "  gl_Position = aPosition;\n" +
		        "  vTextureCoord = aTextureCoord;\n" +
		        "}\n";

		    private static final String FRAGMENT_SHADER_SOURCE = "precision mediump float;" +
		    "varying vec2 vTextureCoord;" +
		    "" +
		    "uniform sampler2D SamplerY; " +
		    "uniform sampler2D SamplerU;" +
		    "uniform sampler2D SamplerV;" +
		    "" +
		    "const mat3 yuv2rgb = mat3(1, 0, 1.2802,1, -0.214821, -0.380589,1, 2.127982, 0);" +
		    "" +
		    "void main() {    " +
		    "    vec3 yuv = vec3(1.1643 * (texture2D(SamplerY, vTextureCoord).r - 0.0625)," +
		    "                    texture2D(SamplerU, vTextureCoord).r - 0.5," +
		    "                    texture2D(SamplerV, vTextureCoord).r - 0.5);" +
		    "    vec3 rgb = yuv * yuv2rgb;    " +
		    "    gl_FragColor = vec4(rgb, 1.0);" +
		    "} ";

		 
		private float[] mMVPMatrix = new float[16]; 
		private float[] mSTMatrix = new float[16]; 
		 
		private int mProgram; 
		private int mTextureID; 
		private int muMVPMatrixHandle; 
		private int muSTMatrixHandle; 
		private int maPositionHandle; 
		private int maTextureHandle; 
		 
		private SurfaceTexture mSurfaceTexture;
		private boolean updateSurface =  false; 
		 
		private   int GL_TEXTURE_EXTERNAL_OES =  0x8D65; 
		
		int mBufferWidth, mBufferHeight;
		private int mViewWidth, mViewHeight, mViewX, mViewY;
		private int mFps;
		private boolean mSurfaceCreated;
	    private boolean mSurfaceDestroyed;
	    private boolean mFullScreenRequired;
	    private boolean startDecodeFrame = false;
	  
		 
	    public Thread decodeFramePreview = new Thread("decodeFrameThread"){
			@Override
			public void run() {	
				Log.d(TAG, "decodeFramePreview run...");
				
				long period = (long)(1000 / mFps);
				long start = new Date().getTime();
				startDecodeFrame = true;
				
				while(true){
					if(!NgnAVSession.mSendFrameThread){						
						break;
					}
					 synchronized (mVideoFrame) {
						 if(dataOut != null){
							 NgnAvcDecoder.getInstance().offerDecoderSurface(dataOut, recvLen);
						 }else {
							MyLog.e(TAG, "dataOut is null.");
						}
					 }
					
					requestRender();
					
					long end = new Date().getTime();
					long deltT = end - start;
					 
					if(period > deltT){
						try {
							
							Thread.sleep(period - deltT);
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				
			}};
		  
		 public NgnProxyVideoConsumerGLPreviewSurface(Context  context,boolean fullScreenRequired,int bufferWidth, int bufferHeight,int fps) { 
			 super(context);
		        setEGLContextClientVersion(2);
				setEGLConfigChooser(8, 8, 8, 8, 16, 0);	
		        setRenderer(this);
//		        getHolder().setFormat(PixelFormat.TRANSLUCENT);
		        getHolder().setType(SurfaceHolder.SURFACE_TYPE_GPU);
		        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		  
		        
			 mTriangleVertices = ByteBuffer.allocateDirect( mTriangleVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer(); 
			 mTriangleVertices.put(mTriangleVerticesData).position(0); 
			  
			 Matrix.setIdentityM(mSTMatrix,0);
			 
			 mBufferWidth = bufferWidth;
			 mBufferHeight = bufferHeight;
			 mFps = fps;
			 mFullScreenRequired = fullScreenRequired;
		 } 
		  
		
		  
		 public boolean isDestroyed() {
			// TODO Auto-generated method stub
			 return mSurfaceDestroyed;
		}

		 public boolean isReady(){
				return (mSurfaceCreated && !mSurfaceDestroyed);
		}
			


		@Override 
		 public void onDrawFrame(GL10 glUnused) { 
			
			 synchronized(this) { 
				 if(updateSurface) { 
					 mSurfaceTexture.updateTexImage(); 
					 mSurfaceTexture.getTransformMatrix(mSTMatrix); 
					 updateSurface = false; 
				 } 
				 
				 GLES20.glViewport(mViewX, mViewY, mViewWidth, mViewHeight);
				 GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT  | GLES20.GL_COLOR_BUFFER_BIT); 			  
				 GLES20.glUseProgram(mProgram);
				 //Log.d(TAG,TAG + "onDrawFrame()");
				
				  
				 //bind texture
				 GLES20.glActiveTexture(GLES20.GL_TEXTURE0); 
				 GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID); 
				 
				 mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET); 
				 GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,TRIANGLE_VERTICES_DATA_STRIDE_BYTES,mTriangleVertices); 
				 //checkGlError("glVertexAttribPointer   maPosition"); 
				 GLES20.glEnableVertexAttribArray(maPositionHandle); 
				 //checkGlError("glEnableVertexAttribArray  maPositionHandle"); 
				  
				 mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET); 
				 GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,TRIANGLE_VERTICES_DATA_STRIDE_BYTES,  mTriangleVertices); 
				 checkGlError("glVertexAttribPointer  maTextureHandle"); 
				 GLES20.glEnableVertexAttribArray(maTextureHandle); 
				// checkGlError("glEnableVertexAttribArray  maTextureHandle"); 
				  
				 Matrix.setIdentityM(mMVPMatrix, 0); 
				 GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0); 
				 GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0); 
			 } 
			 
			  
			 GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4); 
			 //checkGlError("glDrawArrays"); 
			 GLES20.glFinish();
		  
		 } 
		  
		 @Override 
		 public  void surfaceDestroyed(SurfaceHolder holder) {
			mSurfaceCreated = false;
		    mSurfaceDestroyed = true;
			super.surfaceDestroyed(holder);
			mSurfaceTexture.release();
			mDecoderSurface.release();
		 } 
		 @Override 
		 public  void onSurfaceChanged(GL10 glUnused, int width, int height) { 
			 GLES20.glViewport(0, 0, width, height);
			 setViewport(width, height);
		 }  
		 @Override 
		 public  void onSurfaceCreated(GL10 glUnused, EGLConfig config) { 
			 Log.d(TAG,TAG + "onSurfaceCreated()");
			 mProgram = createProgram(mVertexShader, mFragmentShader);
			 //mProgram = createProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE);
			 if(mProgram == 0) { 
				 return; 
			 } 
			 maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition"); 
			 checkGlError("glGetAttribLocation aPosition"); 
			 if (maPositionHandle == -1) { 
			 throw new RuntimeException("Could not get attrib location for aPosition"); 
			 } 
			 maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord"); 
			 checkGlError("glGetAttribLocation aTextureCoord"); 
			 if (maTextureHandle == -1) { 
			 throw new RuntimeException("Could not get attrib location for aTextureCoord"); 
			 } 
			  
			 muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix"); 
			 checkGlError("glGetUniformLocation uMVPMatrix"); 
			 if (muMVPMatrixHandle == -1) { 
			 	throw new RuntimeException("Could not get attrib location for uMVPMatrix"); 
			 } 
			  
			 muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix"); 
			 checkGlError("glGetUniformLocation uSTMatrix"); 
			 if(muSTMatrixHandle == -1) { 
				 throw    new RuntimeException("Could not get attrib location for uSTMatrix"); 
			 } 
			  
			  
			 int[] textures = new int[1]; 
			 GLES20.glGenTextures(1, textures, 0); 
			  
			 mTextureID = textures[0]; 
			 GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID); 
			 checkGlError("glBindTexture mTextureID"); 
			  
			 GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST); 
			 GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR); 
			  
			 /* 
			 * Create the SurfaceTexture that will feed this textureID, 
			 * and pass it to the MediaCodec
			 * Link the texture handler to surface texture 
			 */ 
			 mSurfaceTexture = new SurfaceTexture(mTextureID); 
			 //mSurfaceTexture.setDefaultBufferSize(mViewWidth, mViewHeight);
			 mSurfaceTexture.setOnFrameAvailableListener(this);
			 mDecoderSurface = new Surface(mSurfaceTexture); 
			 NgnAvcDecoder.getInstance().setNgnAveDecoderParameter(mBufferWidth, mBufferHeight,mFps,mDecoderSurface);
			 mSurfaceCreated = true;
			
		} 
		 
		 private int loadShader(int shaderType, String source) 
		 { 
			 int shader = GLES20.glCreateShader(shaderType); 
			 if(shader != 0) { 
				 GLES20.glShaderSource(shader,source); 
				 GLES20.glCompileShader(shader); 
				 int[]  compiled = new int[1]; 
				 GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compiled, 0); 
				 if(compiled[0] == 0) { 
					 Log.e(TAG, "Could  not compile shader " + shaderType + ":"); 
					 Log.e(TAG, GLES20.glGetShaderInfoLog(shader)); 
					 GLES20.glDeleteShader(shader); 
					 shader = 0;		 
				 } 
			 } 
			 return  shader; 
		 } 
		  
		 private int createProgram(String vertexSource, String fragmentSource) 
		 { 
			 int  vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource); 
			 if(vertexShader == 0) {
		 		return   0; 
			 } 
			 int  pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
			 if(pixelShader == 0) {
				 return   0; 
			 } 
		  
			 int   program = GLES20.glCreateProgram();
			 if(program != 0) { 
				 GLES20.glAttachShader(program, vertexShader); 
				 checkGlError("glAttachShader"); 
				 GLES20.glAttachShader(program, pixelShader); 
				 checkGlError("glAttachShader"); 
				 GLES20.glLinkProgram(program); 
				 int[]  linkStatus = new int[1]; 
				 GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS,  linkStatus, 0); 
				 if(linkStatus[0] != GLES20.GL_TRUE) { 
					 Log.e(TAG, "Could   not link program: "); 
					 Log.e(TAG, GLES20.glGetProgramInfoLog(program)); 
					 GLES20.glDeleteProgram(program); 
					 program = 0; 
				 } 
			 } 
			 return   program; 
		 } 
		  
		 private void checkGlError(String op) {
			 int    error; 
			 while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) { 
				 Log.e(TAG, op + ": glError " + error); 
				 throw    new RuntimeException(op  + ":    glError " +    error); 
			 } 
		 }

		 private void setViewport(int width, int height){
//		    	MyLog.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> setViewport()");
		    	if(mFullScreenRequired){
		    		mViewWidth = width;
		    		mViewHeight = height;
		    		mViewX = mViewY = 0;
		    	}
		    	else{
//		    		try {
//			    		if (mIsFirst) {
//							Thread.sleep(1000);
//							mIsFirst = false;
//			    		}
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
					
//					//解决视频通话图像首次不正确的问题 宽高比例
//					long frameWidth = mPVC.getDisplayWidth();
//					long frameHeight = mPVC.getDisplayHeight();
//					mBufferWidthY = (int)frameWidth;
//					mBufferHeightY = (int)frameHeight;
//					MyLog.d(TAG, "2222 2222 mBufferWidthY  = " + mBufferWidthY);
//					MyLog.d(TAG, "2222 2222 mBufferHeightY  = " + mBufferHeightY);
		    		float fRatio = ((float) mBufferWidth / (float) mBufferHeight);
//		    		MyLog.d(TAG, "3333 mBufferWidthY  = " + mBufferWidthY);
//		    		MyLog.d(TAG, "3333 mBufferHeightY  = " + mBufferHeightY);
//		    		MyLog.d(TAG, "3333 fRatio  = " + fRatio);
					mViewWidth = (int) ((float) width / fRatio) > height ? (int) ((float) height * fRatio) : width;
					mViewHeight = (int) (mViewWidth / fRatio) > height ? height : (int) (mViewWidth / fRatio);
					mViewX = ((width - mViewWidth) >> 1);
					mViewY = ((height - mViewHeight) >> 1);
		    	}
		    }

		@Override
		public void onFrameAvailable(SurfaceTexture surfaceTexture) {
			// TODO Auto-generated method stub
			updateSurface = true;
			this.requestRender();
			
			Log.d(TAG,TAG + "onFrameAvailable()");
		} 
		  
	} 
	
	// End of  NgnProxyVideoConsumerGLPreviewSurface. 
	
}
