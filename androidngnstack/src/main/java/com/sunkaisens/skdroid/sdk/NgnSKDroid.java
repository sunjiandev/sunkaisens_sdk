package com.sunkaisens.skdroid.sdk;

import android.content.Context;

public class NgnSKDroid {

	private static Context context;

	private static NgnSKDroid skDroid;

	public static void init(Context context) {
		NgnSKDroid.context = context;

	}

	private NgnSKDroid() {
	}

	public static synchronized NgnSKDroid getInstence() {
		if (skDroid == null) {
			return new NgnSKDroid();

		} else {
			return skDroid;
		}
	}

	public Context getGlobleContext() {
		return context;
	}

}
