package com.sunkaisens.skdroid.groupcall;

import org.doubango.utils.MyLog;

import android.util.Log;

public class GroupPTTCall 
{
	private final static String TAG = GroupPTTCall.class.getCanonicalName();
	private PTTState mState;
	private boolean mStateChanged=true;
	private boolean mFirstOnline=false;
	private boolean mIsSubscribe=false;
	
	private String mCurrentSubscribeName = null;
	
	public String getCurrentSubscribeName() {
		return mCurrentSubscribeName;
	}

	public void setCurrentSubscribeName(String mCurrentSubscribeName) {
		this.mCurrentSubscribeName = mCurrentSubscribeName;
	}

	public enum PTTState{
        NONE,
        REQUESTING,
        GRANTED,
        REJECTED,
        RELEASED,//其他人释放ptt
        RELEASE_SUCCESS,//主动释放成功
        VIDEOSUB_TURNON,//视频订阅_打开
        VIDEOSUB_TURNOFF,//视频订阅_关闭
        CALSUB,//取消订阅
        ALAVE,
        ONLINE,
        OFFLINE,
        SUBSCRIBE_SUCCESS,//主动订阅成功
        SUBSCRIBE_FAILED,
        CANCEL_SUCCESS,//取消订阅成功
        CANCEL_FAILED,
        CONTROL,  //云台控制
        GET_AUDIO  //伴音回传
    }
	
	public GroupPTTCall() {
		mState=PTTState.NONE;
	}
	
