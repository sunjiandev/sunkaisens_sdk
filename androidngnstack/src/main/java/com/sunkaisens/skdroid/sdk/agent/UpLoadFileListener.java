package com.sunkaisens.skdroid.sdk.agent;

public interface UpLoadFileListener {

	void onProgress(String progress);

	void onSuccess();

	void onFailure(Throwable arg1);
}
