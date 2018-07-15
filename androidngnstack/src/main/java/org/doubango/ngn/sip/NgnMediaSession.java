package org.doubango.ngn.sip;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyAudioConsumer;
import org.doubango.ngn.media.NgnProxyAudioProducer;
import org.doubango.ngn.media.NgnProxyPlugin;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.media.NgnProxyVideoConsumer;
import org.doubango.ngn.media.NgnProxyVideoProducer;
import org.doubango.ngn.media.NgnProxyVideoProducer_surface;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.utils.NgnListUtils;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnObservableObject;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.ProxyPlugin;
import org.doubango.tinyWRAP.twrap_media_type_t;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;


import android.content.Context;
import android.util.Log;
import android.view.View;

public class NgnMediaSession extends NgnObservableObject {
	private static final String TAG = NgnMediaSession.class.getCanonicalName();
	// public final static native int setAudioEchoFlag(int flag);
	public enum NgnMediaSessionState{
		INPROGRESS,
		RING,/**/
		INCOMMING,
		CONNECTED,/**/
		TERMINATED
	}
	private NgnProxyVideoConsumer mVideoConsumer;
	private NgnProxyVideoProducer mVideoProducer; 
	private NgnProxyVideoProducer_surface mVideoProducer_surface;
	
	private NgnProxyAudioConsumer mAudioConsumer;
	private NgnProxyAudioProducer mAudioProducer;
	private Context mContext;
	
	private MediaSessionMgr sessionMgr;
	private long mId = -1;
	
	private boolean mOutgoing;
	private boolean mSpeakerOn;
	protected int mSessionType; //通话类型
	protected NgnMediaType mMediaType;
	private NgnMediaSessionState sessionState = null;
	
	private boolean mConsumersAndProducersInitialzed;
	private final NgnHistoryAVCallEvent mHistoryEvent;
	
	protected int mRefCount = 1;
	
	private String mRemoteIp = null;
	
	private final static NgnObservableHashMap<Long, NgnMediaSession> sSessions = new NgnObservableHashMap<Long, NgnMediaSession>(true);
	protected NgnMediaSession(String localIP,NgnMediaType mediaType, NgnMediaSessionState sessionState,int sessionType )
	{
		sessionMgr = new MediaSessionMgr(localIP);
		mMediaType = mediaType;
		
		setSessionState(sessionState);
		setSessionType(sessionType);
		 mHistoryEvent = new NgnHistoryAVCallEvent((mediaType == NgnMediaType.AudioVideo || mediaType == NgnMediaType.Video), null);
		 mHistoryEvent.setSessionType(sessionType); //设置通话类型
		 incRef();
	}
	public boolean makeCall(String targetIp)
	{
		sessionMgr.callGroupAudio(targetIp);
		return true;
	}
	public MediaSessionMgr getMediaSessionMgr()
	{
		return sessionMgr;
	}
	public static NgnObservableHashMap<Long, NgnMediaSession> getSessions(){
		return sSessions;
	}
	public static NgnMediaSession creatOutGoingSession(String localIp,String remoteIp, NgnMediaType mediaType, int sessionType)
	{
	        synchronized (sSessions){
	        	final NgnMediaSession mediaSession = new NgnMediaSession(localIp,mediaType,NgnMediaSessionState.INPROGRESS,sessionType);
	        	
	        	mediaSession.mOutgoing = true;
	        	mediaSession.mRemoteIp = remoteIp;
	        	switch(mediaType){
	        	case Audio:
	        		mediaSession.getMediaSessionMgr().callAudio(remoteIp);
	        		break;
	        	case AudioVideo:
	        	case Video:
	        		mediaSession.getMediaSessionMgr().callVideo(remoteIp);
	        		break;
	        	}
	        	
	        	sSessions.put(mediaSession.getId(),mediaSession);
	        	return mediaSession;
	        }
	}
	
	public static NgnMediaSession takeIncommingSession(String localIp,String remoteIp,NgnMediaType mediaType,int sessionType, int audioPort, int videoPort)
	{
		synchronized (sSessions){
			final NgnMediaSession mediaSession = new NgnMediaSession(localIp,mediaType,NgnMediaSessionState.INCOMMING,sessionType);
			mediaSession.mRemoteIp = remoteIp;
        	mediaSession.mOutgoing = false;
        	if(mediaSession.isGroupAudioCall() || mediaSession.isGroupVideoCall()){
        		mediaSession.getMediaSessionMgr().acceptGroupCall(remoteIp,NgnMediaType.ConvertToNative(mediaType), audioPort, videoPort);
        	}else{
        		mediaSession.getMediaSessionMgr().acceptCall(remoteIp,NgnMediaType.ConvertToNative(mediaType), audioPort, videoPort);
        	}
        	sSessions.put(mediaSession.getId(),mediaSession);
        	return mediaSession;
		}
	}
	 /**
     * Gets a unique identifier defining a session
     * @return a unique identifier defining the session
     */
    public long getId(){
//    	if(mId == -1){
//    		mId = getMediaSessionMgr().getId();
//    	}
//         return mId;
    	return getMediaSessionMgr().getId();
    }

