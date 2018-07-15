package com.sunkaisens.skdroid.sdk;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by sun on 2018/1/17.
 */

public class HttpClient {

	private static final int DEFAULT_TIMEOUT = 30;
	private static String BASE_URI = "http://192.168.1.231:1000/";
	private static HttpClient httpClient;

	private HttpClient() {

		INgnConfigurationService configurationService = NgnEngine.getInstance().getConfigurationService();
		String groupServerIp = configurationService.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM, "test.com");
		String groupPort = configurationService.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT, "1000");

		MyLog.d("sjy", "groupServerIp----->" + groupServerIp + "------groupPort:" + groupPort);
		BASE_URI = "http://" + groupServerIp + ":" + groupPort + "/";
	}

	public static synchronized HttpClient getHttpClient() {

		if (httpClient == null) {
			return new HttpClient();
		} else {
			return httpClient;
		}
	}

	public static <T> Subscription subscribeDefault(Observable<T> observable,
													Observer<T> subscriber) {
		return observable.subscribeOn(Schedulers.io())
				.unsubscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(subscriber);
	}

	public OkHttpClient getOkHttpClient() {
		return new OkHttpClient.Builder()
				.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).build();

	}

	public Retrofit getRetrofit() {
		ScalarsConverterFactory scalarsConverterFactory = ScalarsConverterFactory
				.create();
		return new Retrofit.Builder().baseUrl(BASE_URI)
				.addConverterFactory(scalarsConverterFactory)
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())// 支持RxJava
				.build();

	}

	public Retrofit getDownloadRetrofit(OkHttpClient client) {
		return new Retrofit.Builder().baseUrl(BASE_URI)
				.client(client)
				.addConverterFactory(ScalarsConverterFactory.create())
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.build();
	}

	/**
	 * 创建 OkHttpClient
	 */
	public OkHttpClient createOkHttpClient(Interceptor downloadInterceptor) {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
				.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
		if (downloadInterceptor != null) {
			builder.addNetworkInterceptor(downloadInterceptor);
		}

		return builder.build();
	}
}
