package com.sunkaisens.skdroid.util;

import com.sunkaisens.skdroid.sdk.NgnSKDroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {

	private static NetUtils netUtils;
	private ConnectivityManager manager;
	private Context context;

	private NetUtils() {

		context = NgnSKDroid.getInstence().getGlobleContext();
		if (manager == null) {
			manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
		}

	}

	public static synchronized NetUtils getInstance() {
		if (netUtils == null) {
			return new NetUtils();
		} else {
			return netUtils;
		}

	}

	public boolean isConnected() {
		try {
			if (manager != null) {
				NetworkInfo info = manager.getActiveNetworkInfo();
				if (info != null && info.isAvailable() && info.isConnected()) {
					// 当前网络是连接的
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						// 当前所连接的网络可用
						return true;
					}
				}
			}
		} catch (Exception e) {

		}
		return false;
	}

	public boolean isMobileConnected() {
		try {
			if (manager != null) {
				NetworkInfo info = manager.getActiveNetworkInfo();
				return info != null
						&& info.getType() == ConnectivityManager.TYPE_MOBILE;
			}
		} catch (Exception e) {
		}
		return false;
	}

}
