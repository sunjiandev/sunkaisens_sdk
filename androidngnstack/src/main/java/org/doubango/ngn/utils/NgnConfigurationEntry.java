/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
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

import org.doubango.ngn.sip.NgnPresenceStatus;
import org.doubango.tinyWRAP.tdav_codec_id_t;
import org.doubango.tinyWRAP.tmedia_bandwidth_level_t;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;
import org.doubango.tinyWRAP.tmedia_profile_t;
import org.doubango.tinyWRAP.tmedia_qos_strength_t;
import org.doubango.tinyWRAP.tmedia_qos_stype_t;
import org.doubango.tinyWRAP.tmedia_srtp_mode_t;
import org.doubango.tinyWRAP.tmedia_srtp_type_t;

public class NgnConfigurationEntry {
	private static final String TAG = NgnConfigurationEntry.class.getCanonicalName();
	
	public final static String  SHARED_PREF_NAME = TAG;
	public static final String PCSCF_DISCOVERY_DNS_SRV = "DNS NAPTR+SRV";
	public static final String GENERAL_VIDDO_FPS = "GENERAL_VIDDO_FPS." + TAG;
	
	public static final String GENERAL_BYPASS_ENCODING = "GENERAL_BYPASS_ENCODING." + TAG;
	public static final String GENERAL_BYPASS_DECODING = "GENERAL_BYPASS_DECODING." + TAG;
	// General
	public static final String GENERAL_AUTOSTART = "GENERAL_AUTOSTART." + TAG;
	public static final String GENERAL_AUTOSTART_VIDEO = "GENERAL_AUTOSTART_VIDEO." + TAG;
	public static final String GENERAL_SHOW_WELCOME_SCREEN = "GENERAL_SHOW_WELCOME_SCREEN." + TAG;
	public static final String GENERAL_FULL_SCREEN_VIDEO = "GENERAL_FULL_SCREEN_VIDEO." + TAG;
	public static final String GENERAL_USE_FFC = "GENERAL_USE_FFC." + TAG;
	public static final String GENERAL_INTERCEPT_OUTGOING_CALLS = "GENERAL_INTERCEPT_OUTGOING_CALLS." + TAG;
	public static final String GENERAL_AUDIO_PLAY_LEVEL = "GENERAL_AUDIO_PLAY_LEVEL." + TAG;
	public static final String GENERAL_ENUM_DOMAIN = "GENERAL_ENUM_DOMAIN." + TAG;
	public static final String GENERAL_AEC = "GENERAL_AEC."+ TAG ;
	public static final String GENERAL_VAD = "GENERAL_VAD."+ TAG ;	
	public static final String GENERAL_NR = "GENERAL_NR."+ TAG ;	
	public static final String GENERAL_ECHO_TAIL = "GENERAL_ECHO_TAIL." + TAG ;
	public static final String GENERAL_USE_ECHO_TAIL_ADAPTIVE = "GENERAL_USE_ECHO_TAIL_ADAPTIVE." + TAG ;
	public static final String GENERAL_SEND_DEVICE_INFO = "GENERAL_SEND_DEVICE_INFO" + TAG;
	public static final String GENERAL_USE_FEC = "GENERAL_USE_FEC." + TAG;
	
	// Identity
	public static final String IDENTITY_DISPLAY_NAME = "IDENTITY_DISPLAY_NAME." + TAG;
	public static final String IDENTITY_IMPU = "IDENTITY_IMPU." + TAG;
	public static final String IDENTITY_IMPI = "IDENTITY_IMPI." + TAG;
	public static final String IDENTITY_PASSWORD = "IDENTITY_PASSWORD." + TAG;
	
	// Network
	public static final String NETWORK_REGISTRATION_TIMEOUT = "NETWORK_REGISTRATION_TIMEOUT." + TAG;
	public static final String NETWORK_REALM = "NETWORK_REALM." + TAG;
	public static final String NETWORK_GROUP_REALM = "NETWORK_GROUP_REALM." + TAG;
	public static final String NETWORK_GROUP_PORT = "NETWORK_GROUP_PORT." + TAG;
	public static final String NETWORK_USE_WIFI = "NETWORK_USE_WIFI." + TAG;
	public static final String NETWORK_USE_3G = "NETWORK_USE_3G." + TAG;
	public static final String NETWORK_USE_EARLY_IMS = "NETWORK_USE_EARLY_IMS." + TAG;
	public static final String NETWORK_IP_VERSION = "NETWORK_IP_VERSION." + TAG;
	public static final String NETWORK_PCSCF_DISCOVERY = "NETWORK_PCSCF_DISCOVERY." + TAG;
	public static final String NETWORK_PCSCF_HOST = "NETWORK_PCSCF_HOST." + TAG;
	public static final String NETWORK_PCSCF_PORT = "NETWORK_PCSCF_PORT." + TAG;
	public static final String NETWORK_USE_SIGCOMP = "NETWORK_USE_SIGCOMP." + TAG;
	public static final String NETWORK_TRANSPORT = "NETWORK_TRANSPORT." + TAG;
	public static final String NETWORK_QOS_LOSEPACKETS = "NETWORK_QOS_LOSEPACKETS." + TAG;
	
