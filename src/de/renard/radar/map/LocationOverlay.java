package de.renard.radar.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.maps.model.CameraPosition;

public class LocationOverlay extends View {

	private final float[] mLinePts = new float[8];
	private final Rect mTextBounds = new Rect();
	private final Paint mRecticlePaint = new Paint() {
		{
			setColor(Color.BLACK);
			setStrokeWidth(2);
			setStyle(Paint.Style.STROKE);
			setAntiAlias(true);
		}
	};
	private final Paint mTextPaint = new Paint() {
		{
			setColor(Color.BLACK);
			setTextSize(20f);
			setAntiAlias(true);
			setTextAlign(Align.LEFT);
		}
	};
	private CameraPosition position;

	public LocationOverlay(Context context) {
		super(context);
	}

	public LocationOverlay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (position != null) {
			final int centerY = (getBottom() - getTop()) / 2;
			final int centerX = (getRight() - getLeft()) / 2;
			mLinePts[0] = 0;
			mLinePts[1] = centerY;
			mLinePts[2] = getRight();
			mLinePts[3] = centerY;
			mLinePts[4] = centerX;
			mLinePts[5] = getTop();
			mLinePts[6] = centerX;
			mLinePts[7] = getBottom();
			canvas.drawLines(mLinePts, mRecticlePaint);
			float radius = getWidth() * 0.1f;
			radius = Math.min(10f, radius);
			canvas.drawCircle(centerX, centerY, radius, mRecticlePaint);
			final String lat = String.format("%.6f", position.target.latitude);
			final String lon = String.format("%.6f", position.target.longitude);
			mTextPaint.getTextBounds("0.0000000", 0, 9, mTextBounds);
			canvas.drawText(lat, centerX - mTextBounds.width() - 10, centerY,
					mTextPaint);
			canvas.drawText(lon, centerX + 10, centerY, mTextPaint);
		}
	}

	public void updateCameraPostion(CameraPosition position) {
		this.position = position;
		this.invalidate();
	}
}
