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
 */
package org.doubango.ngn.services.impl;

import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.sunkaisens.skdroid.cpim.CPIMMessage;
import com.sunkaisens.skdroid.cpim.CPIMParser;
import com.sunkaisens.skdroid.sdk.NgnMediaManager;
import com.sunkaisens.skdroid.sdk.NgnSKDroid;
import com.sunkaisens.skdroid.sdk.agent.CallBackHelper;
import com.sunkaisens.skdroid.sdk.agent.NgnSipCallBackHelper;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;
import com.sunkaisens.skdroid.util.HexDump;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMessagingEventTypes;
import org.doubango.ngn.events.NgnPublicationEventArgs;
import org.doubango.ngn.events.NgnPublicationEventTypes;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventTypes;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.media.NgnProxyPluginMgr;
import org.doubango.ngn.model.NgnDeviceInfo.Orientation;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnNetworkService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.services.impl.NgnNetworkService.DNS_TYPE;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.sip.NgnMessagingSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.sip.NgnPresenceStatus;
import org.doubango.ngn.sip.NgnPublicationSession;
import org.doubango.ngn.sip.NgnRegistrationSession;
import org.doubango.ngn.sip.NgnSipPrefrences;
import org.doubango.ngn.sip.NgnSipSession;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.sip.NgnSipStack;
import org.doubango.ngn.sip.NgnSipStack.STACK_STATE;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.sip.NgnSubscriptionSession.EventPackageType;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.CallSession;
import org.doubango.tinyWRAP.DDebugCallback;
import org.doubango.tinyWRAP.DialogEvent;
import org.doubango.tinyWRAP.InviteEvent;
import org.doubango.tinyWRAP.InviteSession;
import org.doubango.tinyWRAP.MessagingEvent;
import org.doubango.tinyWRAP.MessagingSession;
import org.doubango.tinyWRAP.MsrpSession;
import org.doubango.tinyWRAP.OptionsEvent;
import org.doubango.tinyWRAP.OptionsSession;
import org.doubango.tinyWRAP.RPMessage;
import org.doubango.tinyWRAP.RegistrationEvent;
import org.doubango.tinyWRAP.SMSData;
import org.doubango.tinyWRAP.SMSEncoder;
import org.doubango.tinyWRAP.SdpMessage;
import org.doubango.tinyWRAP.SipCallback;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.tinyWRAP.SipSession;
import org.doubango.tinyWRAP.SipStack;
import org.doubango.tinyWRAP.StackEvent;
import org.doubango.tinyWRAP.SubscriptionEvent;
import org.doubango.tinyWRAP.SubscriptionSession;
import org.doubango.tinyWRAP.tinyWRAPConstants;
import org.doubango.tinyWRAP.tsip_invite_event_type_t;
import org.doubango.tinyWRAP.tsip_message_event_type_t;
import org.doubango.tinyWRAP.tsip_options_event_type_t;
import org.doubango.tinyWRAP.tsip_request_type_t;
import org.doubango.tinyWRAP.tsip_subscribe_event_type_t;
import org.doubango.tinyWRAP.twrap_media_type_t;
import org.doubango.tinyWRAP.twrap_sms_type_t;
import org.doubango.utils.MyLog;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public class NgnSipService extends NgnBaseService implements INgnSipService,
        tinyWRAPConstants {
    private final static String TAG = NgnSipService.class.getCanonicalName();
    public static String aorIP = "";
    // private final INgnConfigurationService mConfigurationService;
    private static INgnConfigurationService mConfigurationService;
    private final DDebugCallback mDebugCallback;
    private final MySipCallback mSipCallback;
    private final NgnSipPrefrences mPreferences;
    private final INgnNetworkService mNetworkService;
    private NgnRegistrationSession mRegSession;
    private NgnSipStack mSipStack;
    private ConditionVariable mCondHackAoR;
    // 3145824閿熸枻鎷烽敓鏂ゆ嫹閫夐敓鏂ゆ嫹PCMA+PCMU+H264MP+H264BP
    // 1048672閿熸枻鎷烽敓鏂ゆ嫹閫夐敓鏂ゆ嫹PCMA+PCMU+H264BP
    // 2097248閿熸枻鎷烽敓鏂ゆ嫹閫夐敓鏂ゆ嫹PCMA+PCMU+H264MP gzc 2097248
    // 1065056閿熸枻鎷烽敓鏂ゆ嫹PCMA+PCMU+G729+H264BP
    // 1064960閿熸枻鎷烽敓鏂ゆ嫹G729+H264BP
    private int codes = 1048672;// gzc 閿熺Ц绔唻鎷烽敓鏂ゆ嫹閿燂拷

    public NgnSipService() {
        super();

        mDebugCallback = new DDebugCallback();
        mSipCallback = new MySipCallback(this);
        mPreferences = new NgnSipPrefrences();
        mConfigurationService = NgnEngine.getInstance()
                .getConfigurationService();
        mNetworkService = NgnEngine.getInstance().getNetworkService();
    }

    public static void setAOR(String ip) {
        aorIP = ip;
    }

    @Override
    public boolean start() {
        MyLog.d(TAG, "starting...");
        return true;
    }

    @Override
    public boolean stop() {
        MyLog.d(TAG, "stopping...");
        if (mSipStack != null && mSipStack.getState() == STACK_STATE.STARTED) {
            mSipStack.stop();
            mSipStack = null;
            mRegSession = null;
        }
        return true;
    }

    @Override
    public String getDefaultIdentity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDefaultIdentity(String identity) {
        // TODO Auto-generated method stub

    }

    @Override
    public NgnSipStack getSipStack() {
        return mSipStack;
    }

    /**
     * 閿熺煫浼欐嫹閿熻鍑ゆ嫹娉ㄩ敓鏂ゆ嫹鏅掗敓锟?
     */
    @Override
    public boolean isRegisteSessionConnected() {
        if (GlobalVar.bADHocMode) {
            return true;
        } else {
            if (mRegSession != null) {
                return mRegSession.isConnected();
            }
            return false;
        }
    }

    @Override
    public ConnectionState getRegistrationState() {
        if (mRegSession != null) {
            return mRegSession.getConnectionState();
        }
        return ConnectionState.NONE;
    }

    @Override
    public boolean isXcapEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPublicationEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSubscriptionEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSubscriptionToRLSEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getCodecs() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCodecs(int coddecs) {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] getSubRLSContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getSubRegContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getSubMwiContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getSubWinfoContent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean stopStack() {
        if (mSipStack != null) {
            mSipStack.stop();
            mRegSession = null;
            mSipStack = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean ADHOC_Stop() {
        // if (isRegistered()) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mSipStack != null)
                    mSipStack.stop();
                closeSipStack();
            }
        }).start();
        // }
        return true;
    }

    // Set Proxy-CSCF
    @Override
    public boolean ADHOC_SetPcscfHost(String hostip) {
        if (mSipStack == null) {

            Log.e(TAG, "sipstack is null!!");
            return false;
        } else {
            if (!mSipStack.setProxyCSCF(hostip, mPreferences.getPcscfPort(),
                    mPreferences.getTransport(), mPreferences.getIPVersion())) {
                Log.e(NgnSipService.TAG, "Failed to set Proxy-CSCF parameters");
                return false;
            } else
                return true;
        }
    }

    @Override
    public boolean ADHOC_Start(Context context) {
        MyLog.d(TAG, "ADHOC_Start()");
        mPreferences.setRealm(mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_REALM,
                NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
        mPreferences.setIMPI(mConfigurationService.getString(
                NgnConfigurationEntry.IDENTITY_IMPI,
                NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
        mPreferences.setIMPU(mConfigurationService.getString(
                NgnConfigurationEntry.IDENTITY_IMPU,
                NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));

        MyLog.d(TAG, String.format("realm='%s', impu='%s', impi='%s'",
                mPreferences.getRealm(), mPreferences.getIMPU(),
                mPreferences.getIMPI()));

        // mConfigurationService.putInt(NgnConfigurationEntry.MEDIA_CODECS,
        // codes);

        if (mSipStack == null) {
            mSipStack = new NgnSipStack(mSipCallback, mPreferences.getRealm(),
                    mPreferences.getIMPI(), mPreferences.getIMPU());
            mSipStack.setDebugCallback(mDebugCallback);
            SipStack.setCodecs_2(mConfigurationService.getInt(
                    NgnConfigurationEntry.MEDIA_CODECS,
                    NgnConfigurationEntry.DEFAULT_MEDIA_CODECS));
            // SipStack.setCodecs_2(codes);
            // SipStack.setCodecs(tdav_codec_id_t.tdav_codec_id_pcma);
        } else {
            if (!mSipStack.setRealm(mPreferences.getRealm())) {
                Log.e(TAG, "Failed to set realm");
                return false;
            }
            if (!mSipStack.setIMPI(mPreferences.getIMPI())) {
                Log.e(TAG, "Failed to set IMPI");
                return false;
            }
            if (!mSipStack.setIMPU(mPreferences.getIMPU())) {
                Log.e(TAG, "Failed to set IMPU");
                return false;
            }
        }

        // set the Password
        mSipStack.setPassword(mConfigurationService.getString(
                NgnConfigurationEntry.IDENTITY_PASSWORD,
                NgnConfigurationEntry.DEFAULT_IDENTITY_PASSWORD));
        // Set AMF
        mSipStack.setAMF(mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_IMSAKA_AMF,
                NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_AMF));
        // Set Operator Id
        mSipStack.setOperatorId(mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_IMSAKA_OPID,
                NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_OPID));

        // Check stack validity
        if (!mSipStack.isValid()) {
            Log.e(TAG, "Trying to use invalid stack");
            return false;
        }

        // Set STUN information
        if (mConfigurationService.getBoolean(
                NgnConfigurationEntry.NATT_USE_STUN,
                NgnConfigurationEntry.DEFAULT_NATT_USE_STUN)) {
            MyLog.d(TAG, "STUN=yes");
            mSipStack.setSTUNEnabled(true);
            if (mConfigurationService.getBoolean(
                    NgnConfigurationEntry.NATT_STUN_DISCO,
                    NgnConfigurationEntry.DEFAULT_NATT_STUN_DISCO)) {
                final String realm = mPreferences.getRealm();
                String domain = realm.substring(realm.indexOf(':') + 1);
                int[] port = new int[1];
                String server = mSipStack.dnsSrv(
                        String.format("_stun._udp.%s", domain), port);
                if (server == null) {
                    Log.e(TAG, "STUN discovery has failed");
                }
                MyLog.d(TAG, String.format("STUN1 - server=%s and port=%d",
                        server, port[0]));
                mSipStack.setSTUNServer(server, port[0]);// Needed event if null
            } else {
                String server = mConfigurationService.getString(
                        NgnConfigurationEntry.NATT_STUN_SERVER,
                        NgnConfigurationEntry.DEFAULT_NATT_STUN_SERVER);
                int port = mConfigurationService.getInt(
                        NgnConfigurationEntry.NATT_STUN_PORT,
                        NgnConfigurationEntry.DEFAULT_NATT_STUN_PORT);
                MyLog.d(NgnSipService.TAG, String.format(
                        "STUN2 - server=%s and port=%d", server, port));
                mSipStack.setSTUNServer(server, port);
            }
        } else {
            MyLog.d(TAG, "STUN=no");
            mSipStack.setSTUNEnabled(false);
        }

        // Set Proxy-CSCF
        mPreferences.setPcscfHost("170.4.1.3"); // null will
        // DNS
        // NAPTR+SRV
        // mPreferences.setPcscfHost(mConfigurationService.getString(
        // NgnConfigurationEntry.NETWORK_PCSCF_HOST,
        // NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST)); // null will
        mPreferences.setPcscfPort(mConfigurationService.getInt(
                NgnConfigurationEntry.NETWORK_PCSCF_PORT,
                NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
        mPreferences.setTransport(mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_TRANSPORT,
                NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT));
        mPreferences.setIPVersion(mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_IP_VERSION,
                NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION));

        MyLog.d(TAG,
                String.format(
                        "pcscf-host='%s', pcscf-port='%d', transport='%s', ipversion='%s'",
                        mPreferences.getPcscfHost(),
                        mPreferences.getPcscfPort(),
                        mPreferences.getTransport(),
                        mPreferences.getIPVersion()));

        if (!mSipStack.setProxyCSCF(mPreferences.getPcscfHost(),
                mPreferences.getPcscfPort(), mPreferences.getTransport(),
                mPreferences.getIPVersion())) {
            Log.e(NgnSipService.TAG, "Failed to set Proxy-CSCF parameters");
            return false;
        }

        // Set local IP (If your reusing this code on non-Android platforms
        // (iOS, Symbian, WinPhone, ...),
        // let Doubango retrieve the best IP address)
        boolean ipv6 = NgnStringUtils.equals(mPreferences.getIPVersion(),
                "ipv6", true);
        mPreferences.setLocalIP(mNetworkService.getLocalIP(ipv6));
        if (mPreferences.getLocalIP() == null) {
            // if(fromNetworkService){
            // this.preferences.localIP = ipv6 ? "::" : "10.0.2.15"; /* Probably
            // on the emulator */
            // }
            // else{
            // Log.e(TAG, "IP address is Null. Trying to start network");
            // this.networkService.setNetworkEnabledAndRegister();
            // return false;
            // }
        }
        if (!mSipStack.setLocalIP(mPreferences.getLocalIP())) {
            Log.e(TAG, "Failed to set the local IP");
            return false;
        }
        if (!mSipStack.setLocalPort(mPreferences.getPcscfPort())) {
            Log.e(TAG, "Failed to set the local port");
            return false;
        }
        //
        MyLog.d(TAG, String.format("Local IP='%s'", mPreferences.getLocalIP()));

		/*
         * if(GlobalSession.bSocketService == true){ if(!checkMACADDR()){
		 * Log.e(TAG, "Failed to setup on this MAC: " +
		 * mNetworkService.getLocalMacAddressFromIp(mPreferences.getLocalIP()));
		 * return false; } }
		 */

        // Whether to use DNS NAPTR+SRV for the Proxy-CSCF discovery (even if
        // the DNS requests are sent only when the stack starts,
        // should be done after setProxyCSCF())
        String discoverType = mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
                NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY);
        // mSipStack.setDnsDiscovery(NgnStringUtils.equals(discoverType,
        // NgnConfigurationEntry.PCSCF_DISCOVERY_DNS_SRV, true));
        mSipStack.setDnsDiscovery(false);

        // enable/disable 3GPP early IMS
        mSipStack.setEarlyIMS(mConfigurationService.getBoolean(
                NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
                NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));

        // SigComp (only update compartment Id if changed)
        if (mConfigurationService.getBoolean(
                NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
                NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP)) {
            String compId = String.format("urn:uuid:%s", UUID.randomUUID()
                    .toString());
            mSipStack.setSigCompId(compId);
        } else {
            mSipStack.setSigCompId(null);
        }

        // TLS
        final String pvFilePath = mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_TLS_PRIVKEY_FILE_PATH,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_PRIVKEY_FILE_PATH);
        final String pbFilePath = mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_TLS_PUBKEY_FILE_PATH,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_PUBKEY_FILE_PATH);
        final String caFilePath = mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_TLS_CA_FILE_PATH,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_CA_FILE_PATH);
        final boolean verifyCerts = mConfigurationService.getBoolean(
                NgnConfigurationEntry.SECURITY_TLS_VERIFY_CERTS,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_VERIFY_CERTS);
        MyLog.d(TAG, String.format("TLS - pvk='%s' pbk='%s' ca='%s' verify=%s",
                pvFilePath, pbFilePath, caFilePath, verifyCerts));
        if (!mSipStack.setSSLCertificates(pvFilePath, pbFilePath, caFilePath,
                verifyCerts)) {
            Log.e(TAG, "Failed to set TLS certificates");
            return false;
        }

        // Start the Stack
        if (!mSipStack.start()) {
            if (context != null
                    && Thread.currentThread() == Looper.getMainLooper()
                    .getThread()) {
                Toast.makeText(context, "Failed to start the SIP stack",
                        Toast.LENGTH_LONG).show();
            }
            closeSipStack();
            Log.e(TAG, "Failed to start the SIP stack");
            return false;
        }

        // Preference values
        mPreferences.setXcapEnabled(mConfigurationService.getBoolean(
                NgnConfigurationEntry.XCAP_ENABLED,
                NgnConfigurationEntry.DEFAULT_XCAP_ENABLED));
        mPreferences.setPresenceEnabled(mConfigurationService.getBoolean(
                NgnConfigurationEntry.RCS_USE_PRESENCE,
                NgnConfigurationEntry.DEFAULT_RCS_USE_PRESENCE));
        mPreferences.setMWI(mConfigurationService.getBoolean(
                NgnConfigurationEntry.RCS_USE_MWI,
                NgnConfigurationEntry.DEFAULT_RCS_USE_MWI));

		/* Before registering, check if AoR hacking id enabled */
        mPreferences.setHackAoR(mConfigurationService.getBoolean(
                NgnConfigurationEntry.NATT_HACK_AOR,
                NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR));
        if (mPreferences.isHackAoR()) {
            if (mCondHackAoR == null) {
                mCondHackAoR = new ConditionVariable();
            }
            final OptionsSession optSession = new OptionsSession(mSipStack);
            // optSession.setToUri(String.format("sip:%s@%s", "hacking_the_aor",
            // this.preferences.realm));
            // optSession.send();
            try {
                synchronized (mCondHackAoR) {
                    mCondHackAoR
                            .wait(mConfigurationService
                                    .getInt(NgnConfigurationEntry.NATT_HACK_AOR_TIMEOUT,
                                            NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR_TIMEOUT));
                }
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
            mCondHackAoR = null;
            optSession.delete();
        }

        // if (!mRegSession.register()) {
        // Log.e(TAG, "Failed to send REGISTER request");
        // return false;
        // }
        return true;
    }

    @Override
    public boolean register(Context context) {
        MyLog.d(TAG, "register()");
        GlobalVar.mLogout = false;
        mPreferences.setRealm(mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_REALM,
                NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
        mPreferences.setIMPI(mConfigurationService.getString(
                NgnConfigurationEntry.IDENTITY_IMPI,
                NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
        mPreferences.setIMPU(mConfigurationService.getString(
                NgnConfigurationEntry.IDENTITY_IMPU,
                NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));

        MyLog.d(TAG, String.format("realm='%s', impu='%s', impi='%s'",
                mPreferences.getRealm(), mPreferences.getIMPU(),
                mPreferences.getIMPI()));

        // mConfigurationService.putInt(NgnConfigurationEntry.MEDIA_CODECS,
        // codes);

        if (mSipStack == null) {
            MyLog.d(TAG, "SipStack is null,i will create it.");
            mSipStack = new NgnSipStack(mSipCallback, mPreferences.getRealm(),
                    mPreferences.getIMPI(), mPreferences.getIMPU());
            mSipStack.setDebugCallback(mDebugCallback);
            SipStack.setCodecs_2(mConfigurationService.getInt(
                    NgnConfigurationEntry.MEDIA_CODECS,
                    NgnConfigurationEntry.DEFAULT_MEDIA_CODECS));
            // SipStack.setCodecs_2(codes);
        } else {
            MyLog.d(TAG, "SipStack is not null.");
            if (GlobalSession.bSocketService) {
                MyLog.d(TAG, "Is Socket Service.Exit");
                System.exit(0);
                return false;
            }
            if (!mSipStack.setRealm(mPreferences.getRealm())) {
                Log.e(TAG, "Failed to set realm");
                return false;
            }
            if (!mSipStack.setIMPI(mPreferences.getIMPI())) {
                Log.e(TAG, "Failed to set IMPI");
                return false;
            }
            if (!mSipStack.setIMPU(mPreferences.getIMPU())) {
                Log.e(TAG, "Failed to set IMPU");
                return false;
            }
        }

        // set the Password
        mSipStack.setPassword(mConfigurationService.getString(
                NgnConfigurationEntry.IDENTITY_PASSWORD,
                NgnConfigurationEntry.DEFAULT_IDENTITY_PASSWORD));
        // Set AMF
        mSipStack.setAMF(mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_IMSAKA_AMF,
                NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_AMF));
        // Set Operator Id
        mSipStack.setOperatorId(mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_IMSAKA_OPID,
                NgnConfigurationEntry.DEFAULT_SECURITY_IMSAKA_OPID));

        // Check stack validity
        if (!mSipStack.isValid()) {
            Log.e(TAG, "Trying to use invalid stack");
            return false;
        }

        // Set STUN information
        if (mConfigurationService.getBoolean(
                NgnConfigurationEntry.NATT_USE_STUN,
                NgnConfigurationEntry.DEFAULT_NATT_USE_STUN)) {
            MyLog.d(TAG, "STUN=yes");
            mSipStack.setSTUNEnabled(true);
            if (mConfigurationService.getBoolean(
                    NgnConfigurationEntry.NATT_STUN_DISCO,
                    NgnConfigurationEntry.DEFAULT_NATT_STUN_DISCO)) {
                final String realm = mPreferences.getRealm();
                String domain = realm.substring(realm.indexOf(':') + 1);
                int[] port = new int[1];
                String server = mSipStack.dnsSrv(
                        String.format("_stun._udp.%s", domain), port);
                if (server == null) {
                    Log.e(TAG, "STUN discovery has failed");
                }
                MyLog.d(TAG, String.format("STUN1 - server=%s and port=%d",
                        server, port[0]));
                mSipStack.setSTUNServer(server, port[0]);// Needed event if null
            } else {
                String server = mConfigurationService.getString(
                        NgnConfigurationEntry.NATT_STUN_SERVER,
                        NgnConfigurationEntry.DEFAULT_NATT_STUN_SERVER);
                int port = mConfigurationService.getInt(
                        NgnConfigurationEntry.NATT_STUN_PORT,
                        NgnConfigurationEntry.DEFAULT_NATT_STUN_PORT);
                MyLog.d(NgnSipService.TAG, String.format(
                        "STUN2 - server=%s and port=%d", server, port));
                mSipStack.setSTUNServer(server, port);
            }
        } else {
            MyLog.d(TAG, "STUN=no");
            mSipStack.setSTUNEnabled(false);
        }

        // Set Proxy-CSCF

        mPreferences.setPcscfHost(mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_PCSCF_HOST,
                NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST)); // null will

        // mPreferences.setPcscfHost(GlobalVar.pcscfIp);

        mPreferences.setPcscfPort(mConfigurationService.getInt(
                NgnConfigurationEntry.NETWORK_PCSCF_PORT,
                NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
        mPreferences.setTransport(mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_TRANSPORT,
                NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT));
        mPreferences.setIPVersion(mConfigurationService.getString(
                NgnConfigurationEntry.NETWORK_IP_VERSION,
                NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION));
        String dnsServer = mNetworkService.getDnsServer(DNS_TYPE.DNS_1);

        MyLog.d(TAG,
                String.format(
                        "pcscf-host='%s', pcscf-port='%d', transport='%s', ipversion='%s'",
                        mPreferences.getPcscfHost(),
                        mPreferences.getPcscfPort(),
                        mPreferences.getTransport(),
                        mPreferences.getIPVersion()));

        if (!mSipStack.setProxyCSCF(mPreferences.getPcscfHost(),
                mPreferences.getPcscfPort(), mPreferences.getTransport(),
                mPreferences.getIPVersion())) {
            Log.e(NgnSipService.TAG, "Failed to set Proxy-CSCF parameters");
            return false;
        }

        // Set local IP (If your reusing this code on non-Android platforms
        // (iOS, Symbian, WinPhone, ...),
        // let Doubango retrieve the best IP address)
        boolean ipv6 = NgnStringUtils.equals(mPreferences.getIPVersion(),
                "ipv6", true);
        GlobalVar.mCurrIp = mNetworkService.getLocalIP(ipv6);
        MyLog.d("ywh", "getLocalIP()=" + GlobalVar.mCurrIp);
        mPreferences.setLocalIP(GlobalVar.mCurrIp);
        if (mPreferences.getLocalIP() == null) {
            // if(fromNetworkService){
            // this.preferences.localIP = ipv6 ? "::" : "10.0.2.15"; /* Probably
            // on the emulator */
            // }
            // else{
            // Log.e(TAG, "IP address is Null. Trying to start network");
            // this.networkService.setNetworkEnabledAndRegister();
            // return false;
            // }
            return false; // zhaohua add 20140702
        }

        if (dnsServer != null) {
            mSipStack.addDnsServer(dnsServer);
        }

        if (!mSipStack.setLocalIP(mPreferences.getLocalIP())) {
            Log.e(TAG, "Failed to set the local IP");
            return false;
        }

        // 閿熸枻鎷蜂綇閿熸枻鎷穝ip鏍堟椂閿熸枻鎷穒p gzc20140925
        mConfigurationService.putString(NgnConfigurationEntry.LOCAL_IP,
                mPreferences.getLocalIP());

        MyLog.d(TAG, String.format("Local IP='%s'", mPreferences.getLocalIP()));

        // Whether to use DNS NAPTR+SRV for the Proxy-CSCF discovery (even if
        // the DNS requests are sent only when the stack starts,
        // should be done after setProxyCSCF())
        // String discoverType = mConfigurationService.getString(
        // NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
        // NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY);
        // mSipStack.setDnsDiscovery(NgnStringUtils.equals(discoverType,
        // NgnConfigurationEntry.PCSCF_DISCOVERY_DNS_SRV, true));
        if (NgnUriUtils.checkIPAddress(mPreferences.getPcscfHost())) {
            mSipStack.setDnsDiscovery(false);
        } else {
            mSipStack.setDnsDiscovery(true);
        }

        // enable/disable 3GPP early IMS
        mSipStack.setEarlyIMS(mConfigurationService.getBoolean(
                NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
                NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));

        // SigComp (only update compartment Id if changed)
        if (mConfigurationService.getBoolean(
                NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
                NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP)) {
            String compId = String.format("urn:uuid:%s", UUID.randomUUID()
                    .toString());
            mSipStack.setSigCompId(compId);
        } else {
            mSipStack.setSigCompId(null);
        }

        // TLS
        final String pvFilePath = mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_TLS_PRIVKEY_FILE_PATH,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_PRIVKEY_FILE_PATH);
        final String pbFilePath = mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_TLS_PUBKEY_FILE_PATH,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_PUBKEY_FILE_PATH);
        final String caFilePath = mConfigurationService.getString(
                NgnConfigurationEntry.SECURITY_TLS_CA_FILE_PATH,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_CA_FILE_PATH);
        final boolean verifyCerts = mConfigurationService.getBoolean(
                NgnConfigurationEntry.SECURITY_TLS_VERIFY_CERTS,
                NgnConfigurationEntry.DEFAULT_SECURITY_TLS_VERIFY_CERTS);
        MyLog.d(TAG, String.format("TLS - pvk='%s' pbk='%s' ca='%s' verify=%s",
                pvFilePath, pbFilePath, caFilePath, verifyCerts));
        if (!mSipStack.setSSLCertificates(pvFilePath, pbFilePath, caFilePath,
                verifyCerts)) {
            Log.e(TAG, "Failed to set TLS certificates");
            return false;
        }
        if (!TextUtils.isEmpty(aorIP)) {
            boolean a = mSipStack.setAoR(aorIP, 0);
            Log.e(TAG, "aorIP = " + aorIP + ", mSipStack.setAoR = " + a);
        } else {
            Log.e(TAG, "aorIP is null");
        }

        // Start the Stack
        if (!mSipStack.start()) {
            closeSipStack();
            if (context != null
                    && Thread.currentThread() == Looper.getMainLooper()
                    .getThread()) {
                Toast.makeText(context, "Failed to start the SIP stack",
                        Toast.LENGTH_LONG).show();
            }
            Log.e(TAG, "Failed to start the SIP stack");
            return false;
        }

        // Preference values
        mPreferences.setXcapEnabled(mConfigurationService.getBoolean(
                NgnConfigurationEntry.XCAP_ENABLED,
                NgnConfigurationEntry.DEFAULT_XCAP_ENABLED));
        mPreferences.setPresenceEnabled(mConfigurationService.getBoolean(
                NgnConfigurationEntry.RCS_USE_PRESENCE,
                NgnConfigurationEntry.DEFAULT_RCS_USE_PRESENCE));
        mPreferences.setMWI(mConfigurationService.getBoolean(
                NgnConfigurationEntry.RCS_USE_MWI,
                NgnConfigurationEntry.DEFAULT_RCS_USE_MWI));

        // Create registration session
        if (mRegSession == null) {
            mRegSession = new NgnRegistrationSession(mSipStack);
        } else {
            mRegSession.setSigCompId(mSipStack.getSigCompId());
            mRegSession
                    .setExpires(NgnEngine
                            .getInstance()
                            .getConfigurationService()
                            .getInt(NgnConfigurationEntry.NETWORK_REGISTRATION_TIMEOUT,
                                    NgnConfigurationEntry.DEFAULT_NETWORK_REGISTRATION_TIMEOUT));
        }

        // Set/update From URI. For Registration ToUri should be equals to realm
        // (done by the stack)
        MyLog.d(TAG, "FromUro=" + mPreferences.getIMPU());
        mRegSession.setFromUri(mPreferences.getIMPU());

		/* Before registering, check if AoR hacking id enabled */
        mPreferences.setHackAoR(mConfigurationService.getBoolean(
                NgnConfigurationEntry.NATT_HACK_AOR,
                NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR));
        if (mPreferences.isHackAoR()) {
            MyLog.d(TAG, "isHackAoR()?true");
            if (mCondHackAoR == null) {
                mCondHackAoR = new ConditionVariable();
            }
            final OptionsSession optSession = new OptionsSession(mSipStack);
            // optSession.setToUri(String.format("sip:%s@%s", "hacking_the_aor",
            // this.preferences.realm));
            optSession.send();
            try {
                synchronized (mCondHackAoR) {
                    mCondHackAoR
                            .wait(mConfigurationService
                                    .getInt(NgnConfigurationEntry.NATT_HACK_AOR_TIMEOUT,
                                            NgnConfigurationEntry.DEFAULT_NATT_HACK_AOR_TIMEOUT));
                }
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
            mCondHackAoR = null;
            optSession.delete();
        }

        MyLog.d(TAG, "Prepare to register...");
        if (!mRegSession.register()) {
            Log.e(TAG, "Failed to send REGISTER request");
            return false;
        }
        MyLog.d(TAG, "Register sended.");

        return true;
    }

    @Override
    public boolean refrashRegiste() {
        return mRegSession.register();
    }

    public boolean checkMACADDR() {
        String localMAC = mNetworkService.getLocalMacAddressFromIp(mPreferences
                .getLocalIP());
        int acceptMACListNum = NgnConfigurationEntry.DEFAULT_NETWORK_MAC_ADDRLIST.length;
        for (int i = 0; i < acceptMACListNum; ++i) {
            if (localMAC
                    .equalsIgnoreCase(NgnConfigurationEntry.DEFAULT_NETWORK_MAC_ADDRLIST[i])) {
                Log.d(TAG, "localAddr(MAC; ip):[" + localMAC + "; "
                        + mPreferences.getLocalIP() + "]");
                return true;
            }
        }
        return false;
    }

    public void closeSipStack() {
        if (mSipStack != null) {
            mSipStack = null;
        }
    }

    @Override
    public boolean unRegister() {
        MyLog.d(TAG, "unRegister()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isRegisteSessionConnected() && mSipStack != null
                        && mSipStack.isValid()) {
                    mRegSession.unregister();
                    mSipStack.stop();
                    if (mSipStack != null) {
                        mSipStack.setState(STACK_STATE.STOPPED);
                        MyLog.d(TAG, "mSipStack is stopped");
                    }
                    mRegSession = null;
                    mSipStack = null;
                }
            }
        }).start();
        return true;
    }

    @Override
    public boolean PresencePublish() {
        return false;
    }

    @Override
    public boolean PresencePublish(NgnPresenceStatus status) {
        // TODO Auto-generated method stub
        return false;
    }

    private void broadcastRegistrationEvent(NgnRegistrationEventArgs args) {
        final Intent intent = new Intent(
                NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        intent.putExtra("pid", GlobalVar.mMyPid);
        intent.putExtra(NgnRegistrationEventArgs.EXTRA_EMBEDDED, args);
        // NgnApplication.getContext().sendBroadcast(intent);
        if (GlobalVar.orderedbroadcastSign) {
            NgnSKDroid.getInstence().getGlobleContext().sendOrderedBroadcast(intent, null);
        } else {
            NgnSKDroid.getInstence().getGlobleContext().sendBroadcast(intent);
        }
    }

    // private void broadcastInviteEvent(NgnInviteEventArgs args) {
    // final Intent intent = new Intent(NgnInviteEventArgs.ACTION_INVITE_EVENT);
    // intent.putExtra("pid", GlobalVar.mMyPid);// 閿熸枻鎷烽敓鎺ユ枻鎷烽敓鏂ゆ嫹id閿熸枻鎷烽敓锟?
    // intent.putExtra(NgnInviteEventArgs.EXTRA_EMBEDDED, args);
    // intent.putExtra(NgnInviteEventArgs.EXTRA_SIPCODE, 0);
    // intent.putExtra(NgnInviteEventArgs.EXTRA_CSEQ, args.getmCSeq());
    // // NgnApplication.getContext().sendBroadcast(intent);
    // if (GlobalVar.orderedbroadcastSign) {
    // NgnApplication.getContext().sendOrderedBroadcast(intent, null);
    // } else {
    // NgnApplication.getContext().sendBroadcast(intent);
    // }
    // // MyLog.d(TAG,
    // // "InviteEvent broadcast componant:"+intent.getComponent());
    // // MyLog.d(TAG, "InviteEvent broadcast action:"+intent.getAction());
    // // MyLog.d(TAG, "InviteEvent broadcast packet:"+intent.getPackage());
    // // MyLog.d(TAG, "InviteEvent broadcast scheme:"+intent.getScheme());
    // // MyLog.d(TAG, "InviteEvent broadcast data:"+intent.getData());
    // MyLog.d(TAG, "InviteEvent broadcast send ok.");
    // }

//	private void broadcastMessagingEvent(NgnMessagingEventArgs args,
//			String remoteParty, String date) {
//		final Intent intent = new Intent(
//				NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
//		intent.putExtra("pid", GlobalVar.mMyPid);// 閿熸枻鎷烽敓鎺ユ枻鎷烽敓鏂ゆ嫹id閿熸枻鎷烽敓锟?
//		intent.putExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY, remoteParty);
//		intent.putExtra(NgnMessagingEventArgs.EXTRA_DATE, date);
//		intent.putExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED, args);
//		// NgnApplication.getContext().sendBroadcast(intent);
//		if (GlobalVar.orderedbroadcastSign) {
//			NgnApplication.getContext().sendOrderedBroadcast(intent, null);
//		} else {
//			NgnApplication.getContext().sendBroadcast(intent);
//		}
//	}

//    private void broadcastInviteEvent(NgnInviteEventArgs args, short sipCode) {
//        final Intent intent = new Intent(NgnInviteEventArgs.ACTION_INVITE_EVENT);
//        intent.putExtra("pid", GlobalVar.mMyPid);// 閿熸枻鎷烽敓鎺ユ枻鎷烽敓鏂ゆ嫹id閿熸枻鎷烽敓锟?
//        intent.putExtra(NgnInviteEventArgs.EXTRA_EMBEDDED, args);
//        intent.putExtra(NgnInviteEventArgs.EXTRA_SIPCODE, sipCode);
//        // NgnApplication.getContext().sendBroadcast(intent);
//        if (GlobalVar.orderedbroadcastSign) {
//            NgnSKDroid.getInstence().getGlobleContext().sendOrderedBroadcast(intent, null);
//        } else {
//            NgnSKDroid.getInstence().getGlobleContext().sendBroadcast(intent);
//        }
//        // MyLog.d(TAG,
//        // "InviteEvent(SipCode) broadcast componant:"+intent.getComponent());
//        // MyLog.d(TAG,
//        // "InviteEvent(SipCode) broadcast action:"+intent.getAction());
//        // MyLog.d(TAG,
//        // "InviteEvent(SipCode) broadcast packet:"+intent.getPackage());
//        // MyLog.d(TAG,
//        // "InviteEvent(SipCode) broadcast scheme:"+intent.getScheme());
//        // MyLog.d(TAG,
//        // "InviteEvent(SipCode) broadcast data:"+intent.getData());
//        MyLog.d(TAG, "InviteEvent(SipCode) broadcast send ok.");
//    }

    private void broadcastTransferRequestEvent(NgnInviteEventArgs args,
                                               String referToUri) {
        final Intent intent = new Intent(NgnInviteEventArgs.ACTION_INVITE_EVENT);
        intent.putExtra("pid", GlobalVar.mMyPid);// 閿熸枻鎷烽敓鎺ユ枻鎷烽敓鏂ゆ嫹id閿熸枻鎷烽敓锟?
        intent.putExtra(NgnInviteEventArgs.EXTRA_EMBEDDED, args);
        intent.putExtra(NgnInviteEventArgs.EXTRA_REFERTO_URI, referToUri);
        // NgnApplication.getContext().sendBroadcast(intent);
        if (GlobalVar.orderedbroadcastSign) {
            NgnSKDroid.getInstence().getGlobleContext().sendOrderedBroadcast(intent, null);
        } else {
            NgnSKDroid.getInstence().getGlobleContext().sendBroadcast(intent);
        }
    }

    private void broadcastPublicationEvent(NgnPublicationEventArgs args) {
        final Intent intent = new Intent(
                NgnPublicationEventArgs.ACTION_PUBLICATION_EVENT);
        intent.putExtra("pid", GlobalVar.mMyPid);// 閿熸枻鎷烽敓鎺ユ枻鎷烽敓鏂ゆ嫹id閿熸枻鎷烽敓锟?
        intent.putExtra(NgnPublicationEventArgs.EXTRA_EMBEDDED, args);
        // NgnApplication.getContext().sendBroadcast(intent);
        if (GlobalVar.orderedbroadcastSign) {
            NgnSKDroid.getInstence().getGlobleContext().sendOrderedBroadcast(intent, null);
        } else {
            NgnSKDroid.getInstence().getGlobleContext().sendBroadcast(intent);
        }
    }

//    private void broadcastSubscriptionEvent(NgnSubscriptionEventArgs args) {
//        final Intent intent = new Intent(
//                NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
//        intent.putExtra("pid", GlobalVar.mMyPid);// 閿熸枻鎷烽敓鎺ユ枻鎷烽敓鏂ゆ嫹id閿熸枻鎷烽敓锟?
//        intent.putExtra(NgnSubscriptionEventArgs.EXTRA_EMBEDDED, args);
//        // NgnApplication.getContext().sendBroadcast(intent);
//        if (GlobalVar.orderedbroadcastSign) {
//            NgnSKDroid.getInstence().getGlobleContext().sendOrderedBroadcast(intent, null);
//        } else {
//            NgnSKDroid.getInstence().getGlobleContext().sendBroadcast(intent);
//        }
//        MyLog.d(TAG, "发送订阅数据  args");
//    }

//    private void broadcastSubscriptionEvent(NgnSubscriptionEventArgs args,
//                                            String toHeader) {
//        final Intent intent = new Intent(
//                NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT);
//        intent.putExtra("pid", GlobalVar.mMyPid);// 閿熸枻鎷烽敓鎺ユ枻鎷烽敓鏂ゆ嫹id閿熸枻鎷烽敓锟?
//        intent.putExtra(NgnSubscriptionEventArgs.EXTRA_EMBEDDED, args);
//        intent.putExtra(NgnSubscriptionEventArgs.EXTRA_TO_HEADER, toHeader);
//        if (GlobalVar.orderedbroadcastSign) {
//            NgnSKDroid.getInstence().getGlobleContext().sendOrderedBroadcast(intent, null);
//        } else {
//            NgnSKDroid.getInstence().getGlobleContext().sendBroadcast(intent);
//        }
//        MyLog.d(TAG, "发送订阅数据  toHeader:" + toHeader);
//    }

    /**
     * MySipCallback
     */
    static class MySipCallback extends SipCallback {
        private final NgnSipService mSipService;

        private HashMap<String, HashMap<String, Object>> mLongInfoMap;
        private HashMap<String, Integer> mLongInforeceivedCount;

        private MySipCallback(NgnSipService sipService) {
            super();
            mSipService = sipService;
            mLongInfoMap = new HashMap<String, HashMap<String, Object>>();
            mLongInforeceivedCount = new HashMap<String, Integer>();
        }

        @Override
        public int OnDialogEvent(DialogEvent e) {
            final String phrase = e.getPhrase(); // Dialog connecting Dialog //
            // connected // (un)REGISTER
            // request successfully
            // sent. // Loop Detected
            // (Check your iFCs)
            final short eventCode = e.getCode(); // 900 // 901 // 804 // 903
            final short sipCode;
            final SipSession session = e.getBaseSession();
            if (session == null) {
                return 0;
            }
            final long sessionId = session.getId();
            SipMessage message = e.getSipMessage();
            NgnSipSession mySession = null;
            sipCode = (message != null && message.isResponse()) ? message
                    .getResponseCode() : eventCode;
            MyLog.d(TAG, String.format("OnDialogEvent ---- (%s,%d,%d) %d",
                    phrase, sessionId, eventCode, sipCode));
            // NgnMediaManager.getInstance().initServiceAv(sessionId);
            switch (eventCode) {
                case tinyWRAPConstants.tsip_event_code_dialog_network_warming: {
                    // Network lose packets...
                    if ((mySession = NgnAVSession.getSession(sessionId)) != null
                            && mySession.getConnectionState() == ConnectionState.CONNECTED) {
                        MyLog.d(TAG,
                                "Network status is not good: bear lose packet = "
                                        + mConfigurationService
                                        .getInt(NgnConfigurationEntry.NETWORK_QOS_LOSEPACKETS,
                                                NgnConfigurationEntry.DEFAULT_NETWORK_QOS_LOSEPACKETS));
                    /*
                     * Here user can handle ...
					 */
                        if (((mySession = NgnAVSession.getSession(sessionId)) != null)
                                || ((mySession = NgnMsrpSession
                                .getSession(sessionId)) != null)) {
                            // mSipService
                            // .broadcastInviteEvent(new NgnInviteEventArgs(
                            // sessionId,
                            // NgnInviteEventTypes.CURRENT_NETWORK_UNGOOD,
                            // ((NgnInviteSession) mySession)
                            // .getMediaType(), phrase));
                            NgnSipCallBackHelper
                                    .getInstence()
                                    .InviteState(
                                            new NgnInviteEventArgs(
                                                    sessionId,
                                                    NgnInviteEventTypes.CURRENT_NETWORK_UNGOOD,
                                                    ((NgnInviteSession) mySession)
                                                            .getMediaType(), phrase));
                        }
                    }
                    break;
                }
                // == Connecting ==
                case tinyWRAPConstants.tsip_event_code_dialog_connecting: {
                    // Registration
                    if (mSipService.mRegSession != null
                            && mSipService.mRegSession.getId() == sessionId) {
                        mSipService.mRegSession
                                .setConnectionState(ConnectionState.CONNECTING);
                        MyLog.d(TAG, "RegSessionState=CONNECTING");
                        // mSipService
                        // .broadcastRegistrationEvent(new NgnRegistrationEventArgs(
                        // sessionId,
                        // NgnRegistrationEventTypes.REGISTRATION_INPROGRESS,
                        // eventCode, phrase));
                        NgnSipCallBackHelper.getInstence().RegisterState(
                                NgnRegistrationEventTypes.REGISTRATION_INPROGRESS,
                                sessionId, sipCode, phrase);
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null)
                            || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.CONNECTING);
                        ((NgnInviteSession) mySession)
                                .setState(InviteState.INPROGRESS);
//                        mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                                sessionId, NgnInviteEventTypes.INPROGRESS,
//                                ((NgnInviteSession) mySession).getMediaType(),
//                                phrase), sipCode);
                        NgnSipCallBackHelper.getInstence().InviteState(
                                new NgnInviteEventArgs(sessionId,
                                        NgnInviteEventTypes.INPROGRESS,
                                        ((NgnInviteSession) mySession)
                                                .getMediaType(), phrase));
                    }
                    // Publication
                    else if (((mySession = NgnPublicationSession
                            .getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.CONNECTING);
                        mSipService
                                .broadcastPublicationEvent(new NgnPublicationEventArgs(
                                        sessionId,
                                        NgnPublicationEventTypes.PUBLICATION_INPROGRESS,
                                        eventCode, phrase));
                    }
                    // Subscription
                    else if (((mySession = NgnSubscriptionSession
                            .getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.CONNECTING);
                        MyLog.d(TAG, "发送订阅数据  1");
//                        mSipService
//                                .broadcastSubscriptionEvent(new NgnSubscriptionEventArgs(
//                                        sessionId,
//                                        NgnSubscriptionEventTypes.SUBSCRIPTION_INPROGRESS,
//                                        eventCode, phrase, null, null,
//                                        ((NgnSubscriptionSession) mySession)
//                                                .getEventPackage()));


                        NgnSipCallBackHelper.getInstence().SubscriptionState(new NgnSubscriptionEventArgs(
                                sessionId,
                                NgnSubscriptionEventTypes.SUBSCRIPTION_INPROGRESS,
                                eventCode, phrase, null, null,
                                ((NgnSubscriptionSession) mySession)
                                        .getEventPackage()), "");

                    }

                    break;
                }
                // == PreConnected == // //add by gle
                case tinyWRAPConstants.tsip_event_code_dialog_preconnected: {
                    if (((mySession = NgnAVSession.getSession(sessionId)) != null)
                            || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)) {
                    }
                    break;
                }

                // == Connected == //
                case tinyWRAPConstants.tsip_event_code_dialog_connected: {
                    MyLog.i(TAG, "tsip_event_code_dialog_connected");
                    // Registration
                    if (mSipService.mRegSession != null
                            && mSipService.mRegSession.getId() == sessionId) {
                        mSipService.mRegSession
                                .setConnectionState(ConnectionState.CONNECTED);
                        MyLog.d(TAG, "RegSessionState=CONNECTED");
                        // Update default identity (vs barred)
                        String _defaultIdentity = mSipService.mSipStack
                                .getPreferredIdentity();
                        if (!NgnStringUtils.isNullOrEmpty(_defaultIdentity)) {
                            mSipService.setDefaultIdentity(_defaultIdentity);
                        }
                        // mSipService
                        // .broadcastRegistrationEvent(new NgnRegistrationEventArgs(
                        // sessionId,
                        // NgnRegistrationEventTypes.REGISTRATION_OK,
                        // sipCode, phrase));
                        NgnSipCallBackHelper.getInstence().RegisterState(
                                NgnRegistrationEventTypes.REGISTRATION_OK,
                                sessionId, sipCode, phrase);
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null)
                            || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.CONNECTED);
                        ((NgnInviteSession) mySession).setState(InviteState.INCALL);

//                        mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                                sessionId, NgnInviteEventTypes.CONNECTED,
//                                ((NgnInviteSession) mySession).getMediaType(),
//                                phrase), sipCode);

                        NgnSipCallBackHelper.getInstence().InviteState(
                                new NgnInviteEventArgs(sessionId,
                                        NgnInviteEventTypes.CONNECTED,
                                        ((NgnInviteSession) mySession)
                                                .getMediaType(), phrase));
                        MyLog.d(TAG,
                                "接通----((NgnInviteSession) mySession).getMediaType()---"
                                        + ((NgnInviteSession) mySession)
                                        .getMediaType() + "----phrase----"
                                        + phrase);

                        MyLog.d(TAG, "av connected broadcast send ok.");
                    }
                    // Publication
                    else if (((mySession = NgnPublicationSession
                            .getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.CONNECTED);
                        mSipService
                                .broadcastPublicationEvent(new NgnPublicationEventArgs(
                                        sessionId,
                                        NgnPublicationEventTypes.PUBLICATION_OK,
                                        sipCode, phrase));
                    }
                    // Subscription
                    else if (((mySession = NgnSubscriptionSession
                            .getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.CONNECTED);

                        MyLog.d(TAG, "发送订阅数据  2");
//                        mSipService
//                                .broadcastSubscriptionEvent(new NgnSubscriptionEventArgs(
//                                        sessionId,
//                                        NgnSubscriptionEventTypes.SUBSCRIPTION_OK,
//                                        sipCode, phrase, null, null,
//                                        ((NgnSubscriptionSession) mySession)
//                                                .getEventPackage()));

                        NgnSipCallBackHelper.getInstence().SubscriptionState(new NgnSubscriptionEventArgs(
                                sessionId,
                                NgnSubscriptionEventTypes.SUBSCRIPTION_OK,
                                sipCode, phrase, null, null,
                                ((NgnSubscriptionSession) mySession)
                                        .getEventPackage()), "");

                    }

                    break;
                }

                // == Terminating == //
                case tinyWRAPConstants.tsip_event_code_dialog_terminating: {
                    // Registration
                    if (mSipService.mRegSession != null
                            && mSipService.mRegSession.getId() == sessionId) {
                        mSipService.mRegSession
                                .setConnectionState(ConnectionState.TERMINATING);
                        MyLog.d(TAG, "RegSessionState=TERMINATING");
                        // mSipService
                        // .broadcastRegistrationEvent(new NgnRegistrationEventArgs(
                        // sessionId,
                        // NgnRegistrationEventTypes.UNREGISTRATION_INPROGRESS,
                        // eventCode, phrase));
                        NgnSipCallBackHelper
                                .getInstence()
                                .RegisterState(
                                        NgnRegistrationEventTypes.UNREGISTRATION_INPROGRESS,
                                        sessionId, sipCode, phrase);
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null)
                            || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)) {
                        MyLog.d("", "GLE OnDialogEvent Terminating ");

                        if (mySession.isConnected()) {
                            NgnAVSession.mSendFrameThread = false;
                            GlobalVar.mSendVideo = true;
                            MyLog.d(TAG,
                                    "VideoSend 閿熺Ц纰夋嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸彮璁规嫹閿熸枻鎷锋伅  mSendFrameThread="
                                            + NgnAVSession.mSendFrameThread);
                        }

                        mySession.setConnectionState(ConnectionState.TERMINATING);
                        ((NgnInviteSession) mySession)
                                .setState(InviteState.TERMINATING);
                        if (mySession instanceof NgnAVSession) {
                            MyLog.d("", "GLE OnDialogEvent releaseSession() 1");
                            NgnAVSession.releaseSession((NgnAVSession) mySession);
                        } else if (mySession instanceof NgnMsrpSession) {
                            MyLog.d("", "GLE OnDialogEvent releaseSession() 2");
                            NgnMsrpSession
                                    .releaseSession((NgnMsrpSession) mySession);
                        }
//                        mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                                sessionId, NgnInviteEventTypes.TERMWAIT,
//                                ((NgnInviteSession) mySession).getMediaType(),
//                                phrase), sipCode);
                        NgnSipCallBackHelper.getInstence().InviteState(
                                new NgnInviteEventArgs(sessionId,
                                        NgnInviteEventTypes.TERMWAIT,
                                        ((NgnInviteSession) mySession)
                                                .getMediaType(), phrase));
                        MyLog.d(TAG,
                                "接通之后挂断----((NgnInviteSession) mySession).getMediaType()---"
                                        + ((NgnInviteSession) mySession)
                                        .getMediaType() + "----phrase----"
                                        + phrase);

                        if (((NgnInviteSession) mySession).getMediaType().equals(
                                NgnMediaType.Audio)) {
                            CallBackHelper.exutesingleAudioCallHungUp();
                        } else if (((NgnInviteSession) mySession).getMediaType()
                                .equals(NgnMediaType.AudioVideo)) {
                            CallBackHelper.exutesingleVideoCallHungup();
                        }

                    }
                    // Publication
                    else if (((mySession = NgnPublicationSession
                            .getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.TERMINATING);
                        mSipService
                                .broadcastPublicationEvent(new NgnPublicationEventArgs(
                                        sessionId,
                                        NgnPublicationEventTypes.UNPUBLICATION_INPROGRESS,
                                        eventCode, phrase));
                    }
                    // Subscription
                    else if (((mySession = NgnSubscriptionSession
                            .getSession(sessionId)) != null)) {
                        mySession.setConnectionState(ConnectionState.TERMINATING);
                        MyLog.d(TAG, "发送订阅数据  3");
//                        mSipService
//                                .broadcastSubscriptionEvent(new NgnSubscriptionEventArgs(
//                                        sessionId,
//                                        NgnSubscriptionEventTypes.UNSUBSCRIPTION_INPROGRESS,
//                                        eventCode, phrase, null, null,
//                                        ((NgnSubscriptionSession) mySession)
//                                                .getEventPackage()));
                        NgnSipCallBackHelper.getInstence().SubscriptionState(new NgnSubscriptionEventArgs(
                                sessionId,
                                NgnSubscriptionEventTypes.UNSUBSCRIPTION_INPROGRESS,
                                eventCode, phrase, null, null,
                                ((NgnSubscriptionSession) mySession)
                                        .getEventPackage()), "");
                    }

                    break;
                }

                // == Terminated == //
                case tinyWRAPConstants.tsip_event_code_dialog_terminated: {
                    // Registration
                    if (mSipService.mRegSession != null
                            && mSipService.mRegSession.getId() == sessionId) {

                        // if(mSipService.isRegisteSessionConnected()){
                        // mSipService.mRegSession.setConnectionState(ConnectionState.TERMINATED);
                        // mSipService.broadcastRegistrationEvent(new
                        // NgnRegistrationEventArgs(
                        // sessionId,NgnRegistrationEventTypes.REFRESHREGISTRATION_NOK,
                        // sipCode, phrase));
                        // }else {
                        mSipService.mRegSession
                                .setConnectionState(ConnectionState.TERMINATED);

                        MyLog.d(TAG, "RegSessionState== Terminated ==sipCode = "
                                + sipCode + "==phrase==" + phrase);
                        NgnRegistrationEventArgs args = new NgnRegistrationEventArgs(
                                sessionId,
                                NgnRegistrationEventTypes.REGISTRATION_NOK,
                                sipCode, phrase);
                        if (sipCode == 403 || sipCode == 404) {
                            // mSipService.broadcastRegistrationEvent(args);
                            NgnSipCallBackHelper.getInstence().RegisterState(
                                    NgnRegistrationEventTypes.REGISTRATION_NOK,
                                    sessionId, sipCode, phrase);
                            break;
                        }
                        if (GlobalVar.mLogout) {
                            // mSipService
                            // .broadcastRegistrationEvent(new
                            // NgnRegistrationEventArgs(
                            // sessionId,
                            // NgnRegistrationEventTypes.UNREGISTRATION_OK,
                            // sipCode, phrase));
                            NgnSipCallBackHelper.getInstence().RegisterState(
                                    NgnRegistrationEventTypes.UNREGISTRATION_OK,
                                    sessionId, sipCode, phrase);
                            // }
                        } else {
                            NgnSipCallBackHelper.getInstence().RegisterState(
                                    NgnRegistrationEventTypes.REGISTRATION_NOK,
                                    sessionId, sipCode, phrase);
                        }

                    }
                    // PagerMode IM
                    else if (NgnMessagingSession.hasSession(sessionId)) {
                        NgnMessagingSession.releaseSession(sessionId);
                    }
                    // Audio/Video/MSRP(Chat, FileTransfer)
                    else if (((mySession = NgnAVSession.getSession(sessionId)) != null)
                            || ((mySession = NgnMsrpSession.getSession(sessionId)) != null)) {
                        MyLog.d(TAG, "NgnAVSessionState=TERMINATED");

                        if (mySession.isConnected()
                                && NgnAVSession.mSendFrameThread) {
                            NgnAVSession.mSendFrameThread = false;
                            GlobalVar.mSendVideo = true;
                            MyLog.d(TAG,
                                    "VideoSend 閿熺Ц纰夋嫹閿熸彮璁规嫹閿熸枻鎷锋伅  mSendFrameThread="
                                            + NgnAVSession.mSendFrameThread);
                        }

                        mySession.setConnectionState(ConnectionState.TERMINATED);
                        ((NgnInviteSession) mySession)
                                .setState(InviteState.TERMINATED);

                        // gzc 20141023
                        // 閿熸枻鎷烽敓绲猠ssion閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷峰獟閿熸枻鎷烽敓鏂ゆ嫹閿熼叺鎲嬫嫹閿熸枻鎷烽敓绲孭DATED閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹鍘熼敓鏂ゆ嫹閿熸枻鎷峰獟閿熸枻鎷烽敓鏂ゆ嫹閿熼叺鍑ゆ嫹閿熼叺鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹鎭?
                        NgnMediaType mediaType = null;
                        if (((NgnInviteSession) mySession).getmOldMediaType() != null) {
                            mediaType = ((NgnInviteSession) mySession)
                                    .getmOldMediaType();
                            MyLog.d(TAG, "MediaType userOldMediaType:" + mediaType);
                        } else {
                            mediaType = ((NgnInviteSession) mySession)
                                    .getMediaType();
                            MyLog.d(TAG, "MediaType mediaType not change:"
                                    + mediaType);
                        }

                        if (mySession instanceof NgnAVSession) {
                            MyLog.d("", "GLE OnDialogEvent releaseSession() 1");
                            NgnAVSession.releaseSession((NgnAVSession) mySession);
                        } else if (mySession instanceof NgnMsrpSession) {
                            MyLog.d("", "GLE OnDialogEvent releaseSession() 2");
                            NgnMsrpSession
                                    .releaseSession((NgnMsrpSession) mySession);
                        }

                        MyLog.d(TAG,
                                "挂断----((NgnInviteSession) mySession).getMediaType()---"
                                        + ((NgnInviteSession) mySession)
                                        .getMediaType() + "----phrase----"
                                        + phrase
                                        + "----mySession.getRemotePartyUri()---"
                                        + mySession.getRemotePartyUri());

//                        mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                                sessionId, NgnInviteEventTypes.TERMINATED,
//                                mediaType, phrase), sipCode);
                        NgnSipCallBackHelper.getInstence().InviteState(
                                new NgnInviteEventArgs(sessionId,
                                        NgnInviteEventTypes.TERMINATED, mediaType,
                                        phrase));

                    }
                    // Publication
                    else if (((mySession = NgnPublicationSession
                            .getSession(sessionId)) != null)) {
                        ConnectionState previousConnState = mySession
                                .getConnectionState();
                        mySession.setConnectionState(ConnectionState.TERMINATED);
                        mSipService
                                .broadcastPublicationEvent(new NgnPublicationEventArgs(
                                        sessionId,
                                        (previousConnState == ConnectionState.TERMINATING) ? NgnPublicationEventTypes.UNPUBLICATION_OK
                                                : NgnPublicationEventTypes.PUBLICATION_NOK,
                                        sipCode, phrase));
                    }
                    // Subscription
                    else if (((mySession = NgnSubscriptionSession
                            .getSession(sessionId)) != null)) {
                        final String toHeader = message.getSipHeaderValue("t");
                        ConnectionState previousConnState = mySession
                                .getConnectionState();
                        mySession.setConnectionState(ConnectionState.TERMINATED);
                        MyLog.d(TAG, "发送订阅数据  4");
//                        mSipService
//                                .broadcastSubscriptionEvent(
//                                        new NgnSubscriptionEventArgs(
//                                                sessionId,
//                                                (previousConnState == ConnectionState.TERMINATING) ? NgnSubscriptionEventTypes.UNSUBSCRIPTION_OK
//                                                        : NgnSubscriptionEventTypes.SUBSCRIPTION_NOK,
//                                                sipCode,
//                                                phrase,
//                                                null,
//                                                null,
//                                                ((NgnSubscriptionSession) mySession)
//                                                        .getEventPackage()),
//                                        toHeader);
                        NgnSipCallBackHelper.getInstence().SubscriptionState(new NgnSubscriptionEventArgs(
                                sessionId,
                                (previousConnState == ConnectionState.TERMINATING) ? NgnSubscriptionEventTypes.UNSUBSCRIPTION_OK
                                        : NgnSubscriptionEventTypes.SUBSCRIPTION_NOK,
                                sipCode,
                                phrase,
                                null,
                                null,
                                ((NgnSubscriptionSession) mySession)
                                        .getEventPackage()), toHeader);
                    }
                    break;
                }
            }

            return 0;
        }

        @Override
        public int OnInviteEvent(InviteEvent e) {
            final tsip_invite_event_type_t type = e.getType();
            final short code = e.getCode();
            final String phrase = e.getPhrase();
            InviteSession session = e.getSession();
            NgnSipSession mySession = null;
            SipSession sipSession = e.getBaseSession();
            // long sessionId = sipSession.getId();
            // sunkaisens:xunzy 閿熸枻鎷烽敓锟?
            // String strsipcontent = new
            // String(e.getSipMessage().getSipContent());
            SipMessage message = e.getSipMessage();
            if (message == null) {
                MyLog.d(TAG, "sipMessage is null.");
                return -1;
            }
            String app_Type = message.getSipHeaderValue("APP_Type");
            MyLog.d(TAG,
                    String.format(
                            "GLE OnInviteEvent app_Type = %s, phrase= %s, code=%d, type=%s",
                            app_Type, phrase, code, type));
            if (app_Type != null) {

                if (app_Type.contains("groupcall/audio") // 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓锟?
                        || app_Type.contains("groupcall/video") // 閿熸枻鎷烽閿熸枻鎷烽敓锟?
                        // || app_Type.contains("groupcall/video/monitor")
                        || app_Type.contains("monitor") // 閿熸枻鎷烽閿熸枻鎷烽敓锟?
                        // || app_Type.contains("surveil") // add by gle
                        // || app_Type.contains("monitor/surveil") // add by gle
                        // after del
                        || app_Type.contains("transmit") // 閿熸枻鎷烽閿熸埅杈炬嫹
                        || app_Type.contains("audio/evn")
                        || app_Type.contains("directcall")) { //

                    twrap_media_type_t sessionType = e.getMediaType();

                    MyLog.d(TAG, "InviteEventCallback phrase = " + phrase
                            + ",sessionType = " + sessionType);
                    tsip_request_type_t requestType = message.getRequestType();
                    MyLog.d(TAG, "閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹:" + requestType);
                    if (requestType == tsip_request_type_t.tsip_CANCEL) {
                        return -1;
                    }

                    if ((session = e.takeCallSessionOwnership()) == null) {
                        Log.e(TAG,
                                "Failed to take audio/video session ownership");
                        return -1;
                    }

                    final NgnAVSession avSession = NgnAVSession
                            .takeIncomingSession(mSipService.getSipStack(),
                                    (CallSession) session, sessionType, message);
                    avSession.setSessionType(app_Type);
                    // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                    // avSession.getId(), NgnInviteEventTypes.INCOMING,
                    // avSession.getMediaType(), app_Type));
                    NgnSipCallBackHelper.getInstence().InviteState(
                            new NgnInviteEventArgs(avSession.getId(),
                                    NgnInviteEventTypes.INCOMING, avSession
                                    .getMediaType(), app_Type));
                    return 0;

                } else if (app_Type.equals("encryptcall")) { // 閿熸枻鎷烽敓鏉扮數璇?
                    MyLog.d(TAG, "Receive a encrypted call, and phrase = "
                            + phrase);
                    twrap_media_type_t sessionType = e.getMediaType();
                    if ((session = e.takeCallSessionOwnership()) == null) {
                        Log.e(TAG,
                                "Failed to take audio/video session ownership");
                        return -1;
                    }
                    final NgnAVSession avSession = NgnAVSession
                            .takeIncomingSession(mSipService.getSipStack(),
                                    (CallSession) session, sessionType, message);
                    // avSession.setGroupCall(true);
                    // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                    // avSession.getId(), NgnInviteEventTypes.INCOMING,
                    // avSession.getMediaType(), app_Type));
                    NgnSipCallBackHelper.getInstence().InviteState(
                            new NgnInviteEventArgs(avSession.getId(),
                                    NgnInviteEventTypes.INCOMING, avSession
                                    .getMediaType(), app_Type));
                    return 0;

                } else if (app_Type.equals("video/surv")) { // 閿熸枻鎷烽閿熸枻鎷烽敓锟?

                    MyLog.d(TAG, "Receive a video monitoring, and phrase = "
                            + phrase);
                    twrap_media_type_t sessionType = e.getMediaType();
                    if ((session = e.takeCallSessionOwnership()) == null) {
                        Log.e(TAG,
                                "Failed to take audio/video session ownership");
                        return -1;
                    }

                    final NgnAVSession avSession = NgnAVSession
                            .takeIncomingSession(mSipService.getSipStack(),
                                    (CallSession) session, sessionType, message);
                    // avSession.setGroupCall(true);
                    // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                    // avSession.getId(),
                    // NgnInviteEventTypes.GROUP_VIDEO_MONITORING,
                    // avSession.getMediaType(), app_Type));
                    return 0;

                } else if (app_Type.equals("video/transmit")) { // 閿熸枻鎷烽杞敓鏂ゆ嫹
                    // 閿熸枻鎷烽杞敓鏂ゆ嫹
                    MyLog.d(TAG, "Receive a video transmit, and phrase = "
                            + phrase);
                    twrap_media_type_t sessionType = e.getMediaType();
                    if ((session = e.takeCallSessionOwnership()) == null) {
                        Log.e(TAG,
                                "Failed to take audio/video session ownership");
                        return -1;
                    }

                    final NgnAVSession avSession = NgnAVSession
                            .takeIncomingSession(mSipService.getSipStack(),
                                    (CallSession) session, sessionType, message);
                    // avSession.setGroupCall(true);
                    // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                    // avSession.getId(),
                    // NgnInviteEventTypes.VIEDO_TRANSMINT, avSession
                    // .getMediaType(), app_Type));
                    return 0;

                }
            }
            // xunzy 閿熸枻鎷烽敓锟?閿熸枻鎷烽敓鏂ゆ嫹
            MyLog.d(TAG, "type------>" + type);
            switch (type) {
                case tsip_i_newcall:
                case tsip_i_ect_newcall:
                    if (session != null) /*
									 * As we are not the owner, then the session
									 * MUST be null
									 */ {
                        Log.e(TAG, "Invalid incoming session");
                        session.hangup(); // To avoid another callback event
                        return -1;
                    }

                    if (message == null) {
                        Log.e(TAG, "Invalid message");
                        return -1;
                    }
                    twrap_media_type_t sessionType = e.getMediaType();
                    MyLog.d("", String.format("OnInviteEvent sessionType = %s",
                            sessionType));

                    switch (sessionType) {
                        case twrap_media_msrp: {
                            if ((session = e.takeMsrpSessionOwnership()) == null) {
                                Log.e(TAG, "Failed to take MSRP session ownership");
                                return -1;
                            }

                            NgnMsrpSession msrpSession = NgnMsrpSession
                                    .takeIncomingSession(mSipService.getSipStack(),
                                            (MsrpSession) session, message);
                            if (msrpSession == null) {
                                Log.e(TAG, "Failed to create new session");
                                session.hangup();
                                session.delete();
                                return 0;
                            }
                            NgnMediaType mediaType = msrpSession.getMediaType();
                            MyLog.d(TAG, "mediaType----->" + mediaType);
                            MyLog.d(TAG, "phrase----->" + mediaType);
                            // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                            // msrpSession.getId(), NgnInviteEventTypes.INCOMING,
                            // mediaType, phrase));

                            NgnSipCallBackHelper.getInstence().InviteState(
                                    new NgnInviteEventArgs(msrpSession.getId(),
                                            NgnInviteEventTypes.INCOMING, mediaType,
                                            phrase));
                            // wangds add,auto accept;
                            session.accept();// added
                            msrpSession.accept();// added
                            break;
                        }

                        case twrap_media_audio:
                        case twrap_media_audio_video:
                        case twrap_media_audiovideo:
                        case twrap_media_video:
                        case twrap_media_audio_t140:
                        case twrap_media_audio_video_t140:
                        case twrap_media_video_t140: {
                            if ((session = e.takeCallSessionOwnership()) == null) {
                                Log.e(TAG,
                                        "Failed to take audio/video session ownership");
                                return -1;
                            }
                            final NgnInviteEventTypes eType = type == tsip_invite_event_type_t.tsip_i_newcall ? NgnInviteEventTypes.INCOMING
                                    : NgnInviteEventTypes.REMOTE_TRANSFER_INPROGESS;
                            Log.e(TAG,
                                    "NgnAVSession.getSize() 1 = "
                                            + NgnAVSession.getSize());
                            final NgnAVSession avSession = NgnAVSession
                                    .takeIncomingSession(mSipService.getSipStack(),
                                            (CallSession) session, sessionType, message);
                            Log.e(TAG,
                                    "NgnAVSession.getSize() 2 = "
                                            + NgnAVSession.getSize());
                            // sks add start
                            if (sessionType == twrap_media_type_t.twrap_media_audio)
                                avSession.setSessionType(SessionType.AudioCall);
                            else {
						/*
						 * 閿熸枻鎷烽敓鏂ゆ嫹閿熺嫛纰夋嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓閾板嚖鎷烽敓鏂ゆ嫹Invite SDP
						 * 閿熸枻鎷
						 * ?閿熸枻鎷烽閿熸枻鎷峰皬閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熶茎顒婃嫹閿熺禒magesize=cif
						 */
                                SdpMessage sdpMessage = message.getSdpMessage();
                                if (sdpMessage != null) {
                                    String imageattr = sdpMessage.getSdpHeaderAValue(
                                            "video", "imageattr");
                                    MyLog.d("", "OnInviteEvent SDP=" + imageattr);
                                    if (imageattr == null)
                                        NgnProxyPluginMgr.setDefaultMinVideoSize();
                                }
                                avSession.setSessionType(SessionType.VideoCall);
                            }
                            // sks end

                            NgnMediaType mediaType = avSession.getMediaType();
                            NgnAVSession.getSession(session.getId())
                                    .getRemotePartyUri();
                            String remotePartyUri = NgnAVSession.getSession(
                                    session.getId()).getRemotePartyUri();
                            MyLog.d(TAG, "mediaType---->" + mediaType
                                    + "--mySession.getRemotePartyUri()--"
                                    + remotePartyUri);
                            MyLog.d(TAG,
                                    "phrase---->" + phrase
                                            + "-----avSession.getSessionType()----"
                                            + avSession.getSessionType());
                            NgnMediaManager.getInstance().initServiceAv(
                                    avSession.getId());
                            // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                            // avSession.getId(), eType, mediaType, phrase));
                            NgnSipCallBackHelper.getInstence().InviteState(
                                    new NgnInviteEventArgs(avSession.getId(), eType,
                                            mediaType, phrase));
                            break;
                        }

                        default:
                            MyLog.d("", String.format(
                                    "GLE OnInviteEvent sessionType = NULL %s",
                                    sessionType));
                            Log.e(TAG, "Invalid media type");
                            return 0;

                    }
                    break;

                case tsip_ao_request:
                    // For backward compatibility keep both "RINGING" and
                    // "SIP_RESPONSE"
                    if (code == 180 && session != null) {
                        if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                                || ((mySession = NgnMsrpSession.getSession(session
                                .getId())) != null)) {
                            SdpMessage sdpMessage = e.getSipMessage()
                                    .getSdpMessage();
                            if (sdpMessage != null
                                    && !NgnInviteEventArgs.m180HasSdp) {
                                NgnInviteEventArgs.m180HasSdp = true;
                            }
                            MyLog.d(TAG, "NgnSipService hasSdp : "
                                    + NgnInviteEventArgs.m180HasSdp);
                            if (((mySession = NgnAVSession.getSession(session
                                    .getId())) != null)) {

                                ((NgnInviteSession) mySession)
                                        .setState(InviteState.EARLY_MEDIA);
                            }
//                            mSipService.broadcastInviteEvent(
//                                    new NgnInviteEventArgs(mySession.getId(),
//                                            NgnInviteEventTypes.RINGING,
//                                            ((NgnInviteSession) mySession)
//                                                    .getMediaType(), phrase), code);
                            NgnSipCallBackHelper.getInstence().InviteState(
                                    new NgnInviteEventArgs(mySession.getId(),
                                            NgnInviteEventTypes.RINGING,
                                            ((NgnInviteSession) mySession)
                                                    .getMediaType(), phrase));
                        }
                    }
                    if (code == 486 && session != null) { // sks add 486 Busy Here
                        // 閿熺殕绔嫆鏂ゆ嫹
                        if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                                || ((mySession = NgnMsrpSession.getSession(session
                                .getId())) != null)) {
//                            mSipService.broadcastInviteEvent(
//                                    new NgnInviteEventArgs(mySession.getId(),
//                                            NgnInviteEventTypes.REMOTE_REFUSE,
//                                            ((NgnInviteSession) mySession)
//                                                    .getMediaType(), phrase), code);
                            NgnSipCallBackHelper.getInstence().InviteState(
                                    new NgnInviteEventArgs(mySession.getId(),
                                            NgnInviteEventTypes.REMOTE_REFUSE,
                                            ((NgnInviteSession) mySession)
                                                    .getMediaType(), phrase));
                        }
                        break;
                    }
                    if (session != null) {
                        if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                                || ((mySession = NgnMsrpSession.getSession(session
                                .getId())) != null)) {
//                            mSipService.broadcastInviteEvent(
//                                    new NgnInviteEventArgs(mySession.getId(),
//                                            NgnInviteEventTypes.SIP_RESPONSE,
//                                            ((NgnInviteSession) mySession)
//                                                    .getMediaType(), phrase), code);
                            NgnSipCallBackHelper.getInstence().InviteState(
                                    new NgnInviteEventArgs(mySession.getId(),
                                            NgnInviteEventTypes.SIP_RESPONSE,
                                            ((NgnInviteSession) mySession)
                                                    .getMediaType(), phrase));
                        }
                    }
                    break;

                case tsip_i_request: {
                    final SipMessage sipMessage = e.getSipMessage();
                    if (sipMessage != null
                            && session != null
                            && ((mySession = NgnAVSession.getSession(session
                            .getId())) != null)) {
                        if (sipMessage.getRequestType() == tsip_request_type_t.tsip_INFO) {
                            final String contentType = sipMessage
                                    .getSipHeaderValue("c");
                            if (NgnStringUtils.equals(contentType,
                                    NgnContentType.PTT_INFO, true)) {
                                MyLog.d(TAG, "Receive a INFO request, and phrase="
                                        + phrase);
                                final byte content[] = sipMessage.getSipContent();
                                // 閿熸枻鎷峰彇INFO閿熸枻鎷锋伅閿熸枻鎷稢Seq CSeq 1156126807 INFO
                                final String mCSeq = sipMessage
                                        .getSipHeaderValue("CSeq");
                                long CSeqNum = Integer
                                        .parseInt(mCSeq.split(" ")[0]);
                                MyLog.d(TAG, "CSeq=" + mCSeq + "   num=" + CSeqNum);
                                String commond = null;
                                if (content != null) {
                                    MyLog.d(TAG,
                                            "receive contentlen="
                                                    + sipMessage
                                                    .getSipContentLength());
                                    // MyLog.d(TAG,"content[0]="+content[0]+"and content[13]="+content[1]);
                                    MyLog.d(TAG, "receive contentInfo ="
                                            + new String(content));
                                    // mySession.setmInfoContent(content);
                                    NgnInviteEventArgs ngnInviteEventArgs = new NgnInviteEventArgs(
                                            session.getId(),
                                            NgnInviteEventTypes.PTT_INFO_REQUEST, // GROUP_PTT_INFO,
                                            ((NgnInviteSession) mySession)
                                                    .getMediaType(), commond);
                                    ngnInviteEventArgs.setmInfoContent(content); // 閿熸枻鎷烽敓鐭細璇濋敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鎹凤綇鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓閾扮》鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹
                                    ngnInviteEventArgs.setmCSeq(CSeqNum);

                                    // mSipService
                                    // .broadcastInviteEvent(ngnInviteEventArgs);
                                    NgnSipCallBackHelper.getInstence().InviteState(
                                            ngnInviteEventArgs);
                                }
                            } else if (NgnStringUtils.equals(contentType,
                                    NgnContentType.USSD_INFO, true)) {
                                MyLog.d(TAG,
                                        "Receive a INFO request USSD_Content, and phrase="
                                                + phrase);
                                byte[] content = sipMessage.getSipContent();

                                // String commond=null;
                                if (content != null) {
                                    MyLog.d(TAG,
                                            "receive contentlen="
                                                    + sipMessage
                                                    .getSipContentLength());
                                    // MyLog.d(TAG,"content[0]="+content[0]+"and content[13]="+content[1]);
                                    MyLog.d(TAG, HexDump.dump(content, 0, 0));
                                    mySession.setmInfoContent(content);
                                    // mSipService
                                    // .broadcastInviteEvent(new NgnInviteEventArgs(
                                    // session.getId(),
                                    // NgnInviteEventTypes.ENCRYPT_INFO,
                                    // ((NgnInviteSession) mySession)
                                    // .getMediaType(), phrase));
                                }
                            } else if (NgnStringUtils.equals(contentType,
                                    NgnContentType.DOUBANGO_DEVICE_INFO, true)) {
                                final byte content[] = sipMessage.getSipContent();
                                if (content != null) {
                                    final String values[] = new String(content)
                                            .split("\r\n");
                                    for (String value : values) {
                                        if (value == null)
                                            continue;
                                        final String kvp[] = value.split(":");
                                        if (kvp.length == 2) {
                                            if (NgnStringUtils.equals(kvp[0],
                                                    "orientation", true)) {
                                                if (NgnStringUtils.equals(kvp[1],
                                                        "landscape", true)) {
                                                    ((NgnInviteSession) mySession)
                                                            .getRemoteDeviceInfo()
                                                            .setOrientation(
                                                                    Orientation.LANDSCAPE);
                                                } else if (NgnStringUtils.equals(
                                                        kvp[1], "portrait", true)) {
                                                    ((NgnInviteSession) mySession)
                                                            .getRemoteDeviceInfo()
                                                            .setOrientation(
                                                                    Orientation.PORTRAIT);
                                                }
                                            } else if (NgnStringUtils.equals(
                                                    kvp[0], "lang", true)) {
                                                ((NgnInviteSession) mySession)
                                                        .getRemoteDeviceInfo()
                                                        .setLang(kvp[1]);
                                            }
                                        }
                                    }
                                    // mSipService
                                    // .broadcastInviteEvent(new NgnInviteEventArgs(
                                    // session.getId(),
                                    // NgnInviteEventTypes.REMOTE_DEVICE_INFO_CHANGED,
                                    // ((NgnInviteSession) mySession)
                                    // .getMediaType(), phrase));
                                    NgnSipCallBackHelper
                                            .getInstence()
                                            .InviteState(
                                                    new NgnInviteEventArgs(
                                                            session.getId(),
                                                            NgnInviteEventTypes.REMOTE_DEVICE_INFO_CHANGED,
                                                            ((NgnInviteSession) mySession)
                                                                    .getMediaType(),
                                                            phrase));
                                }
                            }
                        }
                    }
                    break;
                }
                case tsip_o_ect_trying: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.LOCAL_TRANSFER_TRYING,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper
                                .getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.LOCAL_TRANSFER_TRYING,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_o_ect_accepted: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                        // session.getId(),
                        // NgnInviteEventTypes.LOCAL_TRANSFER_ACCEPTED,
                        // ((NgnInviteSession) mySession).getMediaType(),
                        // phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(session.getId(),
                                NgnInviteEventTypes.LOCAL_TRANSFER_ACCEPTED, ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_o_ect_completed: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.LOCAL_TRANSFER_COMPLETED,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.LOCAL_TRANSFER_COMPLETED,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_o_ect_failed: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.LOCAL_TRANSFER_FAILED,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.LOCAL_TRANSFER_FAILED,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_o_ect_notify:
                case tsip_i_ect_notify: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        NgnInviteEventTypes eType = (type == tsip_invite_event_type_t.tsip_o_ect_notify ? NgnInviteEventTypes.LOCAL_TRANSFER_NOTIFY
                                : NgnInviteEventTypes.REMOTE_TRANSFER_NOTIFY);
                        NgnInviteEventArgs args = new NgnInviteEventArgs(
                                session.getId(), eType,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase);
