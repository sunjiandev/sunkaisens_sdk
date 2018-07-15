package com.sunkaisens.skdroid.sdk.agent;

import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;
import com.sunkaisens.skdroid.sdk.agent.ServiceConstants.EX_RST_EN;
import com.sunkaisens.skdroid.sdk.agent.ServiceConstants.FILE_TYPE;
import com.sunkaisens.skdroid.sdk.agent.ServiceConstants.LoginFailedRes;

public class CallBackHelper {

	private static CallBack callback;

	public static void initCallBack(CallBack loginCallBack) {
		callback = loginCallBack;
	}

	public static void exuteOnRecvP2PMsg(String strUri, String strMsg) {

		callback.onRecvP2PMsg(strUri, strMsg);
	}
	public static void excuteOnReceiveMsgReport(String msgID,String msgType){
		callback.onReceiveMsgReport(msgID,msgType);
	}

	public static void exuteOnRecvP2PFile(FILE_TYPE type, String strUri,
			String strFileName, String fileUri) {
		callback.onRecvP2PFile(type, strUri, strFileName, fileUri);
	}

	public static void exuteOnServiceAbilities(String serviceAbilities) {
		callback.onServiceAbilitiesCallBack(serviceAbilities);
	}

	// ======================================群组=====================================

	public static void exuteOnGroupTypes(String groupTypes) {
		callback.onGroupTypesCallBack(groupTypes);
	}

	public static void exuteOnUserTypes(String userType) {
		callback.onUserTypeCallBack(userType);
	}

	public static void exuteOnUserInfor(String userInfor) {
		callback.onUserInforCallBack(userInfor);
	}

	public static void exuteOnGroupInfor(String groupInfor) {
		callback.onGroupInforCallBack(groupInfor);
	}

	public static void exuteOnContactList(String contactList) {
		callback.onContactListCallBack(contactList);
	}

	public static void exuteOnCreateGroup(String onCreateGroup) {
		callback.onCreateGroupCallBack(onCreateGroup);
	}

	public static void exuteOnUpdateGroup(String onUpdateGroup) {
		callback.onUpdateGroupCallBack(onUpdateGroup);
	}

	public static void exuteOnDeleteGroup(String deleteGroup) {
		callback.onDeleteGroupCallBack(deleteGroup);
	}

	public static void exuteOnGetGroupMem(String onGetGroupMem) {
		callback.onGetGroupMemCallBack(onGetGroupMem);
	}

	public static void exuteOnAddGroupMem(String onAddGroupMem) {
		callback.onAddGroupMemCallBack(onAddGroupMem);
	}

	public static void exuteOnDelGroupMem(String delGroupMem) {
		callback.onDelGroupMemCallBack(delGroupMem);
	}

	public static void exuteOnAddContact(String addContact) {
		callback.onAddContactCallBack(addContact);
	}

	public static void exuteOnUpdateContact(String updateContact) {
		callback.onUpdateContactCallBack(updateContact);
	}

	public static void exuteOnDelContact(String delContact) {
		callback.onDelContactCallBack(delContact);
	}

	public static void exuteOnGroupList(String groupList) {
		callback.onGroupListCallBack(groupList);
	}

	// ======================================注销=====================================
	public static void exuteOnLogoutSuccess() {
		callback.onLogoutSuccessCallBack();
	}

	public static void exuteOnLogoutFailed() {
		callback.onLogoutFailedCallBack();
	}

	// ======================================登录=====================================
	public static void exuteOnLoginSuccess() {
		callback.onLoginSuccessCallBack();
	}

	public static void exuteOnLoginFailed(LoginFailedRes loginFailedRes) {
		callback.onLoginFailedCallBack(loginFailedRes);
	}

	// ======================================媒体=====================================
	public static void exuteSingleAudioCallIncoming(String incomNum) {
		callback.singleAudioCallIncomingCallBack(incomNum);
	}

	public static void exuteSingleVidioCallIncoming(String incomNum) {
		callback.singleVidioCallIncomingCallBack(incomNum);
	}

	public static void exuteGroupPttCallIncoming() {
		callback.groupPttCallIncomingCallBack();
	}

	public static void exutesinglePttCAllIncoming(String incomNum) {
		callback.singlePttCallIncomingCallBack(incomNum);
	}

