/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
 *  Copyright (C) 2011, Tiscali
 *
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
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
 * 
 * @contributors: See $(DOUBANGO_HOME)\contributors.txt
 */
package org.doubango.ngn.media;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import javax.security.auth.PrivateCredentialPermission;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnProxyVideoProducer_surface.MyProxyVideoProducerCallback;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.tinyWRAP.ProxyVideoProducer;
import org.doubango.tinyWRAP.ProxyVideoProducerCallback;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.R.bool;
import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;

/**
 * MyProxyVideoProducer
 */
public class NgnProxyVideoProducer extends NgnProxyPlugin {
	public static final String TAG = NgnProxyVideoProducer.class
			.getCanonicalName();
	private static final int DEFAULT_VIDEO_WIDTH = 256;
	private static final int DEFAULT_VIDEO_HEIGHT = 188;
	private static final int DEFAULT_VIDEO_FPS = 15;
	private static final int CALLABACK_BUFFERS_COUNT = 3;
	private static final boolean sAddCallbackBufferSupported = NgnCameraProducer
			.isAddCallbackBufferSupported();

	private final ProxyVideoProducer mProducer;
	private final MyProxyVideoProducerCallback mCallback;
	private Context mContext;
	private MyProxyVideoProducerPreview mPreview;
	private int mWidth; // negotiated width
	private int mHeight; // negotiated height
	private int mFps;
	private int mFrameWidth; // camera picture output width
	private int mFrameHeight; // camera picture output height

	public static boolean mIsVideoCallbackStop = false;// 视频相关callback停止标记

	private ByteBuffer mVideoFrame;
	private byte[] mVideoCallbackData;

	private static Handler mTvHandler = null;

	private boolean mVideoSendPause;

	private byte[] dataResult;
	private boolean isUseBypassEncoding = false;

	public NgnProxyVideoProducer(BigInteger id, ProxyVideoProducer producer) {
		super(id, producer);
		mCallback = new MyProxyVideoProducerCallback(this);
		mProducer = producer;
		mProducer.setCallback(mCallback);

		// Initialize video stream parameters with default values
		mFrameWidth = mWidth = NgnProxyVideoProducer.DEFAULT_VIDEO_WIDTH;
		mFrameHeight = mHeight = NgnProxyVideoProducer.DEFAULT_VIDEO_HEIGHT;
		mFps = NgnProxyVideoProducer.DEFAULT_VIDEO_FPS;
	}

	@Override
	public void finalize() throws Throwable{
		super.finalize();
	}

	@Override
	public void invalidate() {
		super.invalidate();

		mVideoFrame = null;
		if (isUseBypassEncoding == true)
			NgnAvcEncoder.getInstance().releaseEncoder();
		System.gc();
	}

	public void setContext(Context context) {
		mContext = context;
	}

	// Very important: Must be done in the UI thread
	@SuppressLint("NewApi")
	public final View startPreview(Context context) {
		MyLog.d(NgnProxyVideoProducer.TAG, "startPreview()");
		mContext = context == null ? mContext : context;
		if (mPreview == null && mContext != null) {
			mPreview = new MyProxyVideoProducerPreview(this);
			mPreview.setSurfaceTextureListener(mPreview);
		}
		if (mPreview != null) {
			if (GlobalSession.bSocketService == true) {
				mPreview.servicemode_Start();// wangds add,2014.6.26
			} else {
				mPreview.setVisibility(View.VISIBLE);
				// mPreview.getHolder().setSizeFromLayout();
				// mPreview.getSurfaceTexture().setSizeFromLayout();
				mPreview.bringToFront();
			}
		}

		return mPreview;
	}

	public final View startPreview() {
		return startPreview(null);
	}

	public final void servicemode_StopPreView() {
		if (GlobalSession.bSocketService == true
				|| GlobalVar.bBackOrSwitch == true) {
			if (mPreview != null) {
				mPreview.servicemode_Stop();
			} else {
				MyLog.e(TAG, "mPreview is null.");
			}
		}
	}

	public void pushBlankPacket() {
		if (super.mValid && mProducer != null) {
			if (mVideoFrame == null) {
				mVideoFrame = ByteBuffer
						.allocateDirect((mWidth * mHeight * 3) >> 1);
			}
			//final ByteBuffer buffer = ByteBuffer.allocateDirect(mVideoFrame.capacity());
			//mProducer.push(buffer, buffer.capacity());
			 if(isUseBypassEncoding)
			  {
				if(dataResult == null)
					dataResult = new byte[(mWidth * mHeight * 3) >> 1];
				Log.d(TAG,String.format("MediaCodec pushBlankPacket..."));
			  }
			 else
				 mProducer.push(mVideoFrame, mVideoFrame.capacity());
		}
	}

