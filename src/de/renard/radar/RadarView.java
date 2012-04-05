package de.renard.radar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

public class RadarView extends View {

	// text size for compass Labels (N,E,W,S)
	private final static float LABEL_TEXT_SIZE = 28f;
	// text size for compass directions
	private final static float DIRECTION_TEXT_SIZE = 24f;
	// text size for angles
	private final static float ANGLE_TEXT_SIZE = 18f;
	// length of marker lines in pixels
	private static final int sMarkerLength = 20;
	// labels for compass directions
	private final static String sNorthString = "N";
	private final static String sSouthString = "S";
	private final static String sEastString = "E";
	private final static String sWestString = "W";

	private Paint mCirclePaint;
	private Paint mCirclePaintDestination;
	private Paint mDestinationTextPaint;
	private Paint mDrawingCachePaint;
	private Paint mMarkerPaint;
	private Paint mDirectionTextPaint;
	private Paint mLabelTextPaint;
	private Paint mAngleTextPaint;

	private Location mDestination = null;
	private double mAzimuth;
	private final Rect mDisplayFrame = new Rect();
	private float mDeclination;
	private int mDistanceToDestinationMeters = 0;
	private float mBearingToDestination = 0;
	private Location mMapCenter;
	private float mDirectionTextSize = 0;
	private float mAngleTextSize = 0;
	private float mLabelTextSize = 0;
	private Bitmap mDrawingCacheDistance;
	private Bitmap mDrawingCacheCompass;
	private final Rect mTextBounds = new Rect();
	private String mDistanceToDestinationMetersString;
	private float mCompassRadius = 0;

