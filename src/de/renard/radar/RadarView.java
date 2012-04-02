package de.renard.radar;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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
	private Paint mDestinationTextPaint;
	private Paint mDrawingCachePaint;
	private Location mDestination = null;
	private double mAzimuth;
	private final Rect mDisplayFrame = new Rect();
	private float mDeclination;
	private int mDistanceToDestinationMeters = 0;
	private float mBearingToDestination = 0;
	private Location mMapCenter;
	private final static float TEXT_SIZE = 22f;
	private float mTextSize = 0;
	private Bitmap mDrawingCacheDistance;
	private Rect mTextBounds = new Rect();
	private String mDistanceToDestinationMetersString;

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

		mTextSize = TEXT_SIZE * getResources().getDisplayMetrics().density;
		mDestinationTextPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setTextAlign(Paint.Align.LEFT);
				setTextSize(mTextSize);
				setAntiAlias(true);
			}
		};
		mDrawingCachePaint = new Paint() {
			{
				setAntiAlias(true);
			}
		};
	}
	
//	void foo(final String text) throws IOException{
//		final Paint textPaint = new Paint() {
//			{
//				setColor(Color.WHITE);
//				setTextAlign(Paint.Align.LEFT);
//				setTextSize(20f);
//				setAntiAlias(true);
//			}
//		};
//		final Rect bounds = new Rect();
//		textPaint.getTextBounds(text, 0, text.length(), bounds);
//		
//		final Bitmap bmp = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.RGB_565); //use ARGB_8888 for better quality
//		final Canvas canvas = new Canvas(bmp);
//		canvas.drawText(text, 0, 20f, textPaint);
//		FileOutputStream stream = new FileOutputStream(...); //create your FileOutputStream here
//		bmp.compress(CompressFormat.PNG, 85, stream);
//		bmp.recycle();
//		stream.close();
//	}

	@Override
	protected void onDraw(Canvas canvas) {
		final float halfWidth = getWidth() / 2;
		final float halfHeight = getHeight() / 2;
		final float radius = Math.min(halfHeight, halfWidth);
		final float r = radius * 0.8f;
		canvas.save();
		// draw cirlce
		canvas.translate(getLeft(), getTop());
		canvas.drawCircle(radius, radius, r, mCirclePaint);

		// draw needle
		canvas.translate(radius, radius);
		canvas.rotate((float) Math.toDegrees(mAzimuth) + mDeclination);
		canvas.drawLine(0, 0, 0, r, mCirclePaint);

		// draw destination
		if (null != mDestination && null != mMapCenter) {
			canvas.rotate(mBearingToDestination);
			canvas.drawCircle(0, r, 5, mCirclePaintDestination);
			canvas.scale(-1, -1);
			//canvas.drawBitmap(mDrawingCacheDistance, -mTextBounds.width() / 2, -r - mTextSize, mDrawingCachePaint);
			 canvas.drawText(mDistanceToDestinationMetersString, -mTextBounds.width()/2, -r - mTextBounds.height()/2, mDestinationTextPaint);
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

	public void setDestination(double latitude, double longitude) {
		if (null == mDestination) {
			mDestination = new Location("user");
		}
		mDestination.setLatitude(latitude);
		mDestination.setLongitude(longitude);
		calculateDestinationAndBearing();
	}

	public void setDestination(final int latitude, final int longitude) {
		setDestination(latitude / 1E6, longitude / 1E6);
	}

	private void calculateDestinationAndBearing() {
		if (null != mDestination && null != mMapCenter) {
			mDistanceToDestinationMeters = (int) mMapCenter.distanceTo(mDestination);
			buildDrawCacheForDistance();
			mBearingToDestination = mMapCenter.bearingTo(mDestination);
		}
	}

	private void buildDrawCacheForDistance() {
		mDistanceToDestinationMetersString = mDistanceToDestinationMeters + "m";
//		if (mDrawingCacheDistance != null) {
//			mDrawingCacheDistance.recycle();
//		}
		mDestinationTextPaint.getTextBounds(mDistanceToDestinationMetersString, 0, mDistanceToDestinationMetersString.length(), mTextBounds);
//		mDrawingCacheDistance = Bitmap.createBitmap(mTextBounds.width()+2, mTextBounds.height(), Bitmap.Config.RGB_565);
//		Canvas canvas = new Canvas(mDrawingCacheDistance);
//		canvas.drawText(distance, 0, mTextBounds.height(), mDestinationTextPaint);
	}

	public void setMapCenter(final Location location) {
		mMapCenter = location;
		GeomagneticField geoField = new GeomagneticField(Double.valueOf(location.getLatitude()).floatValue(), Double.valueOf(location.getLongitude()).floatValue(), Double.valueOf(location.getAltitude()).floatValue(), System.currentTimeMillis());
		mDeclination = geoField.getDeclination();
		calculateDestinationAndBearing();
	}

	public Location getMapCenter() {
		return mMapCenter;
	}

	public Location getDestination() {
		return mDestination;
	}

	public void updateDirection(final double direction) {
		mAzimuth = direction;
		this.invalidate();
	}
}
