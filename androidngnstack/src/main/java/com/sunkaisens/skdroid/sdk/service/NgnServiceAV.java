package com.sunkaisens.skdroid.sdk.service;

import android.content.Context;
import android.util.Log;

import com.sunkaisens.skdroid.groupcall.PTTActionTypes;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.groupcall.PTTResultTypes;
import com.sunkaisens.skdroid.groupcall.PTTTypes;
import com.sunkaisens.skdroid.sdk.NgnMediaManager;
import com.sunkaisens.skdroid.sdk.NgnSKDroid;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnCameraProducer;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import java.util.TimerTask;

public class NgnServiceAV {
	private static final String TAG = NgnServiceAV.class.getCanonicalName();
	private static NgnServiceAV lastServiceAV = null;
	private static boolean antiRotation = false;
	private NgnAVSession mAVSession;
	private NgnMediaSession mMediaSession;

	public NgnServiceAV(NgnAVSession session, Context mContext) {
		mAVSession = session;
		mAVSession.incRef(); // Increments the reference counting 增加引用计数
		mAVSession.setContext(mContext); // 本AVSession关联的上下文
		GlobalSession.avSession = mAVSession; // 记住这个会话对象，备用
	}

	private NgnServiceAV(NgnAVSession session) {
		mAVSession = session;
		mAVSession.incRef(); // Increments the reference counting 增加引用计数
	}

	private NgnServiceAV(NgnMediaSession session, Context mContext) {
		mMediaSession = session;
		mMediaSession.incRef(); // Increments the reference counting 增加引用计数
		mMediaSession.setContext(mContext); // 本AVSession关联的上下文
		GlobalSession.mediaSession = mMediaSession; // 记住这个会话对象，备用
	}

	private NgnServiceAV(NgnMediaSession session) {
		mMediaSession = session;
		mMediaSession.incRef(); // Increments the reference counting 增加引用计数
	}

	// 创建服务型的av服务对象
	public static NgnServiceAV create(NgnAVSession session, Context mContext) {
		return (lastServiceAV = new NgnServiceAV(session, mContext));
	}

	// 创建界面类型的av服务对象
	public static NgnServiceAV create(NgnAVSession session) {
		return (lastServiceAV = new NgnServiceAV(session));
	}

	// 创建服务型的av服务对象
	public static NgnServiceAV create(NgnMediaSession session, Context mContext) {
		return (lastServiceAV = new NgnServiceAV(session, mContext));
	}

	public static NgnServiceAV makeCall(String remoteUri,
			NgnMediaType mediaType, int sessionType) {


		Log.d(TAG, "呼叫类型:" + sessionType + " |媒体类型:" + mediaType + " |被叫："
				+ remoteUri);

		if (remoteUri == null) {
			MyLog.d(TAG, "remoteUri is null");
			return null;
		}
		// 解决视频通话协商SDP问题 视频分辨率由主叫来定，主被叫都是该分辨率
		NgnProxyPluginMgr.setCurrentVideoSize();

		final NgnEngine engine = NgnEngine.getInstance();

		final INgnSipService sipService = engine.getSipService();

		final INgnConfigurationService configurationService = engine
				.getConfigurationService();
		String validUri = NgnUriUtils.makeValidSipUri(remoteUri); // sip:#@test.com

		if (validUri == null) {
			Log.e(TAG, "failed to normalize sip uri '" + remoteUri + "'");
			return null;
		} else {
			remoteUri = validUri;
			if (remoteUri.startsWith("tel:")) {
				// E.164 number => use ENUM protocol
				final NgnSipStack sipStack = sipService.getSipStack();
				if (sipStack != null) {
					String phoneNumber = NgnUriUtils
							.getValidPhoneNumber(remoteUri);
					if (phoneNumber != null) {
						Log.d(TAG, "!!!!!!!!!@@@@-----" + phoneNumber);
						String enumDomain = configurationService
								.getString(
										NgnConfigurationEntry.GENERAL_ENUM_DOMAIN,
										NgnConfigurationEntry.DEFAULT_GENERAL_ENUM_DOMAIN);
						String sipUri = sipStack.dnsENUM("E2U+SIP",
								phoneNumber, enumDomain);
						if (sipUri != null) {
							remoteUri = sipUri;
						}
					}
				}
			}
		}

		final NgnAVSession avSession = NgnAVSession.createOutgoingSession(
				sipService.getSipStack(), mediaType); // 建立一个拨打电话用的outgoing
		// NgnAVSession
		NgnMediaManager.getInstance().initServiceAv(avSession.getId());

		Log.i("tag", Long.toString(avSession.getId()));

		avSession.setRemotePartyUri(remoteUri); // HACK
		avSession.setSessionType(sessionType);

		// GlobalSession.avSession = avSession; //记住这个会话对象，备用

		// flag_group = flag;

		// if (GlobalSession.bSocketService == false) {
		// screenService
		// .show(ScreenAV.class, Long.toString(avSession.getId()));
		// } else {
		//
		// }

		// Hold the active call
		final NgnAVSession activeCall = NgnAVSession
				.getFirstActiveCallAndNot(avSession.getId());
		if (activeCall != null) {
			activeCall.holdCall();
		}
		avSession.makeCall(remoteUri, sessionType);

		if (GlobalSession.bSocketService) {
			return (lastServiceAV = create(avSession, NgnSKDroid.getInstence()
					.getGlobleContext()));
		} else {
			MyLog.d(TAG, "bSocketService false.");
			return null;
		}
	}

