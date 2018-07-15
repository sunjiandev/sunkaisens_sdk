package org.doubango.ngn.events;

//author duhaitao
import android.os.Parcel;
import android.os.Parcelable;

public class NgnInfoEventArgs extends NgnEventArgs{
private final static String TAG = NgnInfoEventArgs.class.getCanonicalName();
	
	private long mSessionId;
    private NgnInfoEventTypes mEventType;
//    private NgnMediaType mMediaType;
    private String mPhrase;
    
    public static final String ACTION_INFO_EVENT = TAG + ".ACTION_INFO_EVENT";
    
    public static final String EXTRA_EMBEDDED = NgnEventArgs.EXTRA_EMBEDDED;
    public static final String EXTRA_SESSION = "session";
    public static final String EXTRA_SIPCODE = "sipCode";
    
    public NgnInfoEventArgs(long sessionId, NgnInfoEventTypes eventType, String phrase) {
		super();
		// TODO Auto-generated constructor stub
		mSessionId = sessionId;
    	mEventType = eventType;
//    	mMediaType = mediaType;
    	mPhrase = phrase;
	}

	public NgnInfoEventArgs(Parcel in) {
		super(in);
		// TODO Auto-generated constructor stub
	}
	
	public static final Parcelable.Creator<NgnInfoEventArgs> CREATOR = new Parcelable.Creator<NgnInfoEventArgs>() {
        public NgnInfoEventArgs createFromParcel(Parcel in) {
            return new NgnInfoEventArgs(in);
        }

        public NgnInfoEventArgs[] newArray(int size) {
            return new NgnInfoEventArgs[size];
        }
    };

    public long getmSessionId() {
		return mSessionId;
	}

	public void setmSessionId(long mSessionId) {
		this.mSessionId = mSessionId;
	}

	public NgnInfoEventTypes getmEventType() {
		return mEventType;
	}

	public void setmEventType(NgnInfoEventTypes mEventType) {
		this.mEventType = mEventType;
	}

//	public NgnMediaType getmMediaType() {
//		return mMediaType;
//	}
//
//	public void setmMediaType(NgnMediaType mMediaType) {
//		this.mMediaType = mMediaType;
//	}

	public String getmPhrase() {
		return mPhrase;
	}

	public void setmPhrase(String mPhrase) {
		this.mPhrase = mPhrase;
	}

	@Override
	protected void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		mSessionId = in.readLong();
		mEventType = Enum.valueOf(NgnInfoEventTypes.class, in.readString());
//		mMediaType = Enum.valueOf(NgnMediaType.class, in.readString());
		mPhrase = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(mSessionId);
		dest.writeString(mEventType.toString());
//		dest.writeString(mMediaType.toString());
		dest.writeString(mPhrase);
	}

}
