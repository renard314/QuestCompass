package de.renard.radar.views;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

public class RotateView extends View {

	private final Rect mDisplayFrame = new Rect();
	private final Paint mCirclePaint;
	private final Paint mLabelPaint;
	private final Paint mDescriptionPaint;
	private float mRotation;
	private String mDistanceText;
	private String mSpeedText;
	private Rect mRect = new Rect();
	private ObjectAnimator mAnimator;
	private int mMinimumSize=Integer.MAX_VALUE;
	private float mTargetDegree;
	
	@SuppressWarnings("unused")
	private final static String DEBUG_TAG =  RotateView.class.getSimpleName();


	
	public RotateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCirclePaint = new Paint() {
			{
				setColor(Color.WHITE);
				setStyle(Paint.Style.STROKE);
				setAntiAlias(true);
				setStrokeWidth(3);
				setMaskFilter(new BlurMaskFilter(2, Blur.SOLID));

			}
		};
		mLabelPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setAntiAlias(true);
				setStrokeWidth(3);
				setTextSize(40);
				setTextAlign(Align.CENTER);
				setMaskFilter(new BlurMaskFilter(2, Blur.SOLID));

			}
		};

		mDescriptionPaint = new Paint() {
			{
				setColor(Color.DKGRAY);
				setAntiAlias(true);
				setStrokeWidth(2);
				setTextSize(16);
				setTextAlign(Align.CENTER);
				setMaskFilter(new BlurMaskFilter(1, Blur.SOLID));

			}
		};

	}

	/**
	 * try to be a square view
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		getWindowVisibleDisplayFrame(mDisplayFrame);
		if (heightMode == MeasureSpec.UNSPECIFIED) {
			height = mDisplayFrame.height();
		}
		if (widthMode == MeasureSpec.UNSPECIFIED) {
			width = mDisplayFrame.width();
		}
		
		mMinimumSize = Math.min(mMinimumSize, width);
		mMinimumSize = Math.min(mMinimumSize, height);
		if (heightMode == MeasureSpec.UNSPECIFIED){
			Log.i(DEBUG_TAG,"height unspecified = " + height);
		}
		if (heightMode == MeasureSpec.AT_MOST){
			Log.i(DEBUG_TAG,"height atMost = " + height);
		}
		if (heightMode == MeasureSpec.EXACTLY){
			Log.i(DEBUG_TAG,"height exactly = "+ height);
		}
		if (widthMode == MeasureSpec.UNSPECIFIED){
			Log.i(DEBUG_TAG,"width unspecified = " + width);
		}
		if (widthMode == MeasureSpec.AT_MOST){
			Log.i(DEBUG_TAG,"width atMost = "+width);
		}
		if (widthMode == MeasureSpec.EXACTLY){
			Log.i(DEBUG_TAG,"width exactly= "+width);
		}

		int size = Math.min(width, height);
		size = Math.min(mMinimumSize, size);
		Log.i(DEBUG_TAG,"onMeasure: "+size);
		setMeasuredDimension(size, size);
	}
	
	private String buildDistanceString(final float distanceMeters) {
		float distance = distanceMeters;
		String format;
		String unit;
		if (distanceMeters >= 1000) {
			distance /= 1000;
			unit = "km";
			format = "%,.2f%s";
		} else {
			unit = "m";
			format = "%,.0f%s";
		}
		return String.format(format, distance, unit);
	}

	private String buildSpeedString(final float speedMPerSecond) {
		float speed = speedMPerSecond * 3.6f;
		String format = "%.1f%s";
		String unit = "Km/s";
		return String.format(format, speed, unit);
	}
	private void findTextSize() {
		if (mDistanceText != null && mRect.width() > 0 && mRect.height() > 0) {
			final int maxWidth = mRect.width();
			final int maxHeight = mRect.height();
			Rect textBounds = new Rect();
			int textSize = 60;
			mLabelPaint.setTextSize(textSize);
			mLabelPaint.getTextBounds(mDistanceText, 0, mDistanceText.length(), textBounds);
			while (textBounds.width() > maxWidth && textBounds.height() > maxHeight) {
				mLabelPaint.setTextSize(textSize -= 2);
				mLabelPaint.getTextBounds(mDistanceText, 0, mDistanceText.length(), textBounds);
			}
		}
	}

	public void setDistance(final float distance) {
		mDistanceText = buildDistanceString(distance);
		findTextSize();
		this.invalidate();
	}
	public void setSpeedText(final float speed) {
		mSpeedText = buildSpeedString(speed);
		invalidate();
	}

	public void startRotateAnimation(final float degrees) {
		if (mTargetDegree==degrees){
			return;
		}
		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {

				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					startAnimation(degrees);
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			mAnimator.cancel();
		} else {
			startAnimation(degrees);
		}
	}

	private void startAnimation(float degrees) {
		if (mRotation>180 && degrees==0){
			degrees =  360;
		}
		if (degrees>180 && mRotation==00){
			mRotation = 360;
		}
		if (degrees<180 && mRotation==360){
			mRotation=0;
		}
		//Log.i("rotating","from: " + mRotation + " to: " +degrees);
		mTargetDegree = degrees;
		mAnimator = ObjectAnimator.ofFloat(this, "orienation", mRotation, degrees);
		mAnimator.setDuration(400);
		mAnimator.start();
	}

	public void setOrienation(float rotation) {
		mRotation = rotation;
		this.invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		final float halfWidth = getWidth() / 2;
		final float halfHeight = getHeight() / 2;
		final float r = Math.min(halfHeight, halfWidth);
		final float h = 60f;
		final float offsetx = (float) (r - Math.sqrt(r * r - h * h));
		mRect.set((int) offsetx, (int) (r - h), (int) (2 * r - offsetx), (int) (r + h));
		findTextSize();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final float halfWidth = getWidth() / 2;
		final float halfHeight = getHeight() / 2;
		final float r = Math.min(halfHeight, halfWidth);
		//Log.i(DEBUG_TAG,"width = " + getWidth() +" height = "+getHeight());
		canvas.save();
		canvas.rotate(mRotation, r, r);
		//canvas.drawRect(mRect, mCirclePaint);
		//canvas.drawCircle(r, r, r, mCirclePaint);
		if (mDistanceText != null) {
			canvas.drawText(mDistanceText, mRect.centerX(), mRect.top, mLabelPaint);
		}
		if (mSpeedText!=null){
			canvas.drawText(mSpeedText, mRect.centerX(), mRect.bottom, mLabelPaint);
		}
		canvas.restore();

	}

}
