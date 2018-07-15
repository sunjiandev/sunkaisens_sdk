package org.doubango.ngn.events;

import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.media.NgnMediaType;

import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class AdhocSessionEventArgs extends NgnEventArgs{
	private final static String TAG = AdhocSessionEventArgs.class.getCanonicalName();
	
	public static final String ADHOC_SESSION_EVENT = TAG + ".ADHOC_SESSION_EVENT";
	 
	private long mCSeq = 0;
	private long mSessionId;
    private AdhocSessionEventTypes mEventType;
    private NgnMediaType mMediaType;
    private String mPhrase;
    private int mAudioPort;
    private int mVideoPort;
    private PTTState mPTTState;
    private int mGroupCallType;

//    public AdhocSessionEventArgs(AdhocSessionEventTypes eventType, NgnMediaType mediaType, long sessionId){
//    	super();
//    	
//    	mEventType = eventType;
//    	mMediaType = mediaType;
//    	mSessionId = sessionId;
//    }
    public AdhocSessionEventArgs(Parcel in){
    	super(in);
    }
    public AdhocSessionEventArgs(AdhocSessionEventTypes eventType, NgnMediaType mediaType, long sessionId,int callType,PTTState state){
    	super();
    	
    	mEventType = eventType;
    	mMediaType = mediaType;
    	mSessionId = sessionId;
    	mPTTState = state;
    	mGroupCallType = callType;
    }
    public static final Parcelable.Creator<AdhocSessionEventArgs> CREATOR = new Parcelable.Creator<AdhocSessionEventArgs>() {
        public AdhocSessionEventArgs createFromParcel(Parcel in) {
            return new AdhocSessionEventArgs(in);
        }

        public AdhocSessionEventArgs[] newArray(int size) {
            return new AdhocSessionEventArgs[size];
        }
    };
    
    public long getSessionId(){
        return mSessionId;
    }

    public AdhocSessionEventTypes getEventType(){
        return mEventType;
    }
    
    public NgnMediaType getMediaType(){
        return mMediaType;
    }

    public int getAudioPort(){
        return mAudioPort;
    }
    public int getVideoPort(){
    	return mVideoPort;
    }

    public PTTState getmPTTState() {
		return mPTTState;
	}
    public int getGroupCallType()
    {
    	return this.mGroupCallType;
    }
//    public String getmPhrase()
//    {
//    	return this.mPhrase;
//    }
	public void setmPTTState(PTTState mState) {
		this.mPTTState = mState;
	}

    @Override
	protected void readFromParcel(Parcel in) {
    	
		mEventType = Enum.valueOf(AdhocSessionEventTypes.class, in.readString());
		mMediaType = Enum.valueOf(NgnMediaType.class, in.readString());
		mSessionId = in.readLong();
		mPTTState = Enum.valueOf(PTTState.class, in.readString());
		mGroupCallType = in.readInt();
//		mPhrase = in.readString();
//		mVideoPort = in.readInt();
//		
//		mInfoContent = in.createByteArray();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeString(mEventType.toString());
		dest.writeString(mMediaType.toString());
		dest.writeLong(mSessionId);
		dest.writeString(mPTTState.toString());
		dest.writeInt(mGroupCallType);
//		dest.writeString(mPhrase);
//		dest.writeInt(mAudioPort);
//		
//		dest.writeByteArray(mInfoContent);
	}

	

}