	// NAT Traversal
	public static final String NATT_HACK_AOR = "NATT_HACK_AOR." + TAG;
	public static final String NATT_HACK_AOR_TIMEOUT = "NATT_HACK_AOR_TIMEOUT." + TAG;
	public static final String NATT_USE_STUN = "NATT_USE_STUN." + TAG;
	public static final String NATT_USE_ICE = "NATT_USE_ICE." + TAG;
	public static final String NATT_STUN_DISCO = "NATT_STUN_DISCO." + TAG;
	public static final String NATT_STUN_SERVER = "NATT_STUN_SERVER." + TAG;
	public static final String NATT_STUN_PORT = "NATT_STUN_PORT." + TAG;
	
	// QoS
	public static final String QOS_PRECOND_BANDWIDTH_LEVEL = "QOS_PRECOND_BANDWIDTH_LEVEL." + TAG;
	public static final String QOS_PRECOND_STRENGTH = "QOS_PRECOND_STRENGTH." + TAG;
    public static final String QOS_PRECOND_TYPE = "QOS_PRECOND_TYPE." + TAG;
    public static final String QOS_REFRESHER = "QOS_REFRESHER." + TAG;
    public static final String QOS_SIP_CALLS_TIMEOUT = "QOS_SIP_CALLS_TIMEOUT." + TAG;
    public static final String QOS_SIP_SESSIONS_TIMEOUT = "QOS_SIP_SESSIONS_TIMEOUT" + TAG;
    public static final String QOS_USE_SESSION_TIMERS = "QOS_USE_SESSION_TIMERS." + TAG;
    public static final String QOS_PREF_VIDEO_SIZE = "QOS_PREF_VIDEO_SIZE." + TAG;
    public static final String QOS_USE_ZERO_VIDEO_ARTIFACTS = "QOS_USE_ZERO_VIDEO_ARTIFACTS." + TAG;
	
	// Media
	public static final String MEDIA_CODECS = "MEDIA_CODECS." + TAG;
	public static final String MEDIA_AUDIO_RESAMPLER_QUALITY = "MEDIA_AUDIO_RESAMPLER_QUALITY." + TAG;
	public static final String MEDIA_AUDIO_CONSUMER_GAIN = "MEDIA_AUDIO_CONSUMER_GAIN." + TAG;
	public static final String MEDIA_AUDIO_PRODUCER_GAIN = "MEDIA_AUDIO_PRODUCER_GAIN." + TAG;
	public static final String MEDIA_AUDIO_CONSUMER_ATTENUATION = "MEDIA_AUDIO_CONSUMER_ATTENUATION." + TAG;
	public static final String MEDIA_AUDIO_PRODUCER_ATTENUATION = "MEDIA_AUDIO_PRODUCER_ATTENUATION." + TAG;
	public static final String MEDIA_PROFILE = "MEDIA_PROFILE." + TAG;
	
	// Security
	public static final String SECURITY_SRTP_MODE = "SECURITY_SRTP_MODE." + TAG;
	public static final String SECURITY_SRTP_TYPE = "SECURITY_SRTP_TYPE." + TAG;
	public static final String SECURITY_IMSAKA_AMF = "SECURITY_IMSAKA_AMF." + TAG;
	public static final String SECURITY_IMSAKA_OPID = "SECURITY_IMSAKA_OPID." + TAG;
	public static final String SECURITY_TLS_PRIVKEY_FILE_PATH = "SECURITY_TLS_PRIVKEY_FILE_PATH." + TAG;
	public static final String SECURITY_TLS_PUBKEY_FILE_PATH = "SECURITY_TLS_PUBKEY_FILE_PATH." + TAG;
	public static final String SECURITY_TLS_CA_FILE_PATH = "SECURITY_TLS_CA_FILE_PATH." + TAG;
	public static final String SECURITY_TLS_VERIFY_CERTS = "SECURITY_TLS_VERIFY_CERTS." + TAG;
	
