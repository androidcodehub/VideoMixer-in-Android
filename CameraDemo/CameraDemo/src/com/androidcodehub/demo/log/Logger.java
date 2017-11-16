package com.androidcodehub.demo.log;

import android.content.ActivityNotFoundException;
import android.database.sqlite.SQLiteFullException;
import android.util.Log;


import com.androidcodehub.demo.BuildConfig;

import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

public class Logger {

	public static final boolean IsDebug = BuildConfig.DEBUG;
	private static final String TAG = "[FFMPEGCameraDemo]";

	public static void printStackTrace(String TAG, Exception e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	public static void printStackTrace(String TAG, IOException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	public static void printStackTrace(String TAG, ClientProtocolException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	public static void printStackTrace(String TAG, MalformedURLException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}


	public static void printStackTrace(String TAG, IllegalArgumentException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	public static void printStackTrace(String TAG, HttpException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	public static void printStackTrace(String TAG, ActivityNotFoundException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	public static void printStackTrace(String TAG, IndexOutOfBoundsException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	/**
	 * 
	 * @param e
	 */
	public static void printStackTrace(String TAG, FileNotFoundException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	// ~~~ 数据库相关

	public static void printStackTrace(String TAG,
			android.database.sqlite.SQLiteException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}

	public static void printStackTrace(String TAG, SQLiteFullException e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}


	public static void printStackTrace(String TAG, Throwable e) {
		if (IsDebug) {
			e.printStackTrace();
		} else {
			logException(TAG, e);
		}
	}


	private static void logException(String TAG, Throwable ex) {

	}

	public static void d(String tag, String msg) {
		if (IsDebug) {
			Log.d(tag, msg);
		}
	}

	public static void d(String msg) {
		Log.d(TAG, msg);
	}


	public static void d(String tag, String msg, Throwable tr) {
		if (IsDebug) {
			Log.d(tag, msg, tr);
		}
	}

	public static void e(Throwable tr) {
		if (IsDebug) {
			Log.e(TAG, "", tr);
		}
	}

	public static void i(String msg) {
		if (IsDebug) {
			Log.i(TAG, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (IsDebug) {
			Log.i(tag, msg);
		}
	}


	public static void i(String tag, String msg, Throwable tr) {
		if (IsDebug) {
			Log.i(tag, msg, tr);
		}

	}


	public static void e(String tag, String msg) {
		if (IsDebug) {
			Log.e(tag, msg);
		}
	}

	public static void e(String msg) {
		if (IsDebug) {
			Log.e(TAG, msg);
		}
	}


	public static void e(String tag, String msg, Throwable tr) {
		if (IsDebug) {
			Log.e(tag, msg, tr);
		}
	}

	public static void e(String msg, Throwable tr) {
		if (IsDebug) {
			Log.e(TAG, msg, tr);
		}
	}
}
