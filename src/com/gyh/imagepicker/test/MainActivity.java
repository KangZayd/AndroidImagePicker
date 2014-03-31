package com.gyh.imagepicker.test;

import java.io.File;

import com.gyh.imagepicker.ImagePickerActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends ImagePickerActivity {
	private static final String AVATAR_CORP_CACHE_NAME = "/avatar_corp_cache";
	private static final String AVATAR_CAMARA_CACHE_NAME = "/avatar_camara_cache";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FrameLayout container = new FrameLayout(this);
		container.setBackgroundResource(android.R.color.white);
		Button btn = new Button(this);
		btn.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		btn.setText("click me");
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleImagePicker();
			}
		});
		container.addView(btn);
		setContentView(container);
	}

	@Override
	protected void onCropSucceed(File file) {
		Log.d("gaoyihang.debug", file.getAbsolutePath());
	}

	@Override
	protected void onCropFailed() {
		Log.d("gaoyihang.debug", "onCropFailed");
	}

	private boolean isSDCardAvailable() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	protected File getCorpCacheFile() {
		String corp_cache_path = null;
		if (isSDCardAvailable()) {
			corp_cache_path = Environment.getExternalStorageDirectory()
					+ AVATAR_CORP_CACHE_NAME;
		} else {
			corp_cache_path = getCacheDir().getAbsolutePath()
					+ AVATAR_CORP_CACHE_NAME;
		}
		Log.d("gaoyihang.debug", corp_cache_path);
		return new File(corp_cache_path);
	}

	protected File getCamaraCacheFile() {
		String camara_temp_path = null;
		if (isSDCardAvailable()) {
			camara_temp_path = Environment.getExternalStorageDirectory()
					+ AVATAR_CAMARA_CACHE_NAME;
		} else {
			camara_temp_path = getCacheDir().getAbsolutePath()
					+ AVATAR_CAMARA_CACHE_NAME;
			Log.d("gaoyihang.debug", camara_temp_path);
		}

		return new File(camara_temp_path);
	}

}
