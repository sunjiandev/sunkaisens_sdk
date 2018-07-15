package com.sunkaisens.skdroid.sdk.agent;

public interface DownloadLListener {

	void downloadSuccess();

	void downloadProgress(String progress);

	void downloadFailed(Throwable throwable);
}
