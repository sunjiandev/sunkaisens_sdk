package com.sunkaisens.skdroid.sdk;

import java.util.TimerTask;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;
import com.sunkaisens.skdroid.sdk.service.NgnServiceAV;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class NgnMediaManager {

	private volatile static NgnMediaManager audioCallManager;
	private static final String TAG = NgnMediaManager.class.getCanonicalName();
	public static NgnAVSession avSession;
	private NgnServiceAV serviceAV;
	private boolean mIsVideoCall;
	private FrameLayout mViewLocalVideoPreview;
	private boolean isSingleVideoFragmentAdded = false;
	private GroupPTTCall mPttCall;
	// 标记ptt键是否按下
	private boolean mPttTouched = false;
	private NgnTimer mPttTimerTmp;
	private boolean isSpeaker = false;
	private String contactMobileNo;
	public static NgnMediaManager getInstance() {

		if (audioCallManager == null) {
			synchronized (NgnMediaManager.class) {
				if (audioCallManager == null) {
					audioCallManager = new NgnMediaManager();
				}
			}
		}
		return audioCallManager;

	}
	private void MediaManager() {
		// MediaGroupState.setMediaGroupStateListener(this);
	}

	public void initServiceAv(long id) {
		session = NgnAVSession.getSession(id);
		serviceAV = NgnServiceAV.create(session);
	}

	/**
	 * 发起语音单呼
	 * 
	 * @param contactMobileNo
	 *            contactNumber,
	 * 
	 * */
	public void singleAudioCall(String contactMobileNo, Context context) {
		NgnServiceAV.makeCall(contactMobileNo, NgnMediaType.Audio,
				SessionType.AudioCall);
	}

	/**
	 * PTT直通
	 * 
	 * @param contactMobileNo
	 *            contactNumber,
	 * @param context
	 *            要显示notify的actvity
	 * 
	 * */
	public void singleDirectcall(String contactMobileNo, Context context) {
		NgnServiceAV.makeCall(contactMobileNo, NgnMediaType.Audio,
				SessionType.Directcall);
	}

	/**
	 * 发起视频单呼
	 * 
	 * @param contactMobileNo
	 *            contactNumber,
	 * @param context
	 *            要显示notify的actvity
	 * */
	public void singleVideoCall(String contactMobileNo, Context context) {

		NgnServiceAV.makeCall(contactMobileNo, NgnMediaType.Video,
				SessionType.VideoCall);
	}

	/**
	 * 挂断
	 * */
	public void hungUp() {
		serviceAV.hangUpCall();

	}

	/**
	 * 接通
	 * */
	public NgnServiceAV acceptCall() {
		if (serviceAV != null) {

			serviceAV.acceptCall();
			return serviceAV;
		} else {
			return null;
		}

	}

	/**
	 * 
	 * @param orgNumber
	 *            组号
	 * @param
	 *
	 */
	public void makeGroupAudioCall(String orgNumber) {
		String realm = NgnEngine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
		String g_v_dial_Uri = "sip:" + orgNumber + "@" + realm;
		if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {
			NgnServiceAV.makeCall(g_v_dial_Uri, NgnMediaType.Audio,
					SessionType.GroupAudioCall);
		}

	}

	/**
	 * 切换扬声器or听筒
	 * */
	public void switchSpeakerOrHeadPhones() {

		if (!isSpeaker) {
			serviceAV.getAVSession().setSpeakerphoneOn(true);
			isSpeaker = true;
		} else {
			serviceAV.getAVSession().setSpeakerphoneOn(false);
			isSpeaker = false;
		}
	}

	/**
	 * 静音
	 * */
	public void mute(boolean isMute) {
		if (!isMute) {
			serviceAV.setProducerOnPause(true);
		} else {
			serviceAV.setProducerOnPause(false);
		}
	}

	/**
	 * 切换摄像头
	 * */
	private NgnTimer mSwitchCameraTimer = new NgnTimer();
	private PTTState mPttTmp;
	private String pttName;
	private NgnAVSession session;

	public void switchCamera(View view) {

		final ImageView mSwitchCamera = (ImageView) view;
		serviceAV.switchCameraFrontOrBack();

		if (mSwitchCamera != null) {
			mSwitchCamera.setClickable(false);
			mSwitchCameraTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (mSwitchCamera != null) {
						mSwitchCamera.setClickable(true);
						MyLog.d(TAG, "mSwitchCamera.setClickable(true)");
					}
				}
			}, 1500);
		}

	}

	/**
	 * 发送ptt消息
	 * */
	public void sendPttMsg() {
		Log.d(TAG, "PTT MotionEvent.ACTION_DOWN");

		if (GlobalVar.PTTHasLongClickedDown) {
			Log.d(TAG, "ptt - PTTHasLongClickedDown"
					+ GlobalVar.PTTHasLongClickedDown);
		} else {
			Log.d(TAG, "ptt - PTTHasLongClickedDown"
					+ GlobalVar.PTTHasLongClickedDown);
			GlobalVar.PTTHasLongClickedDown = true;
			mPttTouched = true;
		}
		mPttTimerTmp = new NgnTimer();
		mPttTimerTmp.schedule(new TimerTask() {
			@Override
			public void run() {
				synchronized (NgnMediaManager.this) {
					NgnServiceAV.sendPTTRequestInfoMsg();
					mPttCall.setState(PTTState.REQUESTING);
					mPttTimerTmp = null;
				}

			}
		}, 500);

		if (mPttCall == null) {
			mPttCall = new GroupPTTCall();
		}
	}

