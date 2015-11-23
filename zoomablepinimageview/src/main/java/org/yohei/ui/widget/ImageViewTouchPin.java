package org.yohei.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.yohri.zoomablepinimageview.R;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by yohei on 2015/11/20.
 */
public class ImageViewTouchPin extends ImageViewTouch {

    private List<Pin> mPinList = new ArrayList<>();
    private Pin mSelectedPin = null;
    private OnPinTapListener mOnPinTapListener;

    private static float sPinImgWidth = -1;
    private static float sPinImgHeight = -1;
    private static final float TAP_SPACE = 20f;

    private Bitmap mUnselectedMarker;
    private Bitmap mSelectedMarker;


    public ImageViewTouchPin(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewTouchPin(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyle) {
        super.init(context, attrs, defStyle);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pin);
        sPinImgWidth = (float) bitmap.getWidth();
        sPinImgHeight = (float) bitmap.getHeight();
        bitmap.recycle();
        mUnselectedMarker = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pin_unselected);
        mSelectedMarker = BitmapFactory.decodeResource(getResources(), R.drawable.ic_pin);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        final float x = e.getX();
        final float y = e.getY();
        final RectF bitmapRect = getBitmapRect();
        float scale = getScale();
        float baseScale = getScale(mBaseMatrix);
        final PointF pointInImage = new PointF(((-(bitmapRect.left) + x) / scale / baseScale), ((-(bitmapRect.top) + y) / scale / baseScale));
        final Pin foundPin = findPinByPoint(pointInImage);
        if (foundPin != null) {
            mSelectedPin = foundPin;
            if (mOnPinTapListener != null) {
                mOnPinTapListener.onPinSelected(foundPin);
            }
        } else if (isTapInImage(x, y)) {
            final Pin newPin = new Pin(pointInImage);
            mPinList.add(newPin);
            if (mOnPinTapListener != null) {
                mOnPinTapListener.onCreatedPin(newPin);
            }
        }
        invalidate();
        return super.onSingleTapConfirmed(e);
    }

    private boolean isTapInImage(float x, float y) {
        return getBitmapRect().contains(x, y);
    }

    @Nullable
    private Pin findPinByPoint(final PointF pointInImage) {
        for (Pin pin : mPinList) {
            // TODO: どうにか探す。今のままだとscaleしてるのとしてないのとで。
            final float pinX = pin.getPointInImage().x;
            final float pinY = pin.getPointInImage().y;
            if ((pinX - TAP_SPACE - sPinImgWidth / 2 <= pointInImage.x && pointInImage.x <= pinX + TAP_SPACE + sPinImgWidth / 2)
                    && (pinY - TAP_SPACE - sPinImgHeight <= pointInImage.y && pointInImage.y <= pinY + TAP_SPACE)) {
                return pin;
            }
        }
        return null;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mPinList.isEmpty()) {
            final RectF imgRect = getBitmapRect();
            final float scale = getScale();
            final float baseScale = getScale(mBaseMatrix);
            for (Pin pin : mPinList) {
                final float scaledX = pin.getPointInImage().x * scale * baseScale;
                final float scaledY = pin.getPointInImage().y * scale * baseScale;
                if (imgRect.contains(scaledX + imgRect.left, scaledY + imgRect.top)) { // これじゃだめだ。
                    boolean isSelectedPin = pin.equals(mSelectedPin);
                    final Bitmap marker = isSelectedPin ? mSelectedMarker : mUnselectedMarker;
                    // 描画はこれでOKぽい
                    final float left = (imgRect.left + scaledX) - marker.getWidth() / 2;
                    final float top = (imgRect.top + scaledY) - marker.getHeight();
                    canvas.drawBitmap(marker, left, top, null);
                }
            }
        }
    }

    public OnPinTapListener getOnPinTapListener() {
        return mOnPinTapListener;
    }

    public void setOnPinTapListener(OnPinTapListener onPinTapListener) {
        mOnPinTapListener = onPinTapListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    /**
     * @param pin
     */
    public void addPin(Pin pin) {
        mPinList.add(pin);
    }

    /**
     * 選択中のpin
     *
     * @return
     */
    public Pin getSelectedPin() {
        return mSelectedPin;
    }

    /**
     * pin削除
     *
     * @param pin
     * @return true: 削除した, false:それ以外
     */
    public boolean removePin(Pin pin) {
        for (Pin p : mPinList) {
            if (p.equals(pin)) {
                mPinList.remove(p);
                return true;
            }
        }
        return false;
    }

    /**
     * 登録してあるpinリスト
     *
     * @return
     */
    public List<Pin> getPinList() {
        return new ArrayList<>(mPinList);
    }

    public static class Pin implements Parcelable {
        @NonNull
        private final PointF mPointInImage;

        public Pin(@NonNull PointF pointInImage) {
            mPointInImage = pointInImage;
        }

        @NonNull
        public PointF getPointInImage() {
            return mPointInImage;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pin pin = (Pin) o;

            return mPointInImage.equals(pin.mPointInImage);

        }

        @Override
        public int hashCode() {
            return mPointInImage.hashCode();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mPointInImage, 0);
        }

        protected Pin(Parcel in) {
            this.mPointInImage = in.readParcelable(PointF.class.getClassLoader());
        }

        public static final Creator<Pin> CREATOR = new Creator<Pin>() {
            public Pin createFromParcel(Parcel source) {
                return new Pin(source);
            }

            public Pin[] newArray(int size) {
                return new Pin[size];
            }
        };
    }

    public interface OnPinTapListener {
        void onPinSelected(Pin pin);

        void onCreatedPin(Pin pin);
    }
}
