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
		void onScreenRotationChanged(final int degrees);
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
		mListener.onScreenRotationChanged(orientation);

		if (mCurrentOrientation == newOrientation) {
			return;
		}
		mListener.onScreenOrientationChanged(newOrientation);
		mCurrentOrientation = newOrientation;
	}
}