	public void handlePTTInfoMsg(PTTInfoMsg msg) {			
		MyLog.d(TAG,"receive ptt info msg:"+msg.toString());
		this.setState(PTTState.NONE);
		
		if(PTTTypes.PTT_TYPE_GRANT.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_GRANT == msg.getmPTTType2()) {
			
			mStateChanged=true;
			this.setState(PTTState.GRANTED);
		} else if(PTTTypes.PTT_TYPE_REJECT.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_REJECT == msg.getmPTTType2()) {
			
			mStateChanged=true;
			this.setState(PTTState.REJECTED);
		} else if(PTTTypes.PTT_TYPE_RELEASE_ACK.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_RELEASE_ACK == msg.getmPTTType2()) {
			
			mStateChanged=true;
			this.setState(PTTState.RELEASE_SUCCESS);
		} else if(PTTTypes.PTT_TYPE_SUBSCRIBE.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_SUBSCRIBE == msg.getmPTTType2()) {
			
			MyLog.d("zhangjie:GroupPTTCall-handlePTTInfoMsg()", "PTTTypes.PTT_TYPE_SUBSCRIBE == (" + "msg.getPTTType() = " + msg.getPTTType() + ")");
			if(PTTActionTypes.PTT_ACT_TURN_ON_VIDEO.equals(msg.getPTTAction()) || 
					PTTActionTypes.PTT_EXT_ACT_ACT_VIDEO == msg.getmPTTAction2()) {
				
				mStateChanged=true;
				this.setState(PTTState.VIDEOSUB_TURNON);
			} else if(PTTActionTypes.PTT_ACT_TURN_OFF_VIDEO.equals(msg.getPTTAction()) ||
					PTTActionTypes.PTT_EXT_ACT_INACT_VIDEO == msg.getmPTTAction2()) {
				
				mStateChanged=true;
				this.setState(PTTState.VIDEOSUB_TURNOFF);
			}
				
		} else if(PTTTypes.PTT_TYPE_CANCEL.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_CANCEL == msg.getmPTTType2()) {
			
			mStateChanged=true;
			this.setState(PTTState.CALSUB);
		
		} else if(PTTTypes.PTT_TYPE_REPORT.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_REPORT == msg.getmPTTType2()) {
			
			MyLog.d("zhangjie:GroupPTTCall-handleGroupPTTInfoMsg()", "PTTTypes.PTT_TYPE_REPORT");
			mStateChanged=true;
			if(PTTActionTypes.PTT_ACT_ONLINE.equals(msg.getPTTAction()) ||
					PTTActionTypes.PTT_EXT_ACT_ONLINE == msg.getmPTTAction2()) {
				MyLog.d("zhangjie:GroupPTTCall-handleGroupPTTInfoMsg()", "PTTActionTypes.PTT_ACT_ONLINE");
				if(!isFirstOnline()) {
					this.setFirstOnline(true);
					MyLog.d("zhangjie:GroupPTTCall-handleGroupPTTInfoMsg()", "not isFirstOnline and then setFirstOnline");
				}
				
				this.setState(PTTState.ONLINE);
				MyLog.d("zhangjie:GroupPTTCall-handleGroupPTTInfoMsg()", "PTTState.ONLINE");
			}else if(PTTActionTypes.PTT_ACT_OFFLINE.equals(msg.getPTTAction()) ||
					PTTActionTypes.PTT_EXT_ACT_OFFLINE == msg.getmPTTAction2()) {
				MyLog.d("zhangjie:GroupPTTCall-handleGroupPTTInfoMsg()", "PTTActionTypes.PTT_ACT_OFFLINE");
				this.setState(PTTState.OFFLINE);
				MyLog.d("zhangjie:GroupPTTCall-handleGroupPTTInfoMsg()", "PTTState.OFFLINE");
			}
			
		} else if(PTTTypes.PTT_TYPE_RELEASE.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_RELEASE == msg.getmPTTType2()) {
			
			mStateChanged=true;
			this.setState(PTTState.RELEASED);
			
		} else if(PTTTypes.PTT_TYPE_SUBSCRIBE_ACK.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_SUBSCRIBE_ACK == msg.getmPTTType2()) {
			
			if(PTTResultTypes.PTT_RLT_OK.equals(msg.getPTTResult()) ||
					PTTResultTypes.PTT_EXT_RLT_OK == msg.getmPTTResult2()) {
				
				mStateChanged=true;
				this.setState(PTTState.SUBSCRIBE_SUCCESS);
			} else {
				mStateChanged=true;
				this.setState(PTTState.SUBSCRIBE_FAILED);
			}
			
		} else if(PTTTypes.PTT_TYPE_CANCEL_ACK.equals(msg.getPTTType()) ||
				PTTTypes.PTT_EXT_TYPE_CANCEL_ACK == msg.getmPTTType2()) {
			
			if(PTTResultTypes.PTT_RLT_OK.equals(msg.getPTTResult()) || 
					PTTResultTypes.PTT_EXT_RLT_OK == msg.getmPTTResult2()){
				mStateChanged=true;
				this.setState(PTTState.CANCEL_SUCCESS);
			} else {
				mStateChanged=true;
				this.setState(PTTState.CANCEL_FAILED);
			}
		}else if(PTTTypes.PTT_TYPE_CONTROL.equals(msg.getPTTType())) {
			
			mStateChanged=true;
			this.setState(PTTState.CONTROL);
		}else if(PTTTypes.PTT_TYPE_GET_AUDIO.equals(msg.getPTTType())) {
			
			mStateChanged=true;
			this.setState(PTTState.GET_AUDIO);
		}
	}

	public PTTState getState() {
		return mState;
	}

	public void setState(PTTState mState) {
		this.mState = mState;
	}

	public boolean isStateChanged() {
		return mStateChanged;
	}

	public void setStateChanged(boolean mStateChanged) {
		this.mStateChanged = mStateChanged;
	}

	public boolean isFirstOnline() {
		return mFirstOnline;
	}

	public void setFirstOnline(boolean mFirstOnline) {
		this.mFirstOnline = mFirstOnline;
	}

	public boolean isSubscribe() {
		return mIsSubscribe;
	}

	public void setIsSubscribe(boolean mIsSubscribe) {
		this.mIsSubscribe = mIsSubscribe;
	}
	
	
}
