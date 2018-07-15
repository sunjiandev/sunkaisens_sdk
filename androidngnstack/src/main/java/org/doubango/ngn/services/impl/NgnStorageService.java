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
package org.doubango.ngn.services.impl;

import org.doubango.ngn.services.INgnStorageService;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.sdk.NgnSKDroid;

/**@page NgnStorageService_page Storage Service
 * This service is used to manage storage functions.
 */

/**
 * Storage service.
 */
public class NgnStorageService  extends NgnBaseService implements INgnStorageService{
	private final static String TAG = NgnStorageService.class.getCanonicalName();
	
	private String mCurrentDir;
	private final String mContentShareDir;
	private String sdcardDir;
	private String sdcardMyDir;
	
	public NgnStorageService(){
		mCurrentDir = String.format("/data/data/%s", NgnSKDroid.getInstence().getGlobleContext().getPackageName());
		mContentShareDir = "/sdcard/wiPhone";
		initFilePath();
		
	}
	
	@Override
	public void initFilePath(){
//		StorageManager sm = (StorageManager) NgnApplication.getContext().getSystemService(Context.STORAGE_SERVICE);
//		String[] paths;
//		try {
//			paths = (String[]) sm.getClass().getMethod("getVolumePaths", null).invoke(sm, null);
//			if(paths != null && paths.length > 1){
//
//				for(String s : paths){
//					File f2 = new File(s+"/SKDroid/");
//					boolean b = false;
//					if(!f2.exists()){
//						b = f2.mkdir();
//					} else {
//						b = true;
//					}
//
//					Log.i(TAG, "sdcard file="+s+"  existed?"+b);
//					if(b){
//						sdcardDir = s;
//						sdcardMyDir = f2.getPath();
//						Log.i(TAG, "sdcard sdcardDir="+sdcardMyDir);
//					}
//				}
//			}
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchMethodException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}catch (Exception e) {
//			// TODO: handle exception
//			e.printStackTrace();
//		}
	}
	
	@Override
	public boolean start() {
		MyLog.init(sdcardMyDir);
		MyLog.d(TAG, "starting...");
		return true;
	}
	
	@Override
	public boolean stop() {
		MyLog.d(TAG, "stopping...");
		return true;
	}
	
	@Override
	public String getCurrentDir(){
		return this.mCurrentDir;
	}
	
	@Override
	public String getContentShareDir(){
		return this.mContentShareDir;
	}

	@Override
	public String getSdcardDir() {
		return sdcardMyDir;
	}
	public String getSdcardRootDir() {
		return sdcardDir;
	}

	public void setSdcardDir(String sdcardDir) {
		this.sdcardMyDir = sdcardDir;
	}
}