    public String getRemotePartyUri()
    {
    	return this.mRemoteIp;
    }
    public boolean isOutgoing(){
    	return mOutgoing;
    }
    public void setSessionState(NgnMediaSessionState   _sessionState)
	{
		this.sessionState = _sessionState;
		Log.d(TAG,"setSessionState (" +  this.sessionState +")");
		switch(sessionState){
		case CONNECTED:
			initializeConsumersAndProducers();
			
			setOnPause(false);
			break;
		case TERMINATED:
			deInitializeMediaSession();
			
			break;
		
		}
		
	}
	public NgnMediaSessionState getSessionState()
	{
		return this.sessionState;
	}
    /**
	 * Retrieves an audio/video session by id.
	 * @param id the id of the audio/video session to retrieve
	 * @return an audio/video session with the specified id if exist and null otherwise
	 */
	public static NgnMediaSession getSession(long id) {
		synchronized (sSessions) {
			if (sSessions.containsKey(id))
				return sSessions.get(id);
			else
				return null;
		}
	}
	public static NgnMediaSession getSession(NgnPredicate<NgnMediaSession> predicate) {
		synchronized (sSessions) {
			return NgnListUtils.getFirstOrDefault(sSessions.values(), predicate);
		}
	}
	private boolean initializeConsumersAndProducers(){
		MyLog.d(TAG, "initializeConsumersAndProducers()");
		if(mConsumersAndProducersInitialzed){
			return true;
		}
		
		final MediaSessionMgr mediaMgr;
		if((mediaMgr = getMediaSessionMgr()) != null){
			ProxyPlugin plugin;
			NgnProxyPlugin myProxyPlugin;
			// Video
			if(NgnMediaType.isVideoType(mMediaType)){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mVideoConsumer = (NgnProxyVideoConsumer)myProxyPlugin;
						mVideoConsumer.setContext(mContext);
						mVideoConsumer.setSipSessionId(getId());
					}
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_video)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
//						mVideoProducer = (NgnProxyVideoProducer)myProxyPlugin;
//						mVideoProducer.setContext(mContext);
//						mVideoProducer.setSipSessionId(super.getId());
						// Android 4.0 or later
						if (NgnApplication.getSDKVersion() >= 14) {
							mVideoProducer = (NgnProxyVideoProducer)myProxyPlugin;
							mVideoProducer.setContext(mContext);
							mVideoProducer.setSipSessionId(getId());
						}
						else {
							mVideoProducer_surface = (NgnProxyVideoProducer_surface)myProxyPlugin;
							mVideoProducer_surface.setContext(mContext);
							mVideoProducer_surface.setSipSessionId(getId());
						}
					}
				}
			}
			// Audio
			if(NgnMediaType.isAudioType(mMediaType)){
				if((plugin = mediaMgr.findProxyPluginConsumer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioConsumer = (NgnProxyAudioConsumer)myProxyPlugin;
						mAudioConsumer.setSipSessionId(getId());
					}
				
				}
				if((plugin = mediaMgr.findProxyPluginProducer(twrap_media_type_t.twrap_media_audio)) != null){
					if((myProxyPlugin = NgnProxyPluginMgr.findPlugin(plugin.getId())) != null){
						mAudioProducer = (NgnProxyAudioProducer)myProxyPlugin;
						mAudioProducer.setSipSessionId(getId());
					}
//					
				}
//				
			}

			
			mConsumersAndProducersInitialzed = true;
			return true;
		}
		
		return false;	
	}
	
	private void deInitializeMediaSession(){
		MyLog.d(TAG, "deInitializeMediaSession");
		if(sessionMgr != null){
//			sessionMgr.delete();
//			MyLog.d(TAG, "deInitializeMediaSession mMediaSessionMgr.delete");
//			sessionMgr = null;
//			hungUp();
		}
		
		MyLog.d(TAG, "deInitializeMediaSession OK");
	}
	public boolean hungUp()
	{
//		if(!this.isActive())
//			return false;
		if(sessionMgr == null){
			Log.d(TAG,"NgnMediaSession mgr == null No need hungUp() ");
		}
			synchronized (sSessions){
				Log.d(TAG,"NgnMediaSession hungUp() id = " + getId());
				sSessions.remove(getId());
				sessionMgr.hungup();
				decRef();
				//sessionMgr.delete();
				sessionMgr = null;
				
			}
		return true;
	}
	/**
	 * Gets the context associated to this session. Only used for video session to track the SurfaceView
	 * lifecycle
	 * @return the context
	 */
	public Context getContext(){
		return mContext;
	}
	
	/**
	 * Sets a context to associated to this session
	 * @param context the context
	 */
	public void setContext(Context context){
		mContext = context;
	}
	 /**
     * Increments the reference counting
     * @return the new reference counting value
     * @sa @ref decRef()
     */
	public int incRef(){
    	synchronized (this) {
    		if(mRefCount>0){
    			mRefCount++;
    		}
    		MyLog.d(TAG, "mRefCount="+mRefCount);
    		return mRefCount;
		}
    }
    
	/**
	 * Decrements the reference counting
	 * @return the new reference counting value
	 * @sa @ref incRef()
	 */
    public int decRef(){
    	synchronized (this) {
			if(--mRefCount == 0){
				getMediaSessionMgr().delete();
			}
			MyLog.d(TAG, "mRefCount="+mRefCount);
			return mRefCount;
		}
    }
    
