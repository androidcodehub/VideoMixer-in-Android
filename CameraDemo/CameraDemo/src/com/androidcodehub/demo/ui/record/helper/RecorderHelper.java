package com.androidcodehub.demo.ui.record.helper;

import com.androidcodehub.demo.log.Logger;
import com.androidcodehub.demo.preference.PreferenceKeys;
import com.androidcodehub.demo.preference.PreferenceUtils;
import com.google.gson.Gson;
import com.yixia.camera.util.FileUtils;
import com.yixia.camera.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;


public class RecorderHelper {


	public static int getVideoBitrate() {
		//		if (NetworkUtils.isWifiAvailable(VideoApplication.getContext()) || !PreferenceUtils.getBoolean(PreferenceKeys.VIDEO_BITRATE_3G_600K, PreferenceKeys.VIDEO_BITRATE_3G_600K_DEFAULT))
		//			return 1500;//MediaRecorder.VIDEO_BITRATE_MEDIUM;//WIFI下800码率
		//		else {
		//			return 800;//MediaRecorder.VIDEO_BITRATE_NORMAL;//3G、2G下600码率
		//		}
		return 1500;
	}


	public static int getMaxDuration() {
		return PreferenceUtils.getIntProcess(PreferenceKeys.VIDEO_TIME_LIMIT, PreferenceKeys.VIDEO_TIME_LIMIT_DEFAULT);
	}


	public static void removeDuration() {
		PreferenceUtils.remove(PreferenceKeys.VIDEO_TIME_LIMIT);
	}


	public static boolean saveObject(Object obj, String target) {
		try {
			if (StringUtils.isNotEmpty(target)) {
				FileOutputStream out = new FileOutputStream(target);
				Gson gson = new Gson();
				out.write(gson.toJson(obj).getBytes());
				out.flush();
				out.close();
				return true;
			}
		} catch (Exception e) {
			Logger.e(e);
		}
		return false;
	}

	public static <T> T restoreObject(Class<T> cls, String target) {
		try {
			String sb = FileUtils.readFile(new File(target));
			if (sb != null) {
				String str = sb.toString();
				Gson gson = new Gson();
				T result = gson.fromJson(str.toString(), cls);
				return result;
			}
		} catch (Exception e) {
			Logger.e(e);
		}
		return null;
	}
}
