package com.sunkaisens.skdroid.sdk.agent;

import android.util.Log;

import com.sunkaisens.skdroid.bean.MessageBodyInfo;
import com.sunkaisens.skdroid.sdk.DefaultSubscriber;
import com.sunkaisens.skdroid.sdk.HttpClient;
import com.sunkaisens.skdroid.sdk.NgnSKDroid;
import com.sunkaisens.skdroid.sdk.SubscribeHelper;
import com.sunkaisens.skdroid.sdk.apiservice.ApiService;
import com.sunkaisens.skdroid.util.GlobalVar;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.utils.MyLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author sun 基础的 sdk api
 */
public class NgnSKClient {

    private static final String TAG = NgnSKClient.class.getCanonicalName();
    private static final String DEFALT_INFOR = "4060";
    private static final String DEFALT_DOMAIN = "test.com";
    private static final String DEFALT_GROUP_SERVER = "appserver.test.com";
    private static final String DEFALT_FILESERVER = "appserver.test.com:8010";
    private static NgnSKClient ngnSKClient;
    private INgnConfigurationService mConfigurationService;
    private INgnSipService sipService;
    private INgnConfigurationService mConfigurationService1;

    private NgnSKClient() {
        mConfigurationService = NgnEngine.getInstance()
                .getConfigurationService();
        sipService = NgnEngine.getInstance().getSipService();
    }

    public static synchronized NgnSKClient getInstence() {
        if (ngnSKClient == null) {
            return new NgnSKClient();
        } else {
            return ngnSKClient;
        }
    }

    /**
     * 小数点后保留两位
     *
     * @param value
     * @return
     */

    public static String format2(float value) {
        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(value);
    }

    /**
     * 配置服务器地址
     *
     * @param domainName     服务器域名
     * @param GroupServerurl 群组服务器地址（ip:port）
     * @param fileServer     文件服务器地址（ip:port）
     * @param ProxyHost      CSCF代理（Proxy-CSCF）主机
     * @param ProxyPort      CSCF代理（Proxy-CSCF）端口号
     */


    public void configIP(String domainName, String GroupServerurl,
                         String fileServer, String ProxyHost, String ProxyPort) {
        mConfigurationService1 = NgnEngine
                .getInstance().getConfigurationService();
        mConfigurationService1.putString(NgnConfigurationEntry.NETWORK_REALM,
                domainName);

        String groupRealm = "";
        String groupPort = "";
        if (GroupServerurl.contains(":")) {
            groupRealm = GroupServerurl.split(":")[0];
            groupPort = GroupServerurl.split(":")[1];
        }

        mConfigurationService1.putString(
                NgnConfigurationEntry.NETWORK_GROUP_REALM, groupRealm.trim());

        mConfigurationService1.putString(
                NgnConfigurationEntry.NETWORK_GROUP_PORT, groupPort.trim());

        mConfigurationService1.putString(NgnConfigurationEntry.FILE_SERVER_URL,
                fileServer);
        mConfigurationService1.putString(
                NgnConfigurationEntry.NETWORK_PCSCF_HOST, ProxyHost);
        mConfigurationService1.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
                NgnStringUtils.parseInt(ProxyPort,
                        NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));

