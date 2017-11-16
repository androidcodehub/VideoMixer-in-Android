package com.androidcodehub.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.androidcodehub.demo.service.AssertService;
import com.yixia.camera.VCamera;
import com.yixia.camera.util.DeviceUtils;

import java.io.File;

public class CameraDemoApplication extends Application {

	private static CameraDemoApplication application;

	@Override
	public void onCreate() {
		super.onCreate();
		application = this;

		File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		if (DeviceUtils.isZte()) {
			if (dcim.exists()) {
				VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
			} else {
				VCamera.setVideoCachePath(dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/Camera/VCameraDemo/");
			}
		} else {
			VCamera.setVideoCachePath(dcim + "/Camera/VCameraDemo/");
		}

		VCamera.setDebugMode(true);
		VCamera.initialize(this);

		startService(new Intent(this, AssertService.class));
	}

	public static Context getContext() {
		return application;
	}
}