	// XCAP
	public static final String XCAP_PASSWORD = "XCAP_PASSWORD." + TAG;
	public static final String XCAP_USERNAME = "XCAP_USERNAME." + TAG;
	public static final String XCAP_ENABLED = "XCAP_ENABLED." + TAG;
	public static final String XCAP_XCAP_ROOT = "XCAP_XCAP_ROOT." + TAG;
	
	// RCS (Rich Communication Suite)
	public static final String RCS_AVATAR_PATH = "RCS_AVATAR_PATH." + TAG;
	public static final String RCS_USE_BINARY_SMS = "RCS_USE_BINARY_SMS." + TAG;
	public static final String RCS_CONF_FACT = "RCS_CONF_FACT." + TAG;
	public static final String RCS_FREE_TEXT = "RCS_FREE_TEXT." + TAG;
	public static final String RCS_HACK_SMS = "RCS_HACK_SMS." + TAG;
	public static final String RCS_USE_MSRP_FAILURE = "RCS_USE_MSRP_FAILURE." + TAG;
	public static final String RCS_USE_MSRP_SUCCESS = "RCS_USE_MSRP_SUCCESS." + TAG;
	public static final String RCS_USE_MWI = "RCS_USE_MWI." + TAG;
	public static final String RCS_USE_OMAFDR = "RCS_USE_OMAFDR." + TAG;
	public static final String RCS_USE_PARTIAL_PUB = "RCS_USE_PARTIAL_PUB." + TAG;
	public static final String RCS_USE_PRESENCE = "RCS_USE_PRESENCE." + TAG;
	public static final String RCS_USE_RLS = "RCS_USE_RLS." + TAG;
	public static final String RCS_SMSC = "RCS_SMSC." + TAG;
	public static final String RCS_STATUS  = "RCS_STATUS." + TAG;
	
	public static final String LOGS_WRITE_TO_FILE_OPEN = "LOGS_WRITE_TO_FILE_OPEN"+TAG;
	public static final String LOGS_WRITE_TO_FILE_SYS_OPEN = "LOGS_WRITE_TO_FILE_SYS_OPEN"+TAG;
	
	/**
	 * 通锟斤拷锟斤拷史锟斤拷录锟酵硷拷时锟斤拷息锟斤拷史锟斤拷录时锟斤拷锟斤拷式  true锟斤拷12小时锟斤拷   false锟斤拷24小时锟斤拷
	 */
	public static final String SYSTEM_FORMAT_TIME = "SYSTEM_FORMAT_TIME"+TAG;
	
	//
	//	Default values
	//
	public static final boolean DEFAULT_GENERAL_BYPASS_ENCODING = false;
	public static final boolean DEFAULT_GENERAL_BYPASS_DECODING = false;
	// General
	public static final int DEFAULT_GENERAL_VIDDO_FPS = 15;
	public static final boolean DEFAULT_GENERAL_SHOW_WELCOME_SCREEN = true;
//	public static final boolean DEFAULT_GENERAL_FULL_SCREEN_VIDEO = true;
	public static final boolean DEFAULT_GENERAL_FULL_SCREEN_VIDEO = false; //默锟斤拷锟斤拷锟矫凤拷锟斤拷频通锟斤拷全锟斤拷模式
	public static final boolean DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS = true;
//	public static final boolean DEFAULT_GENERAL_USE_FFC = true;
	public static final boolean DEFAULT_GENERAL_USE_FFC = false; //默锟斤拷锟斤拷锟矫诧拷锟斤拷锟斤拷前锟斤拷锟斤拷锟斤拷头模式
	public static final boolean DEFAULT_GENERAL_AUTOSTART = true;
	public static final boolean DEFAULT_GENERAL_AUTOSTART_VIDEO = false;
	public static final float DEFAULT_GENERAL_AUDIO_PLAY_LEVEL = 1.0f;
	public static final String DEFAULT_GENERAL_ENUM_DOMAIN = "e164.org";
	public static final boolean DEFAULT_GENERAL_AEC = false;
	public static final boolean DEFAULT_GENERAL_USE_ECHO_TAIL_ADAPTIVE = true;
	public static final boolean DEFAULT_GENERAL_VAD = false; // speex-dsp don't support VAD for fixed-point implementation
	public static final boolean DEFAULT_GENERAL_NR = false;
	public static final int DEFAULT_GENERAL_ECHO_TAIL = 200;
	public static final boolean DEFAULT_GENERAL_SEND_DEVICE_INFO = false;
	public static final boolean DEFAULT_GENERAL_USE_FEC = false;// default use FEC
	
