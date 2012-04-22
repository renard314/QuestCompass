package de.renard.radar.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class RadarView extends View {

	// text size for compass Labels (N,E,W,S)
	private final static float LABEL_TEXT_SIZE = 28f;
	// text size for angles
	private final static float ANGLE_TEXT_SIZE = 18f;
	// labels for compass directions
	private final static String sNorthString = "N";
	private final static String sSouthString = "S";
	private final static String sEastString = "E";
	private final static String sWestString = "W";

	private final RectF mBounds = new RectF();
	private final RectF mInnerBounds = new RectF();
	private final RectF mGlowBounds = new RectF();
	// pre allocated arrays
	final int[] glowGradientColors = new int[3];
	final float[] glowGradientPositions = new float[3];
	final float[] hsv = new float[3];
	int mLightColor = Color.BLACK;
	float mLightIntensity = 0;

	private final Paint mRimPaint; // metallic outer background
	private final Paint mBackgroundPaint; // metallic inner background
	private Paint mCirclePaint;
	private Paint mDestinationPaint;
	private Paint mDrawingCachePaint;
	private Paint mMarkerPaint;
	private Paint mLabelTextPaint;
	private Paint mAngleTextPaint;
	private final Paint mGlowPaint; // paint for light source

	private double mAzimuth;
	private final Rect mDisplayFrame = new Rect();
	private float mDeclination;
	private float mBearingToDestination = 0;
	private float mAngleTextSize = 0;
	private float mLabelTextSize = 0;
	private Bitmap mDrawingCacheCompass;
	private final Rect mTextBounds = new Rect();
	private final int sAngleTextHeight;
	private final int sLabelTextHeight;
	private boolean mHasDestination = false;

	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mRimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setStyle(Paint.Style.STROKE);
		mCirclePaint.setColor(Color.argb(0x4f, 0x43, 0x46, 0x43));
		mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGlowPaint.setStyle(Paint.Style.FILL);

		mMarkerPaint = new Paint() {
			{
				setStyle(Paint.Style.FILL);
				setColor(0xff050604);
				setAntiAlias(true);
			}
		};

		mDestinationPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {
			{
				setColor(0xffe34929);
				setStyle(Paint.Style.FILL_AND_STROKE);
				setMaskFilter(new EmbossMaskFilter(new float[] { 1, 1, 1 }, 0.4f, 10, 8.2f));
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
		mLabelTextPaint.getTextBounds("1", 0, 1, mTextBounds);
		sLabelTextHeight = mTextBounds.height();
		mAngleTextPaint.getTextBounds("1", 0, 1, mTextBounds);
		sAngleTextHeight = mTextBounds.height();
		initGlowColors();
	}

	/**
	 * the glow is made up of a radial gradient dependant on the current light
	 * intensity
	 */
	private void initGlowColors() {
		// outer edges fade out
		final int i1 = (int) Math.min(255, 0 * mLightIntensity);
		final int i2 = (int) Math.min(255, 100 * mLightIntensity);
		final int i3 = (int) Math.min(255, 255 * mLightIntensity);
		// convert to hsv to change intensity
		Color.colorToHSV(mLightColor, hsv);
		hsv[2] = mLightIntensity;
		// convert back to argb
		final int color = Color.HSVToColor(hsv);
		final int r = Color.red(color);
		final int g = Color.green(color);
		final int b = Color.blue(color);
		glowGradientColors[2] = Color.argb(i1, r, g, b);
		glowGradientColors[1] = Color.argb(i2, r, g, b);
		hsv[1] -= hsv[1] * 0.45f * mLightIntensity; // make the center of the
													// light a litte bit white
		glowGradientColors[0] = Color.HSVToColor(i3, hsv);

		glowGradientPositions[2] = 1f;
		glowGradientPositions[1] = 0.70f;
		glowGradientPositions[0] = 0.33f;

	}

	private float getRimSize() {
		return (mBounds.width() - mInnerBounds.width()) / 2f;
	}

	/**
	 * Draw the marker every 15 degrees and text every 30.
	 * 
	 * @param canvas
	 */
	private void drawMarkers(final Canvas canvas, final float radius) {
		final float rimSize = getRimSize();
		RadialGradient glowShader = new RadialGradient(rimSize / 2, rimSize / 2, rimSize / 2, glowGradientColors, glowGradientPositions, TileMode.CLAMP);
		mMarkerPaint.setShader(glowShader);
		mGlowBounds.set(0, 0, rimSize, rimSize);

		canvas.save();
		final float inset = mGlowBounds.width() * 0.15f;
		canvas.translate(mBounds.centerX(), mBounds.centerY());

		for (int i = 0; i < 24; i++) {
			canvas.save();
			canvas.translate(0, -radius);
			// Draw the cardinal points
			if (i % 6 == 0) {
				canvas.save();
				canvas.translate(-rimSize / 2, 0);
				canvas.scale(0.8f, 0.8f, rimSize / 2, rimSize / 2);
				mGlowBounds.inset(inset, inset);
				canvas.drawOval(mGlowBounds, mBackgroundPaint); // inner
																// background
				canvas.drawOval(mGlowBounds, mMarkerPaint);
				mGlowBounds.inset(-inset, -inset);
				canvas.restore();

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
				canvas.drawText(dirString, -xoffset, sLabelTextHeight + rimSize * 1.2f, mLabelTextPaint);
			} else {
				if (i % 2 == 0) {
					canvas.save();
					canvas.translate(-rimSize / 2, 0);
					canvas.scale(0.6f, 0.6f, rimSize / 2, rimSize / 2);
					mGlowBounds.inset(inset, inset);
					canvas.drawOval(mGlowBounds, mBackgroundPaint); // inner
																	// background
					canvas.drawOval(mGlowBounds, mMarkerPaint);
					mGlowBounds.inset(-inset, -inset);
					canvas.restore();

					// canvas.drawCircle(0, rimSize / 2, rimSize * 0.25f,
					// mMarkerPaint);
					// Draw the text every alternate 30deg
					final String angle = String.valueOf(i * 15);
					final float xoffset = mAngleTextPaint.measureText(angle) / 2;
					canvas.drawText(angle, -xoffset, sAngleTextHeight + rimSize * 1.2f, mAngleTextPaint);
				}
			}
			canvas.restore();
			canvas.rotate(15);
		}
		canvas.restore();

	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		boolean changed = false;
		changed |= w != oldw;
		changed |= h != oldh;
		changed &= w > 0;
		changed &= h > 0;
		if (changed) {
			// calculate centered maximum size square rect inside view bounds
			final float size = Math.min(w, h);
			final float l = (w - size) / 2;
			final float t = (h - size) / 2;
			final float o = 0.07f * size;

			mBounds.set(l, t, l + size, t + size);
			mInnerBounds.set(mBounds.left + o, mBounds.top + o, mBounds.right - o, mBounds.bottom - o);
			// outer ring is a lighter metall
			mBackgroundPaint.setShader(new LinearGradient(mBounds.left, 0.0f + mBounds.top, mBounds.right, mBounds.bottom, Color.rgb(0x35, 0x35, 0x35), Color.rgb(0x05, 0x05, 0x10), Shader.TileMode.CLAMP));
			mRimPaint.setShader(new LinearGradient(mBounds.left, 0.0f + mBounds.top, mBounds.left, mBounds.bottom, Color.rgb(0xa0, 0xa0, 0xa0), Color.rgb(0x45, 0x45, 0x45), Shader.TileMode.CLAMP));
			mCirclePaint.setStrokeWidth(0.005f * mBounds.width());
			// mDestinationPaint.setShader(new LinearGradient(0, 0.0f,
			// mBounds.width() - mInnerBounds.width(), mBounds.width() -
			// mInnerBounds.width(), Color.rgb(0xa5, 0x35, 0x35),
			// Color.rgb(0x85, 0x15, 0x15), Shader.TileMode.CLAMP));
		}
	}

	private void buildCompassBitmap() {
		mDrawingCacheCompass = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		final float radius = mBounds.width() / 2;
		final float r1 = radius * 0.66f;
		final float r2 = radius * 0.33f;
		Canvas canvas = new Canvas(mDrawingCacheCompass);
		canvas.drawOval(mBounds, mRimPaint); // border
		canvas.drawOval(mBounds, mCirclePaint); // border edge
		canvas.drawOval(mInnerBounds, mBackgroundPaint); // inner background
		canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), r1, mCirclePaint); // first
																					// inner
																					// circle
		canvas.drawCircle(mBounds.centerX(), mBounds.centerY(), r2, mCirclePaint); // second
																					// inner
																					// cirlce
		canvas.drawLine(mInnerBounds.left, mInnerBounds.centerY(), mInnerBounds.right, mInnerBounds.centerY(), mCirclePaint); // vertical
																																// line
		canvas.drawLine(mInnerBounds.centerX(), mInnerBounds.top, mInnerBounds.centerX(), mInnerBounds.bottom, mCirclePaint); // horizontal
																																// line
		drawMarkers(canvas, radius);
	}

	public void setLight(final int color, final float intensity) {
		mLightColor = color;
		mLightIntensity = intensity;
		initGlowColors();
		if (null != mDrawingCacheCompass) {
			mDrawingCacheCompass.recycle();
			mDrawingCacheCompass = null;
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == mDrawingCacheCompass) {
			buildCompassBitmap();
		}
		final double rotateAngle = -(mAzimuth + mDeclination);

		canvas.rotate((float) rotateAngle, mBounds.centerX(), mBounds.centerY());
		canvas.drawBitmap(mDrawingCacheCompass, 0, 0, mDrawingCachePaint);

		// draw destination marker
		if (true == mHasDestination) {
			final float circleRadius = getRimSize()/2;
			final float radius = mInnerBounds.width() / 2 ;
			canvas.translate(mBounds.centerX(), mBounds.centerY());
			canvas.rotate(mBearingToDestination);
			canvas.drawCircle(0, -radius*0.66f, circleRadius, mDestinationPaint);
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

	public void setBearing(final float bearing) {
		mHasDestination = true;
		mBearingToDestination = bearing;
	}

	public void setDelination(final float declination) {
		mDeclination = declination;
	}

	public void setAzimuth(final double azimuth) {
		mAzimuth = azimuth;
		this.invalidate();
	}
}