	public static void exutesingleAudioCallAnswer() {
		callback.singleAudioCallAnswerCallBack();
	}

	public static void exutesingleVideoCallAnswer() {
		callback.singleVideoCallAnswerCallBack();
	}

	public static void exutegroupPttCallAnswer() {
		callback.groupPttCallAnswerCallBack();
	}

	public static void exutesinglePttCallAnswer() {
		callback.singlePttCallAnswerCallBack();
	}

	public static void exutesingleAudioCallHungUp() {
		callback.singleAudioCallHungUpCallBack();
	}

	public static void exutesingleVideoCallHungup() {
		callback.singleVideoCallHungupCallBack();
	}

	public static void exutegroupPttCallHungup() {
		callback.groupPttCallHungupCallBack();
	}

	public static void exutesinglePttCallHungup() {
		callback.singlePttCallHungupCallBack();
	}

	public static void exuteOnRinging() {
		callback.onRingingCallBack();
	}

	public static void exuteOnPttState(PTTState state, String userName,
			String groupNum) {
		callback.pttState(state, userName, groupNum);
	}
	public static void exuteOnNotify(String notify) {
		callback.onNotify(notify);
	}

	public interface CallBack {

		// ======================来电=========================
		void singleAudioCallIncomingCallBack(String incomNum);

		void singleVidioCallIncomingCallBack(String incomNum);

		void singlePttCallIncomingCallBack(String incomNum);


		// ======================组呼=========================
		void groupPttCallIncomingCallBack();

		void pttState(PTTState state, String userName, String groupNum);

		// ======================响铃=========================

		void onRingingCallBack();

		// ======================接听=========================
		void singleAudioCallAnswerCallBack();

		void singleVideoCallAnswerCallBack();

		void groupPttCallAnswerCallBack();

		void singlePttCallAnswerCallBack();

		// ======================挂断=========================
		void singleAudioCallHungUpCallBack();//单呼语音挂断

		void singleVideoCallHungupCallBack();//单呼视频挂断

		void groupPttCallHungupCallBack();//组呼语音挂断

		void singlePttCallHungupCallBack();//单呼语音ptt挂断

		// ======================登录=========================
		void onLoginSuccessCallBack();

		void onLoginFailedCallBack(LoginFailedRes loginFailedRes);

		// ======================退出=========================
		void onLogoutSuccessCallBack();

		void onLogoutFailedCallBack();

		// ======================群组=========================

		void onServiceAbilitiesCallBack(String serviceAbilities);

		void onGroupTypesCallBack(String groupTypes);

		void onUserTypeCallBack(String userType);

		void onUserInforCallBack(String userInfor);

		void onGroupInforCallBack(String groupInfor);

		void onContactListCallBack(String contactList);

		void onGroupListCallBack(String groupList);

		void onCreateGroupCallBack(String createGroup);

		void onUpdateGroupCallBack(String updateGroup);

		void onDeleteGroupCallBack(String deleteGroup);

		void onGetGroupMemCallBack(String getGroupMem);

		void onAddGroupMemCallBack(String addGroupMem);

		void onDelGroupMemCallBack(String delGroupMem);

		void onAddContactCallBack(String addContact);

		void onUpdateContactCallBack(String updateContact);

		void onDelContactCallBack(String delContact);

		void onNotify(String notify);//收到的订阅消息
		// ======================================及时消息,文件=====================================

		void onRecvP2PMsg(String strUri, String strMsg);

		void onReceiveMsgReport(String msgID,String msgType);

		void onRecvP2PFile(FILE_TYPE type, String strUri, String strFileName,
						   String fileUri);

		void onRecvFileProgress(String strUri, int iProgress);

		void onAcceptP2PFileRst(EX_RST_EN iRst, String strUri,
								String strFilePath);

		// ======================================群组及时消息、文件=====================================
		void onRecvGroupMsg(String strGroupUri, String strSender, String strMsg);

		void onRecvGroupFile(FILE_TYPE type, String strUri,
							 String strSenderUri, String strFileName, int iFileSize);

		void onAcceptGroupFileRst(EX_RST_EN iRst, String strUri,
								  String strFilePath);

	}
}