	public void toggleCamera() {
		MyLog.d(NgnProxyVideoProducer.TAG, "toggleCamera()");
		if (super.mValid && super.mStarted && !super.mPaused
				&& mProducer != null) {
			final Camera camera = NgnCameraProducer.toggleCamera();
			try {
				startCameraPreview(camera);
			} catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
		}
	}

	public int getTerminalRotation() {
		MyLog.d(NgnProxyVideoProducer.TAG, "getTerminalRotation()");
		final android.content.res.Configuration conf = NgnApplication
				.getContext().getResources().getConfiguration();
		int terminalRotation = 0;
		switch (conf.orientation) {
		case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
			terminalRotation = 0;// The starting position is 0 (landscape).
			break;
		case android.content.res.Configuration.ORIENTATION_PORTRAIT:
			terminalRotation = 90;
			break;
		}
		return terminalRotation;
	}

	public int getNativeCameraHardRotation(boolean preview) {
		MyLog.d(NgnProxyVideoProducer.TAG, "getNativeCameraHardRotation()");
		// only for 2.3 and above
		if (NgnApplication.getSDKVersion() >= 9) {
			try {

				int orientation = 0;
				int cameraId = 0;
				int numOfCameras = NgnCameraProducer.getNumberOfCameras();
				MyLog.d(NgnProxyVideoProducer.TAG, "numOfCameras = "
						+ numOfCameras);
				MyLog.d(NgnProxyVideoProducer.TAG,
						"NgnCameraProducer.isFrontFacingCameraEnabled() = "
								+ NgnCameraProducer
										.isFrontFacingCameraEnabled());
				if (numOfCameras > 1) {
					if (NgnCameraProducer.isFrontFacingCameraEnabled()) {
						cameraId = numOfCameras - 1;
					}
				}

				Class<?> clsCameraInfo = null;

				final Class<?>[] classes = android.hardware.Camera.class
						.getDeclaredClasses();
				for (Class<?> c : classes) {
					if (c.getSimpleName().equals("CameraInfo")) {
						clsCameraInfo = c;
						break;
					}
				}
				if(clsCameraInfo == null){
					return 0;
				}
				
				final Object info = clsCameraInfo.getConstructor((Class[]) null).newInstance((Object[]) null);
				Method getCamInfoMthd = android.hardware.Camera.class.getDeclaredMethod("getCameraInfo", int.class, clsCameraInfo);
				getCamInfoMthd.invoke(null, cameraId, info);

				Display display = NgnApplication.getDefaultDisplay();
				if (display != null) {
					orientation = display.getOrientation();
				}
				orientation = (orientation + 45) / 90 * 90;
				int rotation = 0;

				final Field fieldFacing = clsCameraInfo.getField("facing");
				final Field fieldOrient = clsCameraInfo.getField("orientation");
				final Field fieldFrontFacingConst = clsCameraInfo
						.getField("CAMERA_FACING_FRONT");

				if (fieldFacing.getInt(info) == fieldFrontFacingConst
						.getInt(info)) {
					rotation = (fieldOrient.getInt(info) - orientation + 360) % 360;
				} else {
					// back-facing camera
					rotation = (fieldOrient.getInt(info) + orientation) % 360;
				}

				return rotation;
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		} else {
			int terminalRotation = getTerminalRotation();
			boolean isFront = NgnCameraProducer.isFrontFacingCameraEnabled();
			if (NgnApplication.isSamsung()
					&& !NgnApplication.isSamsungGalaxyMini()) {
				if (preview) {
					if (isFront) {
						if (terminalRotation == 0)
							return 0;
						else
							return 90;
					} else
						return 0;
				} else {
					if (isFront) {
						if (terminalRotation == 0)
							return -270;
						else
							return 90;
					} else {
						if (terminalRotation == 0)
							return 0;
						else
							return 0;
					}
				}
			} else if (NgnApplication.isToshiba()) {
				if (preview) {
					if (terminalRotation == 0)
						return 0;
					else
						return 270;
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		}
	}

	public int compensCamRotation(boolean preview) {

		final int cameraHardRotation = getNativeCameraHardRotation(preview);
		final android.content.res.Configuration conf = NgnApplication
				.getContext().getResources().getConfiguration();
		if (conf.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
			return 0;
		}

		if (NgnApplication.getSDKVersion() >= 9) {
			if (preview) {
				return cameraHardRotation;
			} else {
				switch (cameraHardRotation) {
				case 0:
				case 180:
				default:
					return 0;
				case 90:
				case 270:
					return 90;
				}
			}
		} else {
			int terminalRotation = getTerminalRotation();
			int rotation = 0;
			rotation = (terminalRotation - cameraHardRotation) % 360;
			return rotation;
		}
	}

	public boolean isFrontFacingCameraEnabled() {
		return NgnCameraProducer.isFrontFacingCameraEnabled();
	}

	public void setRotation(int rot) {
		if (mProducer != null && super.mValid) {
			mProducer.setRotation(rot);
		}
	}

	public void setOnPause(boolean pause) {
		if (super.mPaused == pause) {
			return;
		}
		try {
			if (super.mStarted) {
				final Camera camera = NgnCameraProducer.getCamera();
				if (pause) {
					camera.stopPreview();
				} else {
					camera.startPreview();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		super.mPaused = pause;
	}

	private synchronized int prepareCallback(int width, int height, int fps) {
		MyLog.d(NgnProxyVideoProducer.TAG, "prepareCallback(" + width + ","
				+ height + "," + fps + ")");

		// width = 640;
		// height = 480;
		// width = 1280;
		// height = 720;

		mFrameWidth = mWidth = width;
		mFrameHeight = mHeight = height;
		mFps = fps;

		super.mPrepared = true;

		isUseBypassEncoding = NgnEngine
				.getInstance()
				.getConfigurationService()
				.getBoolean(NgnConfigurationEntry.GENERAL_BYPASS_ENCODING,
						NgnConfigurationEntry.DEFAULT_GENERAL_BYPASS_ENCODING);
		if (isUseBypassEncoding)
			dataResult = new byte[(mWidth * mHeight * 3) >> 1];
		return 0;
	}

	private synchronized int startCallback() {
		MyLog.d(TAG, "startCallback");
		mStarted = true;

		if (mPreview != null) {
			startCameraPreview(mPreview.getCamera());
		} else {
			MyLog.d(TAG, "mPreview is null");
		}
		return 0;
	}

	private synchronized int pauseCallback() {
		MyLog.d(TAG, "pauseCallback");
		setOnPause(true);
		return 0;
	}

	public synchronized int stopCallback() {
		MyLog.d(TAG, "stopCallback");

		mIsVideoCallbackStop = true;
		NgnAVSession.mSendFrameThread = false;
		sign = false;
		if (mPreview != null) {
			stopCameraPreview(mPreview.getCamera());
		}

		super.mStarted = false;

		return 0;
	}

	// private Size getCameraBestPreviewSize(Camera camera){
	// final List<Size> prevSizes =
	// camera.getParameters().getSupportedPreviewSizes();
	//
	// Size minSize = null;
	// int minScore = Integer.MAX_VALUE;
	// for(Size size : prevSizes){
	// final int score = Math.abs(size.width - mWidth) + Math.abs(size.height -
	// mHeight);
	// if(minScore > score){
	// minScore = score;
	// minSize = size;
	// }
	// }
	// return minSize;
	// }

	private Size getCameraBestPreviewSize(Camera camera, int width, int height) {
		final List<Size> prevSizes = camera.getParameters()
				.getSupportedPreviewSizes();

		Size minSize = null;
		int minScore = Integer.MAX_VALUE;
		for (Size size : prevSizes) {
			final int score = Math.abs(size.width - width)
					+ Math.abs(size.height - height);
			if (minScore > score) {
				minScore = score;
				minSize = size;
			}
		}
		return minSize;
	}

	private synchronized void startCameraPreview(Camera camera) {
		MyLog.d(NgnProxyVideoProducer.TAG, "startCameraPreview()");

		if (!mStarted) {
			Log.w(TAG,
					"Someone requested to start camera preview but producer not ready ...delaying");
			return;
		}
		if (camera != null && mProducer != null) {

			boolean focusModeSupported = false;
			try {

				Camera.Parameters parameters = camera.getParameters();
				Size prevSize = getCameraBestPreviewSize(camera,mWidth,mHeight);
//				final Size prevSize = camera.getParameters().getSupportedPreviewSizes().get(1);
				if(prevSize == null){
					parameters.setPreviewSize(mWidth, mHeight);
					System.out.println("setPreviewSize("+mWidth+","+mHeight+")");
				}else {
					parameters.setPreviewSize(prevSize.width, prevSize.height);
					System.out.println("setPreviewSize("+prevSize.width+","+prevSize.height+")");
				}
				
				//parameters.setPictureSize(prevSize.width, prevSize.height);
				
				List<String> mFocusModes = parameters.getSupportedFocusModes();

				if (mFocusModes
						.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {

					focusModeSupported = true;
				}

				Log.i(TAG, "是否支持自动对焦:" + (focusModeSupported ? "是" : "否"));

				if (!GlobalSession.bSocketService && focusModeSupported) {

					parameters
							.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				}

				System.out.println("setPreviewSize(" + prevSize.width + ","
						+ prevSize.height + ")");

				camera.setParameters(parameters);

				if (prevSize != null
						&& super.isValid()
						&& (mWidth != prevSize.width || mHeight != prevSize.height)) {
					mFrameWidth = prevSize.width;
					mFrameHeight = prevSize.height;
				}

				// alert the framework that we cannot respect the negotiated
				// size
				mProducer.setActualCameraOutputSize(mFrameWidth, mFrameHeight);

				// allocate buffer
				MyLog.d(TAG, String.format("setPreviewSize [%d x %d ]",
						mFrameWidth, mFrameHeight));
				mVideoFrame = ByteBuffer.allocateDirect((mFrameWidth
						* mFrameHeight * 3) >> 1);

				// 16 : 9
				if (mWidth / 16 == mHeight / 9) {
					mFrameWidth = 1280;
					mFrameHeight = 720;
					parameters.setPreviewSize(mFrameWidth, mFrameHeight);
					camera.setParameters(parameters);

					// alert the framework that we cannot respect the negotiated
					// size
					mProducer.setActualCameraOutputSize(mFrameWidth,
							mFrameHeight);

					// The size is camera actual supported
					mVideoFrame = ByteBuffer.allocateDirect((mFrameWidth
							* mFrameHeight * 3) >> 1);
				}

			} catch (RuntimeException re) {
				if (re.getMessage().equals("setParameters failed")) {
					MyLog.d(TAG, "Not Supported 720P.Use last setted size");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				int terminalRotation = getTerminalRotation();

				Camera.Parameters parameters = camera.getParameters();

				if (terminalRotation == 0) {
					parameters.set("orientation", "landscape");
				} else {
					parameters.set("orientation", "portrait");
				}

				camera.setParameters(parameters);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// Camera Orientation
			int rotation = compensCamRotation(false);
			MyLog.d(TAG, String.format("setDisplayOrientation [%d] ", rotation));
			NgnCameraProducer.setDisplayOrientation(camera, rotation);

			// Callback Buffers
			if (NgnProxyVideoProducer.sAddCallbackBufferSupported) {
				for (int i = 0; i < NgnProxyVideoProducer.CALLABACK_BUFFERS_COUNT; i++) {
					if (i == 0 || (mVideoCallbackData == null)) {
						mVideoCallbackData = new byte[mVideoFrame.capacity()];
					}

					NgnCameraProducer.addCallbackBuffer(camera,
							new byte[mVideoFrame.capacity()]);

				}
			}

			try {
				NgnAVSession.mSendFrameThread = true;// 启动push视频帧线程

				MyLog.d(TAG, "startPreview() start... ");

				camera.startPreview();

				MyLog.d(TAG, "startPreview()  ok");

				if (!GlobalSession.bSocketService && focusModeSupported) {
					camera.cancelAutoFocus();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void startCameraPreviewNotBuffer(Camera camera) {
		MyLog.d(NgnProxyVideoProducer.TAG, "startCameraPreview()");

		if (!mStarted) {
			Log.w(TAG,
					"Someone requested to start camera preview but producer not ready ...delaying");
			return;
		}
		if (camera != null && mProducer != null) {

			boolean focusModeSupported = false;
			try {

				Camera.Parameters parameters = camera.getParameters();

				final Size prevSize = getCameraBestPreviewSize(camera, mWidth,
						mHeight);
				// final Size prevSize =
				// camera.getParameters().getSupportedPreviewSizes().get(1);
				parameters.setPreviewSize(prevSize.width, prevSize.height);
				// parameters.setPictureSize(prevSize.width, prevSize.height);

				List<String> mFocusModes = parameters.getSupportedFocusModes();

				if (mFocusModes
						.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {

					focusModeSupported = true;
				}

				Log.i(TAG, "是否支持自动对焦:" + (focusModeSupported ? "是" : "否"));

				if (!GlobalSession.bSocketService && focusModeSupported) {

					parameters
							.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
				}

				System.out.println("setPreviewSize(" + prevSize.width + ","
						+ prevSize.height + ")");

				camera.setParameters(parameters);

				if (prevSize != null
						&& super.isValid()
						&& (mWidth != prevSize.width || mHeight != prevSize.height)) {
					mFrameWidth = prevSize.width;
					mFrameHeight = prevSize.height;
				}

				mProducer.setActualCameraOutputSize(mFrameWidth, mFrameHeight);

				if (mWidth / 16 == mHeight / 9) {
					mFrameWidth = 1280;
					mFrameHeight = 720;
					parameters.setPreviewSize(mFrameWidth, mFrameHeight);
					camera.setParameters(parameters);
					mProducer.setActualCameraOutputSize(mFrameWidth,
							mFrameHeight);

				}

			} catch (RuntimeException re) {
				if (re.getMessage().equals("setParameters failed")) {
					MyLog.d(TAG, "Not Supported 720P.Use last setted size");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				int terminalRotation = getTerminalRotation();

				Camera.Parameters parameters = camera.getParameters();

				if (terminalRotation == 0) {
					parameters.set("orientation", "landscape");
				} else {
					parameters.set("orientation", "portrait");
				}

				camera.setParameters(parameters);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// Camera Orientation
			int rotation = compensCamRotation(false);
			MyLog.d(TAG, String.format("setDisplayOrientation [%d] ", rotation));
			NgnCameraProducer.setDisplayOrientation(camera, rotation);

			try {
				NgnAVSession.mSendFrameThread = true;// 启动push视频帧线程

				MyLog.d(TAG, "startPreview() start... ");

				camera.startPreview();

				MyLog.d(TAG, "startPreview()  ok");

				if (!GlobalSession.bSocketService && focusModeSupported) {
					camera.cancelAutoFocus();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void stopCameraPreview(Camera camera) {
		MyLog.d(NgnProxyVideoProducer.TAG, "stopCameraPreview()");
		if (camera != null) {
			try {
				mTvHandler = null;
				camera.stopPreview();
				if (isUseBypassEncoding == true)
					NgnAvcEncoder.getInstance().releaseEncoder();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	long start = 0;
	private PreviewCallback previewCallback = new PreviewCallback() {

		private long max_count = 0;

		public void onPreviewFrame(byte[] _data, Camera _camera) {
			if (mStarted) {
				if (mTvHandler != null && GlobalSession.bSocketService == true) { // 发送大终端模拟视频输出消息
					MyLog.d(NgnProxyVideoProducer.TAG,
							"onPreviewFrame() - mTvHandler != null && GlobalSession.bSocketService == true");
					// MyLog.d(TAG, "mFps = " + mFps);
					// MyLog.d(TAG, "mFrameWidth = " + mFrameWidth);
					// MyLog.d(TAG, "mFrameHeight = " + mFrameHeight);
					Message msg = mTvHandler.obtainMessage();
					Bundle b = new Bundle();
					// b.putInt("width", mWidth);
					// b.putInt("height", mHeight);
					b.putInt("width", mFrameWidth);
					b.putInt("height", mFrameHeight);
					b.putByteArray("data", _data);
					msg.setData(b);
					msg.sendToTarget();
				}

				// System.arraycopy(_data, 0, mFrameBuf, 0, _data.length);
				// videoPacketSend();

				if (NgnProxyVideoProducer.super.mValid && mVideoFrame != null) {
					if (_data != null) {
//						StringBuffer sb = new StringBuffer();
//						for(int i=0;i<20;i++){
//							sb.append(_data[i]);
//						}
//						MyLog.d(TAG, "Frame(20):"+sb.toString());
						if (_data.length > mVideoFrame.capacity()) {
							Log.e(TAG, String.format("Error ### %d != %d",
									_data.length, mVideoFrame.capacity()));
							return;
						}
					} else {
						_data = new byte[mVideoFrame.capacity()];
						MyLog.d(TAG, "PreviewCallback _data is null.");
					}
					if (isUseBypassEncoding) {
						// mVideoFrame.get(dataResult);
						System.arraycopy(_data, 0, dataResult, 0, _data.length);
					} else {
						mVideoFrame.rewind();
						mVideoFrame.put(_data);
					}
					// mProducer.push(mVideoFrame, mVideoFrame.capacity());
					mVideoFrame.rewind();
					// long end = new Date().getTime();

					// Log.d("","deltT = " + (end-start));
					// start = end;
				}
			}
			if (NgnProxyVideoProducer.sAddCallbackBufferSupported) {
				// do not use "_data" which could be null (e.g. on GSII)
				NgnCameraProducer
						.addCallbackBuffer(_camera, mVideoCallbackData);
			}
		}

	};
	
	public static boolean sign = false;

	/***
	 * MyProxyVideoProducerPreview
	 */
	@SuppressLint("NewApi")
	class MyProxyVideoProducerPreview extends TextureView implements
			TextureView.SurfaceTextureListener {
		private SurfaceTexture mSurfaceTexture;
		private final NgnProxyVideoProducer myProducer;
		private Camera mCamera = null;
		private final int MAGIC_TEXTURE_ID = 10;

		private Object mCameraLock = new Object();

		private long num = 0;
		private boolean stopSign = false;

		MyProxyVideoProducerPreview(NgnProxyVideoProducer _producer) {
			super(_producer.mContext);

			myProducer = _producer;
			MyLog.d(NgnProxyVideoProducer.TAG,
					"MyProxyVideoProducerPreview(NgnProxyVideoProducer _producer)");
			if (GlobalSession.bSocketService == true) {
				mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
			}
		}

		public Camera getCamera() {
			return mCamera;
		}

		public Thread sendFramePreview = new Thread("sendFrameThread") {
			@Override
			public void run() {
				MyLog.d(TAG, "视频发送线程启动...");
				sign = true;
				long period = (long) (1000 / mFps);
				byte[] h264Temp = new byte[mFrameWidth * mFrameHeight * 3 >> 1];

				ByteBuffer mEncodeVideoFrame = ByteBuffer
						.allocateDirect((mWidth * mHeight * 3) >> 1);
				while (true) {
					// MyLog.d(TAG,
					// "线程结束标记="+NgnAVSession.mSendFrameThread+"  ThreadId="+this.getId());
					if (!NgnAVSession.mSendFrameThread) {
						break;
					}
					long start = new Date().getTime();
					if(GlobalVar.mSendVideo && mProducer != null && mVideoFrame!= null){	
						 if(isUseBypassEncoding)
						  {
							 if(dataResult == null){
								 MyLog.e(TAG, "dataResult is null.");
								 break;
							 }
							 int ret = NgnAvcEncoder.getInstance().offerEncoder(dataResult,h264Temp);
							 Log.d(TAG,String.format("MediaCodec ret= %d",ret));
							 if(ret > 0)
							 {
								 if(ret != mEncodeVideoFrame.capacity())
									  mEncodeVideoFrame = ByteBuffer.allocateDirect(ret);
								  mEncodeVideoFrame.put(h264Temp,0,ret);
								  mEncodeVideoFrame.rewind();
								  mProducer.push(mEncodeVideoFrame, ret);
								 
								 // Log.d("",String.format("MediaCodec %d",dst_pos));
							  }
							 
						  }
						  else{
							  mProducer.push(mVideoFrame, mVideoFrame.capacity());		
						  }
						num ++;	
					}					
					long end = new Date().getTime();
					long time = end - start;

					// MyLog.d(TAG, "period: "+period);
					// MyLog.d(TAG, "time: "+time);

					if (period > time) {
						try {
							Thread.sleep(period - time);
							// MyLog.d(TAG,"Thread sleep :"+(period - time));

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// else {
					// MyLog.d(TAG,"period < time");
					// }
					// MyLog.d(TAG,
					// num+"|运行时间:"+(end-start)+"|start:"+start+"|end:"+end);
				}
				sign = false;
				MyLog.d(TAG, "视频发送线程结束");
			}
		};

		// use only for socket service mode
		public void servicemode_Start() {
			MyLog.d(TAG, "servicemode_Start");
			if (GlobalSession.bSocketService == true) {
//				Object[] objs = new Object[5];
//				objs[0] = mCamera;
//				objs[1] = myProducer;
//				objs[2] = mSurfaceTexture;
//				objs[3] = sendFramePreview;
//				
//				MyCameraOpenTask mct = new MyCameraOpenTask();
//				mct.execute(objs);
				
				try {
					MyLog.d(TAG, "准备释放摄像头...");
					servicemode_Stop();

					MyLog.d(TAG, "准备开启摄像头...");
					mCamera = NgnCameraProducer.openCamera(myProducer.mFps,
							myProducer.mWidth, myProducer.mHeight,
							mSurfaceTexture, myProducer.previewCallback);

					MyLog.d(TAG, "摄像头启动完毕   mCamera=" + mCamera);

					if (mCamera != null) {
						MyLog.d(TAG, "准备开启preview...");
						myProducer.startCameraPreview(mCamera);
						MyLog.d(TAG, "preview 启动完成");
						if (!sign) {
							sendFramePreview.start();
						}
					}
					MyLog.d(TAG, "camera started");
				} catch (Exception exception) {
					Log.e(TAG, exception.toString());
				}
			}
		}

		//
		// use only for socket service mode
		public void servicemode_Stop() {
			MyLog.d(TAG, "servicemode_Stop start...");
//			if(!myCameraLoacked){
//				NgnCameraProducer.releaseCamera(mCamera);
//				MyLog.d(TAG, "camera released.");
//				return;
//			}
//			final NgnTimer clockTimer = new NgnTimer();
//			clockTimer.schedule(new TimerTask() {
//				
//				@Override
//				public void run() {
//					if(!myCameraLoacked){
//						NgnCameraProducer.releaseCamera(mCamera);
//						MyLog.d(TAG, "camera released.");
//						this.cancel();
//						clockTimer.cancel();
//						clockTimer.purge();
//						MyLog.d(TAG, "Timer canceled.");
//					}else {
//						MyLog.d(TAG, "Camera is locaked.wait for next turn.");
//					}
//				}
//			}, 0,2000);
			if (GlobalSession.bSocketService == true) {
				NgnCameraProducer.releaseCamera(mCamera);
				mCamera = null;
				MyLog.d(TAG, "camera released");
			}
		}

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1,
				int arg2) {
			MyLog.d(NgnProxyVideoProducer.TAG, "onSurfaceTextureAvailable()");

//			Object[] objs = new Object[5];
//			objs[0] = mCamera;
//			objs[1] = myProducer;
//			objs[2] = arg0;
//			objs[3] = sendFramePreview;
//			
//			MyCameraOpenTask mct = new MyCameraOpenTask();
//			mct.execute(objs);
			try {

				// normal_Stop();
				if (mCamera == null) { // 第一次打开摄像头并设置
					mCamera = NgnCameraProducer.openCamera(myProducer.mFps,
							myProducer.mWidth, myProducer.mHeight, arg0,
							myProducer.previewCallback);
					MyLog.d(TAG, "First time  to open Camera");

					MyLog.d(TAG, "prepare to startCameraPreview..");

					if (mCamera != null) {
						myProducer.startCameraPreview(mCamera);
						if (!sign) {
							sendFramePreview.start();
							MyLog.d(TAG, "sendFramePreview start ok");
						}

					}

				} else { // 第二次重置摄像头，不打开
					NgnCameraProducer.releaseCameraPreview(mCamera);
					mCamera = NgnCameraProducer.setCameraParameters(
							myProducer.mFps, myProducer.mWidth,
							myProducer.mHeight, arg0,
							myProducer.previewCallback);

					MyLog.d(TAG, "Not  First time  to open Camera");

					if (mCamera != null) {
						myProducer.startCameraPreviewNotBuffer(mCamera);
					}

				}

				MyLog.d(TAG, "camera started");
			} catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
			MyLog.d(TAG, "surfaceDestroyed()");
			try {
				MyLog.d(TAG, "mcamera :" + mCamera);
				if (mCamera != null) {
					// 解决在视频通话中，按下回退按钮或者切换界面时，图像静止不动问题。
//					MyLog.d(TAG, "bSocketService :"
//							+ GlobalSession.bSocketService);
//					if(GlobalSession.bSocketService
//							||!GlobalVar.bBackOrSwitch){
//						if(!myCameraLoacked){
//							NgnCameraProducer.releaseCamera(mCamera);
//							MyLog.d(TAG, "camera released.");
//							return true;
//						}
//						final NgnTimer clockTimer = new NgnTimer();
//						clockTimer.schedule(new TimerTask() {
//							
//							@Override
//							public void run() {
//								if(!myCameraLoacked){
//									NgnCameraProducer.releaseCamera(mCamera);
//									MyLog.d(TAG, "camera released.");
//									this.cancel();
//									clockTimer.cancel();
//									clockTimer.purge();
//								}
//							}
//						}, 0,2000);
//						return true;
//					}
					
					if (GlobalSession.bSocketService == false) {
						if (GlobalVar.bBackOrSwitch == false) {
							stopSign = true;
							NgnCameraProducer.releaseCamera(mCamera);
							mCamera = null;
							MyLog.d(TAG, "camera released");
						}
					} else {
						NgnCameraProducer.releaseCamera(mCamera);
						mCamera = null;
						MyLog.d(TAG, "camera released");
					}
				}
				MyLog.d(TAG, "surfaceDestroyed() exec over");
			} catch (Exception exception) {
				Log.e(TAG, exception.toString());
			}
			return false;
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1,
				int arg2) {
			MyLog.d(NgnProxyVideoProducer.TAG, "onSurfaceTextureSizeChanged()");
			boolean focusModeSupported = false;
			if (mCamera != null) {
				try {
					List<String> mFocusModes = mCamera.getParameters()
							.getSupportedFocusModes();
					if (mFocusModes
							.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
						focusModeSupported = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				if (!GlobalSession.bSocketService && focusModeSupported) {
					mCamera.autoFocus(new AutoFocusCallback() {

						@Override
						public void onAutoFocus(boolean arg0, Camera arg1) {
							if (arg0) {
								myProducer.startCameraPreview(mCamera);
							}
						}
					});
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
			// MyLog.d(NgnProxyVideoProducer.TAG, "onSurfaceTextureUpdated()");

		}

		//
		// use only for normal case
		public void normal_Stop() {
			MyLog.d(TAG, "normal_Stop()");
			if (mCamera != null) {
				NgnCameraProducer.releaseCamera(mCamera);
				mCamera = null;
			}
		}
		
		private boolean myCameraLoacked = false;
		
		class MyCameraOpenTask extends AsyncTask<Object, Integer, Camera>{
			@Override
			protected Camera doInBackground(Object... params) {
				MyLog.d(TAG, "doInBackground()");
				
				myCameraLoacked = true;
				
				Camera mCamera = (Camera) params[0];
				NgnProxyVideoProducer myProducer = (NgnProxyVideoProducer) params[1];
				MyLog.d(TAG, "myProducer = "+myProducer);
				SurfaceTexture arg0 = (SurfaceTexture) params[2];
				Thread sendFramePreview = (Thread) params[3];
				try {
					// normal_Stop();
					if (mCamera == null) { // 第一次打开摄像头并设置
						mCamera = NgnCameraProducer.openCamera(myProducer.mFps,
								myProducer.mWidth, myProducer.mHeight, arg0,
								myProducer.previewCallback);
						MyLog.d(TAG, "First time  to open Camera");

						MyLog.d(TAG, "prepare to startCameraPreview..");

						if (mCamera != null) {
							myProducer.startCameraPreview(mCamera);
							if (!sign) {
								sendFramePreview.start();
								MyLog.d(TAG, "sendFramePreview start ok");
							}

						}

					} else { // 第二次重置摄像头，不打开
//						NgnCameraProducer.releaseCameraPreview(mCamera);
						mCamera.stopPreview();
						mCamera = NgnCameraProducer.setCameraParameters(
								myProducer.mFps, myProducer.mWidth,
								myProducer.mHeight, arg0,
								myProducer.previewCallback);

						MyLog.d(TAG, "Not  First time  to open Camera");

						if (mCamera != null) {
							myProducer.startCameraPreviewNotBuffer(mCamera);
						}

					}

					myCameraLoacked = false;
					
					MyLog.d(TAG, "camera started");
				} catch (Exception exception) {
					Log.e(TAG, exception.toString());
				}
				return mCamera;
			}
			
			@Override
			protected void onPostExecute(Camera result) {
				super.onPostExecute(result);
				MyLog.d(TAG, "onPostExecute("+result+")");
				mCamera = result;
			}
		}
		
		class MyCameraCloseTask extends AsyncTask<Camera, Integer, Boolean>{
			@Override
			protected Boolean doInBackground(Camera... params) {
				MyLog.d(TAG, "doInBackground()");
				if(!myCameraLoacked){
					Camera mCamera = (Camera) params[0];
					try {
						NgnCameraProducer.releaseCamera(mCamera);
						MyLog.d(TAG, "camera released.");
					} catch (Exception exception) {
						exception.printStackTrace();
						return false;
					}
				}
				return true;
			}
		}
	}

	/**
	 * MyProxyVideoProducerCallback
	 */
	static class MyProxyVideoProducerCallback extends
			ProxyVideoProducerCallback {
		final NgnProxyVideoProducer myProducer;

		public MyProxyVideoProducerCallback(NgnProxyVideoProducer producer) {
			super();
			myProducer = producer;
		}

		@Override
		public int prepare(int width, int height, int fps) {
			// MyLog.d(NgnProxyVideoProducer.TAG,
			// "MyProxyVideoProducerCallback prepare() - width = " + width);
			// MyLog.d(NgnProxyVideoProducer.TAG,
			// "MyProxyVideoProducerCallback prepare() - height = " + height);
			// MyLog.d(NgnProxyVideoProducer.TAG,
			// "MyProxyVideoProducerCallback prepare() - fps = " + fps);
			return myProducer.prepareCallback(width, height, fps);
		}

		@Override
		public int start() {
			return myProducer.startCallback();
		}

		@Override
		public int pause() {
			return myProducer.pauseCallback();
		}

		@Override
		public int stop() {
			return myProducer.stopCallback();
		}
	}

	public static void setTvHandler(Handler tvHandler) {
		mTvHandler = tvHandler;
	}

	/**
	 * 只有一个摄像头 获取摄像头的方位（前置：1 后置：0） CAMERA_FACING_BACK = 0 CAMERA_FACING_FRONT =
	 * 1
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public static int getCameraFacing() { // for test
		// Camera.CameraInfo info = new Camera.CameraInfo();
		// int cameraId = 0;
		// Camera.getCameraInfo(cameraId, info);
		//
		// CameraInfo info2 =
		// CameraHolder.instance().getCameraInfo()[mCameraId];
		// if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
		// //stopFaceDetection();
		// return true;
		// }
		// else {
		// return false;
		// }

		// int cameraCount = 0;
		// Camera cam = null;
		//
		// Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		// cameraCount = Camera.getNumberOfCameras(); //get cameras number
		//
		// for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
		// Camera.getCameraInfo(camIdx, cameraInfo); //get camerainfo
		// if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
		// try {
		// cam = Camera.open(camIdx);
		// } catch (RuntimeException e) {
		// e.printStackTrace();
		// }
		// }
		// }

		try {
			// int numOfCameras = NgnCameraProducer.getNumberOfCameras();
			int numOfCameras = Camera.getNumberOfCameras();
			MyLog.d(NgnProxyVideoProducer.TAG, "numOfCameras = " + numOfCameras);
			if (numOfCameras > 1) {
				return -1;
			}

			Class<?> clsCameraInfo = null;

			final Class<?>[] classes = android.hardware.Camera.class
					.getDeclaredClasses();
			for (Class<?> c : classes) {
				if (c.getSimpleName().equals("CameraInfo")) {
					clsCameraInfo = c;
					break;
				}
			}

			if(clsCameraInfo == null){
				return 0;
			}
			int cameraId = 0;

			final Object info = clsCameraInfo.getConstructor((Class[]) null)
					.newInstance((Object[]) null);
			Method getCamInfoMthd = android.hardware.Camera.class
					.getDeclaredMethod("getCameraInfo", int.class,
							clsCameraInfo);
			getCamInfoMthd.invoke(null, cameraId, info);

			final Field fieldFacing = clsCameraInfo.getField("facing");
			// final Field fieldOrient = clsCameraInfo.getField("orientation");
			// final Field fieldFrontFacingConst =
			// clsCameraInfo.getField("CAMERA_FACING_FRONT");

			return fieldFacing.getInt(info);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public boolean ismVideoSendPause() {
		return mVideoSendPause;
	}

	public void setmVideoSendPause(boolean mVideoSendPause) {
		this.mVideoSendPause = mVideoSendPause;
	}
}