	//	Identity
	public static final String DEFAULT_IDENTITY_DISPLAY_NAME = "10658811102";
	public static final String DEFAULT_IDENTITY_IMPU = "sip:10658811102@test.com";
	public static final String DEFAULT_IDENTITY_IMPI = "10658811102";
	public static final String DEFAULT_IDENTITY_PASSWORD = null;
	
	// Network
	public static final int DEFAULT_NETWORK_REGISTRATION_TIMEOUT = 120; //1700 注锟斤拷刷锟斤拷时锟斤拷锟斤拷锟斤拷
//	public static final String DEFAULT_NETWORK_REALM = "doubango.org";
	//add by Gongle 20160903: to test how many packets lose the network can bear  
	public static final int DEFAULT_NETWORK_QOS_LOSEPACKETS = 25;
	
//	public static final String DEFAULT_NETWORK_REALM = "192.168.1.192";
	public static final String DEFAULT_NETWORK_REALM = "test.com";
	public static final String DEFAULT_NETWORK_GROUP_REALM = "appserver.test.com";
	public static final String DEFAULT_NETWORK_GROUP_PORT = "1000";
	
	public static final boolean DEFAULT_NETWORK_USE_WIFI = true;
	public static final boolean DEFAULT_NETWORK_USE_3G = false;
	public static final String DEFAULT_NETWORK_PCSCF_DISCOVERY = "None";
//	public static final String DEFAULT_NETWORK_PCSCF_HOST = "127.0.0.1";
	
	public static final String DEFAULT_NETWORK_PCSCF_HOST = "test.com";
	
	public static final int DEFAULT_NETWORK_PCSCF_PORT = 4060;
	public static final boolean DEFAULT_NETWORK_USE_SIGCOMP = false;
	public static final String DEFAULT_NETWORK_TRANSPORT = "udp";
	public static final String DEFAULT_NETWORK_IP_VERSION = "ipv4";
	public static final boolean DEFAULT_NETWORK_USE_EARLY_IMS = false;
	
	public final static String[] DEFAULT_NETWORK_MAC_ADDRLIST = new String[] {
		"fa:ed:35:73:66:70",
		
		"da:3b:3d:6a:66:fe",
		"42:3d:66:8c:a2:62",
		"ba:00:38:ca:1b:ae",
		"2a:3e:a2:12:56:95",
		"36:9a:ff:ee:ab:f6",
		"7a:34:9f:b5:7f:d8",
		
		"e2:03:28:be:ce:01",
		"aa:73:2c:90:c8:06",
		"62:4c:82:e5:8e:25",
		"22:51:ed:01:95:0e",
		"a2:5a:e6:32:85:f9",
		"c2:95:9b:17:61:7c",
		"7e:5d:14:7d:b2:a8",
		
		"78:e8:b6:80:2f:3c",
		"34:80:b3:1a:bf:e6",
		};
	
	// NAT Traversal
	public static final int DEFAULT_NATT_HACK_AOR_TIMEOUT = 2000;
	public static final boolean DEFAULT_NATT_HACK_AOR = false;
	public static final boolean DEFAULT_NATT_USE_STUN = false;
	public static final boolean DEFAULT_NATT_USE_ICE = false;
	public static final boolean DEFAULT_NATT_STUN_DISCO = false;
//	public static final String DEFAULT_NATT_STUN_SERVER = "numb.viagenie.ca";
	public static final String DEFAULT_NATT_STUN_SERVER = "124.205.124.82";
	public static final int DEFAULT_NATT_STUN_PORT = 3478;
	
