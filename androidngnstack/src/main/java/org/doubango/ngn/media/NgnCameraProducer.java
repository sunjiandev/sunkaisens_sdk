/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
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
*/
package org.doubango.ngn.media;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.sdk.NgnSKDroid;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;

public class NgnCameraProducer {
	private static final String TAG = NgnCameraProducer.class.getCanonicalName();
	private static Camera instance;
//	private static boolean useFrontFacingCamera;
	public static boolean useFrontFacingCamera;
	
	// Default values
	private static int fps = 15;
	private static int width = 176;
	private static int height = 144;
	private static SurfaceTexture holder = null;
	private static PreviewCallback callback = null;
	
	private static final int MIN_SDKVERSION_addCallbackBuffer = 7;
	private static final int MIN_SDKVERSION_setPreviewCallbackWithBuffer = 7;
	private static final int MIN_SDKVERSION_setDisplayOrientation = 8;
	//private static final int MIN_SDKVERSION_getSupportedPreviewSizes = 5;
	
	private static Method addCallbackBufferMethod = null;
	private static Method setDisplayOrientationMethod = null;
	private static Method setPreviewCallbackWithBufferMethod = null;
	
	static{
		NgnCameraProducer.useFrontFacingCamera = NgnEngine
				.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
						NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);
	}
	
	static{
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer.MIN_SDKVERSION_addCallbackBuffer){
			// According to http://developer.android.com/reference/android/hardware/Camera.html both addCallbackBuffer and setPreviewCallbackWithBuffer
			// are only available starting API level 8. But it's not true as these functions exist in API level 7 but are hidden.
			try {
				NgnCameraProducer.addCallbackBufferMethod = Camera.class.getMethod("addCallbackBuffer", byte[].class);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			} 
		}
		
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer.MIN_SDKVERSION_setPreviewCallbackWithBuffer){
			try {
				NgnCameraProducer.setPreviewCallbackWithBufferMethod = Camera.class.getMethod(
					"setPreviewCallbackWithBuffer", PreviewCallback.class);
			}  catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
		}
				
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer.MIN_SDKVERSION_setDisplayOrientation){
			try {
				NgnCameraProducer.setDisplayOrientationMethod = Camera.class.getMethod("setDisplayOrientation", int.class);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			} 
		}
	}
	
	public static Camera getCamera(){
		return NgnCameraProducer.instance;
	}
	
	public static void setCamera(Camera mCamera){
		NgnCameraProducer.instance = mCamera;
	}
	
	@SuppressLint("NewApi") 
	public static Camera openCamera(int fps, int width, int height, SurfaceTexture mSurfaceTexture, PreviewCallback callback){
		MyLog.d(NgnCameraProducer.TAG, "openCamera()");
		if(NgnCameraProducer.instance == null){
			try{
				int numOfCameras = NgnCameraProducer.getNumberOfCameras();
				MyLog.d(NgnCameraProducer.TAG, "numOfCameras = " + numOfCameras);
				if (numOfCameras > 1) { //两个摄像头
					if (NgnCameraProducer.useFrontFacingCamera) { //前置摄像头
						MyLog.d(NgnCameraProducer.TAG, "NgnCameraProducer.openFrontFacingCamera()");
						NgnCameraProducer.instance = NgnCameraProducer.openFrontFacingCamera();
					}else { //后置摄像头
						MyLog.d(NgnCameraProducer.TAG, "Camera.open()");
						NgnCameraProducer.instance = Camera.open();
					}
				}else { //单个前置/后置摄像头
					MyLog.d(NgnCameraProducer.TAG, "Camera.open()");
					NgnCameraProducer.instance = Camera.open(0);
				}
				
				NgnCameraProducer.fps = fps;
				NgnCameraProducer.width = width;
				NgnCameraProducer.height = height;
				NgnCameraProducer.holder = mSurfaceTexture;
				NgnCameraProducer.callback = callback;
				
				Camera.Parameters parameters = NgnCameraProducer.instance.getParameters();
				
				/*
				 * http://developer.android.com/reference/android/graphics/ImageFormat.html#NV21
				 * YCrCb format used for images, which uses the NV21 encoding format. 
				 * This is the default format for camera preview images, when not otherwise set with setPreviewFormat(int). 
				 */
				parameters.setPreviewFormat(ImageFormat.NV21);
				parameters.setPreviewFrameRate(NgnCameraProducer.fps);
				NgnCameraProducer.instance.setParameters(parameters);
				
				try{
					Size picSize = getCameraBestPictureSize(instance, NgnCameraProducer.width, NgnCameraProducer.height);
					if(picSize == null){
						parameters.setPictureSize(
								NgnCameraProducer.width, NgnCameraProducer.height);
						System.out.println("Picture  Width="+NgnCameraProducer.width+""
								+ "  Height="+NgnCameraProducer.height);
					}else {
						parameters.setPictureSize(picSize.width , picSize.height);
						System.out.println("Picture  Width="+picSize.width+"  Height="+picSize.height);
					}
					
					NgnCameraProducer.instance.setParameters(parameters);
				}
				catch(Exception e){
					// FFMpeg converter will resize the video stream
					MyLog.d(NgnCameraProducer.TAG, e.toString());
				}
				
				//NgnCameraProducer.instance.setPreviewDisplay(NgnCameraProducer.holder);
				NgnCameraProducer.instance.setPreviewTexture(NgnCameraProducer.holder);
				NgnCameraProducer.initializeCallbacks(NgnCameraProducer.callback);
				
				GlobalVar.mCameraIsUsed = true;
				
//				List<int[]> supportedPreviewFps=parameters.getSupportedPreviewFpsRange();
//		        Iterator<int[]> supportedPreviewFpsIterator=supportedPreviewFps.iterator();
//		        while(supportedPreviewFpsIterator.hasNext()){
//		            int[] tmpRate=supportedPreviewFpsIterator.next();
//		            StringBuffer sb=new StringBuffer();
//		            sb.append("supportedPreviewRate: ");
//		            for(int i=tmpRate.length,j=0;j<i;j++){
//		                sb.append(tmpRate[j]+", ");
//		            }
//		            Log.v("CameraTest",sb.toString());
//		        }
//			        
//				List<Size> supportedPreviewSizes=parameters.getSupportedPreviewSizes();
//		        Iterator<Size> supportedPreviewSizesIterator=supportedPreviewSizes.iterator();
//		        while(supportedPreviewSizesIterator.hasNext()){
//		            Size tmpSize=supportedPreviewSizesIterator.next();
//		            Log.v("CameraTest","supportedPreviewSize.width = "+tmpSize.width+" ,supportedPreviewSize.height = "+tmpSize.height);
//		        }
				
			}
			catch(Exception e){ //摄像头不可用（正在占用或不存在）
				NgnCameraProducer.releaseCamera();
				e.printStackTrace(); //java.lang.RuntimeException: Fail to connect to camera service
				Log.e(NgnCameraProducer.TAG, "当前摄像头不可用（正在占用或不存在）：" + (NgnCameraProducer.useFrontFacingCamera ? "前置摄像头" : "后置摄像头"));
				GlobalVar.mCameraIsUsed = false;
//    			if (GlobalSession.bSocketService) {
//    				final NgnAVSession mySession = GlobalSession.avSession;
//            		broadcastInviteEvent(new NgnInviteEventArgs(mySession.getId(),NgnInviteEventTypes.REMOTE_REFUSE, mySession.getMediaType(), "cameraerror"));
//    			}
			}
		}
		return NgnCameraProducer.instance; //摄像头不可用则返回null
	}
	
	public static Camera setCameraParameters(int fps, int width, int height, SurfaceTexture mSurfaceTexture, PreviewCallback callback){
		MyLog.d(NgnCameraProducer.TAG, "openCamera()");
		if(NgnCameraProducer.instance != null){
			try{
			
				NgnCameraProducer.fps = fps;
				NgnCameraProducer.width = width;
				NgnCameraProducer.height = height;
				NgnCameraProducer.holder = mSurfaceTexture;
				NgnCameraProducer.callback = callback;
				
				Camera.Parameters parameters = NgnCameraProducer.instance.getParameters();
				
				parameters.setPreviewFormat(ImageFormat.NV21);
				parameters.setPreviewFrameRate(NgnCameraProducer.fps);
				NgnCameraProducer.instance.setParameters(parameters);
				
				try{
					Size picSize = getCameraBestPictureSize(instance, NgnCameraProducer.width, NgnCameraProducer.height);					
					parameters.setPictureSize(picSize.width , picSize.height);
					System.out.println("Picture  Width="+picSize.width+"  Height="+picSize.height);
					NgnCameraProducer.instance.setParameters(parameters);
				}
				catch(Exception e){
				
					MyLog.d(NgnCameraProducer.TAG, e.toString());
				}
				
			
				NgnCameraProducer.instance.setPreviewTexture(NgnCameraProducer.holder);
				NgnCameraProducer.initializeCallbacks(NgnCameraProducer.callback);
				
				GlobalVar.mCameraIsUsed = true;
				
			}
			catch(Exception e){ //摄像头不可用（正在占用或不存在）
				NgnCameraProducer.releaseCamera();
				Log.e(NgnCameraProducer.TAG, e.toString()); //java.lang.RuntimeException: Fail to connect to camera service
				Log.e(NgnCameraProducer.TAG, "当前摄像头不可用（正在占用或不存在）：" + (NgnCameraProducer.useFrontFacingCamera ? "前置摄像头" : "后置摄像头"));
				GlobalVar.mCameraIsUsed = false;

			}
		}
		return NgnCameraProducer.instance; //摄像头不可用则返回null
	}
	
	
	
