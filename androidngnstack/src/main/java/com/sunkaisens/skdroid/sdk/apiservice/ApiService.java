package com.sunkaisens.skdroid.sdk.apiservice;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

import com.sunkaisens.skdroid.util.GlobalVar;

public interface ApiService {
	/**
	 * 获取业务能力
	 * 
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("serviceabilities")
	Observable<String> getServiceAbilities();
	/**
	 * 获取组类型
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("grouptypes")
	Observable<String> getGroupTypes();

	/**
	 * 获取组的使用类型*
	 * 
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("usetypes")
	Observable<String> getUseTypes();

	/**
	 * 查询开户信息
	 * 
	 * @param keyword
	 *            关键字 uri name nikename
	 * @return
	 * 
	 *         ok
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("users")
	Observable<String> getUserInfor(@Query("keyword") String keyword);

	/**
	 * 查询一个组
	 * 
	 * @param groupNo
	 *            组号
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("groups/groupNo/{groupNo}/entrys")
	Observable<String> getGroupInfor(@Path("groupNo") String groupNo);

	/**
	 * 查询好友列表
	 * 
	 * @param sipuri
	 *            sip账号 ok
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("friends/{uri}/list")
	Observable<String> getContactList(@Path("uri") String sipuri);

	/**
	 * 获取所有组包括组成员*
	 * 
	 * @param groupType
	 *            组类型
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("groups/list/groupType/{groupType}")
	Observable<String> getAllGroup(@Path("groupType") String groupType);

	/**
	 * 创建一个组
	 * 
	 * @param body
	 *            上传的json数据
	 * @return
	 */
	@Headers({ GlobalVar.ASSERTED, GlobalVar.CONTEN_TYPE })
	@POST("groups")
	Observable<String> createGroup(@Body String body);

	/**
	 * 更新组信息
	 * 
	 * @param body
	 * @return
	 */
	@Headers({ GlobalVar.ASSERTED, GlobalVar.CONTEN_TYPE })
	@PUT("groups")
	Observable<String> updateGroup(@Body String body);

	/**
	 * 删除组
	 * 
	 * @param groupNo
	 *            组号
	 * 
	 *            ok
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@DELETE("groups/groupNo/{groupNo}")
	Observable<String> deleteGroup(@Path("groupNo") String groupNo);

	/**
	 * 根据组号获取组成员
	 * 
	 * @param groupNo
	 * @return
	 * 
	 *         ok
	 */
	@Headers(GlobalVar.ASSERTED)
	@GET("groups/groupNo/{groupNo}/entrys")
	Observable<String> getGroupMem(@Path("groupNo") String groupNo);

	/**
	 * 根据组号添加组成员
	 * 
	 * @param groupNo
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@POST("groups/groupNo/{groupNo}/entrys")
	Observable<String> addGroupMem(@Path("groupNo") String groupNo,
			@Body String body);

	/**
	 * 根据组好删除组成员
	 * 
	 * @param groupNo
	 *            组号
	 * @param params
	 *            组成员的sip 账号
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@DELETE("groups/groupNo/{groupNo}/entrys")
	Observable<String> delGroupMem(@Path("groupNo") String groupNo,
			@Query("params") String params);

	/**
	 * 添加好友
	 * 
	 * @param groupNo
	 *            自己的sip账号
	 * @param friendUri
	 *            添加好友的sip账号
	 * 
	 *            ok
	 */
	@Headers(GlobalVar.ASSERTED)
	@POST("friends/{myUri}/friend/{friendUri}")
	Observable<String> addContact(@Path("myUri") String groupNo,
			@Path("friendUri") String friendUri);

	/**
	 * 更新联系人信息
	 * 
	 * @param groupNo
	 *            自己的sip账号
	 * @param friendUri
	 *            添加好友的sip账号
	 * @param body
	 *            修改备注信息
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@PUT("friends/{myUri}/friend/{friendUri}")
	Observable<String> updateContact(@Path("myUri") String groupNo,
			@Query("friendUri") String friendUri, @Body String body);

	/**
	 * 删除好友
	 * 
	 * @param groupNo
	 *            自己的sip账号
	 * @param friendUri
	 *            好友的sip账号
	 * @return
	 */
	@Headers(GlobalVar.ASSERTED)
	@DELETE("friends/{myUri}/friend/{friendUri}")
	Observable<String> delContact(@Path("myUri") String groupNo,
			@Path("friendUri") String friendUri);

	/**
	 * 文件发送
	 * 
	 * @param myUri
	 *            自己的登录账号
	 * @param remoteUri
	 *            对端的登录账户
	 * @param isgroup
	 *            是否是群组
	 * @param file
	 * @return
	 */

	// http://192.168.1.10:8010/files/Default_picture_8.jpg;from=19800005005;to=19800005004;isgroup=false
	@Headers({ GlobalVar.UPLOAD_FILE_CON, GlobalVar.UPLOAD_FILE_TYPE })
	@Multipart
	@POST("files/{filename};from={myUri};to={remoteUri};isgroup={isgroup}")
	Observable<String> sendFileMsg(@Path("filename") String filename,
			@Path("myUri") String myUri, @Path("remoteUri") String remoteUri,
			@Path("isgroup") boolean isgroup, @Part MultipartBody.Part file);

	/**
	 *
	 * @param url 文件下载
	 * @return
	 */
	@Headers(GlobalVar.UPLOAD_FILE_CON)
	@GET("files/{uri}")
	Call<ResponseBody> downloadFile(@Path("uri") String url);

}
