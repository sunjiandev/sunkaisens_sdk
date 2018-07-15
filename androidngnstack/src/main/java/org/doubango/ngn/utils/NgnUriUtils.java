/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
 *	
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 *
 * imsdroid is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *	
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.doubango.ngn.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.model.GroupInfor;
import org.doubango.ngn.model.NgnContact;
import org.doubango.tinyWRAP.SipUri;
import org.doubango.utils.MyLog;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.sunkaisens.skdroid.util.GlobalVar;

import android.util.Log;

//FIXME: THIS IS THE WORST CLASS YOU WILL FIND IN THE PROJECT ;)
public class NgnUriUtils {

	private static final String TAG = NgnUriUtils.class.getCanonicalName();
	
	public static String mGroupNum;
	public static String mMemberNum;

	public static String getGroupNum() {
		return mGroupNum;
	}

	public static String getMemberNum() {
		return mMemberNum;
	}

	public static void setGroupAndMemberNum(String notifyContent) {
		if (notifyContent.contains(":list[@uri=&quot;sip:"))// 业务组成员
		{
			int nsStart = notifyContent.indexOf(":list[@uri=&quot;sip:")
					+ ":list[@uri=&quot;sip:".length();
			notifyContent = notifyContent.substring(nsStart);
			int nsEnd = notifyContent.indexOf("@");
			NgnUriUtils.mGroupNum = notifyContent.substring(0, nsEnd).trim();
		} else
			NgnUriUtils.mGroupNum = null;

		if (notifyContent.contains(":entry[@uri=&quot;sip:"))// 业务组成员
		{
			int nsStart = notifyContent.indexOf(":entry[@uri=&quot;sip:")
					+ ":entry[@uri=&quot;sip:".length();
			notifyContent = notifyContent.substring(nsStart);
			int nsEnd = notifyContent.indexOf("@");
			NgnUriUtils.mMemberNum = notifyContent.substring(0, nsEnd).trim();
		} else
			NgnUriUtils.mMemberNum = null;
	}

	// public static void setGroupAndMemberNum2(String notifyContent) {
	// //删除操作获取组号码和成员号码
	// if(notifyContent.contains("name"))//群组号码
	// {
	// int nsStart = notifyContent.indexOf("name=")+"name=".length()+1;
	// notifyContent = notifyContent.substring(nsStart);
	// int nsEnd = notifyContent.indexOf("\"");
	// NgnUriUtils.mGroupNum = notifyContent.substring(0, nsEnd).trim();
	// }
	// else
	// NgnUriUtils.mGroupNum = null;
	//
	// if(notifyContent.contains("entry"))//群组成员
	// {
	// int nsStart = notifyContent.indexOf("sip:")+"sip:".length();
	// notifyContent = notifyContent.substring(nsStart);
	// int nsEnd = notifyContent.indexOf("@");
	// NgnUriUtils.mMemberNum = notifyContent.substring(0, nsEnd).trim();
	// }
	// else
	// NgnUriUtils.mMemberNum = null;
	// }
	//
	//
	//
	// public static void getGroupAndMemberNum2(String notifyContent) {
	// //删除操作获取组号码和成员号码
	// if(notifyContent.contains("name"))//群组号码
	// {
	// int nsStart = notifyContent.indexOf("name=")+"name=".length()+1;
	// notifyContent = notifyContent.substring(nsStart);
	// int nsEnd = notifyContent.indexOf("\"");
	// NgnUriUtils.mGroupNum = notifyContent.substring(0, nsEnd).trim();
	// }
	// else
	// NgnUriUtils.mGroupNum = null;
	//
	// if(notifyContent.contains("entry"))//群组成员
	// {
	// int nsStart = notifyContent.indexOf("sip:")+"sip:".length();
	// notifyContent = notifyContent.substring(nsStart);
	// int nsEnd = notifyContent.indexOf("@");
	// NgnUriUtils.mMemberNum = notifyContent.substring(0, nsEnd).trim();
	// }
	// else
	// NgnUriUtils.mMemberNum = null;
	// }

