package de.renard.radar;

import android.content.Context;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;
import android.view.Surface;

/**
 * listens to device screen orienation changes reports back the degrees a view
 * must be rotated to be horizontal.
 * I use this class to keep the labels always in a horizontal position.
 * 
 * @author renard
 * 
 */
public class ScreenOrienationEventListener extends OrientationEventListener {

	private int mCurrentOrientation = -1;

	public interface OnScreenOrientationChangeListener {
		void onScreenOrientationChanged(final int orientation);
	};

	private final OnScreenOrientationChangeListener mListener;

	public ScreenOrienationEventListener(Context context, OnScreenOrientationChangeListener listener) {
		super(context, SensorManager.SENSOR_DELAY_NORMAL);
		mListener = listener;
	}

	private int getOrientationFromDegrees(final int orientation) {
		if (orientation <= 45) {
			return Surface.ROTATION_0;
		} else if (orientation <= 135) {
			return Surface.ROTATION_90;
		} else if (orientation <= 225) {
			return Surface.ROTATION_180;
		} else if (orientation <= 315) {
			return Surface.ROTATION_270;
		}
		return Surface.ROTATION_0;
	}

	@Override
	public void onOrientationChanged(int orientation) {
		if (orientation == ORIENTATION_UNKNOWN) {
			return;
		}
		final int newOrientation = getOrientationFromDegrees(orientation);

		if (mCurrentOrientation == newOrientation) {
			return;
		}

		switch (newOrientation) {
		case Surface.ROTATION_0:
			mListener.onScreenOrientationChanged(0);
			break;
		case Surface.ROTATION_90:
			mListener.onScreenOrientationChanged(-90);
			break;
		case Surface.ROTATION_180:
			mListener.onScreenOrientationChanged(180);
			break;
		case Surface.ROTATION_270:
			mListener.onScreenOrientationChanged(90);
			break;
		}
		mCurrentOrientation = newOrientation;
	}
}
