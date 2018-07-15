package com.sunkaisens.skdroid.sdk;

import org.doubango.utils.MyLog;

import rx.Subscriber;


public abstract class DefaultSubscriber<T> extends Subscriber<T> {
	private final String TAG = DefaultSubscriber.class.getSimpleName();

	@Override
	public void onCompleted() {
		if (!isUnsubscribed()) {
			unsubscribe();
		}
	}

	@Override
	public void onError(final Throwable e) {
		MyLog.e(TAG, e);
		onCompleted();

	}

}