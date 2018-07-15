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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnPredicate;

import com.sunkaisens.skdroid.util.GlobalVar;

//@Root
public class NgnHistorySMSEvent extends NgnHistoryEvent{
	
	public String id;
	
//	@Element(data=true, required=false)
	public String mContent;
//	@Element(data=true, required=false)
	public String mGMMember; //member of a group; group msg use;
//	@Element(data=true, required=false)
	public String mLocalMsgID; //保存消息id
	
//	@Element(data=true,required=false)
	public String  mIsDraft =  "false";        //标记是否为草稿，默认为false，表示不是草稿
	
//	@Element(data=true,required=false)
	public String  mIsRead =  "false";        //标记是否已读，默认为false，表示未读
	
//	@Element(data=true,required=false)
	public String  mDraftString  =  "";       

	public NgnHistorySMSEvent(){
		this(null, StatusType.Failed,"");
	}
	
	public NgnHistorySMSEvent(String remoteParty, StatusType status,String gmmember) {
		super(NgnMediaType.SMS, remoteParty);
		mGMMember = gmmember;
		super.setStatus(status);
		id = String.valueOf(new Date().getTime());
	}
	
	public void setContent(String content){
		this.mContent = content;
	}
	
	public String getContent(){
		return this.mContent;
	}
	
	public String getGMMember(){
		return this.mGMMember;
	}
	
	
	public void setIsDraft(String isdraft){
		this.mIsDraft=isdraft;
	}
	
	public String getIsDraft(){
		return this.mIsDraft;
	}
	
	
	public void setDraftString(String draft){
		this.mDraftString=draft;
	}
	
	public String getDraftString(){
		return this.mDraftString;
	}
	
	
	
	@Override
	public String[] getAllValuesNoId() {
		String[] values = new String[]{id,
				mLocalMsgID,mStatus.toString(),mLocalParty,mRemoteParty,mDisplayName,mMediaType.toString(),
				String.valueOf(mSeen),String.valueOf(mStartTime),String.valueOf(mEndTime),mIsRead,
				String.valueOf(mSessionType),String.valueOf(mIsDraft),mContent,mDraftString,mGMMember
			};
		return values;
	}
	
	public void setIsSeen(String isseen){
		this.mIsRead=isseen;
	}
	
	public String getIsSeen(){
		return this.mIsRead;
	}
	
	public static class HistoryEventSMSIntelligentFilter implements NgnPredicate<NgnHistoryEvent>{
		private final List<String> mRemoteParties = new ArrayList<String>();
		
		protected void reset(){
			mRemoteParties.clear();
		}
		
		@Override
		public boolean apply(NgnHistoryEvent event) {
			if (event != null && event.getmLocalParty().equals(GlobalVar.mLocalNum) 
					&&(event.getMediaType() == NgnMediaType.SMS || event.getMediaType() == NgnMediaType.Chat   )){
				if(!mRemoteParties.contains(event.getRemoteParty())){
					mRemoteParties.add(event.getRemoteParty());
					return true;
				}
			}
			return false;
		}
	}
	
	public static class HistoryEventSMSFilter implements NgnPredicate<NgnHistoryEvent>{
		@Override
		public boolean apply(NgnHistoryEvent event) {
			return (event != null && (event.getMediaType() == NgnMediaType.SMS || event.getMediaType() == NgnMediaType.Chat));
		}
	}
	
	public void setLocalMsgID(String mLocalMsgID){
		this.mLocalMsgID = mLocalMsgID;
	}
	
	public String getLocalMsgID(){
		return this.mLocalMsgID;
	}

	
}
