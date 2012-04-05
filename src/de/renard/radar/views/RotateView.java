package de.renard.radar.views;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class RotateView extends View {
	
	private final Rect mDisplayFrame = new Rect();
	private final Paint mCirclePaint;
	private float mRotation;

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
		final int size = Math.min(height, width);
		setMeasuredDimension(size, size);
	}
	
	public void setRotation(float rotation){
		mRotation = rotation;
		this.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		final float halfWidth = getWidth() / 2;
		final float halfHeight = getHeight() / 2;
		final float r = Math.min(halfHeight, halfWidth);
		final float h = 60f;
		final float offsetx = (float) (r - Math.sqrt(r*r - h*h));
		final RectF rect = new RectF(offsetx, (r-h),(2*r-offsetx), (r+h));
		canvas.save();
		canvas.rotate(-mRotation, r, r);
		canvas.drawRect(rect, mCirclePaint);
		canvas.drawCircle(r, r, r, mCirclePaint);
		canvas.restore();

	}

}
