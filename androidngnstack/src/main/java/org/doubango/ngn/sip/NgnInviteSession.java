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
 * 
 * @contributors: See $(DOUBANGO_HOME)\contributors.txt
 */
package org.doubango.ngn.sip;

import java.nio.ByteBuffer;
import java.util.Date;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnDeviceInfo;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.ActionConfig;
import org.doubango.tinyWRAP.InviteSession;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.util.GlobalVar;

/**
 * Generic INVITE session. Could be either audio/video or MSRP session. This is
 * an abstract class and you should only used it if you want to define you own
 * session.
 */
public abstract class NgnInviteSession extends NgnSipSession {
	protected NgnMediaType mMediaType;
	protected MediaSessionMgr mMediaSessionMgr = null;
	protected InviteState mState;
	protected boolean mRemoteHold;
	protected boolean mLocalHold;
	private boolean mEventAdded;
	private boolean mEventIncoming;
	private final NgnDeviceInfo mRemoteDeviceInfo;

	// gzc 20141023 当session收到媒体类型变更时，用以记住原来的媒体类型
	protected NgnMediaType mOldMediaType = null;

	public NgnMediaType getmOldMediaType() {
		return mOldMediaType;
	}

	public void setmOldMediaType(NgnMediaType mOldMediaType) {
		this.mOldMediaType = mOldMediaType;
	}

	public enum InviteState {
		NONE, INCOMING, INPROGRESS, REMOTE_RINGING, EARLY_MEDIA, INCALL, TERMINATING, TERMINATED,
	}

	/**
	 * Creates new Invite session
	 * 
	 * @param sipStack
	 *            the stack to use
	 */
	public NgnInviteSession(NgnSipStack sipStack) {
		super(sipStack);

		mRemoteDeviceInfo = new NgnDeviceInfo();
		mState = InviteState.NONE;
	}

	protected abstract NgnHistoryEvent getHistoryEvent();

	/**
	 * Gets the media type
	 * 
	 * @return the media type
	 */
	public NgnMediaType getMediaType() {
		return mMediaType;
	}

	/**
	 * Gets the session state
	 * 
	 * @return the session state
	 */
	public InviteState getState() {
		return mState;
	}

	/**
	 * Sets the session state
	 * 
	 * @param state
	 *            the new session state
	 */
	public void setState(InviteState state) {
		mState = state;
		NgnHistoryAVCallEvent historyEvent = (NgnHistoryAVCallEvent) getHistoryEvent();
		switch (state) {
		case INCOMING:
			mEventIncoming = true;
			if (historyEvent != null) {
				historyEvent.setmCallStartTime(new Date().getTime());
				historyEvent.setStatus(StatusType.Missed);
			}
			break;

		case INPROGRESS:
			mEventIncoming = false;
			if (historyEvent != null) {
				historyEvent.setmCallStartTime(new Date().getTime());
				historyEvent.setStatus(StatusType.Failed);
			}
			break;

		case INCALL:
			mEventAdded = false;
			if (historyEvent != null) {
				historyEvent.setStartTime(new Date().getTime());
				historyEvent.setEndTime(historyEvent.getEndTime());
				historyEvent.setStatus(mEventIncoming ? StatusType.Incoming
						: StatusType.Outgoing);
				// historyEvent.setAccepted(AvEventTypes.ACCEPTED);
			}
			break;

		case TERMINATED:
		case TERMINATING:
			if (historyEvent != null && !mEventAdded) {
				mEventAdded = true;
				if (historyEvent.getStatus() != StatusType.Missed
						&& historyEvent.getStatus() != StatusType.Failed) {
					historyEvent.setEndTime(new Date().getTime());
				} else {
					historyEvent.setEndTime(historyEvent.getStartTime());
				}

				String usrName = NgnUriUtils.getUserName(getRemotePartyUri());
				if (usrName != null) {
					if (usrName.startsWith(GlobalVar.videoMonitorPrefix)) {
						usrName = usrName
								.substring(GlobalVar.videoMonitorPrefix
										.length());
					}
				}
				String validUri = NgnUriUtils.makeValidSipUri(usrName);
				MyLog.d("", "validUri:" + validUri);
				historyEvent.setRemoteParty(validUri);

				historyEvent.setmLocalParty(GlobalVar.mLocalNum);
				MyLog.d("", "localName:" + GlobalVar.mLocalNum);
				
			}
			break;
		default: {
			break;
		}
		}
	}

	/**
	 * Checks whether the session is active or not
	 * 
	 * @return
	 */
	public boolean isActive() {
		return mState != InviteState.NONE && mState != InviteState.TERMINATING
				&& mState != InviteState.TERMINATED;
	}

	public boolean isLocalHeld() {
		return mLocalHold;
	}

	public void setLocalHold(boolean localHold) {
		mLocalHold = localHold;
	}

	public boolean isRemoteHeld() {
		return mRemoteHold;
	}

	public void setRemoteHold(boolean remoteHold) {
		mRemoteHold = remoteHold;
	}

	public NgnDeviceInfo getRemoteDeviceInfo() {
		return mRemoteDeviceInfo;
	}

	public boolean sendInfo(ByteBuffer content, String contentType) {
		if (content != null) {
			ActionConfig config = new ActionConfig();
			config.addHeader("Content-Type", contentType);
			boolean ret = ((InviteSession) this.getSession()).sendInfo(content,
					content.capacity(), config);
			config.delete();
			return ret;
		}
		return false;
	}

	public boolean sendInfo(String content, String contentType) {
		if (content != null) {
			ActionConfig config = new ActionConfig();
			config.addHeader("Content-Type", contentType);
			final byte[] bytes = content.getBytes();
			ByteBuffer payload = ByteBuffer.allocateDirect(bytes.length);
			payload.put(bytes);
			boolean ret = ((InviteSession) this.getSession()).sendInfo(payload,
					payload.capacity(), config);
			config.delete();
			return ret;
		}
		return false;
	}

	/**
	 * Gets the media session manager associated to this session
	 * 
	 * @return the media session manager
	 */
	public MediaSessionMgr getMediaSessionMgr() {
		if (mMediaSessionMgr == null) {
			mMediaSessionMgr = ((InviteSession) getSession()).getMediaMgr();
		}
		return mMediaSessionMgr;
	}
}
