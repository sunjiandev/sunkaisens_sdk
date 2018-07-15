package com.sunkaisens.skdroid.sdk.agent;

import android.util.Log;

import com.sunkaisens.skdroid.bean.PttInfor;
import com.sunkaisens.skdroid.cpim.CPIMMessage;
import com.sunkaisens.skdroid.cpim.CPIMParser;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.sdk.SubscribeHelper;
import com.sunkaisens.skdroid.sdk.agent.ServiceConstants.LoginFailedRes;
import com.sunkaisens.skdroid.sdk.service.NgnServiceAV;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;
import com.sunkaisens.skdroid.util.ModelFileTransport;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMessagingEventTypes;
import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnSubscriptionSession.EventPackageType;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import java.util.TimerTask;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class NgnSipCallBackHelper {

    private static final String TAG = NgnSipCallBackHelper.class
            .getCanonicalName();
    public static boolean bLogin = false;
    private static boolean isGroupAudioCall = false;
    private static boolean isDirectcall = false;// 是否是半双工呼叫
    private static NgnSipCallBackHelper registerHelper;

    private GroupPTTCall mPttCall;
    private NgnInviteEventTypes eventType;

    private NgnSipCallBackHelper() {
    }

    public static NgnSipCallBackHelper getInstence() {
        if (registerHelper == null) {
            return new NgnSipCallBackHelper();
        } else {
            return registerHelper;
        }
    }

    /**
     * 处理注册消息的状态
     *
     * @param types     注册消息的结果
     * @param sessionId session
     * @param sipCode   sip码
     * @param phrase    不知道有什么用，保留
     */
    public void RegisterState(final NgnRegistrationEventTypes types, final long sessionId,
                              final int sipCode, final String phrase) {
        MyLog.d(TAG, "type--sessionid--sipcode--phrase" + types + "--"
                + sessionId + "--" + sipCode + "--" + phrase);

        Observable
                .create(new Observable.OnSubscribe<NgnRegistrationEventTypes>() {//线程调度，接受到位子线程
                    @Override
                    public void call(Subscriber<? super NgnRegistrationEventTypes> subscriber) {
                        subscriber.onNext(types);
                        MyLog.d(TAG, "Thread.currentThread().getName()--->"
                                + Thread.currentThread().getName());
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<NgnRegistrationEventTypes>() {
                    @Override
                    public void call(NgnRegistrationEventTypes result) {

                        switch (result) {
                            case REGISTRATION_OK:// 注册成功
                                CallBackHelper.exuteOnLoginSuccess();
                                bLogin = true;
                                break;

                            case REGISTRATION_NOK:// 注册失败
                                if (sipCode != 403 && sipCode != 404) {
                                    CallBackHelper
                                            .exuteOnLoginFailed(LoginFailedRes.LOGINFAILED_CHECK_CONFIG);// 登录失败原因未知
                                }
                                if (sipCode == 403) {
                                    CallBackHelper
                                            .exuteOnLoginFailed(LoginFailedRes.LOGINFAILED_USER_PWD_ERR);
                                } else if (sipCode == 404) {
                                    CallBackHelper
                                            .exuteOnLoginFailed(LoginFailedRes.LOGINFAILED_USER_NOT_EXIST);
                                }

                                break;
                            case REGISTRATION_INPROGRESS:// 注册中
                                break;
                            case UNREGISTRATION_OK:// 不知道谁定义的傻逼枚举值 字面意思是反注册成功，实际处理的是登录失败的原因

                                boolean connected = NgnEngine.getInstance().getSipService()
                                        .isRegisteSessionConnected();
                                if (!GlobalSession.bSocketService && !connected && !bLogin) {
                                    CallBackHelper
                                            .exuteOnLoginFailed(LoginFailedRes.LOGINFAILED_USER_PWD_ERR);// 用户名或者密码错误
                                }
                                if (GlobalVar.mLogout) {
                                    CallBackHelper.exuteOnLogoutSuccess();
                                }
                                break;
                            case UNREGISTRATION_NOK:// 反注册失败
                                if (GlobalVar.mLogout) {
                                    CallBackHelper.exuteOnLogoutFailed();
                                }

                                break;
                            case UNREGISTRATION_INPROGRESS:// 反注册中
                                break;
                            default:
                                if (!GlobalSession.bSocketService) {
                                    CallBackHelper
                                            .exuteOnLoginFailed(LoginFailedRes.LOGINFAILED_NO_REASON);
                                }
                                break;
                        }

                    }
                });
    }

    /**
     * 处理媒体的状态
     *
     * @param eventArgs
     */
    public void InviteState(final NgnInviteEventArgs eventArgs) {

        Observable.create(new Observable.OnSubscribe<NgnInviteEventArgs>() {//将所有事件都切换到主线程
            @Override
            public void call(Subscriber<? super NgnInviteEventArgs> subscriber) {
                subscriber.onNext(eventArgs);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<NgnInviteEventArgs>() {
            @Override
            public void call(NgnInviteEventArgs eventArgs) {
                eventType = eventArgs.getEventType();
                long sessionId = eventArgs.getSessionId();
                MyLog.d(TAG, "Receive a call,handling...eventType=" + eventType);
                String phrase = eventArgs.getPhrase();
                MyLog.d(TAG, "Receive a call,handling...phrase=" + phrase);
                NgnMediaType mediaType = eventArgs.getMediaType();
                MyLog.d(TAG, "Receive a call,handling...mediaType=" + mediaType);
                NgnAVSession avSession = NgnAVSession.getSession(sessionId);


                switch (eventType) {
                    case TERMWAIT:

                        break;
                    case TERMINATED:
                        MyLog.d(TAG, "isGroupAudioCall----->" + isGroupAudioCall);
                        if (mediaType.equals(NgnMediaType.Audio)
                                && isGroupAudioCall == false && isDirectcall == false) {
                            CallBackHelper.exutesingleAudioCallHungUp();// 音频通话挂断
                        } else if (mediaType.equals(NgnMediaType.AudioVideo)) {
                            CallBackHelper.exutesingleVideoCallHungup();// 视频通话挂断
                        } else if (mediaType.equals(NgnMediaType.Audio)
                                && isGroupAudioCall == true && isDirectcall == false) {
                            CallBackHelper.exutegroupPttCallHungup();
                            isGroupAudioCall = false;
                        } else if (mediaType.equals(NgnMediaType.Audio)
                                && isDirectcall == true && isGroupAudioCall == false) {// 半双工通话挂断
                            CallBackHelper.exutesinglePttCallHungup();
                            isDirectcall = false;
                        }
                        break;
                    case INCOMING:
                        if (NgnAVSession.getSize() > 1) { // 只允许一个拨打进来,优先级一样。高优先级打低优先级会先收到Byte消息
                            if (avSession != null) {
                                avSession.hangUpCall();
                            }
                            return;
                        }
                        String remotePartyUri = avSession.getRemotePartyUri();
                        MyLog.d(TAG, "mediaType---->" + mediaType
                                + "--mySession.getRemotePartyUri()--" + remotePartyUri);
                        boolean equals = phrase.equals("groupcall/audio");
                        MyLog.d(TAG, "equals---->" + equals);
                        if (mediaType.equals(NgnMediaType.Audio) && equals == false
                                && isDirectcall == false) {
                            CallBackHelper.exuteSingleAudioCallIncoming(remotePartyUri);
                        } else if (mediaType.equals(NgnMediaType.AudioVideo)) {
                            CallBackHelper.exuteSingleVidioCallIncoming(remotePartyUri);
                        } else if (mediaType.equals(NgnMediaType.Audio)
                                && avSession.getSessionType() == 11
                                && isGroupAudioCall == false) {

                            CallBackHelper.exutesinglePttCAllIncoming(remotePartyUri);
                        } else if (mediaType.equals(NgnMediaType.Audio) && equals
                                && isDirectcall == false) {
                            isGroupAudioCall = true;
                            CallBackHelper.exuteGroupPttCallIncoming();
                            NgnServiceAV serviceAV = NgnServiceAV.create(avSession);
                            serviceAV.acceptCall();
                        }
                        int sessionType = avSession.getSessionType();
                        MyLog.d(TAG, "sessionType----->" + sessionType);
                        if (sessionType == SessionType.Directcall) {
                            isDirectcall = true;
                            CallBackHelper.exutesinglePttCAllIncoming(remotePartyUri);
                            NgnServiceAV serviceAV = NgnServiceAV.create(avSession);
                            serviceAV.acceptCall();
                        }

                        break;
                    case INPROGRESS:

                        break;
                    case RINGING:
                        CallBackHelper.exuteOnRinging();
                        break;
                    case CONNECTED:
                        if (mediaType.equals(NgnMediaType.Audio)
                                && isGroupAudioCall == false) {
                            CallBackHelper.exutesingleAudioCallAnswer();
                        } else if (mediaType.equals(NgnMediaType.AudioVideo)) {
                            CallBackHelper.exutesingleVideoCallAnswer();
                        } else if (mediaType.equals(NgnMediaType.Audio)
                                && isGroupAudioCall == true) {
                            CallBackHelper.exutegroupPttCallAnswer();
                        } else if (mediaType.equals(NgnMediaType.Audio)
                                && isDirectcall == true) {
                            CallBackHelper.exutegroupPttCallAnswer();
                        }
                        break;
                    case EARLY_MEDIA:

                        break;
                    case ENCRYPT_INFO:

                        break;
                    case REMOTE_REFUSE:

                        break;
                    case SIP_RESPONSE:

                        break;
                    case REMOTE_MEDIA_NOT_EXIST:

                        break;
                    case CURRENT_NETWORK_UNGOOD:

                        break;
                    case PTT_INFO_REQUEST:

                        NgnInviteSession.InviteState state = avSession.getState();
                        MyLog.d(TAG, "----------state----------" + state);
                        switch (state) {
                            case NONE:
                            case INCOMING:

                                break;
                            case INPROGRESS:
                                break;
                            case REMOTE_RINGING:
                            case EARLY_MEDIA:
                                MyLog.d(TAG, "加载视图");
                                break;
                            case INCALL:
                                MyLog.d(TAG, "INCALL");
                                MyLog.d(TAG, "----------组呼来电----------" + state);
                                byte[] content = eventArgs.getmInfoContent();
                                String strContent = new String(content);
                                MyLog.d(TAG, "content----->" + strContent);
                                handleRequestPTTInfoMsg(content, NgnServiceAV.create(avSession));

                                break;
                            default:
                                break;
                        }

                        break;
                    case REMOTE_RESUME:

                        break;
                    case REMOTE_DEVICE_INFO_CHANGED:
                        break;
                    default:
                        break;
                }

            }
        });


    }

    /**
     * 处理即时消息
     *
     * @param args
     * @param remoteParty
     */
    public void MessageState(final NgnMessagingEventArgs args, final String remoteParty) {

        Observable.create(new Observable.OnSubscribe<NgnMessagingEventArgs>() {
            @Override
            public void call(Subscriber<? super NgnMessagingEventArgs> subscriber) {
                subscriber.onNext(args);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<NgnMessagingEventArgs>() {
            @Override
            public void call(NgnMessagingEventArgs ngnMessagingEventArgs) {

                NgnMessagingEventTypes eventType = args.getEventType();
                String contentType = args.getContentType();
                MyLog.d(TAG, "MessageState---->" + eventType);
                MyLog.d(TAG, "contentType---->" + contentType);
                MyLog.d(TAG, "remoteParty---->" + remoteParty);
                switch (eventType) {
                    case INCOMING:
                        // 空值判断
                        if (NgnStringUtils.isNullOrEmpty(remoteParty)) {
                            String remoteParty = NgnStringUtils.nullValue();
                            Log.d(TAG, "remoteParty 6 = " + remoteParty);
                        }
                        CPIMMessage cpimMessage = CPIMParser.parse(new String(args
                                .getPayload()));
                        String msgType = cpimMessage.getMsgType();
                        String localMsgID = cpimMessage.getLocalMsgID();

                        if (msgType.equals("REPORT")){
                            CallBackHelper.excuteOnReceiveMsgReport(localMsgID, msgType);
                        }
                        MyLog.d(TAG,"msgType : "+msgType);
                        if (cpimMessage == null) {
                            MyLog.e(TAG, "cpimMessage parse failed.");
                            return;
                        }


                        String content = (String) cpimMessage.getContent();
                        MyLog.d(TAG, "content---->" + content);
                        if (content != null && content.startsWith("type:file")) {// 收到的是文件

                            ModelFileTransport transport = new ModelFileTransport();

                            transport.parseFileContent(content);
                            MyLog.d(TAG, "文件消息解析结果：" + transport);

                            CallBackHelper.exuteOnRecvP2PFile(ServiceConstants.FILE_TYPE.FILE, remoteParty,
                                    transport.name, transport.url);

                        } else {// 收到的是及时消息

                            CallBackHelper.exuteOnRecvP2PMsg(remoteParty, content);
                        }

                        break;

                    case OUTGOING:

                        break;
                    case SUCCESS:

                        break;
                    case FAILURE:

                        break;

                }


            }
        });


    }

    /**
     * 订阅消息处理
     *
     * @param args
     * @param toHeader
     */
    public void SubscriptionState(final NgnSubscriptionEventArgs args, final String toHeader) {

        Observable.create(new Observable.OnSubscribe<NgnSubscriptionEventArgs>() {
            @Override
            public void call(Subscriber<? super NgnSubscriptionEventArgs> subscriber) {
                subscriber.onNext(args);

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<NgnSubscriptionEventArgs>() {
            @Override
            public void call(NgnSubscriptionEventArgs ngnSubscriptionEventArgs) {

                MyLog.d(TAG, args.getContentType() + "=====" + args.getContent());
                NgnSubscriptionEventTypes eventType = args.getEventType();
                MyLog.d(TAG, "args.getEventType()========>" + eventType);
                INgnSipService sipService = NgnEngine.getInstance().getSipService();
                NgnSipStack sipStack = sipService.getSipStack();
                switch (eventType) {
                    // SUBSCRIPTION_OK, SUBSCRIPTION_NOK, SUBSCRIPTION_INPROGRESS,
                    // UNSUBSCRIPTION_OK, UNSUBSCRIPTION_NOK, UNSUBSCRIPTION_INPROGRESS,
                    // INCOMING_NOTIFY
                    case SUBSCRIPTION_INPROGRESS:
                        MyLog.d(TAG, "Subscribing。。。");
                        break;
                    case SUBSCRIPTION_OK:
                        MyLog.d(TAG, "Subscribe success");
                        break;
                    case INCOMING_NOTIFY:
                        MyLog.d(TAG, "Subscribe success，receive notify msg");
                        EventPackageType type = args.getEventPackage();
                        if (args.getContent() == null)
                            break;
                        String notifyContent = new String(args.getContent());
                        String contentType = args.getContentType();
                        MyLog.d("INCOMING_NOTIFY", String.format("GLE---type=%s,content=%s", contentType, notifyContent));
                        MyLog.e(TAG, notifyContent + "=====" + contentType);
                        MyLog.d(TAG, "screen.refresh: Notify");
                        CallBackHelper.exuteOnNotify(notifyContent);
                        break;
                    case SUBSCRIPTION_NOK:

                        MyLog.d(TAG, "Subscribe failed");

                        short sipCode = args.getSipCode();
                        MyLog.d(TAG, "sipCode = " + sipCode + ", to: " + toHeader);
                        if (sipCode == 481 && sipStack.isValid()) {
                            if (toHeader.contains("sip:rls@test.com")) {
                                // ServiceContact.subAll(ServiceContact.auidForPresence);
                                MyLog.d(TAG, "subAll(rls)");
                            } else if (toHeader.contains("sip:public-group@test.com")) {
                                SubscribeHelper.getInstance().subscribePublicGroup();
                                MyLog.d(TAG, "subscribePublicGroup()");
                            } else if (toHeader.contains("sip:service-group@test.com")) {
                                SubscribeHelper.getInstance().subscribeServiceGroup();
                                MyLog.d(TAG, "subscribeServiceGroup()");
                            } else if (toHeader.contains("sip:global-group@test.com")) {
                                SubscribeHelper.getInstance().subscribeGlobalGroup();
                                MyLog.d(TAG, "subscribeGlobalGroup()");
                            } else if (toHeader.contains("sip:subscribe-group@test.com")) {
                                SubscribeHelper.getInstance().subscribeSubscribeGroup();
                                MyLog.d(TAG, "subscribeSubscribeGroup()");
                            } else if (toHeader.contains("sip:ims-pim@test.com")) {
                                SubscribeHelper.getInstance().subscribePersionInfo();
                                MyLog.d(TAG, "subscribePersionInfo()");
                            }
                        }
                        if (sipCode == 480 || sipCode == 408 || sipCode == 500) {
                            new NgnTimer().schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    // ServiceContact.subAll(ServiceContact.auidForPresence);
                                }
                            }, 30000);
                        }

                        break;
                    case UNSUBSCRIPTION_OK:
                    case UNSUBSCRIPTION_NOK:
                    case UNSUBSCRIPTION_INPROGRESS:
                    default:
                        break;
                }

            }
        });


    }

    /**
     * 处理群组ptt状态
     *
     * @param infoContent
     * @param mServiceAV
     */

    public void handleRequestPTTInfoMsg(byte[] infoContent,
                                        NgnServiceAV mServiceAV) {
        MyLog.d(TAG, "语音组呼      handleRequestPTTInfoMsg()");

        if (mPttCall == null) {
            mPttCall = new GroupPTTCall();
        }
        PTTInfoMsg msg = new PTTInfoMsg(infoContent);
        mPttCall.handlePTTInfoMsg(msg);

        String content = new String(infoContent);
        mServiceAV.setSpeakerphoneOn(true);

        PttInfor pttInfor = dateCheck(content);

        MyLog.d(TAG, "mPttCall.State = " + mPttCall.getState()
                + "=========content=====" + content);
        if (mPttCall.getState() == PTTState.REJECTED) {
            mServiceAV.setConsumerOnPause(false);
            mServiceAV.setProducerOnPause(true);

        } else if (mPttCall.getState() == PTTState.GRANTED) {
            mServiceAV.setConsumerOnPause(true);
            mServiceAV.setProducerOnPause(false);
        } else if (mPttCall.getState() == PTTState.RELEASE_SUCCESS) {
            mServiceAV.setConsumerOnPause(true);
            mServiceAV.setProducerOnPause(true);
        }
        NgnEngine.getInstance();
        switch (mPttCall.getState()) {
            case NONE:
            case REQUESTING:
                break;
            case GRANTED: // 当前用户PTT抢占成功
                MyLog.d(TAG, "GRANTED");

                CallBackHelper.exuteOnPttState(PTTState.GRANTED,
                        pttInfor.getRemoteNum(), pttInfor.getGroupNum());

                break;
            case RELEASE_SUCCESS: // 当前用户PTT释放成功
                MyLog.d(TAG, "RELEASE_SUCCESS");
                CallBackHelper.exuteOnPttState(PTTState.RELEASE_SUCCESS,
                        pttInfor.getRemoteNum(), pttInfor.getGroupNum());
                break;
            case RELEASED: // 非当前用户释放ptt
                MyLog.d(TAG, "RELEASED");
                NgnServiceAV.sendPTTReleaseAckInfoMsg();
                CallBackHelper.exuteOnPttState(PTTState.RELEASED,
                        pttInfor.getRemoteNum(), pttInfor.getGroupNum());
                break;
            case REJECTED: // 非当前用户ptt抢占成功
                MyLog.d(TAG, "REJECTED");
                CallBackHelper.exuteOnPttState(PTTState.REJECTED,
                        pttInfor.getRemoteNum(), pttInfor.getGroupNum());
                break;
            case ALAVE:

                break;
            case ONLINE:
                MyLog.d(TAG, "语音组呼  ONLINE  用户:" + msg.getPTTPhoneNumber());
                break;
            case OFFLINE:
                MyLog.d(TAG, "语音组呼   OFFLINE:" + msg.getPTTPhoneNumber());
                String offLineUser = msg.getPTTPhoneNumber();
                MyLog.d(TAG, "offLineUser---->" + offLineUser);
                break;
            case CALSUB:
                MyLog.d(TAG, "CALSUB");
                NgnServiceAV.sendPTTCancelAckInfoMsg();
                break;
            case CANCEL_SUCCESS:
                MyLog.d(TAG, "CANCEL_SUCCESS");
                break;

            default:
                break;
        }

    }

    public PttInfor dateCheck(String content) {

        PttInfor pttInfor = new PttInfor();
        String[] split = content.split("\r\n");
        if (split != null && split.length != 0) {
            for (int i = 0; i < split.length; i++) {
                MyLog.d(TAG, "切割之后的内容：---->" + split[i]);
                if (split[1].startsWith("PTT.PhoneNumber:")) {
                    String remoteNum = split[1].split(":")[1];
                    MyLog.d(TAG, "对端的号码：---->" + remoteNum);
                    pttInfor.setRemoteNum(remoteNum);
                }
                if (split[2].startsWith("PTT.IncludeNumber:")) {
                    if (split[2].split(":").length > 1) {
                        MyLog.d(TAG, "groupNum--->" + split[2].split(":")[1]);
                        String groupNum = split[2].split(":")[1];
                        pttInfor.setGroupNum(groupNum);
                    }
                }

            }
        }

        return pttInfor;
    }

}
