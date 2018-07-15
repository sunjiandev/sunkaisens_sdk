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
package org.doubango.ngn.model;

import java.util.Date;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnUriUtils;


//@Root
public abstract class NgnHistoryEvent  implements Comparable<NgnHistoryEvent> {
	
	public enum StatusType{
		Outgoing,
		Incoming,
		Missed,
		Failed
	}
	
	// For performance reasons, do not use Date() class

//	@Element(name = "type", required = true)
	public NgnMediaType mMediaType;
//	@Element(name = "callstart", required = true)
	public long mCallStartTime;
//	@Element(name = "start", required = true)
	public long mStartTime;
//	@Element(name = "end", required = true)
	public long mEndTime;
//	@Element(name = "remote", required = true)
	public String mRemoteParty;
//	@Element(name = "seen", required = true)
	public boolean mSeen;
//	@Element(name = "status", required = true)
	public StatusType mStatus;
//	@Element(name = "sessionType", required = true)
	public int mSessionType;
//	@Element(name = "mLocalParty", required = true)
	public String mLocalParty = "aaa";  //add by gzc
//	@Element(name="mDisplayName",required=false)
	public String mDisplayName;
	
	public String[] getAllValuesNoId(){
//				"mLocalParty,mRemoteParty,mDisplayName,mSessionType,mMediaType,"
//				"mCallStartTime,mStartTime,mEndTime,mSeen,mStatus
		String[] values = new String[]{
				mLocalParty,mRemoteParty,mDisplayName,String.valueOf(mSessionType),mMediaType.toString(),
				String.valueOf(mCallStartTime),String.valueOf(mStartTime),String.valueOf(mEndTime),
				String.valueOf(mSeen),mStatus.toString()
			};
			return values;
	}
	
	protected NgnHistoryEvent(NgnMediaType mediaType, String remoteParty){
		mMediaType = mediaType;
		mStartTime = new Date().getTime();
		mEndTime = mStartTime;
		mRemoteParty = remoteParty;
		mStatus = StatusType.Missed;
	}
	
	public void setmLocalParty(String mLocalParty) {
		this.mLocalParty = mLocalParty;
	}
	
	public String getmLocalParty() {
		return mLocalParty;
	}
	
	public void setStartTime(long time){
		mStartTime = time;
	}
	
	public long getStartTime(){
		return mStartTime;
	}
	
	public long getEndTime(){
		return mEndTime;
	}
	
	public void setEndTime(long time){
		mEndTime = time;
	}
	
	public NgnMediaType getMediaType(){
		return mMediaType;
	}
	
	public String getRemoteParty(){
		return mRemoteParty;
	}
	
	public void setRemoteParty(String remoteParty){
		mRemoteParty = remoteParty;
	}
	
	public boolean isSeen(){
		return mSeen;
	}
	
	public void setSeen(boolean seen){
		mSeen = seen;
	}
	
	public StatusType getStatus(){
		return mStatus;
	}
	
	public void setStatus(StatusType status){
		mStatus = status;
	}
	
	public int getSessionType(){
		return mSessionType;
	}
	
	public void setSessionType(int sessionType){
		mSessionType = sessionType;
	}
	
	public void setDisplayName(String displayName){
		mDisplayName = displayName;
	}
	
	public String getDisplayName(){
		if(mDisplayName == null){
			mDisplayName = NgnUriUtils.getDisplayName(getRemoteParty());
		}
		return mDisplayName;
	}
	
	public void setmCallStartTime(long mCallStartTime) {
		this.mCallStartTime = mCallStartTime;
	}
	public long getmCallStartTime() {
		return mCallStartTime;
	}
	
	@Override
	public int compareTo(NgnHistoryEvent another) {
		return (int)(mStartTime - another.mStartTime);
	}
}