	// QoS
    public static final int DEFAULT_QOS_PRECOND_BANDWIDTH_LEVEL = tmedia_bandwidth_level_t.tmedia_bl_unrestricted.swigValue(); // should be String but do not change for backward compatibility
    public static final String DEFAULT_QOS_PRECOND_STRENGTH = tmedia_qos_strength_t.tmedia_qos_strength_none.toString();
    public static final String DEFAULT_QOS_PRECOND_TYPE = tmedia_qos_stype_t.tmedia_qos_stype_none.toString();
    public static final String DEFAULT_QOS_REFRESHER = "none";
    public static final int DEFAULT_QOS_SIP_SESSIONS_TIMEOUT = 600000;
    public static final int DEFAULT_QOS_SIP_CALLS_TIMEOUT = 120;    //锟斤拷锟斤拷锟斤拷息刷锟斤拷注锟斤拷锟斤拷锟斤拷
    public static final boolean DEFAULT_QOS_USE_SESSION_TIMERS = false;
    public static final boolean DEFAULT_QOS_USE_ZERO_VIDEO_ARTIFACTS = false;
//    public static final String DEFAULT_QOS_PREF_VIDEO_SIZE = tmedia_pref_video_size_t.tmedia_pref_video_size_720p.toString(); //720P (1280 x 720)
//    public static String DEFAULT_QOS_PREF_VIDEO_SIZE = tmedia_pref_video_size_t.tmedia_pref_video_size_skd.toString(); //锟皆讹拷锟斤拷 skd (512 x 288)
    public static final String DEFAULT_QOS_PREF_VIDEO_SIZE = tmedia_pref_video_size_t.tmedia_pref_video_size_cif.toString(); //CIF (352 x 288)
	
	// Media
    public static final String DEFAULT_MEDIA_PROFILE = tmedia_profile_t.tmedia_profile_default.toString();
	public static final int DEFAULT_MEDIA_CODECS = 
			tdav_codec_id_t.tdav_codec_id_g729ab.swigValue() |	
//		tdav_codec_id_t.tdav_codec_id_pcma.swigValue() |
//		tdav_codec_id_t.tdav_codec_id_pcmu.swigValue() |
		tdav_codec_id_t.tdav_codec_id_h264_bp.swigValue();
	public static final int DEFAULT_MEDIA_AUDIO_RESAMPLER_QUALITY = 0;
	public static final int DEFAULT_MEDIA_AUDIO_CONSUMER_GAIN = 0; // disabled
	public static final int DEFAULT_MEDIA_AUDIO_PRODUCER_GAIN = 0; // disabled
	public static final float DEFAULT_MEDIA_AUDIO_CONSUMER_ATTENUATION = 1f; // disabled
	public static final float DEFAULT_MEDIA_AUDIO_PRODUCER_ATTENUATION = 1f; // disabled
	
	// Security
	public static final String DEFAULT_SECURITY_IMSAKA_AMF = "0x0000";
	public static final String DEFAULT_SECURITY_IMSAKA_OPID = "0x00000000000000000000000000000000";
	public static final String DEFAULT_SECURITY_SRTP_MODE = tmedia_srtp_mode_t.tmedia_srtp_mode_none.toString();
	public static final String DEFAULT_SECURITY_SRTP_TYPE = tmedia_srtp_type_t.tmedia_srtp_type_sdes.toString();
	public static final String DEFAULT_SECURITY_TLS_PRIVKEY_FILE_PATH = null;
	public static final String DEFAULT_SECURITY_TLS_PUBKEY_FILE_PATH = null;
	public static final String DEFAULT_SECURITY_TLS_CA_FILE_PATH = null;
	public static final boolean DEFAULT_SECURITY_TLS_VERIFY_CERTS = false;
	
	// XCAP
	public static final boolean DEFAULT_XCAP_ENABLED = false;
//	public static final String DEFAULT_XCAP_ROOT = "http://doubango.org:8080/services";
//	public static final String DEFAULT_XCAP_USERNAME = "sip:johndoe@doubango.org";
	
	public static final String DEFAULT_XCAP_ROOT = "http://192.168.1.72";
	public static final String DEFAULT_XCAP_USERNAME = "sip:123@192.168.1.72";
	public static final String DEFAULT_XCAP_PASSWORD = null;
	
