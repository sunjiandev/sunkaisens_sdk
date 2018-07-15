package demo.skdroid.sunkaisens.com.demo.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.sdk.NgnMediaManager;
import com.sunkaisens.skdroid.sdk.agent.CallBackHelper;
import com.sunkaisens.skdroid.sdk.agent.NgnSKClient;
import com.sunkaisens.skdroid.sdk.agent.ServiceConstants;

import org.doubango.utils.MyLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import demo.skdroid.sunkaisens.com.demo.R;

/**
 * 作者:sjy
 * 邮箱:sunjianyun@sunkaisens.com
 * 时间:2018/1/31 15:37
 */

public class FunctionActivity extends AppCompatActivity implements CallBackHelper.CallBack {

    private static final String remotrNum = "18800000004";
    @BindView(R.id.audio_call)
    Button audioCall;
    @BindView(R.id.video_call)
    Button videoCall;
    @BindView(R.id.group_audio_call)
    Button groupAudioCall;
    @BindView(R.id.singl_directcall)
    Button singlDirectcall;
    @BindView(R.id.switch_speaker_or_headhhones)
    Button switchSpeakerOrHeadhhones;
    @BindView(R.id.mute)
    Button mute;
    @BindView(R.id.switch_camera)
    Button switchCamera;
    @BindView(R.id.send_ptt_msg)
    Button sendPttMsg;
    @BindView(R.id.send_text_message)
    Button sendTextMessage;
    @BindView(R.id.get_service_abilities)
    Button getServiceAbilities;
    @BindView(R.id.get_group_types)
    Button getGroupTypes;
    @BindView(R.id.get_use_types)
    Button getUseTypes;
    @BindView(R.id.get_user_infor)
    Button getUserInfor;
    @BindView(R.id.get_group_infor)
    Button getGroupInfor;
    @BindView(R.id.get_contact_list)
    Button getContactList;
    @BindView(R.id.get_all_group)
    Button getAllGroup;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.answer)
    Button answer;
    @BindView(R.id.hungup)
    Button hungup;
    @BindView(R.id.infor)
    TextView strView;
    private boolean ismute = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        ButterKnife.bind(this);
        CallBackHelper.initCallBack(this);
    }

    @Override
    public void singleAudioCallIncomingCallBack(String incomNum) {

        strView.setText(incomNum + "语音来电");
    }

    @Override
    public void singleVidioCallIncomingCallBack(String incomNum) {
        strView.setText(incomNum + "视频来电");
    }

    @Override
    public void singlePttCallIncomingCallBack(String incomNum) {
        strView.setText(incomNum + "ptt");
    }

    @Override
    public void groupPttCallIncomingCallBack() {
        strView.setText("组呼来电");
    }

    @Override
    public void pttState(GroupPTTCall.PTTState state, String userName, String groupNum) {

    }

    @Override
    public void onRingingCallBack() {
        strView.setText("响铃");
    }

    @Override
    public void singleAudioCallAnswerCallBack() {

        strView.setText("语音对端接听");
    }

    @Override
    public void singleVideoCallAnswerCallBack() {
        strView.setText("视频对端接听");
    }

    @Override
    public void groupPttCallAnswerCallBack() {
        strView.setText("组呼ptt接听");
    }

    @Override
    public void singlePttCallAnswerCallBack() {
        strView.setText("ptt直通接听");
    }

    @Override
    public void singleAudioCallHungUpCallBack() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                strView.setText("语音单呼挂断");
            }
        });

    }

    @Override
    public void singleVideoCallHungupCallBack() {
        strView.setText("视频单呼挂断");
    }

    @Override
    public void groupPttCallHungupCallBack() {
        strView.setText("语音组呼挂断");
    }

    @Override
    public void singlePttCallHungupCallBack() {
        strView.setText("ptt直通挂断");
    }

    @Override
    public void onLoginSuccessCallBack() {

        startActivity(new Intent(this, FunctionActivity.class));
        finish();
    }

    @Override
    public void onLoginFailedCallBack(ServiceConstants.LoginFailedRes loginFailedRes) {

        strView.setText("登录失败");
    }

    @Override
    public void onLogoutSuccessCallBack() {

    }

    @Override
    public void onLogoutFailedCallBack() {

    }

    @Override
    public void onServiceAbilitiesCallBack(String serviceAbilities) {

        strView.setText(serviceAbilities);

    }

    @Override
    public void onGroupTypesCallBack(String groupTypes) {
        strView.setText(groupTypes);
    }

    @Override
    public void onUserTypeCallBack(String userType) {

        strView.setText(userType);
    }

    @Override
    public void onUserInforCallBack(String userInfor) {

        strView.setText(userInfor);
    }

    @Override
    public void onGroupInforCallBack(String groupInfor) {
        strView.setText(groupInfor);
    }

    @Override
    public void onContactListCallBack(String contactList) {
        strView.setText(contactList);
    }

    @Override
    public void onGroupListCallBack(String groupList) {
        strView.setText(groupList);
    }

    @Override
    public void onCreateGroupCallBack(String createGroup) {
        strView.setText(createGroup);
    }

    @Override
    public void onUpdateGroupCallBack(String updateGroup) {
        strView.setText(updateGroup);
    }

    @Override
    public void onDeleteGroupCallBack(String deleteGroup) {
        strView.setText(deleteGroup);
    }

    @Override
    public void onGetGroupMemCallBack(String getGroupMem) {

        strView.setText(getGroupMem);
    }

    @Override
    public void onAddGroupMemCallBack(String addGroupMem) {
        strView.setText(addGroupMem);
    }

    @Override
    public void onDelGroupMemCallBack(String delGroupMem) {
        strView.setText(delGroupMem);
    }

    @Override
    public void onAddContactCallBack(String addContact) {
        strView.setText(addContact);
    }

    @Override
    public void onUpdateContactCallBack(String updateContact) {
        strView.setText(updateContact);
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

        strView.setText("收到点对点消息 :" + strUri + "/r/n" + strMsg);
    }

    @Override
    public void onReceiveMsgReport(String msgID, String msgType) {

    }

    @Override
    public void onRecvP2PFile(ServiceConstants.FILE_TYPE type, String strUri, String strFileName, String fileUri) {

        strView.setText("收到文件消息 :" + type + "/r/n" + strFileName + "----" + fileUri);
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

    @OnClick({R.id.audio_call, R.id.video_call, R.id.group_audio_call, R.id.singl_directcall, R.id.switch_speaker_or_headhhones, R.id.mute, R.id.switch_camera, R.id.send_ptt_msg, R.id.send_text_message, R.id.get_service_abilities, R.id.get_group_types, R.id.get_use_types, R.id.get_user_infor, R.id.get_group_infor, R.id.get_contact_list, R.id.get_all_group, R.id.answer, R.id.hungup})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.audio_call:
                NgnMediaManager.getInstance().singleAudioCall(remotrNum, this);
                break;
            case R.id.video_call:
                NgnMediaManager.getInstance().singleVideoCall(remotrNum, this);
                break;
            case R.id.group_audio_call:
                NgnMediaManager.getInstance().makeGroupAudioCall("012");
                break;
            case R.id.singl_directcall:
                NgnMediaManager.getInstance().singleDirectcall(remotrNum, this);
                break;
            case R.id.switch_speaker_or_headhhones:
                NgnMediaManager.getInstance().switchSpeakerOrHeadPhones();
                break;
            case R.id.mute:
                NgnMediaManager.getInstance().mute(ismute);
                ismute = !ismute;
                break;
            case R.id.switch_camera:
                break;
            case R.id.send_ptt_msg:

                break;
            case R.id.send_text_message:
                NgnSKClient.getInstence().sendTextMessage(remotrNum, "1\n" +
                        "2\n" +
                        "3", false);
                break;
            case R.id.get_service_abilities:
                NgnSKClient.getInstence().getServiceAbilities();
                break;
            case R.id.get_group_types:
                NgnSKClient.getInstence().getGroupTypes();
                break;
            case R.id.get_use_types:
                NgnSKClient.getInstence().getUseTypes();
                break;
            case R.id.get_user_infor:
                NgnSKClient.getInstence().getUserInfor(remotrNum);
                break;
            case R.id.get_group_infor:
                NgnSKClient.getInstence().getGroupInfor("012");
                break;
            case R.id.get_contact_list:
                NgnSKClient.getInstence().getContactList(remotrNum);
                break;
            case R.id.get_all_group:
                NgnSKClient.getInstence().getAllGroup("service-group");
                break;
            case R.id.answer:
                NgnMediaManager.getInstance().acceptCall();
                break;
            case R.id.hungup:
                NgnMediaManager.getInstance().hungUp();
                break;
        }
    }
}
