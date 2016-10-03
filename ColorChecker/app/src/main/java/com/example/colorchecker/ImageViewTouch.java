package com.example.colorchecker;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * This stupid code is created by thantieuhodo on 10/3/16.
 */
public class ImageViewTouch extends ImageView {

    public ImageViewTouch(Context context) {
        super(context);
    }

    public ImageViewTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewTouch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ImageViewTouch(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