	private final static long MAX_PHONE_NUMBER = 1000000000000L;
	private final static String INVALID_SIP_URI = "sip:invalid@"
			+ NgnEngine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.NETWORK_REALM,
							NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

	public static String getDisplayName(String uri) {
		String displayname = null;
		if (!NgnStringUtils.isNullOrEmpty(uri)) {
			NgnContact contact = NgnEngine.getInstance().getContactService()
					.getContactByUri(uri);
			if (contact != null
					&& (displayname = contact.getDisplayName()) != null) {
				return displayname;
			}

			final SipUri sipUri = new SipUri(uri);
			if (sipUri.isValid()) { // # sip:#@test.com * sip:*@test.com
				displayname = sipUri.getUserName();
				contact = NgnEngine.getInstance().getContactService()
						.getContactByPhoneNumber(displayname);
				if (contact != null
						&& !NgnStringUtils.isNullOrEmpty(contact
								.getDisplayName())) {
					displayname = contact.getDisplayName();
				}
			}
			sipUri.delete();
		}

		return displayname == null ? uri : displayname;
	}

	public static String getUserName(String validUri) {
		final SipUri sipUri = new SipUri(validUri);
		String userName = validUri;
		if (sipUri.isValid()) {
			userName = sipUri.getUserName();
		}
		sipUri.delete();
		if (userName != null
				&& userName.startsWith(GlobalVar.videoMonitorPrefix))
			userName = userName.substring(3);
		return userName;
	}

	public static boolean isValidSipUri(String uri) {
		return SipUri.isValid(uri);
	}

	// Very very basic
	public static String makeValidSipUri(String uri) {
		if (NgnStringUtils.isNullOrEmpty(uri)) {
			return NgnUriUtils.INVALID_SIP_URI;
		}
		if (uri.startsWith("sip:") || uri.startsWith("sips:")) {
			return uri.replace("#", "%23");
		} else if (uri.startsWith("tel:")) {
			return uri;
		} else {
			if (uri.contains("@")) {
				return String.format("sip:%s", uri);
			} else {
				String realm = NgnEngine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.NETWORK_REALM,
								NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
				if (realm.startsWith("sip:") || realm.startsWith("sips:")) { // do
																				// not
																				// forget
																				// that
																				// IPv6
																				// address
																				// could
																				// contain
																				// ":"
					realm = realm.substring(realm.indexOf(":") + 1);
				}
				// FIXME: Should be done by doubango
				return String.format("sip:%s@%s",
						uri.replace(" ", "").replace("(", "").replace(")", "")
								.replace("-", "").replace("#", "%23"), realm);
				// return String.format("sip:%s@%s",
				// uri.replace(" ", "").replace("(", "").replace(")",
				// "").replace("-", ""),
				// realm);
			}
		}
	}

	public static SipUri makeValidSipUriObj(String uri) {
		SipUri sipUri = new SipUri(makeValidSipUri(uri));
		if (sipUri.isValid()) {
			return sipUri;
		}
		sipUri.delete();
		return null;
	}

