/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ru.truba.touchgallery.TouchView;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.appcelerator.kroll.common.Log;

import ru.truba.touchgallery.TouchView.InputStreamWrapper.InputStreamProgressListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.gbaldera.titouchgallery.RHelper;
import com.util.UtilBitmap;
import com.util.UtilFile;

public class UrlTouchImageView extends RelativeLayout {

	private static final String LOG_TAG = "UrlTouchImageView";

	private static final int MAX_IMAGE_W = 960;
	private static final int MAX_IMAGE_H = 960;
	
	private static String CACHE_FOLDER = null;

    protected ProgressBar mProgressBar;
    protected TouchImageView mImageView;

    protected Context mContext;

    public UrlTouchImageView(Context ctx)
    {
        super(ctx);
        mContext = ctx;
        init();

    }
    public UrlTouchImageView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mContext = ctx;
        init();
    }
    public TouchImageView getImageView() { return mImageView; }

    @SuppressWarnings("deprecation")
    protected void init() {
        mImageView = new TouchImageView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        mImageView.setLayoutParams(params);
        this.addView(mImageView);
        mImageView.setVisibility(GONE);

        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
        params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMargins(30, 0, 30, 0);
        mProgressBar.setLayoutParams(params);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(100);
        this.addView(mProgressBar);
    }

    public void setUrl(String imageUrl)
    {
        new ImageLoadTask().execute(imageUrl);
    }
    
    public void setScaleType(ScaleType scaleType) {
        mImageView.setScaleType(scaleType);
    }
    
    //No caching load
    public class ImageLoadTask extends AsyncTask<String, Integer, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            Bitmap bitmap = null;

        	if (url != null && url.indexOf("file://") == 0) {

        		url = url.replace("file://", "");

        		bitmap = UtilBitmap.decodeFile(url, MAX_IMAGE_W, MAX_IMAGE_H);
                bitmap = UtilBitmap.rotate(bitmap, UtilBitmap.degree(url));

        	} else if(url != null && url.indexOf("video://") == 0) {

        		url = url.replace("video://", "");

        		bitmap = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Video.Thumbnails.MINI_KIND);

        		mImageView.setMaxScale(1f);

        	} else {
        		
        		if (CACHE_FOLDER == null && mContext != null) {
        			StringBuilder sb = new StringBuilder("/data/data/");
        			sb.append(mContext.getPackageName());
        			sb.append("/app_appdata/cache/");
        			
        			CACHE_FOLDER = sb.toString();
        		}

        		String[] split = url.split(";");
        		if (split.length > 1) {
        			
        			String path = CACHE_FOLDER + UtilFile.name(split[1]);
        			if(new File(path).exists()) {
        				
        				bitmap = UtilBitmap.decodeFile(path, MAX_IMAGE_W, MAX_IMAGE_H);

        			} else {

						HttpURLConnection connection = null;

						try {
							URL aURL = new URL(split[0]);
							connection = (HttpURLConnection) aURL.openConnection();
							connection.setRequestProperty("Connection", "close");
							connection.connect();
		
							InputStream is = connection.getInputStream();
							int totalLength = connection.getContentLength();
		
							InputStreamWrapper bis = new InputStreamWrapper(is, 8192, totalLength);
							bis.setProgressListener(new InputStreamProgressListener() {
		
								public void onProgress(float progressValue, long bytesLoaded, long bytesTotal) {
									publishProgress((int) (progressValue * 100));
								}
							});
		
							bitmap = BitmapFactory.decodeStream(is, null, UtilBitmap.options(mContext, url, MAX_IMAGE_W, MAX_IMAGE_H));
							UtilFile.saveFile(bitmap, path, 100, false);
		
							bis.close();
							is.close();
		
						} catch (Exception e) {
		
							if (Log.isDebugModeEnabled()) {
								Log.d(LOG_TAG, "e = " + e.toString());
							}
						} finally {
		
							if (connection != null) {
								connection.disconnect();
							}
						}
        			}
        		}
        	}
	        return bitmap;
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	if (bitmap == null) 
        	{
        		mImageView.setScaleType(ScaleType.CENTER);
        		bitmap = BitmapFactory.decodeResource(getResources(), RHelper.drawable.no_photo);
        		mImageView.setImageBitmap(bitmap);
        	}
        	else 
        	{
        		mImageView.setScaleType(ScaleType.MATRIX);
	            mImageView.setImageBitmap(bitmap);
        	}
            mImageView.setVisibility(VISIBLE);
            mProgressBar.setVisibility(GONE);
        }

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			mProgressBar.setProgress(values[0]);
		}
    }
}
