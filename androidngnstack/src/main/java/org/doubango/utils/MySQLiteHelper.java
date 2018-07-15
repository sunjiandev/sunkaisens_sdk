package org.doubango.utils;

import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryAVCallEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.model.NgnHistorySMSEvent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	
	private static final String TAG = MySQLiteHelper.class.getSimpleName(); 
	
	private static final String TABLE_AV = "user_av_history";
	private static final String TABLE_SMS = "user_sms_history"; 
	private static final String TABLE_PUSH = "user_push_history"; 
	private static final String TABLE_MSRP = "user_msrp_history"; 
	
	private String CREATE_AV_TABLE = "create table "+TABLE_AV+"(_id integer "
			+ "primary key,"
			+ "mLocalParty varchar(30),mRemoteParty varchar(30),mDisplayName varchar(30),"
			+ "mSessionType varchar(5),mMediaType varchar(10),mCallStartTime varchar(10),"
			+ "mStartTime varchar(10),mEndTime varchar(10),mSeen varchar(5),mStatus varchar(10))";
	private String CREATE_SMS_TABLE = "create table "+TABLE_SMS+"(id integer primary key,localmsgID varchar(50),"
			+ "status varchar(5),mLocalParty varchar(20),mRemoteParty varchar(20),mDisplayName varchar(20),"
			+ "mMediaType varchar(5),seen varchar(5),start varchar(10),end varchar(10),isRead varchar(5),"
			+ "sessiontype varchar(5),"
			+ "isdraft varchar(5),content varchar(200),draftcontent varchar(200),gmmember varchar(20));";
	private String CREATE_PUSH_TABLE = "create table "+TABLE_PUSH+"(localmsgID "
			+ "primary,"
			+ "classname,status,localparty,type,remote,seen,end,start,"
			+ "sessiontype,isaccepted,content,draftcontent,gmmember,isdraft)";
	private String CREATE_MSRP_TABLE = "create table "+TABLE_MSRP+"(localmsgID "
			+ "primary,"
			+ "classname,status,localparty,type,remote,seen,end,start,"
			+ "sessiontype,isaccepted,content,draftcontent,gmmember,isdraft)";

	public MySQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_AV_TABLE);
		Log.i(TAG, "Create table '"+TABLE_AV+"'.");
		
		db.execSQL(CREATE_SMS_TABLE);
		Log.i(TAG, "Create table '"+TABLE_SMS+"'.");
		
