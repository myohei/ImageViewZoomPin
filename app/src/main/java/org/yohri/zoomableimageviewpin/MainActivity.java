package org.yohri.zoomableimageviewpin;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.yohei.ui.widget.ImageViewTouchPin;

import java.io.BufferedInputStream;
import java.io.IOException;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager am = getAssets();
        ImageViewTouchPin imageViewTouchPin = (ImageViewTouchPin) findViewById(R.id.img);
        try {
            final Matrix matrix = imageViewTouchPin.getDisplayMatrix();
            final Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(am.open("niko.jpg")));
            imageViewTouchPin.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.d(TAG, "", e);
        }
        imageViewTouchPin.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        imageViewTouchPin.setOnPinTapListener(new ImageViewTouchPin.OnPinTapListener() {
            @Override
            public void onPinSelected(ImageViewTouchPin.Pin pin) {
                Log.d(TAG, "on Pin Tab");
            }

            @Override
            public void onCreatedPin(ImageViewTouchPin.Pin pin) {
                Log.d(TAG, "on Pin Created");
            }
        });
    }
}
