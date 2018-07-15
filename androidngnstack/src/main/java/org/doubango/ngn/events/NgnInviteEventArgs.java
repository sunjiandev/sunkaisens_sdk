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
package org.doubango.ngn.events;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.tinyWRAP.SdpMessage;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Event argument for SIP INVITE sessions
 */
public class NgnInviteEventArgs extends NgnEventArgs{
	private final static String TAG = NgnInviteEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private NgnInviteEventTypes mEventType;
    private NgnMediaType mMediaType;
    private String mPhrase;
    private byte[] mInfoContent;
    public static boolean m180HasSdp = false;
    
    private long mCSeq = 0;
    
    public static final String ACTION_INVITE_EVENT = TAG + ".ACTION_INVITE_EVENT";
    
    public static final String ACTION_CRASH_HANG_UP_EVENT = TAG + ".ACTION_CRASH_HANG_UP_EVENT";
    
    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED; // @NgnInviteEventArgs
    public static final String EXTRA_SESSION = "session"; // @object
    public static final String EXTRA_SIPCODE = "sipCode"; // @short
    public static final String EXTRA_REFERTO_URI = "referto-uri"; //@String
    public static final String EXTRA_CSEQ = "infocseq"; //@long

    public NgnInviteEventArgs(long sessionId, NgnInviteEventTypes eventType, NgnMediaType mediaType, String phrase){
    	super();
    	mSessionId = sessionId;
    	mEventType = eventType;
    	mMediaType = mediaType;
    	mPhrase = phrase;
    }

    public NgnInviteEventArgs(Parcel in){
    	super(in);
    }

    public static final Parcelable.Creator<NgnInviteEventArgs> CREATOR = new Parcelable.Creator<NgnInviteEventArgs>() {
        public NgnInviteEventArgs createFromParcel(Parcel in) {
            return new NgnInviteEventArgs(in);
        }

        public NgnInviteEventArgs[] newArray(int size) {
            return new NgnInviteEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public NgnInviteEventTypes getEventType(){
        return mEventType;
    }
    
    public NgnMediaType getMediaType(){
        return mMediaType;
    }

    public String getPhrase(){
        return mPhrase;
    }

    public byte[] getmInfoContent() {
		return mInfoContent;
	}

	public void setmInfoContent(byte[] mInfoContent) {
		this.mInfoContent = mInfoContent;
	}

    @Override
	protected void readFromParcel(Parcel in) {
    	mSessionId = in.readLong();
    	mCSeq = in.readLong();
    	Log.i(TAG, "CSeq readFromParcel CSeq="+mCSeq);
		mEventType = Enum.valueOf(NgnInviteEventTypes.class, in.readString());
		mMediaType = Enum.valueOf(NgnMediaType.class, in.readString());
		mPhrase = in.readString();
		mInfoContent = in.createByteArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Log.i(TAG, "CSeq writeToParcel CSeq="+mCSeq);
		dest.writeLong(mSessionId);
		dest.writeLong(mCSeq);
		dest.writeString(mEventType.toString());
		dest.writeString(mMediaType.toString());
		dest.writeString(mPhrase);
		dest.writeByteArray(mInfoContent);
	}

	public long getmCSeq() {
		return mCSeq;
	}

	public void setmCSeq(long mCSeq) {
		this.mCSeq = mCSeq;
	}

}