//	/**
//	 * 开始播放ptt消息
//	 */
//	public void startSendPttMsg() {
//		handler.postDelayed(run, 0);
//	}
//
//	public void stopSendPttMsg() {
//		releasePttMsg();
//		handler.removeCallbacks(run);
//
//	}

	/**
	 * 释放ptt消息
	 * */
	public void releasePttMsg() {
		Log.d(TAG, "PTT MotionEvent.ACTION_UP");
		if (mPttTouched) {
			Log.d(TAG, "ptt - mPttTouched = " + mPttTouched
					+ ", GlobalVar.PTTHasLongClickedDown = "
					+ GlobalVar.PTTHasLongClickedDown);
			GlobalVar.PTTHasLongClickedDown = false;
			mPttTouched = false;
		} else {
			Log.d(TAG, "ptt - mPttTouched = " + mPttTouched
					+ ", GlobalVar.PTTHasLongClickedDown = "
					+ GlobalVar.PTTHasLongClickedDown);
		}
		if (mPttTimerTmp == null) {

			NgnServiceAV.sendPTTReleaseInfoMsg();

		} else {
			mPttTimerTmp.cancel();
			mPttTimerTmp.purge();
			mPttTimerTmp = null;
		}

	}

	/**
	 * 视频组呼
	 * 
	 * @param orgNumber
	 *            id
	 * 
	 * */
	public NgnServiceAV makeGroupVideoCall(String orgNumber) {
		String realm = NgnEngine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
		String g_v_dial_Uri = "sip:" + orgNumber + "@" + realm;
		if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {
			NgnServiceAV.makeCall(g_v_dial_Uri, NgnMediaType.AudioVideo,
					SessionType.GroupVideoCall);
		}

		return serviceAV;
	}

	/**
	 * 挂断视频组呼,用户需要自己处理界面的退出
	 * */
	public void hungUpGroupVideoCall() {

		if (serviceAV.getMediaSession() != null) {
			serviceAV.hangUpCall();
		}
		if (serviceAV.isConnected()) {
			NgnAVSession.mSendFrameThread = false;
		}

	}

	public void startVideo(boolean bStart, boolean bZOrderTop) {
		Log.d("zhangjie:startVideo()", "startVideo()");
		Log.d(TAG, "startStopVideo(" + bStart + ")");
		if (!mIsVideoCall) {
			Log.d(TAG, "startStopVideo(0000000000000000)");
			return;
		}
		// serviceAV.getAVSession().setSendingVideo(bStart);

		if (mViewLocalVideoPreview != null) {
			if (bStart) {
				mViewLocalVideoPreview.removeAllViews();
				// cancelBlankPacket();
				final View localPreview = serviceAV.getAVSession()
						.startVideoProducerPreview();
				if (localPreview != null) {
					final ViewParent viewParent = localPreview.getParent();
					if (viewParent != null && viewParent instanceof ViewGroup) {
						((ViewGroup) (viewParent)).removeView(localPreview);
					}
					if (bZOrderTop == true) {
						if (localPreview instanceof SurfaceView) {
							((SurfaceView) localPreview).setZOrderOnTop(true);
						}
					}
					mViewLocalVideoPreview.addView(localPreview);
					mViewLocalVideoPreview.bringChildToFront(localPreview);
				}
			}
			// mViewLocalVideoPreview.setVisibility(bStart ? View.VISIBLE
			// : View.GONE);
			mViewLocalVideoPreview.setVisibility(View.VISIBLE);
			mViewLocalVideoPreview.bringToFront();
		}
	}



//	private Runnable run = new Runnable() {
//		@Override
//		public void run() {
//			handler.postDelayed(run, 2000);
//			MyLog.d("ywh", "handler--->");
//			sendPttMsg();
//		}
//	};
}