//		db.execSQL(CREATE_PUSH_TABLE);
//		Log.i(TAG, "Create table '"+TABLE_PUSH+"'.");
//		
//		db.execSQL(CREATE_MSRP_TABLE);
//		Log.i(TAG, "Create table '"+TABLE_MSRP+"'.");
	}
	
	/**
	 * 将通话历史记录存到数据库
	 * @param avEvent
	 * @return  出现异常时返回false
	 */
	public synchronized boolean insertAVEvent(NgnHistoryAVCallEvent avEvent){
		MyLog.d(TAG, "insertAVEvent()");
		String sql = "insert into "+TABLE_AV+"(mLocalParty,mRemoteParty,"
				+ "mDisplayName,mSessionType,mMediaType,mCallStartTime,"
				+ "mStartTime,mEndTime,mSeen,mStatus)"
				+ " values(?,?,?,?,?,?,?,?,?,?)";
		try {
			getWritableDatabase().execSQL(sql,avEvent.getAllValuesNoId());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 将通话历史记录存到数据库
	 * @param avEvent
	 * @return  出现异常时返回false
	 */
	public synchronized boolean deleteAVEvent(NgnHistoryAVCallEvent avEvent){
		MyLog.d(TAG, "deleteAVEvent()");
//		String sql = "delete from "+TABLE_AV+" where mCallStartTime=?";
//		try {
//			getWritableDatabase().execSQL(sql,new String[]{String.valueOf(avEvent.mCallStartTime)});
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
		try {
			int count = getWritableDatabase().delete(TABLE_AV, "mCallStartTime=?", 
					new String[]{String.valueOf(avEvent.mCallStartTime)});
			MyLog.d(TAG, "删除记录条数="+count);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 删除当前用户本地所有通话历史记录
	 * @param mLocal  当前用户号码
	 * @return
	 */
	public synchronized boolean deleteAVEvents(String mLocal){
		MyLog.d(TAG, "deleteAVEvents()");
		String sql = "delete from "+TABLE_AV+" where mLocalParty=?";
//		try {
//			getWritableDatabase().execSQL(sql,new String[]{mLocal});
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
		try {
			int count = getWritableDatabase().delete(TABLE_AV, "mLocalParty=?", new String[]{mLocal});
			MyLog.d(TAG, "删除记录数目="+count);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 查询当前用户所有通话记录
	 * @param mlocal 当前用户号码
	 * @return
	 */
	public List<NgnHistoryAVCallEvent> queryAVEvents(String mlocal){
		
		MyLog.d(TAG, "queryAVEvents()");
		
		if(mlocal == null){
			MyLog.d(TAG, "mlocal is null.");
			return null;
		}
		
		List<NgnHistoryAVCallEvent> results = new ArrayList<NgnHistoryAVCallEvent>();
		String sql = "select * from "+TABLE_AV+" where mLocalParty=?;";
		
//		mLocalParty,mRemoteParty,mDisplayName,mSessionType,mMediaType,"
//		"mCallStartTime,mStartTime,mEndTime,mSeen,mStatus
		Cursor cursor = null;
		try {
			cursor = getReadableDatabase().rawQuery(sql, new String[]{mlocal});
			MyLog.d(TAG, "数据库查询成功【AV】("+mlocal+")  条数="+cursor.getCount());
		} catch (Exception e) {
			MyLog.d(TAG, "数据库查询失败");
			e.printStackTrace();
		}
		if(cursor != null){
			while(cursor.moveToNext()){
				NgnHistoryAVCallEvent result = new NgnHistoryAVCallEvent();
//				result.mLocalMsgID = cursor.getString(0);
				result.mLocalParty = mlocal;
				result.mRemoteParty = cursor.getString(2);
				result.mDisplayName = cursor.getString(3);
				result.mSessionType = cursor.getInt(4);
				result.mMediaType = NgnMediaType.valueOf(cursor.getString(5));
				result.mCallStartTime = cursor.getLong(6);
				result.mStartTime = cursor.getLong(7);
				result.mEndTime = cursor.getLong(8);
				result.mSeen = Boolean.valueOf(cursor.getString(9));
				result.mStatus = StatusType.valueOf(cursor.getString(10));
				results.add(result);
			}
			cursor.close();
		}
		return results;
		
	}
	
	/**
	 * 将即时消息历史记录存到数据库 
	 * @param smsEvent
	 * @return  出现异常时返回false
	 */
	public synchronized boolean insertSMSEvent(NgnHistorySMSEvent smsEvent){
		MyLog.d(TAG, "insertSMSEvent()");
		String sql = "insert into "+TABLE_SMS+"(id,localmsgID,"
			+ "status,mLocalParty,mRemoteParty,mDisplayName,"
			+ "mMediaType,seen,start,end,isRead,sessiontype,"
			+ "isdraft,content,draftcontent,gmmember)"
				+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
		try {
			getWritableDatabase().execSQL(sql,smsEvent.getAllValuesNoId());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 更新即时消息历史记录 
	 * @param
	 * @return  出现异常时返回false
	 */
	public synchronized boolean updateSMSEvent(NgnHistorySMSEvent event){
		MyLog.d(TAG, "updateSMSEvent()");
		String sql = "update "+TABLE_SMS+" set seen=?,isRead=?,"
			+ "isdraft=?,content=?,draftcontent=?,gmmember=? where id=?;";
		String[] values = new String[]{
				String.valueOf(event.isSeen()),event.mIsRead,
				event.mIsDraft,event.mContent,event.mDraftString,event.mGMMember,
				event.id
		};
		ContentValues cv = new ContentValues();
		cv.put("seen", String.valueOf(event.isSeen()));
		cv.put("isRead", event.mIsRead);
		cv.put("isdraft", event.mIsDraft);
		cv.put("content", event.mContent);
		cv.put("draftcontent", event.mDraftString);
		cv.put("gmmember", event.mGMMember);
		try {
			int count = getWritableDatabase().update(TABLE_SMS, cv, "id=?", new String[]{
					event.id
			});
			MyLog.d(TAG, "更新记录数="+count);
//			getWritableDatabase().execSQL(sql,values);
		} catch (Exception e) {
			insertSMSEvent(event);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 将通话历史记录存到数据库
	 * @param
	 * @return  出现异常时返回false
	 */
	public synchronized boolean deleteSMSEvent(NgnHistorySMSEvent smsEvent){
		MyLog.d(TAG, "deleteSMSEvent()");
		String sql = "delete from "+TABLE_SMS+" where id=?";
		try {
//			getWritableDatabase().execSQL(sql,new String[]{String.valueOf(smsEvent.mStartTime)});
			int count = getWritableDatabase().delete(TABLE_SMS, "id=?", new String[]{smsEvent.id});
			MyLog.d(TAG, "删除记录条数="+count);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 删除当前用户本地所有即时消息历史记录
	 * @param mLocal 当前用户号码
	 * @return
	 */
	public synchronized boolean deleteSMSEvents(String mLocal){
		MyLog.d(TAG, "deleteSMSEvents("+mLocal+")");
		String sql = "delete from "+TABLE_SMS+" where mLocalParty=?";
		try {
//			getWritableDatabase().execSQL(sql,new String[]{mLocal});
			int count = getWritableDatabase().delete(TABLE_SMS, "mLocalParty=?", new String[]{mLocal});
			MyLog.d(TAG, "删除记录数目="+count);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public List<NgnHistorySMSEvent> querySMSEvents(String mlocal){
		
		MyLog.d(TAG, "querySMSEvents("+mlocal+")");
		
		if(mlocal == null){
			MyLog.d(TAG, "mlocal is null.");
			return null;
		}
		
		List<NgnHistorySMSEvent> results = new ArrayList<NgnHistorySMSEvent>();
		String sql = "select * from "+TABLE_SMS+" where mLocalParty=?;";
		
//		localmsgID,status,mLocalParty,mRemoteParty,mDisplayName,
//		mMediaType,seen,start,end,isRead,sessiontype,
//		isdraft,content,draftcontent,gmmember
		Cursor cursor = null;
		try {
			cursor = getReadableDatabase().rawQuery(sql, new String[]{mlocal});
			MyLog.d(TAG, "数据库查询成功【SMS】("+mlocal+")  条数="+cursor.getCount());
		} catch (Exception e) {
			MyLog.d(TAG, "数据库查询失败【SMS】");
			e.printStackTrace();
		}
		if(cursor != null){
			while(cursor.moveToNext()){
				NgnHistorySMSEvent result = new NgnHistorySMSEvent();
				result.id = cursor.getString(0);
				result.mLocalMsgID = cursor.getString(1);
				result.mStatus = StatusType.valueOf(cursor.getString(2));
				result.mLocalParty = mlocal;
				result.mRemoteParty = cursor.getString(4);
				result.mDisplayName = cursor.getString(5);
				result.mMediaType = NgnMediaType.valueOf(cursor.getString(6));
				result.mSeen = Boolean.valueOf(cursor.getString(7));
				result.mStartTime = cursor.getLong(8);
				result.mEndTime = cursor.getLong(9);
				result.mIsRead = cursor.getString(10);
				result.mSessionType = cursor.getInt(11);
				result.mIsDraft = cursor.getString(12);
				result.mContent = cursor.getString(13);
				result.mDraftString = cursor.getString(14);
				result.mGMMember = cursor.getString(15);
				
				
				results.add(result);
			}
			cursor.close();
		}
		return results;
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