//    public static void releaseSession(NgnMediaSession session){
//		synchronized (sSessions){
//            if (session != null && sSessions.containsKey(session.getId())){
//				// Android 4.0 or later
//				if (NgnApplication.getSDKVersion() >= 14) {
//	            	if(session.mVideoProducer != null)
//	            	{
//	            		session.mVideoProducer.servicemode_StopPreView();
//	            	}
//				}
//				else {
//				}
//                long id = session.getId();
//                session.hungUp();
//                session.decRef();
//                sSessions.remove(id);
//            }
//        }
//    }
    /**
	 * Starts the video consumer. A video consumer view used to display the video stream
	 * sent from the remote party. It's up to you to embed this view into a layout (LinearLayout, RelativeLayou,
	 * FrameLayout, ...) in order to display it.
	 * @return the view where the remote video stream will be displayed
	 */
	public final View startVideoConsumerPreview(){
		if(mVideoConsumer != null){
			return mVideoConsumer.startPreview(mContext);
		}
		return null;
	}
	
	/**
	 * Starts the video producer. A video producer is any device capable to generate video frames.
	 * It's likely a video camera (front facing or rear). The view associated to the producer is used as a feedback to
	 * show the local video stream sent to the remote party.
	 * It's up to you to embed this view into a layout (LinearLayout, RelativeLayou,
	 * FrameLayout, ...) in order to display it.
	 * @return the view where the local video stream will be displayed
	 */
	public final View startVideoProducerPreview(){
//		if(mVideoProducer != null){
//			return mVideoProducer.startPreview(mContext);
//		}
		// Android 4.0 or later
		if (NgnApplication.getSDKVersion() >= 14) {
			if(mVideoProducer != null){
				return mVideoProducer.startPreview(mContext);
			}
		}
		else {
			if(mVideoProducer_surface != null){
				return mVideoProducer_surface.startPreview(mContext);
			}
		}
		return null;
	}
	public boolean onVolumeChanged(boolean bDown){
		if(!NgnApplication.isSLEs2KnownToWork()){
			if(mAudioProducer == null || !mAudioProducer.onVolumeChanged(bDown)){
				return false;
			}
			if(mAudioConsumer == null || !mAudioConsumer.onVolumeChanged(bDown)){
				return false;
			}
		}
		return false;
	}
	 public NgnMediaType getMediaType(){
         return mMediaType;
     }
	public int getSessionType()
	{
		return this.mSessionType;
	}
	protected void setSessionType(int _mSessionType)
	{
		this.mSessionType = _mSessionType;
	}
	    /**
	     * 判断是否是语音组呼
	     * @return
	     */
		public boolean isGroupAudioCall() {
			if(mSessionType == SessionType.GroupAudioCall)
				return true;
			else
				return false;
		}
		/**
		 * 判断是否是视频组呼
		 * @return
		 */
		public boolean isGroupVideoCall() {
			if(mSessionType == SessionType.GroupVideoCall)
				return true;
			else
				return false;
		}
		
		/**
		 * 判断是否是视频监控
		 * @return
		 */
		public boolean isVideoMonitorCall(){
			if(mSessionType == SessionType.VideoSurveilMonitor
					|| mSessionType == SessionType.VideoMonitor
					|| mSessionType == SessionType.GroupVideoMonitor
					|| mSessionType == SessionType.VideoUaMonitor)
				return true;
			else
				return false;
		}
		 public NgnProxyVideoProducer getmVideoProducer() {
				return mVideoProducer;
			}
		 public boolean isConnected()
		 {
			 Log.d(TAG,"sessionState [ " + sessionState);
			 return this.sessionState == NgnMediaSessionState.CONNECTED;
		 }
		 public void pushBlankPacket(){
				// Android 4.0 or later
				if (NgnApplication.getSDKVersion() >= 14) {
					if(mVideoProducer != null){
						mVideoProducer.pushBlankPacket();
					}
				}
				else {
					if(mVideoProducer_surface != null){
						mVideoProducer_surface.pushBlankPacket();
					}
				}
		 }
				
		public int compensCamRotation(boolean preview){

			// Android 4.0 or later
			if (NgnApplication.getSDKVersion() >= 14) {
				if(mVideoProducer != null){
					return mVideoProducer.compensCamRotation(preview);
				}
			}
			else {
				if(mVideoProducer_surface != null){
					return mVideoProducer_surface.compensCamRotation(preview);
				}
			}
			return 0;
		}
		/**
		 * Enables or disables the speakerphone
		 * @param speakerOn true to enable the speakerphone and false to disable it
		 */
		public void setSpeakerphoneOn(boolean speakerOn){
			if(NgnApplication.isSLEs2KnownToWork()){
				final MediaSessionMgr mediaMgr;
				if((mediaMgr = getMediaSessionMgr()) != null){
					if(mediaMgr.consumerSetInt32(twrap_media_type_t.twrap_media_audio, "speaker-on", speakerOn ? 1 : 0)){
						mSpeakerOn = speakerOn;
					}
				}
			}
			else{
				if(mAudioProducer != null){
					mAudioProducer.setSpeakerphoneOn(speakerOn);
				}
				if(mAudioConsumer != null){
					mAudioConsumer.setSpeakerphoneOn(speakerOn);
				}
				mSpeakerOn = speakerOn;
			}
		}
		
		/**
		 * Toggles the speakerphone. Enable it if disabled and vice-versa
		 */
		public void toggleSpeakerphone(){
			setSpeakerphoneOn(!mSpeakerOn);
		}
		
		public boolean isSpeakerOn(){
			if(!NgnApplication.isSLEs2KnownToWork()){
				if(mAudioProducer != null){
					return mAudioProducer.isSpeakerOn();
				}
			}
			return mSpeakerOn;
		}
		public void setGroupAudioTimerStart(boolean start){
			mAudioProducer.setmSendAudioTimerSign(start);
		}
		public void setmSendVIdeo(boolean mSendVIdeo){
			GlobalVar.mSendVideo = mSendVIdeo;
		}
		public long getStartTime()
		{
			return mHistoryEvent.getStartTime();
		}
		
		public void setOnPause(boolean pause)
		{
			if(mAudioProducer != null){
				mAudioProducer.setOnPause(pause);
			}
			if(mAudioConsumer != null){
				mAudioConsumer.setOnPause(pause);
			}
		}
		public void setConsumerOnPause(boolean pause)
		{
			if(mAudioConsumer != null){
				mAudioConsumer.setOnPause(pause);
			}
		}
		public void setProducerOnPause(boolean pause)
		{
			if(mAudioProducer != null){
				mAudioProducer.setOnPause(pause);
			}
		}
		public static int getSize(){
	        synchronized (sSessions){
	            return sSessions.size();
	        }
	    }
		public static int getSize(NgnPredicate<NgnMediaSession> predicate) {
			synchronized (sSessions) {
				return NgnListUtils.filter(sSessions.values(), predicate).size();
			}
		}
		 public boolean isActive(){
	    	 return sessionState !=  NgnMediaSessionState.TERMINATED;
	     }
		 
		 public void startSession(){
			 synchronized (sSessions){
		        	this.getMediaSessionMgr().startSession();
		        	
		        	Log.d(TAG,"NgnMediaSession startSession() id = " + getId());
				}
		 }
		 public void setOnResetJB()
		{
			if(mAudioConsumer != null){
				mAudioConsumer.setOnResetJB();
			}
		}
			
}