	// RCS (Rich Communication Suite)
	public static final String DEFAULT_RCS_AVATAR_PATH = "";
	public static final boolean DEFAULT_RCS_USE_BINARY_SM = false; 
	public static final String DEFAULT_RCS_CONF_FACT = "sip:Conference-Factory@doubango.org";
	public static final String DEFAULT_RCS_FREE_TEXT = "Hello world";
	public static final boolean DEFAULT_RCS_HACK_SMS = false;
	public static final boolean DEFAULT_RCS_USE_MSRP_FAILURE = true;
	public static final boolean DEFAULT_RCS_USE_MSRP_SUCCESS = false;
	public static final boolean DEFAULT_RCS_USE_BINARY_SMS = false;
	public static final boolean DEFAULT_RCS_USE_MWI = false;
	public static final boolean DEFAULT_RCS_USE_OMAFDR = false;
	public static final boolean DEFAULT_RCS_USE_PARTIAL_PUB = false;
	public static final boolean DEFAULT_RCS_USE_PRESENCE = false;
	public static final boolean DEFAULT_RCS_USE_RLS = false;
	public static final String DEFAULT_RCS_SMSC = "sip:+331000000000@doubango.org";
	public static final NgnPresenceStatus DEFAULT_RCS_STATUS = NgnPresenceStatus.Online;
	//sks added	
	//by rockman 2012/9/17
	public static final String XCAP_GroupContacts_Need_Update = "XCAP_GroupContacts_Need_Update." + TAG;
	
	//by rockman 2012/9/26
	public static final String XCAP_GroupContacts_For_Single = "XCAP_GroupContacts_For_Single." + TAG;  //锟芥储锟接凤拷锟斤拷锟斤拷锟斤拷锟截碉拷(锟斤拷锟斤拷)通锟斤拷录锟斤拷锟斤拷 
	public static final String XCAP_GroupContacts_For_Group = "XCAP_GroupContacts_For_Group." + TAG;  //锟芥储锟接凤拷锟斤拷锟斤拷锟斤拷锟截碉拷(锟斤拷)通锟斤拷录锟斤拷锟斤拷 
	
	//GPS by rockman 2012/7/24
	public static final String GPS_SENDTO_HOST = "GPS_SENDTO_HOST." + TAG;
	public static final String GPS_SENDTO_PORT = "GPS_SENDTO_PORT." + TAG;
	
	//default value
	//by rockman 2012/9/17
	public static final boolean DEFAULT_XCAP_GroupContacts_Need_Update = true;
	
	//by rockman 2012/9/26
	public static final String DEFAULT_XCAP_GroupContacts_For_Single = null;  //锟芥储锟接凤拷锟斤拷锟斤拷锟斤拷锟截碉拷(锟斤拷锟斤拷)通锟斤拷录锟斤拷锟斤拷 
	public static final String DEFAULT_XCAP_GroupContacts_For_Group = null + TAG;  //锟芥储锟接凤拷锟斤拷锟斤拷锟斤拷锟截碉拷(锟斤拷)通锟斤拷录锟斤拷锟斤拷 
	
	//GPS by rockman 2012/7/24
	public static final String DEFAULT_GPS_SENDTO_HOST = "10.0.0.254" ;
	public static final int DEFAULT_GPS_SENDTO_PORT = 9000;
	
	public static final boolean DEFAULT_SYSTEM_FORMAT_TIME = false;
	
	// Service-Route
	public static final String SERVICE_ROUTE = "SERVICE_ROUTE." + TAG;
	public static final String DEFAULT_SERVICE_ROUTE = "sip:orig@scscf.test.com:6060;lr";
	//
	//fileserver by wangds 2014/7/12   ip:port
	public static final String FILE_SERVER_URL = "FILE_SERVER_URL." + TAG;
	public static final String FILE_SERVER_HOST = "FILE_SERVER_HOST." + TAG;
	public static final String FILE_SERVER_PORT = "FILE_SERVER_PORT." + TAG;
	public static final String DEFAULT_FILE_SERVER_URL = "appserver.test.com:8010";
	public static final String DEFAULT_FILE_SERVER_HOST = "appserver.test.com";
	public static final String DEFAULT_FILE_SERVER_PORT = "8010";
	//GISserver by gle
	public static final String MAP_SERVER_URL = "MAP_SERVER_URL." + TAG;
	public static final String DEFAULT_MAP_SERVER_URL = "appserver.test.com:8080/GIS_lhr_gd";
//	public static final String DEFAULT_MAP_SERVER_URL = "170.1.1.100:8080/GIS_lhr";
	
	public static final String LOCAL_IP = "localIp";
	
	//Gps Report Period
	public static final String GPS_REPORT_PERIOD = "5"; //锟斤拷位锟斤拷锟斤拷
	
	public static final int AUDIO_HEARDBEAT_PERIOD = 15000; //锟斤拷位锟斤拷锟斤拷
	
	public static final int CHECK_THREAD_DELT = 1000;
		
	//sks added end
}
