package de.renard.radar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

public class RadarView extends View {

	private Paint mCirclePaint;
	private Paint mCirclePaintDestination;
	// private GeoPoint mGeoPoint = new GeoPoint(51.2072022, 6.7734369);
	// private GeoPoint mDestinationMock = new GeoPoint(51.20717242813358,
	// 6.772105693817139);
	// private GeoPoint mDestinationMock2 = new GeoPoint(51.207837869924724,
	// 6.778489351272583);
	// private Point mDestinationPoint = new Point();

	private Location mDestination ;
	{
		mDestination = new Location("gps");
		mDestination.setLatitude(51.20717242813358);
		mDestination.setLongitude(6.772105693817139);
	}
	private double mAzimuth;
	// private MercatorProjection mProjection;
	private final Rect mDisplayFrame = new Rect();
	private float mDeclination;
	private float mDistanceToDestinationMeters = 0;
	private float mBearingToDestination = 0;

	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCirclePaint = new Paint() {
			{
				setColor(Color.WHITE);
				setStyle(Paint.Style.STROKE);
				setAntiAlias(true);
				setStrokeWidth(2);
			}
		};

		mCirclePaintDestination = new Paint() {
			{
				setColor(Color.WHITE);
				setStyle(Paint.Style.FILL);
			}
		};
		// mProjection = new MercatorProjection(this);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final float halfWidth = getWidth() / 2;
		final float halfHeight = getHeight() / 2;
		final float radius = Math.min(halfHeight, halfWidth);
		canvas.save();
		// draw cirlce
		canvas.translate(getLeft(), getTop());
		canvas.drawCircle(radius, radius, radius * 0.9f, mCirclePaint);

		// draw needle
		canvas.translate(radius, radius);
		canvas.rotate((float) Math.toDegrees(mAzimuth) + mDeclination);
		canvas.drawLine(0, 0, 0, radius * 0.85f, mCirclePaint);

		// draw destination
		if (null!=mDestination){
			canvas.rotate(mBearingToDestination);
			canvas.drawCircle(0, radius *0.85f+7.5f, 15, mCirclePaintDestination);
		}
		canvas.restore();
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

	public float getBearingToDestination() {
		return mBearingToDestination;
	}

	public float getDistanceToDestination() {
		return mDistanceToDestinationMeters;
	}

	public void setMapCenter(final Location location) {
		if (null != mDestination) {
			mDistanceToDestinationMeters = location.distanceTo(mDestination);
			mBearingToDestination = location.bearingTo(mDestination);
		}

		GeomagneticField geoField = new GeomagneticField(Double.valueOf(location.getLatitude()).floatValue(), Double.valueOf(location.getLongitude()).floatValue(), Double.valueOf(location.getAltitude()).floatValue(), System.currentTimeMillis());

		mDeclination = geoField.getDeclination();
	}

	public void updateDirection(final double direction) {
		// if (Math.abs(direction-mAzimuth)>=0.1){
		mAzimuth = direction;
		this.invalidate();
		// }
	}
}
