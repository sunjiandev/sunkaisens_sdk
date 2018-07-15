package org.doubango.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

/**
 * 带日志文件输入的，又可控开关的日志调试
 * 
 * @author BaoHang
 * @version 1.0
 * @data 2012-2-20
 */
public class MyLog {
	private static Boolean MYLOG_SWITCH = true; // 日志文件总开关
	private static Boolean MYLOG_WRITE_TO_FILE = false;// 日志写入文件开关
	private static Boolean MYLOG_WRITE_TO_FILE_SYS = true;// 系统日志写入文件开关

	private static char MYLOG_TYPE = 'v';// 输入日志类型，w代表只输出告警信息等，v代表输出所有信息
	public static String MYLOG_PATH_SDCARD_DIR = "/mnt/asec/skdroid/log"; // 日志文件在sdcard中的路径
																			// /data/data/skdroid/log
																			// 大终端环境
	private static int SDCARD_LOG_FILE_SAVE_DAYS = 0;// sd卡中日志文件的最多保存天数
	private static String MYLOG_FILE_NAME = "SKSLog.txt";// 本类输出的日志文件名称
	private static SimpleDateFormat myLogSdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");// 日志的输出格式
	private static SimpleDateFormat logfile = new SimpleDateFormat("yyyy-MM-dd");// 日志文件格式

	public MyLog() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 初始化日志存储路径
	 * 
	 * @param dir
	 */
	public static void init(String dir) {
		if (!GlobalSession.bSocketService) {
			MYLOG_PATH_SDCARD_DIR = dir + "/syslogs/"
					+ logfile.format(new Date()) + "/";
			MYLOG_WRITE_TO_FILE_SYS = NgnEngine
					.getInstance()
					.getConfigurationService()
					.getBoolean(
							NgnConfigurationEntry.LOGS_WRITE_TO_FILE_SYS_OPEN,
							true);
		} else {

		}
		MyLog.d("", "日志文件路径：" + MYLOG_PATH_SDCARD_DIR);
		MyLog.d("", "Open the syslog?" + MYLOG_WRITE_TO_FILE_SYS);

		//MYLOG_WRITE_TO_FILE_SYS = false;

		if (MYLOG_WRITE_TO_FILE_SYS) {
			writeSystemLogToFile();
		}

	}

	public static void w(String tag, Object msg) { // 警告信息
		log(tag, msg.toString(), 'w');
	}

	public static void e(String tag, Object msg) { // 错误信息
		log(tag, msg.toString(), 'e');
	}

	public static void d(String tag, Object msg) {// 调试信息
		log(tag, msg.toString(), 'd');
	}

	public static void i(String tag, Object msg) {//
		log(tag, msg.toString(), 'i');
	}

	public static void v(String tag, Object msg) {
		log(tag, msg.toString(), 'v');
	}

	public static void w(String tag, String text) {
		log(tag, text, 'w');
	}

	public static void e(String tag, String text) {
		log(tag, text, 'e');
	}

	public static void d(String tag, String text) {
		log(tag, text, 'd');
	}

	public static void i(String tag, String text) {
		log(tag, text, 'i');
	}

	public static void v(String tag, String text) {
		log(tag, text, 'v');
	}