//                        mSipService.broadcastInviteEvent(args, code);
                        NgnSipCallBackHelper.getInstence().InviteState(args);
                    }
                    break;
                }
                case tsip_i_ect_requested: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        final SipMessage sipMessage = e.getSipMessage();
                        if (sipMessage != null) {
                            if (sipMessage.getRequestType() == tsip_request_type_t.tsip_REFER) {
                                String referToUri = sipMessage
                                        .getSipHeaderValue("refer-to");
                                if (!NgnStringUtils.isNullOrEmpty(referToUri)) {
                                    NgnInviteEventArgs args = new NgnInviteEventArgs(
                                            session.getId(),
                                            NgnInviteEventTypes.REMOTE_TRANSFER_REQUESTED,
                                            ((NgnInviteSession) mySession)
                                                    .getMediaType(), phrase);
                                    mSipService.broadcastTransferRequestEvent(args,
                                            referToUri);
                                }
                            }
                        }
                    }

                    break;
                }
                case tsip_i_ect_failed: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.REMOTE_TRANSFER_FAILED,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.REMOTE_TRANSFER_FAILED,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_i_ect_completed: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.REMOTE_TRANSFER_COMPLETED,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.REMOTE_TRANSFER_COMPLETED,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_m_early_media: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        ((NgnInviteSession) mySession)
                                .setState(InviteState.EARLY_MEDIA);
                        // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                        // session.getId(), NgnInviteEventTypes.EARLY_MEDIA,
                        // ((NgnInviteSession) mySession).getMediaType(),
                        // phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(
                                new NgnInviteEventArgs(session.getId(),
                                        NgnInviteEventTypes.EARLY_MEDIA,
                                        ((NgnInviteSession) mySession)
                                                .getMediaType(), phrase));
                    }
                    break;
                }
                case tsip_m_local_hold_ok: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        ((NgnInviteSession) mySession).setLocalHold(true);
                        // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                        // session.getId(), NgnInviteEventTypes.LOCAL_HOLD_OK,
                        // ((NgnInviteSession) mySession).getMediaType(),
                        // phrase));
                    }
                    break;
                }
                case tsip_m_updating: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.MEDIA_UPDATING,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.MEDIA_UPDATING,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_m_updated: {
                    if (session == null) {
                        break;
                    }
                    MyLog.i(TAG, "tsip_m_updated");
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        if (mySession instanceof NgnAVSession) {
                            NgnAVSession.handleMediaUpdate(mySession.getId(),
                                    e.getMediaType());
                        }
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(), NgnInviteEventTypes.MEDIA_UPDATED,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(), NgnInviteEventTypes.MEDIA_UPDATED,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_m_local_hold_nok: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.LOCAL_HOLD_NOK,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.LOCAL_HOLD_NOK,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_m_local_resume_ok: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        ((NgnInviteSession) mySession).setLocalHold(false);
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.LOCAL_RESUME_OK,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.LOCAL_RESUME_OK,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_m_local_resume_nok: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(),
//                         NgnInviteEventTypes.LOCAL_RESUME_NOK,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(),
                                NgnInviteEventTypes.LOCAL_RESUME_NOK,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_m_remote_hold: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        ((NgnInviteSession) mySession).setRemoteHold(true);
//                         mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
//                         session.getId(), NgnInviteEventTypes.REMOTE_HOLD,
//                         ((NgnInviteSession) mySession).getMediaType(),
//                         phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(new NgnInviteEventArgs(
                                session.getId(), NgnInviteEventTypes.REMOTE_HOLD,
                                ((NgnInviteSession) mySession).getMediaType(),
                                phrase));
                    }
                    break;
                }
                case tsip_m_remote_resume: {
                    if (session == null) {
                        break;
                    }
                    if (((mySession = NgnAVSession.getSession(session.getId())) != null)
                            || ((mySession = NgnMsrpSession.getSession(session
                            .getId())) != null)) {
                        ((NgnInviteSession) mySession).setRemoteHold(false);
                        // mSipService.broadcastInviteEvent(new NgnInviteEventArgs(
                        // session.getId(), NgnInviteEventTypes.REMOTE_RESUME,
                        // ((NgnInviteSession) mySession).getMediaType(),
                        // phrase));
                        NgnSipCallBackHelper.getInstence().InviteState(
                                new NgnInviteEventArgs(session.getId(),
                                        NgnInviteEventTypes.REMOTE_RESUME,
                                        ((NgnInviteSession) mySession)
                                                .getMediaType(), phrase));
                    }
                    break;
                }
                default:
                    break;
            }

            return 0;
        }

        @Override
        public int OnMessagingEvent(MessagingEvent e) {

            Log.d(TAG, "OnMessagingEvent()");

            SipMessage message = e.getSipMessage();

            if (message == null) {
                MyLog.d(TAG, "message is null.");
                return -1;
            }

            String app_Type = message.getSipHeaderValue("APP_Type"); // IMS
            // messaging/Page-mode
            // messaging
            if (app_Type == null) { // APP_Type閿熸枻鎷烽敓鐭紮鎷烽敓鏂ゆ嫹
                app_Type = message.getSipHeaderValue("app_type");
            }
            if (app_Type != null) {
                MessagingSession sessionType = e.getSession();
                MessagingSession _session = e.getSession();
                if (app_Type.equals(NgnContentType.GPS_APP_TYPE)) {
                    NgnMessagingSession imSession;
                    if (_session == null) {
                        _session = e.takeSessionOwnership();
                    }

                    if (_session == null) {
                        Log.e(NgnSipService.TAG,
                                "Failed to take session ownership");
                        return -1;
                    }
                    imSession = NgnMessagingSession.takeIncomingSession(
                            mSipService.mSipStack, _session, message);
                    if (message == null) {
                        imSession.reject();
                        imSession.decRef();
                        return 0;
                    }

                    String from = message.getSipHeaderValue("f");
                    final String contentType = message.getSipHeaderValue("c");
                    final byte[] bytes = message.getSipContent();
                    byte[] content = null;

                    if (bytes == null || bytes.length == 0) {
                        Log.e(NgnSipService.TAG, "Invalid MESSAGE");
                        imSession.reject();
                        imSession.decRef();
                        return 0;
                    }

                    imSession.accept();
                    if (NgnStringUtils.equals(contentType,
                            NgnContentType.GPS_CONTENT_TYPE_HEADER_COMMAND,
                            true)) {
                        content = bytes;
//						mSipService.broadcastMessagingEvent(
//								new NgnMessagingEventArgs(_session.getId(),
//										NgnMessagingEventTypes.INCOMING, e
//												.getPhrase(), content,
//										contentType), from, NgnDateTimeUtils
//										.now());
                        NgnSipCallBackHelper.getInstence().MessageState(
                                new NgnMessagingEventArgs(_session.getId(),
                                        NgnMessagingEventTypes.INCOMING,
                                        e.getPhrase(), content, contentType),
                                from);
                    } else if (NgnStringUtils.equals(contentType,
                            NgnContentType.GPS_CONTENT_TYPE_HEADER_DATA, true)) {
                        content = bytes;
//						mSipService.broadcastMessagingEvent(
//								new NgnMessagingEventArgs(_session.getId(),
//										NgnMessagingEventTypes.INCOMING, e
//												.getPhrase(), content,
//										contentType), from, NgnDateTimeUtils
//										.now());
                        NgnSipCallBackHelper.getInstence().MessageState(
                                new NgnMessagingEventArgs(_session.getId(),
                                        NgnMessagingEventTypes.INCOMING,
                                        e.getPhrase(), content, contentType),
                                from);
                    }

                    return 0;
                }
                /**
                 * 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷?閿熺即璁规嫹閿熸枻鎷烽敓鏂ゆ嫹鎭敓鏂ゆ嫹閿燂拷 IMS
                 * messaging/Page-mode messaging?id=1/3
                 */
                if (app_Type.startsWith("IMS messaging/Page-mode messaging?")) {
                    String longInfoId_Index_Count = app_Type.substring(app_Type
                            .lastIndexOf("?") + 1); // id=1/3 id=2/3 id=3/3
                    String longInfoId = longInfoId_Index_Count.split("=")[0]; // id
                    String longInfoIndex = (longInfoId_Index_Count.split("=")[1])
                            .split("/")[0]; // 1 2 3
                    String longInfoCount = (longInfoId_Index_Count.split("=")[1])
                            .split("/")[1]; // 3
                    HashMap<String, Object> tMap = new HashMap<String, Object>();
                    tMap.put("id", longInfoId);
                    tMap.put("index", longInfoIndex);
                    tMap.put("count", longInfoCount);
                    // tMap.put("content", new String(message.getSipContent()));
                    tMap.put("content", message.getSipContent());
                    mLongInfoMap.put(longInfoId + "-" + longInfoIndex, tMap); // 閿熸枻鎷烽敓鏂ゆ嫹鑱岄敓鏂ゆ嫹閿熸枻鎷烽敓锟?
                    int mIndex = 0;
                    if (mLongInforeceivedCount.get(longInfoId + "-index") != null) {
                        mIndex = mLongInforeceivedCount.get(longInfoId
                                + "-index");
                    }
                    mIndex++;
                    if (mIndex < Integer.parseInt(longInfoCount)) {
                        mLongInforeceivedCount.put(longInfoId + "-index",
                                mIndex);
                        return 0;
                    }
                    mLongInforeceivedCount.remove(longInfoId + "-index");

                    // contactMessage(longInfoId, longInfoCount);
                    byte[] strContent = null;
                    for (int i = 0; i < Integer.parseInt(longInfoCount); i++) { // 閿熸枻鎷锋垙鑱岄敓鏂ゆ嫹閿熸枻鎷烽敓锟?
                        HashMap<String, Object> map = mLongInfoMap
                                .get(longInfoId + "-" + (i + 1));
						/*
						 * if(map != null) strContent = strContent +
						 * map.get("content");
						 */
                        if (map != null) {
                            byte[] tmp = (byte[]) map.get("content");
                            if (strContent == null) {
                                strContent = tmp;
                            } else {
                                byte[] tmp2 = new byte[strContent.length
                                        + tmp.length];
                                System.arraycopy(strContent, 0, tmp2, 0,
                                        strContent.length);
                                System.arraycopy(tmp, 0, tmp2,
                                        strContent.length, tmp.length);
                                strContent = tmp2;
                            }
                        }
                    }
                    mLongInfoMap.clear();
                    NgnMessagingSession imSession;
                    if (_session == null) {
                        _session = e.takeSessionOwnership();
                    }

                    if (_session == null) {
                        Log.e(NgnSipService.TAG,
                                "Failed to take session ownership");
                        return -1;
                    }
                    imSession = NgnMessagingSession.takeIncomingSession(
                            mSipService.mSipStack, _session, message);

                    String from = message.getSipHeaderValue("f");
                    final String contentType = message.getSipHeaderValue("c");

                    if (strContent == null || strContent.length == 0) {
                        Log.e(NgnSipService.TAG, "Invalid MESSAGE");
                        imSession.reject();
                        imSession.decRef();
                        return 0;
                    }

                    imSession.accept();

                    if (strContent != null) {
                        String date = message.getSipHeaderValue("Date");
//						mSipService.broadcastMessagingEvent(
//								new NgnMessagingEventArgs(_session.getId(),
//										NgnMessagingEventTypes.INCOMING, e
//												.getPhrase(), strContent,
//										contentType), from, date);
                        NgnSipCallBackHelper
                                .getInstence()
                                .MessageState(
                                        new NgnMessagingEventArgs(
                                                _session.getId(),
                                                NgnMessagingEventTypes.INCOMING,
                                                e.getPhrase(), strContent,
                                                contentType), from);
                    }

                    return 0;
                }
            }
            // end gle add

            final tsip_message_event_type_t type = e.getType();
            MessagingSession _session;

            switch (type) {
                case tsip_ao_message:
                    _session = e.getSession();
                    message = e.getSipMessage();
                    short code = e.getCode();
                    if (_session != null && code >= 200 && message != null) {
//					mSipService
//							.broadcastMessagingEvent(
//									new NgnMessagingEventArgs(
//											_session.getId(),
//											(code >= 200 && code <= 299) ? NgnMessagingEventTypes.SUCCESS
//													: NgnMessagingEventTypes.FAILURE,
//											e.getPhrase(), new byte[0], null),
//									message.getSipHeaderValue("f"),
//									NgnDateTimeUtils.now());
                        NgnSipCallBackHelper
                                .getInstence()
                                .MessageState(
                                        new NgnMessagingEventArgs(
                                                _session.getId(),
                                                (code >= 200 && code <= 299) ? NgnMessagingEventTypes.SUCCESS
                                                        : NgnMessagingEventTypes.FAILURE,
                                                e.getPhrase(), new byte[0], null),
                                        message.getSipHeaderValue("f"));
                    }
                    break;
                case tsip_i_message:
                    message = e.getSipMessage();
                    _session = e.getSession();
                    NgnMessagingSession imSession;
                    if (_session == null) {
					/*
					 * "Server-side-session" e.g. Initial MESSAGE sent by the
					 * remote party
					 */
                        _session = e.takeSessionOwnership();
                    }

                    if (_session == null) {
                        Log.e(NgnSipService.TAG, "Failed to take session ownership");
                        return -1;
                    }
                    imSession = NgnMessagingSession.takeIncomingSession(
                            mSipService.mSipStack, _session, message);
                    if (message == null) {
                        imSession.reject();
                        imSession.decRef();
                        return 0;
                    }

                    String from = message.getSipHeaderValue("f");
                    String GM = ""; // sks add
                    String date = message.getSipHeaderValue("Date");
                    final String contentType = message.getSipHeaderValue("c");
                    final byte[] bytes = message.getSipContent();
                    byte[] content = null;

                    if (bytes == null || bytes.length == 0) {
                        Log.e(NgnSipService.TAG, "Invalid MESSAGE");
                        // imSession.reject(); // del by gle
                        // imSession.decRef();
                        // return 0;
                    }

                    imSession.accept();

                    if (bytes != null
                            && NgnStringUtils.equals(contentType,
                            NgnContentType.SMS_3GPP, true)) {
					/* ==== 3GPP SMSIP === */
                        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
                        buffer.put(bytes);
                        SMSData smsData = SMSEncoder.decode(buffer,
                                buffer.capacity(), false);
                        if (smsData != null) {
                            twrap_sms_type_t smsType = smsData.getType();
                            if (smsType == twrap_sms_type_t.twrap_sms_type_rpdata) {
							/* === We have received a RP-DATA message === */
                                long payLength = smsData.getPayloadLength();
                                String SMSC = message
                                        .getSipHeaderValue("P-Asserted-Identity");
                                String SMSCPhoneNumber;
                                String origPhoneNumber = smsData.getOA();

							/* Destination address */
                                if (origPhoneNumber != null) {
                                    from = NgnUriUtils
                                            .makeValidSipUri(origPhoneNumber);
                                } else if ((origPhoneNumber = NgnUriUtils
                                        .getValidPhoneNumber(from)) == null) {
                                    Log.e(NgnSipService.TAG,
                                            "Invalid destination address");
                                    return 0;
                                }

							/*
							 * SMS Center 3GPP TS 24.341 - 5.3.2.4 Sending a
							 * delivery report The address of the IP-SM-GW is
							 * received in the P-Asserted-Identity header in the
							 * SIP MESSAGE request including the delivered short
							 * message.
							 */
                                if ((SMSCPhoneNumber = NgnUriUtils
                                        .getValidPhoneNumber(SMSC)) == null) {
                                    SMSC = NgnEngine
                                            .getInstance()
                                            .getConfigurationService()
                                            .getString(
                                                    NgnConfigurationEntry.RCS_SMSC,
                                                    NgnConfigurationEntry.DEFAULT_RCS_SMSC);
                                    if ((SMSCPhoneNumber = NgnUriUtils
                                            .getValidPhoneNumber(SMSC)) == null) {
                                        Log.e(NgnSipService.TAG,
                                                "Invalid IP-SM-GW address");
                                        return 0;
                                    }
                                }

                                if (payLength > 0) {
								/* Send RP-ACK */
                                    RPMessage rpACK = SMSEncoder.encodeACK(
                                            smsData.getMR(), SMSCPhoneNumber,
                                            origPhoneNumber, false);
                                    if (rpACK != null) {
                                        long ack_len = rpACK.getPayloadLength();
                                        if (ack_len > 0) {
                                            buffer = ByteBuffer
                                                    .allocateDirect((int) ack_len);
                                            long len = rpACK.getPayload(buffer,
                                                    buffer.capacity());
                                            MessagingSession m = new MessagingSession(
                                                    mSipService.getSipStack());
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type",
                                                    NgnContentType.SMS_3GPP);
                                            m.addHeader(
                                                    "Content-Transfer-Encoding",
                                                    "binary");
                                            m.addCaps("+g.3gpp.smsip");
                                            m.send(buffer, len);
                                            m.delete();
                                        }
                                        rpACK.delete();
                                    }

								/* Get ascii content */
                                    buffer = ByteBuffer
                                            .allocateDirect((int) payLength);
                                    content = new byte[(int) payLength];
                                    smsData.getPayload(buffer, buffer.capacity());
                                    buffer.get(content);
                                } else {
								/* Send RP-ERROR */
                                    RPMessage rpError = SMSEncoder.encodeError(
                                            smsData.getMR(), SMSCPhoneNumber,
                                            origPhoneNumber, false);
                                    if (rpError != null) {
                                        long err_len = rpError.getPayloadLength();
                                        if (err_len > 0) {
                                            buffer = ByteBuffer
                                                    .allocateDirect((int) err_len);
                                            long len = rpError.getPayload(buffer,
                                                    buffer.capacity());

                                            MessagingSession m = new MessagingSession(
                                                    mSipService.getSipStack());
                                            m.setToUri(SMSC);
                                            m.addHeader("Content-Type",
                                                    NgnContentType.SMS_3GPP);
                                            m.addHeader("Transfer-Encoding",
                                                    "binary");
                                            m.addCaps("+g.3gpp.smsip");
                                            m.send(buffer, len);
                                            m.delete();
                                        }
                                        rpError.delete();
                                    }
                                }
                            } else {
							/* === We have received any non-RP-DATA message === */
                                if (smsType == twrap_sms_type_t.twrap_sms_type_ack) {
								/*
								 * Find message from the history (by MR) an
								 * update it's status
								 */
                                    MyLog.d(NgnSipService.TAG, "RP-ACK");
                                } else if (smsType == twrap_sms_type_t.twrap_sms_type_error) {
								/*
								 * Find message from the history (by MR) an
								 * update it's status
								 */
                                    MyLog.d(NgnSipService.TAG, "RP-ERROR");
                                }
                            }
                        }
                    } else if (NgnStringUtils.equals(contentType,
                            NgnContentType.TEXT_PLAIN, true)) {
                        MyLog.d("zhangjie:NgnSipService-OnMessagingEvent()-tsip_message_event_type_t = tsip_i_message",
                                "contentType = " + contentType);
					/* ==== text/plain === */
                        content = bytes;
                    } else if (NgnStringUtils.equals(contentType,
                            NgnContentType.CPIM, true)) {
                        MyLog.d(TAG, "CPIM閿熸枻鎷锋伅");
                        if (bytes == null || bytes.length == 0) {
                            String emptyMessage = "NS: MsgExt<http://www.message.com/msgExtensions/>\n"
                                    + "MsgExt.msgType: IM\n"
                                    + "MsgExt.msgReport: Yes\n"
                                    + "MsgExt.localMsgID: \n"
                                    + "Content-Type: text/plain\n\n" + "NULL\n";
                            content = emptyMessage.getBytes();
                            CPIMMessage cpimMessage = CPIMParser
                                    .parse(emptyMessage);
                            if (cpimMessage != null) {
                                GM = cpimMessage.getMsgType();
                            }
                        } else {
                            MyLog.d("===CPIM message===", new String(bytes));
                            CPIMMessage cpimMessage = CPIMParser.parse(new String(
                                    bytes));
                            /**
                             * modified by zhaohua on 20140324
                             * 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹鎭
                             * 敓钘夛紝閿熸枻鎷烽敓鏂ゆ嫹鏃堕敓鍔枻鎷烽敓鍙枻鎷烽敓鏂ゆ嫹
                             */
                            // content =
                            // cpimMessage.getContent().toString().getBytes();
                            content = bytes;
                            if (cpimMessage != null) {
                                GM = cpimMessage.getMsgType();
                            }
                        }
                        //
                        if (GM != null && GM.startsWith("GM")) {
                            GM = GM.replace("GM", "");
                            from = GM + "," + from;
                        }
                    } else if (NgnStringUtils.equals(contentType,
                            NgnContentType.TEXT_PUSHINFO_GROUP, true)) { // 缇ら敓鏂ゆ嫹鎭敓鏂ゆ嫹閿熸枻鎷?
                        MyLog.e("sks:NgnSipService-OnMessagingEvent()-tsip_message_event_type_t = tsip_i_message",
                                "contentType = " + contentType);
                        content = bytes;
                    } else if (NgnStringUtils.equals(contentType,
                            NgnContentType.TEXT_PUSHINFO_PERSON, true)) { // 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋伅閿熸枻鎷烽敓鏂ゆ嫹
                        MyLog.e("sks:NgnSipService-OnMessagingEvent()-tsip_message_event_type_t = tsip_i_message",
                                "contentType = " + contentType);
                        content = bytes;
                    } else if (NgnStringUtils.equals(contentType,
                            NgnContentType.MSG_GROUP_INVITE, true)) {
                        content = bytes;
                    }

				/* Alert the user and add the message to the history */
                    if (content != null) {
                        // 閿熸枻鎷烽敓鏂ゆ嫹鎭敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熼叺骞挎挱
                        NgnMessagingEventArgs mNgnMessagingEventArgs = new NgnMessagingEventArgs(
                                _session.getId(), NgnMessagingEventTypes.INCOMING,
                                e.getPhrase(), content, contentType);
                        mNgnMessagingEventArgs.setmBranchID(message
                                .getSipHeaderParamValue("Via", "branch"));
                        mNgnMessagingEventArgs.setmTo(message
                                .getSipHeaderValue("t"));
                        MyLog.d(TAG,
                                "content != null and then broadcastMessagingEvent");
//					mSipService.broadcastMessagingEvent(mNgnMessagingEventArgs,
//							from, date);
                        NgnSipCallBackHelper.getInstence().MessageState(
                                mNgnMessagingEventArgs, from);
                    }

                    break;
            }

            return 0;
        }

        @Override
        public int OnStackEvent(StackEvent e) {
            // final String phrase = e.getPhrase();
            final short code = e.getCode();
            switch (code) {
                case tinyWRAPConstants.tsip_event_code_stack_started:
                    mSipService.mSipStack.setState(STACK_STATE.STARTED);
                    MyLog.d(NgnSipService.TAG, "Stack started");
                    break;
                case tinyWRAPConstants.tsip_event_code_stack_failed_to_start:
                    mSipService.closeSipStack();
                    final String phrase = e.getPhrase();
                    Log.e(TAG, String.format(
                            "Failed to start the stack. \nAdditional info:\n%s",
                            phrase));
                    break;
                case tinyWRAPConstants.tsip_event_code_stack_failed_to_stop:
                    Log.e(TAG, "Failed to stop the stack");
                    break;
                case tinyWRAPConstants.tsip_event_code_stack_stopped:
                    mSipService.mSipStack.setState(STACK_STATE.STOPPED);
                    MyLog.d(TAG, "Stack stopped");
                    break;
                case tinyWRAPConstants.tsip_event_code_stack_disconnected:
                    mSipService.mSipStack.setState(STACK_STATE.DISCONNECTED);
                    MyLog.d(TAG, "Stack disconnected");
                    break;
            }
            return 0;
        }

        @Override
        public int OnSubscriptionEvent(SubscriptionEvent e) {
            final tsip_subscribe_event_type_t type = e.getType();
            SubscriptionSession _session = e.getSession();

            switch (type) {
                case tsip_i_notify: {
                    final short code = e.getCode();

                    Log.d("NgnSipService",
                            "NgnSipService----OnSubscriptionEvent----tsip_i_notify");
                    final String phrase = e.getPhrase();
                    final SipMessage message = e.getSipMessage();
                    if (message == null || _session == null) {
                        return 0;
                    }
                    final String mFrom = message.getSipHeaderValue("From");
                    final String mState = message
                            .getSipHeaderValue("Subscription-State");

                    final String contentType = message.getSipHeaderValue("c");
                    MyLog.i(TAG, "" + contentType);
                    final byte[] content = message.getSipContent();
                    if (content == null || content.length == 0) {
                        Log.d("NgnSipService", "content == null");
                    } else {
                        String notifyContent = new String(content);
                        Log.d("NgnSipService", "notifyContent===" + notifyContent
                                + "; contentType===" + contentType);
                    }
                    if (NgnStringUtils.equals(contentType, NgnContentType.REG_INFO,
                            true)) {
                        // mReginfo = content;
                    } else if (NgnStringUtils.equals(contentType,
                            NgnContentType.WATCHER_INFO, true)) {
                        // mWInfo = content;
                    }

                    NgnSubscriptionSession ngnSession = NgnSubscriptionSession
                            .getSession(_session.getId());
                    NgnSubscriptionEventArgs eargs = new NgnSubscriptionEventArgs(
                            _session.getId(),
                            NgnSubscriptionEventTypes.INCOMING_NOTIFY, code,
                            phrase, content, contentType,
                            ngnSession == null ? EventPackageType.None : ngnSession
                                    .getEventPackage());
                    eargs.setState(mFrom, mState);
                    MyLog.d(TAG, "发送订阅数据  5");

//                    mSipService.broadcastSubscriptionEvent(eargs);
                    NgnSipCallBackHelper.getInstence().SubscriptionState(eargs,"");


                    break;
                }

                case tsip_ao_notify:
                    MyLog.d(TAG, "tsip_ao_notify");
                    break;
                case tsip_i_subscribe:
                    MyLog.d(TAG, "tsip_i_subscribe");
                    break;
                case tsip_ao_subscribe:
                    // SipMessage message = e.getSipMessage();
                    // if(message != null){
                    // String code =
                    // String contactHeader = message.getSipHeaderValue("Contact");
                    //
                    // }
                    MyLog.d(TAG, "tsip_ao_subscribe");
                    break;
                case tsip_i_unsubscribe:
                    MyLog.d(TAG, "tsip_i_unsubscribe");
                    break;
                case tsip_ao_unsubscribe:
                    MyLog.d(TAG, "tsip_ao_unsubscribe");
                    break;
                default: {
                    break;
                }
            }

            return 0;
        }

        @Override
        public int OnOptionsEvent(OptionsEvent e) {
            final tsip_options_event_type_t type = e.getType();
            OptionsSession ptSession = e.getSession();

            switch (type) {
                case tsip_i_options:
                    if (ptSession == null) { // New session
                        if ((ptSession = e.takeSessionOwnership()) != null) {
                            ptSession.accept();
                            ptSession.delete();
                        }
                    }
                    break;
                default:
                    break;
            }
            return 0;
        }

        @Override
        public int OnRegistrationEvent(RegistrationEvent e) {
            final SipMessage message = e.getSipMessage();
            if (message != null && message.isResponse()) { // 閿熸枻鎷峰綍閿熺即鐧告嫹
                // short code = message.getResponseCode(); //200
                // String phase = message.getResponsePhrase(); //OK
                String Service_Route = message
                        .getSipHeaderValue("Service-Route"); // <sip:orig@scscf.sunkaisens.com:6060;lr>
                if (Service_Route != null && Service_Route.indexOf("<") != -1
                        && Service_Route.indexOf(">") != -1) {
                    Service_Route = Service_Route.substring(
                            Service_Route.indexOf("<") + 1,
                            Service_Route.indexOf(">")); // sip:orig@scscf.sunkaisens.com:6060;lr
                }
                mConfigurationService.putString(
                        NgnConfigurationEntry.SERVICE_ROUTE, Service_Route); // 閿熸枻鎷烽敓鏂ゆ嫹澶撮敓鏂ゆ嫹鎭?
                String From = message.getSipHeaderValue("From");
                //sip:19800005001@sunkaisens.com
                String To = message.getSipHeaderValue("To");
                //sip:19800005001@sunkaisens.com
                String Via = message.getSipHeaderValue("Via"); //SIP/2.0/UDP
//                 192.168.1.240:55885;rport;branch=z9hG4bK-1525448351
            }

            return 0;
        }

        // private boolean contactMessage(String longInfoId, String
        // longInfoCount) {
        //
        // }
    }
}
