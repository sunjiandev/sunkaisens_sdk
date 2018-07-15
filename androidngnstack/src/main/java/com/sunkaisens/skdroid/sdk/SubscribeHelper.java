package com.sunkaisens.skdroid.sdk;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import android.util.Log;

public class SubscribeHelper {

	private static final String TAG = SubscribeHelper.class.getCanonicalName();

	public static String mIdentity = NgnEngine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.IDENTITY_IMPI,
					NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);// 自己的登陆账号

	private static String mNetworkRealm = NgnEngine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.NETWORK_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_REALM);// 域名
	
	private static SubscribeHelper helper;

	private SubscribeHelper() {
	}

	public static synchronized SubscribeHelper getInstance() {
		if (helper == null) {
			return new SubscribeHelper();
		} else {
			return helper;
		}
	}

	/**
	 * 订阅开户信息
	 * 
	 * @return
	 */
	public boolean subscribePersionInfo() {
		try {
			Log.d(TAG, "个人信息订阅");
			final NgnSubscriptionSession subscriptionSessionPublic = NgnSubscriptionSession
					.createOutgoingSession(NgnEngine.getInstance()
							.getSipService().getSipStack(), "sip:" + mIdentity
							+ "@" + mNetworkRealm, "sip:ims-pim@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			MyLog.d(TAG,
					"subscribePersionInfo--->"
							+ subscriptionSessionPublic.toString());
			subscriptionSessionPublic.subscribe();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 业务组成员变更
	 * 
	 * @return
	 */
	public boolean subscribeServiceGroup() {
		try {
			MyLog.d(TAG, "subscribeServiceGroup");
			final NgnSubscriptionSession subscriptionSessionService = NgnSubscriptionSession
					.createOutgoingSession(NgnEngine.getInstance()
							.getSipService().getSipStack(), "sip:" + mIdentity
							+ "@" + mNetworkRealm, "sip:service-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			MyLog.d(TAG, "subscribeServiceGroup--->"
					+ subscriptionSessionService.toString());
			subscriptionSessionService.subscribe();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 公共组成员变更
	 * 
	 * @return
	 */
	public boolean subscribeGlobalGroup() {
		try {
			MyLog.d(TAG, "subscribeGlobalGroup");

			final NgnSubscriptionSession subscriptionSessionService = NgnSubscriptionSession
					.createOutgoingSession(NgnEngine.getInstance()
							.getSipService().getSipStack(), "sip:" + mIdentity
							+ "@" + mNetworkRealm, "sip:global-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			MyLog.d(TAG, "subscribeGlobalGroup--->"
					+ subscriptionSessionService.toString());
			subscriptionSessionService.subscribe();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 订阅组 信息变更
	 * 
	 * @return
	 */

	public boolean subscribeSubscribeGroup() {
		try {
			MyLog.d(TAG, "subscribeSubscribeGroup");

			final NgnSubscriptionSession subscriptionSessionService = NgnSubscriptionSession
					.createOutgoingSession(NgnEngine.getInstance()
							.getSipService().getSipStack(), "sip:" + mIdentity
							+ "@" + mNetworkRealm, "sip:subscribe-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			MyLog.d(TAG, "subscribeSubscribeGroup--->"
					+ subscriptionSessionService.toString());
			subscriptionSessionService.subscribe();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 订阅公共组
	 * 
	 * @return
	 */
	public boolean subscribePublicGroup() {

		try {
			Log.d(TAG, "subscribePublicGroup");
			final NgnSubscriptionSession subscriptionSessionPublic = NgnSubscriptionSession
					.createOutgoingSession(NgnEngine.getInstance()
							.getSipService().getSipStack(), "sip:" + mIdentity
							+ "@" + mNetworkRealm, "sip:public-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			subscriptionSessionPublic.subscribe();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