	public static String getValidPhoneNumber(String uri) {
		if (uri != null) {
			if ((uri.startsWith("sip:") || uri.startsWith("sip:") || uri
					.startsWith("tel:"))) {
				SipUri sipUri = new SipUri(uri);
				if (sipUri.isValid()) {
					String userName = sipUri.getUserName();
					if (userName != null) {
						try {
							String scheme = sipUri.getScheme();
							if (scheme != null && scheme.equals("tel")) {
								userName = userName.replace("-", "");
							}
							long result = Long.parseLong(userName
									.startsWith("+") ? userName.substring(1)
									: userName);
							if (result < NgnUriUtils.MAX_PHONE_NUMBER) {
								return userName;
							}
						} catch (NumberFormatException ne) {
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				sipUri.delete();
			} else {
				try {
					uri = uri.replace("-", "");
					long result = Long.parseLong(uri.startsWith("+") ? uri
							.substring(1) : uri);
					if (result < NgnUriUtils.MAX_PHONE_NUMBER) {
						return uri;
					}
				} catch (NumberFormatException ne) {
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return null;
	}

	public static String getRemoteNumber(String notifyInfo) {
		if (notifyInfo.indexOf("entity=") >= 0) {

			int nsStart = notifyInfo.indexOf("entity=") + "entity=\"".length();
			int nsEnd = notifyInfo.indexOf("\">");

			String remoteSipNumber = notifyInfo.substring(nsStart, nsEnd)
					.trim();
			notifyInfo = notifyInfo.substring(nsEnd + 1);
			String remoteNumber = getUserName(remoteSipNumber);
			return remoteNumber;
		}
		return null;
	}

	public static boolean getContactIsOnline(String notifyInfo)// <basic>
	{
		boolean isOnLine = false;
		if (notifyInfo.indexOf("<basic>") >= 0) {

			int nsStart = notifyInfo.indexOf("<basic>") + "<basic>".length();
			int nsEnd = notifyInfo.indexOf("</basic>");
			String state = notifyInfo.substring(nsStart, nsEnd).trim();
			if (state.endsWith("open"))
				isOnLine = true;
		}
		return isOnLine;

	}

	public static String getGroupRemoveNumber(String notifyInfo) {
		if (notifyInfo.contains("public-group")
				&& notifyInfo.contains(":removeg>"))// 业务组成员
		{
			int nsStart = notifyInfo.indexOf(":removeg>")
					+ ":removeg>".length();
			notifyInfo = notifyInfo.substring(nsStart);
			int nsEnd = notifyInfo.indexOf("<");
			String changedNumber = notifyInfo.substring(0, nsEnd).trim();
			return getUserName(changedNumber);
		}
		if (notifyInfo.contains("service-group")
				&& notifyInfo.contains(":removeg>"))// 业务组成员
		{
			int nsStart = notifyInfo.indexOf(":removeg>")
					+ ":removeg>".length();
			notifyInfo = notifyInfo.substring(nsStart);
			int nsEnd = notifyInfo.indexOf("<");
			String changedNumber = notifyInfo.substring(0, nsEnd).trim();
			return getUserName(changedNumber);
		}
		return null;
	}

	// public static String getGroupRemoveNumber2(String notifyInfo)
	// {
	// if(notifyInfo.contains("public-group") && notifyInfo.contains("remove")
	// && notifyInfo.contains("list"))//业务组成员
	// {
	// int nsStart = notifyInfo.indexOf("name=")+"name=".length()+1;
	// notifyInfo = notifyInfo.substring(nsStart);
	// int nsEnd = notifyInfo.indexOf("\"");
	// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
	//
	// if (changedNumber!=null) {
	// Log.e("删除组 name", changedNumber);
	// }
	//
	// return changedNumber;
	// // return getUserName(changedNumber); //getUserName的参数格式为sip:400@test.com
	// }
	// if(notifyInfo.contains("service-group") &&
	// notifyInfo.contains(":removeg>"))//业务组成员
	// {
	// int nsStart = notifyInfo.indexOf(":removeg>")+":removeg>".length();
	// notifyInfo = notifyInfo.substring(nsStart);
	// int nsEnd = notifyInfo.indexOf("<");
	// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
	// return getUserName(changedNumber);
	// }
	// return null;
	// }

	/*
	 * 获取群组变化的组号码
	 */
	public static String GetGroupNum2(String notifyInfo) {

		MyLog.d("ywh", notifyInfo);
		
		int nsStart = notifyInfo.indexOf("name=") + "name=".length() + 1;
		notifyInfo = notifyInfo.substring(nsStart);
		int nsEnd = notifyInfo.indexOf("\"");
		String changedNumber = notifyInfo.substring(0, nsEnd).trim();

		if (changedNumber != null) {
			Log.e("组 name", changedNumber);
		}

		return changedNumber;

	}
	
	public static GroupInfor parserXml(String content){
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(content));
			GroupInfor groupInfor = new GroupInfor();
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					
					if (tagName.equals("list")) {
						groupInfor.setName(parser.getAttributeValue(null, "name"));
						groupInfor.setUri(parser.getAttributeValue(null, "uri"));
						groupInfor.setDisplayName(parser.getAttributeValue(null, "displayName"));
						groupInfor.setServiceType(parser.getAttributeValue(null, "serviceType"));
						groupInfor.setCreator(parser.getAttributeValue(null, "creator"));
					}

				}
				parser.next();
			}
			return groupInfor;
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	

	/*
	 * 获取删除组成员的成员号码
	 */
	public static String RemoveGroupNumGetMemberNum(String notifyInfo) {
		// if(notifyInfo.contains("public-group") &&
		// notifyInfo.contains("remove") && notifyInfo.contains("entry"))
		// {

		int nsStart = notifyInfo.indexOf("sip:") + "sip:".length();
		notifyInfo = notifyInfo.substring(nsStart);
		int nsEnd = notifyInfo.indexOf("@");
		String changedNumber = notifyInfo.substring(0, nsEnd).trim();

		if (changedNumber != null) {
			Log.e("删除组成员的成员号码为", changedNumber);
		}

		return changedNumber;
		// return getUserName(changedNumber);
		// //getUserName的参数格式为sip:400@test.com
		// }
		// if(notifyInfo.contains("service-group") &&
		// notifyInfo.contains(":removeg>"))//业务组成员
		// {
		// int nsStart = notifyInfo.indexOf(":removeg>")+":removeg>".length();
		// notifyInfo = notifyInfo.substring(nsStart);
		// int nsEnd = notifyInfo.indexOf("<");
		// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
		// return getUserName(changedNumber);
		// }
		// return null;
	}

	/*
	 * 获取添加组成员的成员号码
	 */
	public static List<String> AddGroupNumGetMemberNum(String notifyInfo) {
		if (notifyInfo.contains("public-group")
				|| notifyInfo.contains("service-group")
				|| notifyInfo.contains("global-group")
				|| notifyInfo.contains("subscribe-group")) {

			// if(notifyInfo.contains("list")){
			// return null;
			// }else

			List<String> changedNumList = new ArrayList<String>();

			// if(notifyInfo.contains("entry")){
			//
			// int nsStart = notifyInfo.indexOf("sip:")+"sip:".length();
			// notifyInfo = notifyInfo.substring(nsStart);
			// int nsEnd = notifyInfo.indexOf("@");
			// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
			//
			// if (changedNumber!=null) {
			// Log.e("添加组成员的成员号码为", changedNumber);
			// }
			//
			//
			// return changedNumList;
			// }

			XmlPullParserFactory factory;
			try {
				factory = XmlPullParserFactory.newInstance();
				factory.setNamespaceAware(true);
				XmlPullParser parser = factory.newPullParser();

				parser.setInput(new StringReader(notifyInfo));
				String org = null;
				while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
					if (parser.getEventType() == XmlPullParser.START_TAG) {
						String tagName = parser.getName();

						if (tagName.equals("list")) {

						}
						if (tagName.equals("entry")) {

							String uri = parser.getAttributeValue(null, "uri");

							int nsStart = uri.indexOf("sip:") + "sip:".length();
							uri = uri.substring(nsStart);
							int nsEnd = uri.indexOf("@");
							String changedNumber = uri.substring(0, nsEnd)
									.trim();

							changedNumList.add(changedNumber);
						}
					}
					parser.next();
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return changedNumList;

			// return getUserName(changedNumber);
			// //getUserName的参数格式为sip:400@test.com
		}
		// if(notifyInfo.contains("service-group"))//业务组成员
		// {
		// if(notifyInfo.contains("list")){
		// return null;
		// }else if(notifyInfo.contains("entry")){
		//
		// int nsStart = notifyInfo.indexOf("sip:")+"sip:".length();
		// notifyInfo = notifyInfo.substring(nsStart);
		// int nsEnd = notifyInfo.indexOf("@");
		// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
		//
		// if (changedNumber!=null) {
		// Log.e("添加组成员的成员号码为", changedNumber);
		// }
		//
		//
		// return changedNumber;
		// }
		//
		//
		// }
		//
		// if(notifyInfo.contains("global-group"))//业务组成员
		// {
		// if(notifyInfo.contains("list")){
		// return null;
		// }else if(notifyInfo.contains("entry")){
		//
		// int nsStart = notifyInfo.indexOf("sip:")+"sip:".length();
		// notifyInfo = notifyInfo.substring(nsStart);
		// int nsEnd = notifyInfo.indexOf("@");
		// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
		//
		// if (changedNumber!=null) {
		// Log.e("添加组成员的成员号码为", changedNumber);
		// }
		//
		//
		// return changedNumber;
		// }
		// }
		//
		// if(notifyInfo.contains("subscribe-group"))//订阅号
		// {
		// if(notifyInfo.contains("list")){
		// return null;
		// }else if(notifyInfo.contains("entry")){
		//
		// int nsStart = notifyInfo.indexOf("sip:")+"sip:".length();
		// notifyInfo = notifyInfo.substring(nsStart);
		// int nsEnd = notifyInfo.indexOf("@");
		// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
		//
		// if (changedNumber!=null) {
		// Log.e("添加组成员的成员号码为", changedNumber);
		// }
		//
		//
		// return changedNumber;
		// }
		// }

		return null;
	}

	public static String getReplaceNumber(String notifyInfo) {
		int nsStart = notifyInfo.indexOf("uri=\"") + "uri=\"".length();
		notifyInfo = notifyInfo.substring(nsStart);
		int nsEnd = notifyInfo.indexOf("\"");
		String changedNumber = notifyInfo.substring(0, nsEnd).trim();
		return getUserName(changedNumber);
	}

	// public static String getChangedNumber(String notifyInfo)
	// {
	// if(!notifyInfo.contains("public-group"))
	// return null;
	// if(notifyInfo.contains("remove"))//普通组成员
	// {
	// int nsStart =
	// notifyInfo.indexOf(":entry[@uri=&quot;")+":entry[@uri=&quot;".length();
	// notifyInfo = notifyInfo.substring(nsStart);
	// if(nsStart<0)
	// {
	// catchError("NgnUriUtils", "getChangedNumber", "nsStart", nsStart);
	// return null;
	// }
	// int nsEnd = notifyInfo.indexOf("&");
	// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
	// return getUserName(changedNumber);
	// }
	// if(notifyInfo.contains("addg"))
	// {
	// int nsStart = notifyInfo.indexOf("name=\"")+"name=\"".length();
	// notifyInfo = notifyInfo.substring(nsStart);
	// if(nsStart<0)
	// {
	// catchError("NgnUriUtils", "getChangedNumber <name>", "nsStart", nsStart);
	// return null;
	// }
	// int nsEnd = notifyInfo.indexOf("\"");
	// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
	// return changedNumber;
	// }
	// int nsStart = notifyInfo.indexOf("uri=\"") + "uri=\"".length();
	// notifyInfo = notifyInfo.substring(nsStart);
	// int nsEnd = notifyInfo.indexOf("\"");
	// String changedNumber = notifyInfo.substring(0, nsEnd).trim();
	// return getUserName(changedNumber);
	// }
	public static void catchError(String className, String funName, String arg,
			int index) {
		MyLog.d(className, String.format("%s: %s = %d", funName, index));
	}

	public static String getGroupUri(String groupuri) {
		int nsStart = groupuri.indexOf("<group-uri>") + "<group-uri>".length();
		groupuri = groupuri.substring(nsStart);
		int nsEnd = groupuri.indexOf("<");
		groupuri = groupuri.substring(0, nsEnd).trim();
		return getUserName(groupuri);
	}

	/**
	 * 检查域名拼写
	 * 
	 * @param realm
	 * @return
	 */
	public static boolean checkRealm(String realm) {
		try {

			// 域名正则表达式
			String regex = "[a-zA-Z]{0,62}.(com|cn|edu|([a-zA-Z]{0,20}))";
			return Pattern.matches(regex, realm);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}

	}

	public static boolean checkIPAddress(String ip) {
		String regex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])"
				+ "(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}
}