	/**
	 * 根据tag, msg和等级，输出日志
	 * 
	 * @param tag
	 * @param msg
	 * @param level
	 * @return void
	 * @since v 1.0
	 */
	private static void log(String tag, String msg, char level) {
		if (MYLOG_SWITCH) {
			if ('e' == level && ('e' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) { // 输出错误信息
				Log.e(tag, msg);
			} else if ('w' == level && ('w' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
				Log.w(tag, msg);
			} else if ('d' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
				Log.d(tag, msg);
			} else if ('i' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)) {
				Log.i(tag, msg);
			} else {
				Log.v(tag, msg);
			}
			// if (MYLOG_WRITE_TO_FILE)
			// writeLogtoFile(String.valueOf(level), tag, msg);
		}
	}

	/**
	 * 打开日志文件并写入日志
	 * 
	 * @return
	 */
	private static void writeLogtoFile(String mylogtype, String tag, String text) { // 新建或打开日志文件

		if (MYLOG_PATH_SDCARD_DIR == null
				|| MYLOG_PATH_SDCARD_DIR.startsWith("null")) {
			return;
		}

		Date nowtime = new Date();
		String needWriteFile = logfile.format(nowtime);
		String needWriteMessage = myLogSdf.format(nowtime) + "    " + mylogtype
				+ "    " + tag + "    " + text;
		// File file = new File(MYLOG_PATH_SDCARD_DIR, needWriteFile + "_" +
		// MYLOG_FILE_NAME);
		FileWriter filerWriter = null;
		try {
			File dir = new File(MYLOG_PATH_SDCARD_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(MYLOG_PATH_SDCARD_DIR, needWriteFile + "_"
					+ MYLOG_FILE_NAME);
			filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(needWriteMessage);
			bufWriter.newLine();
			bufWriter.close();

		} catch (FileNotFoundException f) {
			MyLog.d("", "日志文件不存在，日志停止写入文件");
			MYLOG_WRITE_TO_FILE = false;
			f.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			if (filerWriter != null) {
				try {
					filerWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeSystemLogToFile() {
		if (GlobalSession.bSocketService) {
			MYLOG_WRITE_TO_FILE_SYS = false;
			MyLog.d("", "大终端版本，不保存系统日志");
		} else {
			MyLog.d("", "保存系统日志");
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String comm = "logcat";

				Process process;
				InputStreamReader isr = null;
				FileWriter filerWriter = null;
				try {
					process = Runtime.getRuntime().exec(comm);
					isr = new InputStreamReader(process.getInputStream());
					BufferedReader br = new BufferedReader(isr);
					String res = "";
					File dir = new File(MYLOG_PATH_SDCARD_DIR);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					int fileNum = 0;
					int loglines = 0;
					SimpleDateFormat appStartTime = new SimpleDateFormat(
							"HHmmss");
					if (GlobalVar.mAppStartTime == null) {
						GlobalVar.mAppStartTime = new Date();
					}
					File file = new File(MYLOG_PATH_SDCARD_DIR,
							appStartTime.format(GlobalVar.mAppStartTime)
									+ "_systemLogs_0.log");
					// MyLog.d("", "系统日志路径 : "+file.getAbsolutePath());
					filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
					BufferedWriter bufWriter = new BufferedWriter(filerWriter);
					while ((res = br.readLine()) != null) {
						if (!MYLOG_WRITE_TO_FILE_SYS) {
							return;
						}

						if (loglines > 15000) {
							fileNum++;
							loglines = 0;
							file = new File(MYLOG_PATH_SDCARD_DIR,
									appStartTime
											.format(GlobalVar.mAppStartTime)
											+ "_systemLogs_" + fileNum + ".log");
							filerWriter = new FileWriter(file, true);
							bufWriter = new BufferedWriter(filerWriter);
						}

						Date nowtime = new Date();
						String needWriteMessage = myLogSdf.format(nowtime)
								+ "-" + res;
						bufWriter.write(needWriteMessage);
						bufWriter.newLine();
						loglines++;
					}

					bufWriter.close();
				} catch (FileNotFoundException f) {
					MyLog.d("", "系统日志文件路径不存在，停止写入系统日志");
					MYLOG_WRITE_TO_FILE_SYS = false;
					f.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					try {
						if (isr != null) {
							isr.close();
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
					try {
						if (filerWriter != null) {
							filerWriter.close();
						}
					} catch (Exception e2) {
						// TODO: handle exception
					}
				}
			}
		}, "SystemLog").start();

	}

	/**
	 * 删除制定的日志文件
	 */
	public static void delFile() { // 删除日志文件
		String needDelFile = logfile.format(getDateBefore());
		File file = new File(MYLOG_PATH_SDCARD_DIR, needDelFile
				+ MYLOG_FILE_NAME);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 得到现在时间前的几天日期，用来得到需要删除的日志文件名
	 */
	private static Date getDateBefore() {
		Date nowtime = new Date();
		Calendar now = Calendar.getInstance();
		now.setTime(nowtime);
		now.set(Calendar.DATE, now.get(Calendar.DATE)
				- SDCARD_LOG_FILE_SAVE_DAYS);
		return now.getTime();
	}

	/**
	 * 是否将日志保存到文件中的开关
	 * 
	 * @param mYLOG_WRITE_TO_FILE
	 */
	public static void setMYLOG_WRITE_TO_FILE(Boolean mYLOG_WRITE_TO_FILE) {
		MYLOG_WRITE_TO_FILE = mYLOG_WRITE_TO_FILE;
	}

	/**
	 * 日志打印总开关
	 * 
	 * @param mYLOG_SWITCH
	 */
	public static void setMYLOG_SWITCH(Boolean mYLOG_SWITCH) {
		MYLOG_SWITCH = mYLOG_SWITCH;
	}

	public static Boolean getMYLOG_WRITE_TO_FILE_SYS() {
		return MYLOG_WRITE_TO_FILE_SYS;
	}

	public static void setMYLOG_WRITE_TO_FILE_SYS(
			Boolean mYLOG_WRITE_TO_FILE_SYS) {
		MYLOG_WRITE_TO_FILE_SYS = mYLOG_WRITE_TO_FILE_SYS;
	}

}
