package de.renard.radar.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * will center its only child and fit it into its center square. Use
 * startRotateAnimation to rotate the child
 * 
 * @author renard
 * 
 */
public class RotateView extends FrameLayout {

	private float mRotation;
	private final Matrix mRotationMatrix = new Matrix();
	private final Matrix mInvertRotationMatrix = new Matrix();
	private ObjectAnimator mAnimator;
	private float mTargetDegree;
	private RectF mChildBounds = new RectF();
	private float[] mMappedTouchPoint = new float[2];

	@SuppressWarnings("unused")
	private final static String DEBUG_TAG = RotateView.class.getSimpleName();

	public RotateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//setWillNotDraw(false);
	}

	public void startRotateAnimation(final float degrees) {
		if (mTargetDegree == degrees) {
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
		if (mRotation > 180 && degrees == 0) {
			degrees = 360;
		}
		if (degrees > 180 && mRotation == 00) {
			mRotation = 360;
		}
		if (degrees < 180 && mRotation == 360) {
			mRotation = 0;
		}
		mTargetDegree = degrees;
		mAnimator = ObjectAnimator.ofFloat(this, "orienation", mRotation, degrees);
		mAnimator.setDuration(400);
		mAnimator.start();
	}

	public void setOrienation(float rotation) {
		mRotationMatrix.reset();
		mRotationMatrix.postRotate(rotation, mChildBounds.centerX(), mChildBounds.centerY());
		mRotationMatrix.invert(mInvertRotationMatrix);
		mRotation = rotation;
		this.invalidate();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		mMappedTouchPoint[0] = ev.getX();
		mMappedTouchPoint[1] = ev.getY();
		mInvertRotationMatrix.mapPoints(mMappedTouchPoint);
		ev.setLocation(mMappedTouchPoint[0], mMappedTouchPoint[1]);
		return super.onInterceptTouchEvent(ev);
	}

//	@Override
//	protected void onDraw(Canvas canvas) {
//		Paint p = new Paint(){{
//			setStyle(Paint.Style.FILL);
//			setColor(Color.RED);
//		}
//		};
//		canvas.drawRect(mChildBounds, p);
//	}

	@Override
	protected boolean drawChild(final Canvas canvas, final View child, final long drawingTime) {
		canvas.save();
		// canvas.rotate(mRotation, mChildBounds.centerX(),
		// mChildBounds.centerY());
		canvas.concat(mRotationMatrix);
		final boolean result = super.drawChild(canvas, child, drawingTime);
		canvas.restore();
		return result;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int w = right - left;
		final int h = bottom - top;
		if (w != 0 && h != 0) {
			// calculate centered maximum size square rect inside view bounds
			final float size = Math.min(w, h);
			final float l = (w - size) / 2;
			final float t = (h - size) / 2;
			mChildBounds.set(l, t, l + size, t + size);
			final View child = getChildAt(0);
			int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) size, MeasureSpec.AT_MOST);
			int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec((int) size, MeasureSpec.AT_MOST);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

			final float childHeight = child.getMeasuredHeight();
			final float childWidth = child.getMeasuredWidth();
			final float childLeft = mChildBounds.centerX() - childWidth / 2;
			final float childTop = mChildBounds.centerY() - childHeight / 2;
			child.layout((int) childLeft, (int) childTop, (int) (childLeft + childWidth), (int) (childTop + childHeight));
		}
	}

	@Override
	public void addView(View child) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("RotateView can host only one direct child");
		}

		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("RotateView can host only one direct child");
		}

		super.addView(child, index);
	}

	@Override
	public void addView(View child, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("RotateView can host only one direct child");
		}

		super.addView(child, params);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (getChildCount() > 0) {
			throw new IllegalStateException("RotateView can host only one direct child");
		}

		super.addView(child, index, params);
	}

}