//	public static void releaseCamera(Camera camera){
//		if(camera != null){
//			MyLog.d("CameraPreview","releaseCamera()----0");
//			camera.stopPreview();
//			MyLog.d("CameraPreview","releaseCamera()----1");
//			NgnCameraProducer.deInitializeCallbacks(camera);
//			MyLog.d("CameraPreview","releaseCamera()----2");
//			camera.release();
//			MyLog.d("CameraPreview","releaseCamera()----3");
//			if(camera == NgnCameraProducer.instance){
//				MyLog.d("CameraPreview","releaseCamera()----4");
//				NgnCameraProducer.instance = null;
//			}
//			MyLog.d("CameraPreview","releaseCamera()----OK");
//		}
//	}
	public static void releaseCamera(Camera camera){
		MyLog.d(NgnProxyVideoProducer.TAG, "releaseCamera()");
		if(camera==null) return;
		if(NgnCameraProducer.instance != null){
			NgnCameraProducer.instance.stopPreview();
			NgnCameraProducer.deInitializeCallbacks();
			NgnCameraProducer.instance.release();
			NgnCameraProducer.instance = null;
		}
		GlobalVar.mCameraIsUsed = false;
	}
	
	public static void releaseCameraPreview(Camera camera){
		if(camera==null) return;
		if(NgnCameraProducer.instance != null){
			NgnCameraProducer.instance.stopPreview();
			NgnCameraProducer.deInitializeCallbacks();
		
		}
	
	}
	
	
	public static void releaseCamera(){
		if(NgnCameraProducer.instance != null){
			NgnCameraProducer.instance.stopPreview();
			NgnCameraProducer.deInitializeCallbacks();
			NgnCameraProducer.instance.release();
			NgnCameraProducer.instance = null;
		}
		GlobalVar.mCameraIsUsed = false;
	}
	
	public static void setDisplayOrientation(int degrees){
		MyLog.d(NgnCameraProducer.TAG, "setDisplayOrientation()");
		if(NgnCameraProducer.instance != null && NgnCameraProducer.setDisplayOrientationMethod != null){
			try {
				NgnCameraProducer.setDisplayOrientationMethod.invoke(NgnCameraProducer.instance, degrees);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void setDisplayOrientation(Camera camera, int degrees){
		MyLog.d(NgnCameraProducer.TAG, "setDisplayOrientation()");
		if(camera != null && NgnCameraProducer.setDisplayOrientationMethod != null){
			try {
				NgnCameraProducer.setDisplayOrientationMethod.invoke(camera, degrees);
			} catch (Exception e) {
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void addCallbackBuffer(Camera camera, byte[] buffer) {
		try {
			NgnCameraProducer.addCallbackBufferMethod.invoke(camera, buffer);
		} catch (Exception e) {
			Log.e(NgnCameraProducer.TAG, e.toString());
		}
	}
	
	public static void addCallbackBuffer(byte[] buffer) {
		try {
			NgnCameraProducer.addCallbackBufferMethod.invoke(NgnCameraProducer.instance, buffer);
		} catch (Exception e) {
			Log.e(NgnCameraProducer.TAG, e.toString());
		}
	}

	public static boolean isAddCallbackBufferSupported(){
		return NgnCameraProducer.addCallbackBufferMethod != null;
	}
	
	public static boolean isFrontFacingCameraEnabled(){
		return NgnCameraProducer.useFrontFacingCamera;
	}
	
	public static void useRearCamera(){
		NgnCameraProducer.useFrontFacingCamera = false;
	}
	
	public static void useFrontFacingCamera(){
		NgnCameraProducer.useFrontFacingCamera = true;
	}
	
	public static Camera toggleCamera(){
		MyLog.d(NgnCameraProducer.TAG, "toggleCamera()");
		if(NgnCameraProducer.instance != null){
			NgnCameraProducer.useFrontFacingCamera = !NgnCameraProducer.useFrontFacingCamera;
			NgnCameraProducer.releaseCamera();
			NgnCameraProducer.openCamera(NgnCameraProducer.fps, 
					NgnCameraProducer.width, 
					NgnCameraProducer.height,
					NgnCameraProducer.holder, 
					NgnCameraProducer.callback);
		}
		return NgnCameraProducer.instance;
	}
	
	private static void initializeCallbacks(PreviewCallback callback){
		initializeCallbacks(callback, NgnCameraProducer.instance);
	}
	
	private static void initializeCallbacks(PreviewCallback callback, Camera camera){
		if(camera != null){
			if(NgnCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					NgnCameraProducer.setPreviewCallbackWithBufferMethod.invoke(camera, callback);
				} catch (Exception e) {
					Log.e(NgnCameraProducer.TAG, e.toString());
				}
			}else{
				camera.setPreviewCallback(callback);
			}
		}
	}
	
	private static void deInitializeCallbacks(){
		deInitializeCallbacks(NgnCameraProducer.instance);
	}
	
	private static void deInitializeCallbacks(Camera camera){
		if(camera!= null){
			if(NgnCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					NgnCameraProducer.setPreviewCallbackWithBufferMethod.invoke(camera, new Object[]{ null });
				} catch (Exception e) {
					Log.e(NgnCameraProducer.TAG, e.toString());
				}
			}
//			else{ //解决“java.lang.RuntimeException: Method called after release()”这个问题，需要执行下面这条语句。
				camera.setPreviewCallback(null);
//			}
		}
	}
	
	public static int getNumberOfCameras() {
		MyLog.d(NgnCameraProducer.TAG, "getNumberOfCameras()");
		// 1. Android 2.3 or later
		if (NgnApplication.getSDKVersion() >= 9) {
			try {
				Method getNumberOfCamerasMethod = Camera.class.getDeclaredMethod("getNumberOfCameras");
				if (getNumberOfCamerasMethod != null) {
					return (Integer) getNumberOfCamerasMethod.invoke(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 1;
	}

	private static Camera openFrontFacingCamera() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		MyLog.d(NgnCameraProducer.TAG, "openFrontFacingCamera()");
		Camera camera = null;
		
		// 1. Android 2.3 or later
		if(NgnApplication.getSDKVersion() >= 9){
			try {
				Method getNumberOfCamerasMethod = Camera.class.getDeclaredMethod("getNumberOfCameras");
				if(getNumberOfCamerasMethod != null){
					Integer numberOfCameras = (Integer)getNumberOfCamerasMethod.invoke(null);
					MyLog.d(NgnCameraProducer.TAG, "numberOfCameras = " + numberOfCameras);
					if(numberOfCameras > 1){
						Method openMethod = Camera.class.getDeclaredMethod("open", int.class);
						if((camera = (Camera)openMethod.invoke(null, (numberOfCameras - 1))) != null){
							return camera;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//2. From mapper
		if((camera = FrontFacingCameraMapper.getPreferredCamera()) != null){
			return camera;
		}
		
		//3. Use switcher
		if(FrontFacingCameraSwitcher.getSwitcher() != null){
			camera = Camera.open();
			FrontFacingCameraSwitcher.getSwitcher().invoke(camera, (int)1);
			return camera;
		}
		
		//4. Use parameters
		camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		parameters.set("camera-id", 2);
		camera.setParameters(parameters);
		MyLog.d(NgnCameraProducer.TAG, "openFrontFacingCamera() - parameters = " + parameters.toString());
		return camera;
	}
	
	 private static Size getCameraBestPictureSize(Camera camera,int mWidth,int mHeight){
	    	final List<Size> prevSizes = camera.getParameters().getSupportedPictureSizes();
	    	
	    	Size minSize = null;
	    	int minScore = Integer.MAX_VALUE;
	    	for(Size size : prevSizes){
	    		final int score = Math.abs(size.width - mWidth) + Math.abs(size.height - mHeight);
	    		if(minScore > score){
	    			minScore = score;
	    			minSize = size;
	    		}
	    	}
	    	return minSize;
	    }
	
	/***
	 * FrontFacingCameraSwitcher
	 * @author Mamadou Diop
	 *
	 */
	static class FrontFacingCameraSwitcher
	{
		private static Method DualCameraSwitchMethod;
		
		static{
			try{
				FrontFacingCameraSwitcher.DualCameraSwitchMethod = Class.forName("android.hardware.Camera").getMethod("DualCameraSwitch",int.class);
			}
			catch(Exception e){
				MyLog.d(NgnCameraProducer.TAG, e.toString());
			}
		}
		
		static Method getSwitcher(){
			return FrontFacingCameraSwitcher.DualCameraSwitchMethod;
		}
	}
	
	static class FrontFacingCameraMapper
	{
		private static int preferredIndex = -1;
		
		static FrontFacingCameraMapper Map[] = {
			new FrontFacingCameraMapper("android.hardware.HtcFrontFacingCamera", "getCamera"),
			// Sprint: HTC EVO 4G and Samsung Epic 4G
			// DO not forget to change the manifest if you are using OS 1.6 and later
			new FrontFacingCameraMapper("com.sprint.hardware.twinCamDevice.FrontFacingCamera", "getFrontFacingCamera"),
			// Huawei U8230
            new FrontFacingCameraMapper("android.hardware.CameraSlave", "open"),
			// Default: Used for test reflection
			// new FrontFacingCameraMapper("android.hardware.Camera", "open"),
		};
		
		static{
			int index = 0;
			for(FrontFacingCameraMapper ffc: FrontFacingCameraMapper.Map){
				try{
					Class.forName(ffc.className).getDeclaredMethod(ffc.methodName);
					FrontFacingCameraMapper.preferredIndex = index;
					break;
				}
				catch(Exception e){
					MyLog.d(NgnCameraProducer.TAG, e.toString());
				}
				
				++index;
			}
		}
		
		private final String className;
		private final String methodName;
		
		FrontFacingCameraMapper(String className, String methodName){
			this.className = className;
			this.methodName = methodName;
		}
		
		static Camera getPreferredCamera(){
			if(FrontFacingCameraMapper.preferredIndex == -1){
				return null;
			}
			
			try{				
				Method method = Class.forName(FrontFacingCameraMapper.Map[FrontFacingCameraMapper.preferredIndex].className)
				.getDeclaredMethod(FrontFacingCameraMapper.Map[FrontFacingCameraMapper.preferredIndex].methodName);
				return (Camera)method.invoke(null);
			}
			catch(Exception e){
				Log.e(NgnCameraProducer.TAG, e.toString());
			}
			return null;
		}
	}

	private static void broadcastInviteEvent(NgnInviteEventArgs args) {
		final Intent intent = new Intent(NgnInviteEventArgs.ACTION_INVITE_EVENT);
		intent.putExtra(NgnInviteEventArgs.EXTRA_EMBEDDED, args);
		intent.putExtra(NgnInviteEventArgs.EXTRA_SIPCODE, 0);
//		NgnApplication.getContext().sendBroadcast(intent);
		if(GlobalVar.orderedbroadcastSign){
			NgnSKDroid.getInstence().getGlobleContext().sendOrderedBroadcast(intent, null);
		}else {
			NgnSKDroid.getInstence().getGlobleContext().sendBroadcast(intent);
		}
	}
}
