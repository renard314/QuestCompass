package de.renard.radar.map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class LocationOverlay extends Overlay {

	private final Paint mRecticlePaint;
	private final Paint mTextPaint;
	private final float[] mLinePts = new float[8];
	private final Rect mTextBounds = new Rect();

	public LocationOverlay() {
		mRecticlePaint = new Paint() {
			{
				setColor(Color.BLACK);
				setStrokeWidth(2);
				setStyle(Paint.Style.STROKE);
				setAntiAlias(true);
			}
		};
		mTextPaint = new Paint() {
			{
				setColor(Color.BLACK);
				setTextSize(20f);
				setAntiAlias(true);
				setTextAlign(Align.LEFT);
			}
		};
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		super.draw(canvas, mapView, shadow);
		final int centerY = (mapView.getBottom()-mapView.getTop())/2;
		final int centerX = (mapView.getRight()-mapView.getLeft())/2;
		mLinePts[0] = 0;
		mLinePts[1] = centerY;
		mLinePts[2] = mapView.getRight();
		mLinePts[3] = centerY;
		mLinePts[4] = centerX;
		mLinePts[5] = mapView.getTop();
		mLinePts[6] = centerX;
		mLinePts[7] = mapView.getBottom();
		canvas.drawLines(mLinePts, mRecticlePaint);
		float radius = mapView.getWidth()*0.1f;
		radius = Math.min(10f, radius);
		canvas.drawCircle(centerX, centerY, radius, mRecticlePaint);
		final String lat = String.valueOf((float) (mapView.getMapCenter().getLatitudeE6() / 1E6));
		final String lon = String.valueOf((float) (mapView.getMapCenter().getLongitudeE6() / 1E6));
		mTextPaint.getTextBounds("0.0000000", 0, 9, mTextBounds);
		canvas.drawText(lat, centerX-mTextBounds.width()-10, centerY, mTextPaint);
		canvas.drawText(lon, centerX+10, centerY, mTextPaint);
		return true;
	}
}
