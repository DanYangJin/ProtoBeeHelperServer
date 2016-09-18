package com.skyworth.beehelperserver.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import org.apache.http.protocol.HTTP;

import com.google.gson.Gson;
import com.mipt.clientcommon.CommonUtils;
import com.mipt.clientcommon.InternalConstants;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.widget.TextView;

/**
 * all common method should be reside here
 */
public class Utils {

	public static Gson mGson = new Gson();
	public static final String FULL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String SIMPLE_DATE_FORMAT = "HH:mm:ss";

	public static String formatSize(long size) {
		String strSize = null;
		DecimalFormat format = new DecimalFormat("#0.0");
		if (size / 1024.0 / 1024 / 1024 >= 1) {
			strSize = format.format(size / 1024.0 / 1024 / 1024) + "GB";
		} else if (size / 1024.0 / 1024 >= 1) {
			strSize = format.format(size / 1024.0 / 1024) + "MB";
		} else if (size / 1024.0 >= 1) {
			strSize = format.format(size / 1024.0) + "KB";
		} else {
			strSize = size + "B";
		}
		return strSize;
	}

	public static boolean checkInstallStatus(Context context, String packageName) {
		PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
		List<String> pName = new ArrayList<String>();
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				pName.add(pn);
			}
		}
		return pName.contains(packageName);
	}

	public static boolean checkPackageInstall(String packageName, Context ctx) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			PackageManager packageManager = ctx.getPackageManager();
			packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	public static int getVersionCode(String packageName, Context ctx) {
		if (packageName == null || "".equals(packageName))
			return -1;
		try {
			PackageManager packageManager = ctx.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			return -1;
		}
	}

	public static String getVersionName(String packageName, Context ctx) {
		if (packageName == null || "".equals(packageName))
			return "";
		try {
			PackageManager packageManager = ctx.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return packageInfo.versionName;
		} catch (NameNotFoundException e) {
			return "";
		}
	}

	public static String getClientVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (Exception e) {
			return "";
		}
	}

	public static int getClientVersionCode(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (Exception e) {
			return -1;
		}
	}

	public static String getSdkLevel(String minVersionCode) {
		String sdkLevel = null;
		if (minVersionCode != null) {
			int code = Integer.parseInt(minVersionCode);
			switch (code) {
			case 1:
				sdkLevel = "Android 1.0";
				break;
			case 2:
				sdkLevel = "Android 1.1";
				break;
			case 3:
				sdkLevel = "Android 1.5";
				break;
			case 4:
				sdkLevel = "Android 1.6";
				break;
			case 5:
				sdkLevel = "Android 2.0";
				break;
			case 6:
				sdkLevel = "Android 2.0.1";
				break;
			case 7:
				sdkLevel = "Android 2.1";
				break;
			case 8:
				sdkLevel = "Android 2.2";
				break;
			case 9:
				sdkLevel = "Android 2.3";
				break;
			case 10:
				sdkLevel = "Android 2.3.3";
				break;
			case 11:
				sdkLevel = "Android 3.0";
				break;
			case 12:
				sdkLevel = "Android 3.1";
				break;
			case 13:
				sdkLevel = "Android 3.2";
				break;
			case 14:
				sdkLevel = "Android 4.0";
				break;
			case 15:
				sdkLevel = "Android 4.0.3";
				break;
			case 16:
				sdkLevel = "Android 4.1";
				break;
			case 17:
				sdkLevel = "Android 4.2";
				break;
			case 18:
				sdkLevel = "Android 4.3";
				break;
			case 19:
				sdkLevel = "Android 4.4";
				break;
			case 20:
				sdkLevel = "Android L";
				break;
			}
		}
		return sdkLevel;
	}

	public static CharSequence convertStyleText(CharSequence text) {
		String textStr = String.valueOf(text);
		textStr = textStr.replace("-", "- ");
		if (text instanceof Spannable) {
			// appendSpans(text, textStr);
			return text;
		} else {
			return textStr;
		}
	}

	// textview 不同字段显示不同颜色
	public static void setTextViewForegroundColor(TextView v, int color, int start, int end) {
		String text = v.getText().toString();
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
		int dempEnd = end == -1 ? text.length() : end;
		builder.setSpan(colorSpan, start, dempEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		v.setText(builder, null);
	}

	public static void setTextViewForegroundColor(TextView v, int color, String... tags) {
		if (null == tags) {
			return;
		}
		String text = v.getText().toString();
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
		int start, end;
		for (String tag : tags) {
			start = text.indexOf(tag);
			if (start == -1) {
				continue;
			}
			end = start + tag.length();
			builder.setSpan(colorSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		}
		v.setText(builder);
	}

	public static boolean isIntentExisting(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (resolveInfo.size() > 0) {
			return true;
		}
		return false;
	}

	public static String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}

	/**
	 * 时间戳转换成日期格式字符串
	 * 
	 * @param seconds
	 *            精确到秒的字符串
	 * @param formatStr
	 * @return
	 */
	public static String timeStamp2Date(String seconds, String format) {
		if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
			return "";
		}
		if (format == null || format.isEmpty()) {
			format = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(Long.valueOf(seconds + "000")));
	}

	/**
	 * 日期格式字符串转换成时间戳
	 * 
	 * @param date
	 *            字符串日期
	 * @param format
	 *            如：yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String date2TimeStamp(String date_str, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return String.valueOf(sdf.parse(date_str).getTime() / 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 取得当前时间戳（精确到秒）
	 * 
	 * @return
	 */
	public static String timeStamp() {
		long time = System.currentTimeMillis();
		String t = String.valueOf(time / 1000);
		return t;
	}

	public static String toJson(Object object) {
		return mGson.toJson(object);
	}

	public static byte[] encodeBase64(byte[] object) {
		return Base64.encode(object, Base64.DEFAULT);
	}

	public static byte[] decodeBase64(byte[] object) {
		return Base64.decode(object, Base64.DEFAULT);
	}

	public static String toJsonBase64(Object object) {
		String base64 = null;
		try {
			base64 = new String(Base64.encode(mGson.toJson(object).getBytes(), Base64.DEFAULT), HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return base64;
	}

	public static <T> T fromJson(String msg, Class<T> classOft) {
		return mGson.fromJson(msg, classOft);
	}

	public static <T> T fromJsonBase64(String msg, Class<T> classOft) {
		try {
			byte[] base64 = Base64.decode(msg, Base64.DEFAULT);
			return fromJson(new String(base64, HTTP.UTF_8), classOft);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getDeviceIp(Context mCtx) {
		int type = CommonUtils.getDevNetworkType(mCtx);
		String ip = null;
		switch (type) {
		case InternalConstants.NETWORK_TYPE_WIFI:
			ip = getWirelessIp(mCtx);
			break;
		case InternalConstants.NETWORK_TYPE_ETHERNET:
			ip = getEthernetIp();
			break;
		case InternalConstants.NETWORK_TYPE_MOBILE:
			break;
		default:
			break;
		}
		return ip;
	}

	public static String getWirelessIp(Context mCtx) {
		// 获取wifi服务
		WifiManager manager = (WifiManager) mCtx.getSystemService(Context.WIFI_SERVICE);
		// 判断wifi是否开启
		if (!manager.isWifiEnabled()) {
			manager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = manager.getConnectionInfo();
		if (wifiInfo == null) {
			return null;
		}
		int i = wifiInfo.getIpAddress();
		return constructDeviceIp(i);
	}

	public static String constructDeviceIp(int i) {
		StringBuilder builder = new StringBuilder();
		builder.append(i & 0xFF).append(".").append((i >> 8) & 0xFF).append(".").append((i >> 16) & 0xFF).append(".")
				.append((i >> 24) & 0xFF).append(".");
		String address = builder.substring(0, builder.length() - 1).toString();
		return address;
	}

	public static String getEthernetIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "0.0.0.0";
	}

	public static String getConnectWifiSsid(Context mContext) {
		WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		if (!manager.isWifiEnabled()) {
			manager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = manager.getConnectionInfo();
		if (wifiInfo == null) {
			return null;
		}
		return wifiInfo.getSSID();
	}

	public static String getRandomID() {
		char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" + "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
				.toCharArray();
		char[] randBuffer = new char[10];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[new Random().nextInt(71)];
		}
		return new String(randBuffer);
	}

}
