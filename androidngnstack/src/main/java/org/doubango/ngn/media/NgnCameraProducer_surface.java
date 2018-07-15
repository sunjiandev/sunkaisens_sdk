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

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;

public class NgnCameraProducer_surface {
	private static final String TAG = NgnCameraProducer_surface.class.getCanonicalName();
	private static Camera instance;
//	private static boolean useFrontFacingCamera;
	public static boolean useFrontFacingCamera;
	
	// Default values
	private static int fps = 15;
	private static int width = 176;
	private static int height = 144;
	private static SurfaceHolder holder = null;
	private static PreviewCallback callback = null;
	
	private static final int MIN_SDKVERSION_addCallbackBuffer = 7;
	private static final int MIN_SDKVERSION_setPreviewCallbackWithBuffer = 7;
	private static final int MIN_SDKVERSION_setDisplayOrientation = 8;
	//private static final int MIN_SDKVERSION_getSupportedPreviewSizes = 5;
	
	private static Method addCallbackBufferMethod = null;
	private static Method setDisplayOrientationMethod = null;
	private static Method setPreviewCallbackWithBufferMethod = null;
	
	static{
		NgnCameraProducer_surface.useFrontFacingCamera = NgnEngine
				.getInstance().getConfigurationService().getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
						NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);
	}
	
	static{
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer_surface.MIN_SDKVERSION_addCallbackBuffer){
			// According to http://developer.android.com/reference/android/hardware/Camera.html both addCallbackBuffer and setPreviewCallbackWithBuffer
			// are only available starting API level 8. But it's not true as these functions exist in API level 7 but are hidden.
			try {
				NgnCameraProducer_surface.addCallbackBufferMethod = Camera.class.getMethod("addCallbackBuffer", byte[].class);
			} catch (Exception e) {
				Log.e(NgnCameraProducer_surface.TAG, e.toString());
			} 
		}
		
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer_surface.MIN_SDKVERSION_setPreviewCallbackWithBuffer){
			try {
				NgnCameraProducer_surface.setPreviewCallbackWithBufferMethod = Camera.class.getMethod(
					"setPreviewCallbackWithBuffer", PreviewCallback.class);
			}  catch (Exception e) {
				Log.e(NgnCameraProducer_surface.TAG, e.toString());
			}
		}
				
		if(NgnApplication.getSDKVersion() >= NgnCameraProducer_surface.MIN_SDKVERSION_setDisplayOrientation){
			try {
				NgnCameraProducer_surface.setDisplayOrientationMethod = Camera.class.getMethod("setDisplayOrientation", int.class);
			} catch (Exception e) {
				Log.e(NgnCameraProducer_surface.TAG, e.toString());
			} 
		}
	}
	
	public static Camera getCamera(){
		return NgnCameraProducer_surface.instance;
	}
	
	public static Camera openCamera(int fps, int width, int height, SurfaceHolder holder, PreviewCallback callback){
		if(NgnCameraProducer_surface.instance == null){
			try{
				int numOfCameras = NgnCameraProducer_surface.getNumberOfCameras();
				if (numOfCameras > 1) { //两个摄像头
					if (NgnCameraProducer_surface.useFrontFacingCamera) { //前置摄像头
						NgnCameraProducer_surface.instance = NgnCameraProducer_surface.openFrontFacingCamera();
					}
					else { //后置摄像头
						NgnCameraProducer_surface.instance = Camera.open();
					}
				}
				else { //单个前置/后置摄像头
					NgnCameraProducer_surface.instance = Camera.open(0);
				}
				
				NgnCameraProducer_surface.fps = fps;
				NgnCameraProducer_surface.width = width;
				NgnCameraProducer_surface.height = height;
				NgnCameraProducer_surface.holder = holder;
				NgnCameraProducer_surface.callback = callback;
				
				Camera.Parameters parameters = NgnCameraProducer_surface.instance.getParameters();
				
				/*
				 * http://developer.android.com/reference/android/graphics/ImageFormat.html#NV21
				 * YCrCb format used for images, which uses the NV21 encoding format. 
				 * This is the default format for camera preview images, when not otherwise set with setPreviewFormat(int). 
				 */
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
				parameters.setPreviewFrameRate(NgnCameraProducer_surface.fps);
				NgnCameraProducer_surface.instance.setParameters(parameters);
				
				try{
					parameters.setPictureSize(NgnCameraProducer_surface.width , NgnCameraProducer_surface.height);
					NgnCameraProducer_surface.instance.setParameters(parameters);
				}
				catch(Exception e){
					// FFMpeg converter will resize the video stream
					MyLog.d(NgnCameraProducer_surface.TAG, e.toString());
				}
				
				NgnCameraProducer_surface.instance.setPreviewDisplay(NgnCameraProducer_surface.holder);
				NgnCameraProducer_surface.initializeCallbacks(NgnCameraProducer_surface.callback);
			}
			catch(Exception e){
				NgnCameraProducer_surface.releaseCamera();
				Log.e(NgnCameraProducer_surface.TAG, e.toString());
			}
		}
		return NgnCameraProducer_surface.instance;
	}
	
	public static void releaseCamera(Camera camera){
		if(camera != null){
			camera.stopPreview();
			NgnCameraProducer_surface.deInitializeCallbacks(camera);
			camera.release();
			if(camera == NgnCameraProducer_surface.instance){
				NgnCameraProducer_surface.instance = null;
			}
		}
	}
	
	public static void releaseCamera(){
		if(NgnCameraProducer_surface.instance != null){
			NgnCameraProducer_surface.instance.stopPreview();
			NgnCameraProducer_surface.deInitializeCallbacks();
			NgnCameraProducer_surface.instance.release();
			NgnCameraProducer_surface.instance = null;
		}
	}
	
	public static void setDisplayOrientation(int degrees){
		if(NgnCameraProducer_surface.instance != null && NgnCameraProducer_surface.setDisplayOrientationMethod != null){
			try {
				NgnCameraProducer_surface.setDisplayOrientationMethod.invoke(NgnCameraProducer_surface.instance, degrees);
			} catch (Exception e) {
				Log.e(NgnCameraProducer_surface.TAG, e.toString());
			}
		}
	}
	
	public static void setDisplayOrientation(Camera camera, int degrees){
		if(camera != null && NgnCameraProducer_surface.setDisplayOrientationMethod != null){
			try {
				NgnCameraProducer_surface.setDisplayOrientationMethod.invoke(camera, degrees);
			} catch (Exception e) {
				Log.e(NgnCameraProducer_surface.TAG, e.toString());
			}
		}
	}
	
	public static void addCallbackBuffer(Camera camera, byte[] buffer) {
		try {
			NgnCameraProducer_surface.addCallbackBufferMethod.invoke(camera, buffer);
		} catch (Exception e) {
			Log.e(NgnCameraProducer_surface.TAG, e.toString());
		}
	}
	
	public static void addCallbackBuffer(byte[] buffer) {
		try {
			NgnCameraProducer_surface.addCallbackBufferMethod.invoke(NgnCameraProducer_surface.instance, buffer);
		} catch (Exception e) {
			Log.e(NgnCameraProducer_surface.TAG, e.toString());
		}
	}

	public static boolean isAddCallbackBufferSupported(){
		return NgnCameraProducer_surface.addCallbackBufferMethod != null;
	}
	
	public static boolean isFrontFacingCameraEnabled(){
		return NgnCameraProducer_surface.useFrontFacingCamera;
	}
	
	public static void useRearCamera(){
		NgnCameraProducer_surface.useFrontFacingCamera = false;
	}
	
	public static void useFrontFacingCamera(){
		NgnCameraProducer_surface.useFrontFacingCamera = true;
	}
	
	public static Camera toggleCamera(){
		if(NgnCameraProducer_surface.instance != null){
			NgnCameraProducer_surface.useFrontFacingCamera = !NgnCameraProducer_surface.useFrontFacingCamera;
			NgnCameraProducer_surface.releaseCamera();
			NgnCameraProducer_surface.openCamera(NgnCameraProducer_surface.fps, 
					NgnCameraProducer_surface.width, 
					NgnCameraProducer_surface.height,
					NgnCameraProducer_surface.holder, 
					NgnCameraProducer_surface.callback);
		}
		return NgnCameraProducer_surface.instance;
	}
	
	private static void initializeCallbacks(PreviewCallback callback){
		initializeCallbacks(callback, NgnCameraProducer_surface.instance);
	}
	
	private static void initializeCallbacks(PreviewCallback callback, Camera camera){
		if(camera != null){
			if(NgnCameraProducer_surface.setPreviewCallbackWithBufferMethod != null){
				try {
					NgnCameraProducer_surface.setPreviewCallbackWithBufferMethod.invoke(camera, callback);
				} catch (Exception e) {
					Log.e(NgnCameraProducer_surface.TAG, e.toString());
				}
			}
			else{
				camera.setPreviewCallback(callback);
			}
		}
	}
	
	private static void deInitializeCallbacks(){
		deInitializeCallbacks(NgnCameraProducer_surface.instance);
	}
	
	private static void deInitializeCallbacks(Camera camera){
		if(camera!= null){
			if(NgnCameraProducer_surface.setPreviewCallbackWithBufferMethod != null){
				try {
					NgnCameraProducer_surface.setPreviewCallbackWithBufferMethod.invoke(camera, new Object[]{ null });
				} catch (Exception e) {
					Log.e(NgnCameraProducer_surface.TAG, e.toString());
				}
			}
			else{
				camera.setPreviewCallback(null);
			}
		}
	}
	
	public static int getNumberOfCameras() {
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
		Camera camera = null;
		
		// 1. Android 2.3 or later
		if(NgnApplication.getSDKVersion() >= 9){
			try {
				Method getNumberOfCamerasMethod = Camera.class.getDeclaredMethod("getNumberOfCameras");
				if(getNumberOfCamerasMethod != null){
					Integer numberOfCameras = (Integer)getNumberOfCamerasMethod.invoke(null);
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
		return camera;
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
				MyLog.d(NgnCameraProducer_surface.TAG, e.toString());
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
					MyLog.d(NgnCameraProducer_surface.TAG, e.toString());
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
				Log.e(NgnCameraProducer_surface.TAG, e.toString());
			}
			return null;
		}
	}
}