	private final int sDirectionTextHeight;
	private final int sAngleTextHeight;
	private final int sLabelTextHeight;

	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMarkerPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setAntiAlias(true);
				setStyle(Paint.Style.STROKE);
				setStrokeWidth(3);
				setMaskFilter(new BlurMaskFilter(2, Blur.SOLID));
			}
		};

		mCirclePaint = new Paint() {
			{
				setColor(Color.WHITE);
				setStyle(Paint.Style.STROKE);
				setAntiAlias(true);
				setStrokeWidth(6);
				setMaskFilter(new BlurMaskFilter(3, Blur.SOLID));

			}
		};

		mCirclePaintDestination = new Paint() {
			{
				setColor(Color.WHITE);
				setStyle(Paint.Style.FILL);
			}
		};

		mDirectionTextSize = DIRECTION_TEXT_SIZE * getResources().getDisplayMetrics().density;
		mDestinationTextPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setTextAlign(Paint.Align.LEFT);
				setTextSize(mDirectionTextSize);
				setAntiAlias(true);
			}
		};
		mLabelTextSize = LABEL_TEXT_SIZE * getResources().getDisplayMetrics().density;
		mLabelTextPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setTextAlign(Paint.Align.LEFT);
				setTextSize(mLabelTextSize);
				setTypeface(Typeface.DEFAULT_BOLD);
				setAntiAlias(true);
			}
		};
		mDirectionTextPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setTextAlign(Paint.Align.LEFT);
				setTextSize(mDirectionTextSize);
				setAntiAlias(true);
			}
		};
		mAngleTextSize = ANGLE_TEXT_SIZE * getResources().getDisplayMetrics().density;

		mAngleTextPaint = new Paint() {
			{
				setColor(Color.WHITE);
				setTextAlign(Paint.Align.LEFT);
				setTextSize(mAngleTextSize);
				setAntiAlias(true);
			}
		};
		mDrawingCachePaint = new Paint() {
			{
				setAntiAlias(true);
				setFilterBitmap(true);
			}
		};
		mDirectionTextPaint.getTextBounds(sNorthString, 0, sNorthString.length(), mTextBounds);
		sDirectionTextHeight = mTextBounds.height();
		mLabelTextPaint.getTextBounds("1", 0, 1, mTextBounds);
		sLabelTextHeight = mTextBounds.height();
		mAngleTextPaint.getTextBounds("1", 0, 1, mTextBounds);
		sAngleTextHeight = mTextBounds.height();
	}

	/**
	 * Draw the marker every 15 degrees and text every 30.
	 * 
	 * @param canvas
	 */
	private void drawMarkers(final Canvas canvas, final float radius) {
		canvas.save();
		for (int i = 0; i < 24; i++) {
			// Draw a marker.
			canvas.drawLine(0, -radius, 0, -radius + sMarkerLength, mMarkerPaint);

			// Draw the cardinal points
			if (i % 6 == 0) {
				String dirString = "";
				switch (i) {
				case (0): {
					dirString = sNorthString;
					// int arrowY = 2*textHeight;
					// canvas.drawLine(px, arrowY, px-5, 3*textHeight,
					// markerPaint);
					// canvas.drawLine(px, arrowY, px+5, 3*textHeight,
					// markerPaint);
					break;
				}
				case (6):
					dirString = sEastString;
					break;
				case (12):
					dirString = sSouthString;
					break;
				case (18):
					dirString = sWestString;
					break;
				}
				final float xoffset = mLabelTextPaint.measureText(dirString) / 2;
				canvas.drawText(dirString, -xoffset, -radius + sLabelTextHeight + sMarkerLength * 1.2f, mLabelTextPaint);
			}

			else if (i % 2 == 0) {
				// Draw the text every alternate 45deg
				final String angle = String.valueOf(i * 15);
				final float xoffset = mAngleTextPaint.measureText(angle) / 2;
				canvas.drawText(angle, -xoffset, -radius + sAngleTextHeight + sMarkerLength * 1.2f, mAngleTextPaint);
			}
			canvas.rotate(15);
		}
		canvas.restore();

	}

	private void buildCompassBitmap() {
		mDrawingCacheCompass = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		final float halfWidth = getWidth() / 2;
		final float halfHeight = getHeight() / 2;
		final float radius = Math.min(halfHeight, halfWidth);
		final float r = radius * 0.8f;
		Canvas canvas = new Canvas(mDrawingCacheCompass);
		// draw cirlce
		canvas.translate(radius, radius);
		canvas.drawCircle(0, 0, r, mCirclePaint);
		// draw needle
		// canvas.drawLine(0, 0, 0, -r, mCirclePaint);
		drawMarkers(canvas, r);
		mCompassRadius = r;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == mDrawingCacheCompass) {
			buildCompassBitmap();
		}
		final float halfWidth = getWidth() / 2;
		final float halfHeight = getHeight() / 2;
		final float radius = Math.min(halfHeight, halfWidth);
		final double rotateAngle = -(mAzimuth + mDeclination);

		canvas.rotate((float) rotateAngle, radius, radius);
		canvas.drawBitmap(mDrawingCacheCompass, 0, 0, mDrawingCachePaint);

		// draw destination marker
		if (null != mDestination && null != mMapCenter) {
			canvas.translate(radius, radius);
			canvas.rotate(mBearingToDestination+180);
			canvas.rotate((float) -rotateAngle - mBearingToDestination-180 , 0, mCompassRadius);
			canvas.drawCircle(0, mCompassRadius, 5, mCirclePaintDestination);
			canvas.drawBitmap(mDrawingCacheDistance, -mTextBounds.width() / 2, mCompassRadius - sDirectionTextHeight, mDrawingCachePaint);
		}
		canvas.restore();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed && null != mDrawingCacheCompass) {
			mDrawingCacheCompass.recycle();
			mDrawingCacheCompass = null;
		}
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

	private String buildDistanceString() {
		float distance = mDistanceToDestinationMeters;
		String format;
		String unit;
		if (mDistanceToDestinationMeters >= 1000) {
			distance /= 1000;
			unit = "km";
			format = "%,.2f%s";
		} else {
			unit = "m";
			format = "%,.0f%s";
		}
		return String.format(format, distance, unit);
	}

	private void buildDrawCacheForDistance() {
		final String newDestinationString = buildDistanceString();
		if (mDrawingCacheDistance != null && newDestinationString.equals(mDistanceToDestinationMetersString)) {
			return;
		}
		mDistanceToDestinationMetersString = newDestinationString;
		mDestinationTextPaint.getTextBounds(mDistanceToDestinationMetersString, 0, mDistanceToDestinationMetersString.length(), mTextBounds);
		if (null == mDrawingCacheDistance) {
			mDrawingCacheDistance = Bitmap.createBitmap(mTextBounds.width() + 2, mTextBounds.height(), Bitmap.Config.ARGB_8888);
		} else if (mDrawingCacheDistance.getWidth() != mTextBounds.width() + 2 || mDrawingCacheDistance.getHeight() != mTextBounds.height()) {
			mDrawingCacheDistance.recycle();
			mDrawingCacheDistance = Bitmap.createBitmap(mTextBounds.width() + 2, mTextBounds.height(), Bitmap.Config.ARGB_8888);
		}
		Canvas canvas = new Canvas(mDrawingCacheDistance);
		canvas.drawColor(0, android.graphics.PorterDuff.Mode.DST_IN);
		canvas.drawText(mDistanceToDestinationMetersString, 0, mTextBounds.height(), mDestinationTextPaint);
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