        if (!mConfigurationService1.commit()) {
            Log.e("configIP", "Failed to Commit() configuration");
        } else {
            MediaSessionMgr.defaultsSetStunEnabled(mConfigurationService1
                    .getBoolean(NgnConfigurationEntry.NATT_USE_STUN,
                            NgnConfigurationEntry.DEFAULT_NATT_USE_STUN));

        }
    }

    /**
     * 登录
     * <p>
     * ok
     *
     * @param account  用户账号
     * @param password 用户密码
     * @param
     */
    public void login(final String account, final String password) {
        mConfigurationService.putString(
                NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, account.trim());
        mConfigurationService.putString(
                NgnConfigurationEntry.IDENTITY_PASSWORD, password.trim());
        mConfigurationService.putString(
                NgnConfigurationEntry.IDENTITY_IMPU,
                "sip:"
                        + account.trim()
                        + "@"
                        + mConfigurationService.getString(
                        NgnConfigurationEntry.NETWORK_REALM,
                        NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
                account.trim());
        if (!mConfigurationService.commit()) {

        }
        Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        boolean register = sipService.register(NgnSKDroid
                                .getInstence().getGlobleContext());
                        subscriber.onNext(register);
                        MyLog.d(TAG, "Thread.currentThread().getName()--->"
                                + Thread.currentThread().getName());
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        SubscribeHelper.getInstance().subscribeGlobalGroup();
                        SubscribeHelper.getInstance().subscribeServiceGroup();
                        SubscribeHelper.getInstance().subscribePersionInfo();
                        SubscribeHelper.getInstance().subscribeSubscribeGroup();
                        SubscribeHelper.getInstance().subscribePublicGroup();
                        MyLog.d(TAG, "登录结果：	" + result);
                        MyLog.d(TAG, "Thread.currentThread().getName()--->"
                                + Thread.currentThread().getName());
                    }
                });

    }

    /**
     * 退出
     * <p>
     * ok
     */

    public void logout() {
        boolean unRegister = NgnEngine.getInstance().getSipService()
                .unRegister();
        if (unRegister) {
            GlobalVar.mLogout = true;
            MyLog.d(TAG, "注销结果：	" + unRegister);
        }
    }

    /**
     * 发送文本消息
     * <p>
     * ok
     *
     * @param toAccount 接收端号码
     * @param content   文本消息内容
     * @param isGroup   标记是否发送群组消息 true群组消息,false非群组消息
     * @return msgId 发送消息的id
     */
    public String sendTextMessage(String toAccount, String content,
                                  boolean isGroup) {
        INgnSipService mSipService = NgnEngine.getInstance().getSipService();
        final String remotePartyUri = NgnUriUtils.makeValidSipUri(toAccount);
        final NgnMessagingSession imSession = NgnMessagingSession
                .createOutgoingSession(mSipService.getSipStack(),
                        remotePartyUri);
        String localMsgID = "UE" + java.util.UUID.randomUUID().toString();
        String mes = ctreateExpandedField(toAccount, localMsgID, isGroup)
                + "\n\n" + content;
        if (!(imSession.sendExTextMessage(mes))) {
        }
        NgnMessagingSession.releaseSession(imSession);
        return localMsgID;
    }

    private String ctreateExpandedField(String toAccount, String localMsgID,
                                        boolean isGroup) {
        String nameSpace = "MsgExt<http://www.message.com/msgExtensions/>";
        String msgType = "IM";
        if (isGroup) {
            msgType = "GM" + toAccount;
        }
        String msgReport = "Yes";
        String contentType = "text/plain";
        MessageBodyInfo msgBodyInfo = new MessageBodyInfo(nameSpace, msgType,
                msgReport, localMsgID, contentType);
        String strBody = msgBodyInfo.toString();
        return strBody;
    }

    /**
     * 获取业务能力
     * <p>
     * ok
     */
    public void getServiceAbilities() {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getServiceAbilities(),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String serviceAbilities) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "serviceAbilities---->" + serviceAbilities);
                        CallBackHelper
                                .exuteOnServiceAbilities(serviceAbilities);
                    }
                });
    }

    /**
     * 获取组类型 ok
     */
    public void getGroupTypes() {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getGroupTypes(),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String groupTypes) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "groupTypes---->" + groupTypes);
                        CallBackHelper.exuteOnServiceAbilities(groupTypes);
                    }
                });
    }

    /**
     * 获取组的使用类型
     * <p>
     * ok
     */
    public void getUseTypes() {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getUseTypes(),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String useTypes) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "useTypes---->" + useTypes);
                        CallBackHelper.exuteOnUserTypes(useTypes);
                    }
                });
    }

    /**
     * 查询开户信息
     *
     * @param keyword 关键字 可以是 uri 可以是name 可以使 nikename
     *                <p>
     *                ok
     */
    public void getUserInfor(String keyword) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getUserInfor(keyword),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String userInfor) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "userInfor---->" + userInfor);
                        CallBackHelper.exuteOnUserInfor(userInfor);
                    }
                });
    }

    /**
     * 组查询
     *
     * @param groupNo 组号
     *                <p>
     *                ok
     */
    public void getGroupInfor(String groupNo) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getGroupInfor(groupNo),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String groupInfor) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "groupInfor---->" + groupInfor);
                        CallBackHelper.exuteOnGroupInfor(groupInfor);
                    }
                });
    }

    /**
     * 获取好友列表
     *
     * @param sipUri 用户的sip账号
     *               <p>
     *               ok
     */
    public void getContactList(String sipUri) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getContactList(sipUri),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String contactList) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "contactList---->" + contactList);
                        CallBackHelper.exuteOnContactList(contactList);
                    }
                });
    }

    /**
     * 获取所有组包括组内成员
     *
     * @param uri 用户的sip账号
     *            <p>
     *            ok
     */
    public void getAllGroup(String uri) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getAllGroup(uri),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String groupList) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "groupList---->" + groupList);
                        CallBackHelper.exuteOnGroupList(groupList);
                    }
                });
    }

    /**
     * 创建群组
     *
     * @param body 提交的json数据
     */
    public void createGroup(String body) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).createGroup(body),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "createGroupresult---->" + result);
                        CallBackHelper.exuteOnCreateGroup(result);
                    }
                });
    }

    /**
     * 更新群组
     *
     * @param body
     */
    public void updateGroup(String body) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).updateGroup(body),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "updateGroupresult---->" + result);
                        CallBackHelper.exuteOnUpdateGroup(result);
                    }
                });
    }

    /**
     * 删除群组
     *
     * @param groupNo
     */
    public void deleteGroup(String groupNo) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).deleteGroup(groupNo),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "delGroupresult---->" + result);
                        CallBackHelper.exuteOnDeleteGroup(result);
                    }
                });
    }

    /**
     * 根据组号获取组成员
     * <p>
     * ok
     *
     * @param groupNo
     */
    public void getGroupMem(String groupNo) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).getGroupMem(groupNo),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "getGroupMemresult---->" + result);
                        CallBackHelper.exuteOnGetGroupMem(result);
                    }
                });
    }

    /**
     * 根据组号添加组成员
     *
     * @param groupNo
     * @param body
     */
    public void addGroupMem(String groupNo, String body) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).addGroupMem(groupNo, body),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "addGroupMemMemresult---->" + result);
                        CallBackHelper.exuteOnAddGroupMem(result);
                    }
                });
    }

    /**
     * 根据组号删除组成成员
     *
     * @param groupNo
     * @param params
     */
    public void delGroupMem(String groupNo, String params) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).delGroupMem(groupNo, params),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "delGroupMemresult---->" + result);
                        CallBackHelper.exuteOnDelGroupMem(result);
                    }
                });
    }

    /**
     * 添加好友
     *
     * @param myUri
     * @param friendUri ok
     */
    public void addContact(String myUri, String friendUri) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).addContact(myUri, friendUri),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "addContactresult---->" + result);
                        CallBackHelper.exuteOnAddContact(result);
                    }
                });
    }

    /**
     * 更新好友
     *
     * @param myUri
     * @param friendUri
     * @param body
     */
    public void updateContact(String myUri, String friendUri, String body) {
        HttpClient.subscribeDefault(
                HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class)
                        .updateContact(myUri, friendUri, body),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "updateContactresult---->" + result);
                        CallBackHelper.exuteOnUpdateContact(result);
                    }
                });
    }

    /**
     * 删除好友
     *
     * @param myUri
     * @param friendUri ok
     */
    public void delContact(String myUri, String friendUri) {
        HttpClient.subscribeDefault(HttpClient.getHttpClient().getRetrofit()
                        .create(ApiService.class).delContact(myUri, friendUri),
                new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "delContactresult---->" + result);
                        CallBackHelper.exuteOnDelContact(result);
                    }
                });
    }

    /**
     * 文件下载
     *
     * @param uri               下载路径
     * @param savePath          保存路径
     * @param fileName          保存的文件名
     * @param downloadLListener 下载监听
     */
    public void downLoadFile(String uri, final String savePath,
                             final String fileName, final DownloadLListener downloadLListener) {
        HttpClient.getHttpClient().getRetrofit().create(ApiService.class)
                .downloadFile(uri).enqueue(new Callback<ResponseBody>() {
            private OutputStream out;
            private BufferedOutputStream bos;

            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                // TODO Auto-generated method stub
                MyLog.d("ywh", "开始下载文件");
                File file = new File(savePath + "/" + fileName);
                InputStream inputStream = response.body().byteStream();
                long contentLength = response.body().contentLength();
                int downloadLength = 0;
                BufferedInputStream bis = new BufferedInputStream(
                        inputStream);
                try {
                    out = new FileOutputStream(file);
                    bos = new BufferedOutputStream(out);
                    byte[] bys = new byte[1024 * 8];
                    int len = 0;

                    while ((len = bis.read(bys)) != -1) {
                        out.write(bys, 0, len);
                        out.flush();
                        downloadLength += len;
                        float persent1 = (float) downloadLength / (float) contentLength * 100;
                        String progress = format2(persent1);
                        downloadLListener.downloadProgress(progress);
                        MyLog.d("ywh", "下载进度" + progress);
                        if (downloadLength == contentLength) {
                            downloadLListener.downloadSuccess();
                        }
                    }
                    inputStream.close();
                    bis.close();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    downloadLListener.downloadFailed(e);
                } catch (IOException e) {
                    // TODO: handle exception
                    downloadLListener.downloadFailed(e);
                } finally {
                    try {
                        if (bis != null) {
                            bis.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        if (bos != null) {
                            bos.close();
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call,
                                  Throwable throwable) {
                // TODO Auto-generated method stub

                downloadLListener.downloadFailed(throwable);

            }
        });

    }

    /**
     * 文件发送
     *
     * @param filePath  文件路径
     * @param myUri     自己的登陆账号
     * @param remoteUri 对端的登陆账号
     * @param isgroup   是否是群组
     */
    public void sendFileMsg(String filePath, String myUri, String remoteUri,
                            boolean isgroup, final UpLoadFileListener loadFileListener) {
        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(
                MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file",
                file.getName(), new FileRequestBody<String>(requestFile,
                        new RetrofitCallback<String>() {
                            @Override
                            public void onFailure(retrofit2.Call<String> arg0,
                                                  Throwable throwable) {
                                // TODO Auto-generated method stub
                                loadFileListener.onFailure(throwable);
                            }

                            @Override
                            public void onSuccess(retrofit2.Call<String> call,
                                                  Response<String> response) {
                                MyLog.d(TAG, "文件发送成功");
                            }
                            @Override
                            public void onLoading(long total, long progress) {
                                // TODO Auto-generated method stub
                                super.onLoading(total, progress);
                                float persent = (float) progress / (float) total * 100;
                                format2(persent);
                                MyLog.d(TAG, "progress--->" + format2(persent));
                                loadFileListener.onProgress(format2(persent));
                                if (progress == total) {
                                    loadFileListener.onSuccess();
                                }
                            }
                        }));
        HttpClient.subscribeDefault(
                HttpClient
                        .getHttpClient()
                        .getRetrofit()
                        .create(ApiService.class)
                        .sendFileMsg(file.getName(), myUri, remoteUri, isgroup,
                                part), new DefaultSubscriber<String>() {
                    @Override
                    public void onNext(String result) {
                        // TODO Auto-generated method stub
                        MyLog.d(TAG, "sendFileMsg---->" + result);

                    }
                });

        // 192.168.1.10:8010/files/5bffa030-9718-4f4d-b88f-9068bdb123d1
        // 192.168.1.10:8010/files/0b258a24-f6eb-4697-8ecd-ca1802497ae4

    }

    /**
     * 获取群组服务器的信息
     *
     * @return
     */
    public String getGroupServerInfor() {
        String port = mConfigurationService.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT, "1000");
        return mConfigurationService.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM, DEFALT_GROUP_SERVER) + ":" + port;
    }

    /***
     * 获取文件服务器的信息
     */
    public String getFileServerInfor() {
        return mConfigurationService.getString(NgnConfigurationEntry.FILE_SERVER_URL, DEFALT_FILESERVER);
    }

    /**
     * 获取主机地址
     *
     * @return
     */
    public String getPcscfHost() {
        return mConfigurationService.getString(NgnConfigurationEntry.NETWORK_PCSCF_HOST, DEFALT_DOMAIN);

    }

    /**
     * 获取主机端口
     *
     * @return
     */
    public int getPcscfPort() {
        return mConfigurationService.getInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT, Integer.parseInt(DEFALT_INFOR));
    }

    /**
     * 获取自己的登陆账号
     *
     * @return
     */
    public String getMyselfDisPlayName() {
        return mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, DEFALT_INFOR);
    }

    /**
     * 获取域名信息
     *
     * @return
     */
    public String getDomainInfor() {
        return mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM, DEFALT_DOMAIN);
    }

    /**
     * 获取登录状态
     *
     * @return
     */
    public boolean getLoginState() {
        boolean registeSessionConnected = NgnEngine.getInstance()
                .getSipService().isRegisteSessionConnected();
        return registeSessionConnected;
    }

}
