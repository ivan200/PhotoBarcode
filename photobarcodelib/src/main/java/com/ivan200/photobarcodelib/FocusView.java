package com.ivan200.photobarcodelib;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import androidx.core.content.ContextCompat;

public class FocusView extends View {
    private boolean animated;
    private Paint paint;
    private float strokeWidth = 3.0f;
    private AnimationSet animation = null;

    float fromX = 0f;
    float fromY = 0f;
    float toX = 1.0f;
    float toY = 1.0f;

    public FocusView(Context context) {
        super(context);
        init();
    }

    public FocusView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public FocusView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init();
    }

    private void init() {
        this.animated = false;
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setColor(ContextCompat.getColor(getContext(), android.R.color.white));
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(this.strokeWidth);
    }

    public final void anim(long duration, float xValue, float yValue) {
        this.setVisibility(VISIBLE);
        this.animated = true;
        clearAnimation();

        if (this.animation != null) {
            this.animation.setAnimationListener(null);
            this.animation = null;
        }

        animation = new AnimationSet(false);

        ScaleAnimation scale = new ScaleAnimation(fromX, toX, fromY, toY);
        scale.setDuration(duration);
        TranslateAnimation trans = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, xValue - (getWidth() * fromX) / 2,
                TranslateAnimation.ABSOLUTE, xValue - (getWidth() * toX) / 2,
                TranslateAnimation.ABSOLUTE, yValue - (getHeight() * fromY) / 2,
                TranslateAnimation.ABSOLUTE, yValue - (getHeight() * toY) / 2);
        trans.setDuration(duration);
        this.animation.addAnimation(scale);
        this.animation.addAnimation(trans);
        this.animation.setRepeatCount(0);
        this.animation.setFillAfter(true);
        this.animation.setFillEnabled(true);
        this.animation.setFillBefore(true);
        this.animation.setAnimationListener(new Animation.AnimationListener() {
            public final void onAnimationEnd(Animation animation) {
                animated = false;
            }

            public final void onAnimationRepeat(Animation animation) {
            }

            public final void onAnimationStart(Animation animation) {
            }
        });
        startAnimation(this.animation);
    }

    void hide(){
        this.clearAnimation();
        this.setVisibility(INVISIBLE);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.animated) {
            canvas.drawCircle((float) (getWidth() / 2), (float) (getHeight() / 2),
                    (((float) getWidth()) / 2.0f) - (this.strokeWidth / 2.0f) - 1, this.paint);
        }
    }
}
