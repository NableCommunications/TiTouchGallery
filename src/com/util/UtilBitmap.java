package com.util;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

public class UtilBitmap {

	public static Bitmap decodeFile(String file, int reqW, int reqH) {

		if (file == null) {
			return null;
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, options);

		UtilSize size = decodeSize(options.outWidth, options.outHeight, reqW, reqH);
		if (size.pxW() < 1 || size.pxH() < 1) {
			return null;
		}

		// SampleSize
		options.inSampleSize = sampleSize(options.outWidth, options.outHeight, size.pxW(), size.pxH());
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.RGB_565;

		return BitmapFactory.decodeFile(file, options);
	}

	public static BitmapFactory.Options options(Context context, String url, int reqW, int reqH) {

		if (context != null && url != null) {

			try {

				InputStream is = context.getContentResolver().openInputStream(Uri.parse(url));
				if (is == null) {
					return null;
				}

				// Bitmap Size
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(is, null, options);

				is.close();

				UtilSize size = decodeSize(options.outWidth, options.outHeight, reqW, reqH);
				if (size.pxW() < 1 || size.pxH() < 1) {
					return null;
				}

				options.inSampleSize = sampleSize(options.outWidth, options.outHeight, size.pxW(), size.pxH());
				options.inJustDecodeBounds = false;
				options.inPreferredConfig = Config.RGB_565;

				return options;

			} catch (IOException e) {
			}
		}
		return null;
	}

	public static int degree(String file) {

		if (file != null) {

			try {

				ExifInterface exif = new ExifInterface(file);

				int rotate = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
				switch (rotate) {

				case ExifInterface.ORIENTATION_ROTATE_90:
					return 90;

				case ExifInterface.ORIENTATION_ROTATE_180:
					return 180;

				case ExifInterface.ORIENTATION_ROTATE_270:
					return 270;
				}
			} catch (IOException e) {
			}
		}
		return 0;
	}

	public static Bitmap rotate(Bitmap bitmap, int degree) {

		if (bitmap != null && degree != 0) {

			int srcW = bitmap.getWidth();
			int srcH = bitmap.getHeight();

			Matrix matrix = new Matrix();
			matrix.setRotate(degree, (float) srcW / 2, (float) srcH / 2);

			Bitmap rotate = Bitmap.createBitmap(bitmap, 0, 0, srcW, srcH,
					matrix, false);
			if (rotate != bitmap) {
				bitmap.recycle();
				bitmap = rotate;
			}
		}
		return bitmap;
	}

	/**
	 * bitmap 의 원본 크기 이상으로 decode 하지 않기 위해, decode 할 크기를 비율을 적용하여 계산
	 */
	private static UtilSize decodeSize(int srcW, int srcH, int reqW, int reqH) {

		int desW = srcW;
		int desH = srcH;

		if (srcW > reqW || srcH > reqH) {

			float rateW = (float) srcW / (float) reqW;
			float rateH = (float) srcH / (float) reqH;

			if (rateW > rateH) {

				desW = (int) ((float) srcW / rateW);
				desH = (int) ((float) srcH / rateW);

			} else {

				desW = (int) ((float) srcW / rateH);
				desH = (int) ((float) srcH / rateH);
			}
		}
		return new UtilSize(desW, desH);
	}

	/**
	 * decode 를 위한 sample size 를 계산한다.
	 */
	private static int sampleSize(int srcW, int srcH, int desW, int desH) {

		if (srcW > desW || srcH > desH) {

			if (srcW > srcH) {
				return Math.round((float) srcH / (float) desH);
			} else {
				return Math.round((float) srcW / (float) desW);
			}
		}
		return 1;
	}
}