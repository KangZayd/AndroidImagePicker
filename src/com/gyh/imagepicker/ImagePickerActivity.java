
package com.gyh.imagepicker;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

public abstract class ImagePickerActivity extends Activity {

    private static final int REQUEST_CAMERA = 0;

    private static final int REQUEST_GALLERY = 1;

    private static final int REQUEST_CROP = 2;

    private static final int AVATAR_SIZE = 96;

    private View pickerView;

    /**
     * show or hide imagePicker layout
     */
    public void toggleImagePicker() {
        if (pickerView == null) {
            return;
        }
        if (pickerView.getVisibility() == View.VISIBLE) {
            pickerView.setVisibility(View.INVISIBLE);
        } else {
            pickerView.setVisibility(View.VISIBLE);
            Animation fadeInAnim = makeFadeInAnim();
            pickerView.startAnimation(fadeInAnim);
            Animation popUpInAnim = makePopupInAnim(true);
            pickerView.findViewById(R.id.layout_pick_photo)
                    .startAnimation(popUpInAnim);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        FrameLayout rootView = new FrameLayout(this);
        // 1,Add customView
        LayoutInflater mInflater = LayoutInflater.from(this);
        View customView = mInflater.inflate(layoutResID, rootView);
        rootView.addView(customView);
        // 2,Add pickerView
        pickerView = mInflater.inflate(R.layout.image_picker, null);
        pickerView.setVisibility(View.INVISIBLE);
        rootView.addView(pickerView);
        setBtnsListener(pickerView);
        super.setContentView(rootView);
    }

    @Override
    public void setContentView(View customView) {
        FrameLayout rootView = new FrameLayout(this);
        // 1,Add customView
        rootView.addView(customView);
        // 2,Add pickerView
        LayoutInflater mInflater = LayoutInflater.from(this);
        pickerView = mInflater.inflate(R.layout.image_picker, null);
        pickerView.setVisibility(View.INVISIBLE);
        rootView.addView(pickerView);
        setBtnsListener(pickerView);
        super.setContentView(rootView);
    }

    /**
     * set OnclickListener to Buttons
     * 
     * @param view
     */
    private void setBtnsListener(View view) {
        ImagePickerOnClickerListener mOnClickListerner = new ImagePickerOnClickerListener();
        View layout_black = view.findViewById(R.id.layout_black);
        layout_black.setOnClickListener(mOnClickListerner);
        View btn_take_photo = view.findViewById(R.id.btn_take_photo);
        btn_take_photo.setOnClickListener(mOnClickListerner);
        View btn_pick_photo = view.findViewById(R.id.btn_pick_photo);
        btn_pick_photo.setOnClickListener(mOnClickListerner);
        View btn_cancel = view.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(mOnClickListerner);
    }

    @Deprecated
    @Override
    public void setContentView(View view, LayoutParams params) {
        // @Deprecated
    }

    protected Animation makeFadeInAnim() {
        Animation anim;
        // Create a fade in alpha animation
        anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(300);
        anim.setInterpolator(new AccelerateInterpolator());
        return anim;
    }

    protected Animation makePopupInAnim(boolean withAlpha) {
        AnimationSet ret;
        Animation anim;
        ret = new AnimationSet(false);
        if (withAlpha) {
            // Create a fade in alpha animation
            anim = new AlphaAnimation(0f, 1f);
            anim.setDuration(300);
            anim.setInterpolator(new LinearInterpolator());
            ret.addAnimation(anim);
        }
        // Create a bottom pop up animation
        anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_PARENT, 1f,
                Animation.RELATIVE_TO_SELF, 0f);
        anim.setDuration(300);
        anim.setInterpolator(new DecelerateInterpolator(1f));
        ret.addAnimation(anim);
        return ret;
    }

    /**
     * check if intent is available
     * 
     * @param intent
     * @return
     */
    protected boolean isIntentAvailable(Intent intent) {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> list = packageManager
                .queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    protected abstract File getCorpCacheFile();

    protected abstract File getCamaraCacheFile();

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        if (requestCode == REQUEST_GALLERY) {
            // from Gallery
            Uri imageUri = data.getData();
            if (!corp(imageUri)) {
                onCropFailed();
            }
        } else if (requestCode == REQUEST_CAMERA) {
            // from Camera
            final File camaraCacheFile = getCamaraCacheFile();
            if (camaraCacheFile != null) {
                Uri uri = Uri.parse("file://"
                        + camaraCacheFile.getAbsolutePath());
                if (!corp(uri)) {
                    onCropFailed();
                }
            }
        } else if (requestCode == REQUEST_CROP && data != null) {
            onCropSucceed(getCorpCacheFile());
        }
    }

    /**
     * this will be called when a image has been cropped
     * successfully
     * 
     * @param file
     */
    protected abstract void onCropSucceed(File file);

    protected abstract void onCropFailed();

    private boolean corp(Uri uri) {
        Intent cropIntent = new Intent(
                "com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", AVATAR_SIZE);
        cropIntent.putExtra("outputY", AVATAR_SIZE);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("outputFormat",
                Bitmap.CompressFormat.PNG.toString());
        Uri cropUri = Uri.fromFile(getCorpCacheFile());
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        if (!isIntentAvailable(cropIntent)) {
            return false;
        } else {
            try {
                startActivityForResult(cropIntent, REQUEST_CROP);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private class ImagePickerOnClickerListener implements
            OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.layout_black:
                    toggleImagePicker();
                    break;
                // take a photo from camera
                case R.id.btn_take_photo:
                    intent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    File camaraCacheFile = getCamaraCacheFile();
                    if (camaraCacheFile.exists()) {
                        camaraCacheFile.delete();
                    }
                    intent.putExtra(
                            android.provider.MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(camaraCacheFile));
                    if (!isIntentAvailable(intent)) {
                        return;
                    }
                    startActivityForResult(intent, REQUEST_CAMERA);
                    pickerView.setVisibility(View.INVISIBLE);
                    break;
                case R.id.btn_pick_photo:
                    intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "image/*");
                    if (!isIntentAvailable(intent)) {
                        return;
                    }
                    startActivityForResult(intent, REQUEST_GALLERY);
                    pickerView.setVisibility(View.INVISIBLE);
                    break;
                case R.id.btn_cancel:
                    pickerView.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }

    }

}
