package com.util;

import java.io.FileOutputStream;
import java.io.IOException;

import org.appcelerator.kroll.common.Log;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class UtilFile {
	
	private static final String LOG_TAG = "UtilFile";

	public static String name(String path) {
		if (path == null) {
			return null;
		}
		return path.substring(path.lastIndexOf("/") + 1);
	}

	public static boolean saveFile(Bitmap bitmap, String path, int quality, boolean isRecycle) {
		try {

			if (bitmap != null && path != null) {

				FileOutputStream fo = new FileOutputStream(path);
				boolean value = bitmap.compress(CompressFormat.JPEG, quality, fo);

				fo.flush();
				fo.close();

				if (isRecycle) {
					bitmap.recycle();
				}
				return value;
			}
		} catch (IOException e) {

			if (Log.isDebugModeEnabled()) {
				Log.d(LOG_TAG, "e = " + e.toString());
			}
		}
		return false;
	}
}