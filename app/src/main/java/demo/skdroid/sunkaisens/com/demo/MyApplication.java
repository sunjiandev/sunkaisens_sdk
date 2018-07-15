package demo.skdroid.sunkaisens.com.demo;

import android.app.Application;
import android.content.Context;

import com.sunkaisens.skdroid.sdk.NgnSKDroid;
import com.sunkaisens.skdroid.sdk.agent.CallBackHelper;
import com.sunkaisens.skdroid.sdk.agent.NgnSKClient;

import org.doubango.utils.MyLog;

import demo.skdroid.sunkaisens.com.demo.callback.MsgCallBackListener;

/**
 * 作者:sjy
 * 邮箱:sunjianyun@sunkaisens.com
 * 时间:2018/1/31 12:14
 */

public class MyApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        NgnSKDroid.init(context);//初始化sdk

        CallBackHelper.initCallBack(new MsgCallBackListener());//设置回调监听


        String fileServerInfor = NgnSKClient.getInstence().getFileServerInfor();
        String domainInfor = NgnSKClient.getInstence().getDomainInfor();
        String groupServerInfor = NgnSKClient.getInstence().getGroupServerInfor();
        String pcscfHost = NgnSKClient.getInstence().getPcscfHost();
        int pcscfPort = NgnSKClient.getInstence().getPcscfPort();


        MyLog.d("app", ">>>fileServerInfor<<<<<" + fileServerInfor + ">>>domainInfor>>" + domainInfor + ">>>groupServerInfor>>>"
                + groupServerInfor + ">>>pcscfHost>>>" + pcscfHost + ">>>pcscfPort>>>" + pcscfPort);

        /**
         * 配置服务器地址
         *
         * @param String
         *            domainName 服务器域名
         * @param String
         *            GroupServerurl 群组服务器地址（ip:port）
         * @param String
         *            fileServer 文件服务器地址（ip:port）
         * @param String
         *            ProxyHost CSCF代理（Proxy-CSCF）主机
         * @param String
         *            ProxyPort CSCF代理（Proxy-CSCF）端口号
         * @param aorIp
         *            并发时的对外ip
         */
        NgnSKClient.getInstence().configIP(domainInfor, groupServerInfor,
                fileServerInfor, pcscfHost, pcscfPort + "");//配置服务器信息
    }
}