	/**
	 * 判断是否是组呼会话
	 *
	 * @param st
	 * @return
	 */
	public static boolean isGroupCall(int st) {
		return st == SessionType.GroupAudioCall
				|| st == SessionType.GroupVideoCall;
	}

	/**
	 * 发送话权请求消息
	 *
	 * @return
	 */
	public static boolean sendPTTRequestInfoMsg() {
		try {
			Log.d(TAG, "sendPTTRequestInfoMsg()");
			PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REQUEST);
			msg.setPTTPhoneNumber(NgnEngine.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
			Log.d(TAG, "send ptt info msg:" + msg.toString());
			return sendInfo(msg.toString(), "sunkaisens/PTT");

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 发送话权释放消息
	 *
	 * @return
	 */
	public static boolean sendPTTReleaseInfoMsg() {
		Log.d(TAG, "sendPTTReleaseInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_RELEASE);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送话权释放成功消息
	 *
	 * @return
	 */
	public static boolean sendPTTReleaseAckInfoMsg() {
		Log.d(TAG, "sendPTTReleaseAckInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_RELEASE_ACK);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送组呼心跳消息
	 *
	 * @return
	 */
	public static boolean sendPTTReportAliveInfoMsg() {
		Log.d(TAG, "sendPTTReportAliveInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REPORT);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		msg.setPTTAction(PTTActionTypes.PTT_ACT_ALIVE);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送取消订阅成功消息
	 *
	 * @return
	 */
	public static boolean sendPTTCancelAckInfoMsg() {
		Log.d(TAG, "sendPTTCancelAckInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_CANCEL_ACK);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 发送info消息
	 *
	 * @param content1
	 * @param contentType
	 * @return
	 */
	private static boolean sendInfo(String content, String contentType) {
		Log.d(TAG, "sendInfo()");
		NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
				.getSessions();
		for (int i = 0; i < mAVSessions.size(); i++) {
			NgnAVSession session = mAVSessions.getAt(i);
			if (session != null && isGroupCall(session.getSessionType())) {
				return session.sendInfo(content, contentType);
			}
		}
		return false;
	}

	// 使用完毕后，需要去释放
	public void release() {
		if (mAVSession != null) {
			mAVSession.setContext(null);
			mAVSession.decRef();
		}
		if (mMediaSession != null) {
			mMediaSession.setContext(null);
			mMediaSession.decRef();
		}
	}

	/*
	 * get the AVSession
	 */
	public NgnMediaSession getMediaSession() {
		return mMediaSession;
	}

	public void setMediaSession(long sessionID) {
		mMediaSession = NgnMediaSession.getSession(sessionID);
	}

	public void setSpeakerphoneOn(boolean speakerOn) {
		if (mAVSession != null)
			mAVSession.setSpeakerphoneOn(speakerOn);
		else
			mMediaSession.setSpeakerphoneOn(speakerOn);
	}

	// 获取对端的sip账号
	public String getRemotePartyUri() {
		if (mAVSession != null) {
			return mAVSession.getRemotePartyUri();
		} else {
			return mMediaSession.getRemotePartyUri();
		}
	}

	public long getStartTime() {
		if (mAVSession != null) {
			return mAVSession.getStartTime();
		} else {
			return mMediaSession.getStartTime();
		}
	}

	public boolean isSpeakerOn() {
		if (mAVSession != null) {
			return mAVSession.isSpeakerOn();
		} else if (mMediaSession != null) {
			return mMediaSession.isSpeakerOn();
		}
		return false;
	}

	public boolean isConnected() {
		if (mAVSession != null) {
			return mAVSession.isConnected();
		} else if (mMediaSession != null) {
			return mMediaSession.isConnected();
		}
		return false;
	}

	/*
	 * get the AVSession
	 */
	public NgnAVSession getAVSession() {
		return mAVSession;
	}

	public boolean hangUpCall() {
		if (mAVSession != null) {
			// 用户挂断100s后，如果session没有被清空则将其清空 gzc 20140921
			NgnTimer timer = new NgnTimer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					long sessionId = mAVSession.getId();
					NgnAVSession session = NgnAVSession.getSession(sessionId);
					if (session != null) {
						NgnAVSession.releaseSession(session);
					}
				}
			}, 100 * 1000);

			return mAVSession.hangUpCall();
		}
		return false;
	}

	public boolean acceptCall() {
		if (mAVSession != null) {
			Log.i(TAG, "语音/视频 摘机");
			return mAVSession.acceptCall();
		}
		return false;
	}

	public void setOnPause(boolean pause) {
		MyLog.d(TAG, "setOnPause(" + pause + ")");
		if (mAVSession != null) {
			mAVSession.setOnPause(pause);
		}
		if (mMediaSession != null) {
			mMediaSession.setOnPause(pause);
		}
	}

	public void setConsumerOnPause(boolean pause) {
		if (mAVSession != null) {
			mAVSession.setConsumerOnPause(pause);
		}
		if (mMediaSession != null) {
			mMediaSession.setConsumerOnPause(pause);
		}
	}

	public void setProducerOnPause(boolean pause) {
		if (mAVSession != null) {
			mAVSession.setProducerOnPause(pause);
		}
		if (mMediaSession != null) {
			mMediaSession.setProducerOnPause(pause);
		}
	}

	public void setOnLocalHold() {
		if (mAVSession != null) {
			if (!mAVSession.isLocalHeld())
				mAVSession.setLocalHold(true);
		}
	}

	public boolean resumeCall() {
		if (mAVSession != null) {
			if (mAVSession.isLocalHeld())
				return mAVSession.resumeCall();
		}
		return false;
	}

	public void toggleSpeakerphone() {
		if (mAVSession != null) {
			mAVSession.toggleSpeakerphone();
		}
	}

	public boolean sendInfo(java.nio.ByteBuffer payload, String contentType) {
		if (mAVSession != null) {
			return mAVSession.sendInfo(payload, contentType);
		}
		return false;
	}

	/**
	 * 发送组呼成员在线状态请求
	 *
	 * @return
	 */
	public boolean sendPTTInquireInfoMsg() {
		Log.d(TAG, "sendPTTInquireInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_INQUIRE);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTAction("Active Member");
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	/**
	 * 切换摄像头
	 */
	public void switchCameraFrontOrBack() {
		Log.d(TAG, "当前的摄像头数 = " + NgnCameraProducer.getNumberOfCameras());
		if (getAVSession() != null) {
			getAVSession().toggleCamera();
			applyCamRotation(getAVSession().compensCamRotation(true)); // false
			// ->
			// true
			// 解决手持台切换摄像头时，对端设备显示倒像问题
		}

	}

	public void applyCamRotation(int rotation) {
		if (mAVSession != null) {
			switch (rotation) {
			case 0:
				if (antiRotation == false) {
					mAVSession.setRotation(0);
				} else {
					mAVSession.setRotation(180);
				}
				mAVSession.setProducerFlipped(false);
				break;
			case 90:
				if (antiRotation == false) {
					mAVSession.setRotation(90);
				} else {
					mAVSession.setRotation(270);
				}
				mAVSession.setProducerFlipped(false);
				break;
			case 180:
				if (antiRotation == false) {
					mAVSession.setRotation(180);
				} else {
					mAVSession.setRotation(0);
				}
				mAVSession.setProducerFlipped(true);
				break;
			case 270:
				if (antiRotation == false) {
					mAVSession.setRotation(270);
				} else {
					mAVSession.setRotation(90);
				}
				mAVSession.setProducerFlipped(true);
				break;
			}

		}
	}

	public void applyCameraUpOrDown(int rotation) {
		if (getAVSession() != null) {
			switch (rotation) {
			case 0:
				if (antiRotation == false) {
					getAVSession().setRotation(0);
				} else {
					getAVSession().setRotation(180);
				}
				getAVSession().setProducerFlipped(false);
				break;
			case 90:
				if (antiRotation == false) {
					getAVSession().setRotation(270);
				} else {
					getAVSession().setRotation(90);
				}
				getAVSession().setProducerFlipped(false);
				break;
			case 180:
				if (antiRotation == false) {
					getAVSession().setRotation(180);
				} else {
					getAVSession().setRotation(0);
				}
				getAVSession().setProducerFlipped(true);
				break;
			case 270:
				if (antiRotation == false) {
					getAVSession().setRotation(270);
				} else {
					getAVSession().setRotation(90);
				}
				getAVSession().setProducerFlipped(true);
				break;
			}

		}
	}
}
