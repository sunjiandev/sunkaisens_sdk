package demo.skdroid.sunkaisens.com.demo.callback;

import android.content.Intent;
import android.widget.Toast;

import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.sdk.agent.CallBackHelper;
import com.sunkaisens.skdroid.sdk.agent.ServiceConstants;

import org.doubango.utils.MyLog;

import demo.skdroid.sunkaisens.com.demo.MyApplication;
import demo.skdroid.sunkaisens.com.demo.view.FunctionActivity;

/**
 * 作者:sjy
 * 邮箱:sunjianyun@sunkaisens.com
 * 时间:2018/1/31 12:16
 */

public class MsgCallBackListener implements CallBackHelper.CallBack {
    @Override
    public void singleAudioCallIncomingCallBack(String incomNum) {

        Toast.makeText(MyApplication.getContext(), "语音通话来电", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void singleVidioCallIncomingCallBack(String incomNum) {

    }

    @Override
    public void singlePttCallIncomingCallBack(String incomNum) {

    }

    @Override
    public void groupPttCallIncomingCallBack() {

    }

    @Override
    public void pttState(GroupPTTCall.PTTState state, String userName, String groupNum) {

    }

    @Override
    public void onRingingCallBack() {
        Toast.makeText(MyApplication.getContext(), "响铃中", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void singleAudioCallAnswerCallBack() {

        Toast.makeText(MyApplication.getContext(), "语音电话接通", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void singleVideoCallAnswerCallBack() {

    }

    @Override
    public void groupPttCallAnswerCallBack() {

    }

    @Override
    public void singlePttCallAnswerCallBack() {

    }

    @Override
    public void singleAudioCallHungUpCallBack() {

        Toast.makeText(MyApplication.getContext(), "挂断电话", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void singleVideoCallHungupCallBack() {

    }

    @Override
    public void groupPttCallHungupCallBack() {

    }

    @Override
    public void singlePttCallHungupCallBack() {

    }

    @Override
    public void onLoginSuccessCallBack() {
        Intent intent = new Intent(MyApplication.getContext(), FunctionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyLog.d("sjy", "登录成功");
        MyApplication.getContext().startActivity(intent);
        Toast.makeText(MyApplication.getContext(), "登录成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginFailedCallBack(ServiceConstants.LoginFailedRes loginFailedRes) {

        MyLog.d("sjy", loginFailedRes);
    }

    @Override
    public void onLogoutSuccessCallBack() {
        MyLog.d("sjy", "onLogoutSuccessCallBack");
        Toast.makeText(MyApplication.getContext(), "注销成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLogoutFailedCallBack() {

    }

    @Override
    public void onServiceAbilitiesCallBack(String serviceAbilities) {

    }

    @Override
    public void onGroupTypesCallBack(String groupTypes) {

    }

    @Override
    public void onUserTypeCallBack(String userType) {

    }

    @Override
    public void onUserInforCallBack(String userInfor) {

    }

    @Override
    public void onGroupInforCallBack(String groupInfor) {

    }

    @Override
    public void onContactListCallBack(String contactList) {

    }

    @Override
    public void onGroupListCallBack(String groupList) {

        MyLog.d("sjy", groupList + ":所有组");
    }

    @Override
    public void onCreateGroupCallBack(String createGroup) {

    }

    @Override
    public void onUpdateGroupCallBack(String updateGroup) {

    }

    @Override
    public void onDeleteGroupCallBack(String deleteGroup) {

    }

    @Override
    public void onGetGroupMemCallBack(String getGroupMem) {

    }

    @Override
    public void onAddGroupMemCallBack(String addGroupMem) {

    }

    @Override
    public void onDelGroupMemCallBack(String delGroupMem) {

    }

    @Override
    public void onAddContactCallBack(String addContact) {

    }

    @Override
    public void onUpdateContactCallBack(String updateContact) {

    }

    @Override
    public void onDelContactCallBack(String delContact) {

    }

    @Override
    public void onNotify(String notify) {

        MyLog.d("sjy", "notify:  " + notify);
    }

    @Override
    public void onRecvP2PMsg(String strUri, String strMsg) {

    }

    @Override
    public void onReceiveMsgReport(String msgID, String msgType) {

    }

    @Override
    public void onRecvP2PFile(ServiceConstants.FILE_TYPE type, String strUri, String strFileName, String fileUri) {

    }

    @Override
    public void onRecvFileProgress(String strUri, int iProgress) {

    }

    @Override
    public void onAcceptP2PFileRst(ServiceConstants.EX_RST_EN iRst, String strUri, String strFilePath) {

    }

    @Override
    public void onRecvGroupMsg(String strGroupUri, String strSender, String strMsg) {

    }

    @Override
    public void onRecvGroupFile(ServiceConstants.FILE_TYPE type, String strUri, String strSenderUri, String strFileName, int iFileSize) {

    }

    @Override
    public void onAcceptGroupFileRst(ServiceConstants.EX_RST_EN iRst, String strUri, String strFilePath) {

    }
}
