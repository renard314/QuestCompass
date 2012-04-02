package de.renard.radar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.view.WindowManager;

public class CompassSensorListener implements SensorEventListener {

	public interface DirectionListener {
		void onDirectionChanged(final double bearing);
	}

	private final int STACK_SIZE = 10;
	private final float[][] mAccelerationValues = new float[STACK_SIZE][3];
	private final float[][] mMagneticValues = new float[STACK_SIZE][3];
	private final float[] mMagnetic = new float[3];
	private final float[] mAcceleration = new float[3];
	private int mAccelerationIndex = 0;
	private int mMagneticIndex = 0;
	private final DirectionListener mListener;
	private final WindowManager mWindowManager;

	public CompassSensorListener(final DirectionListener listener, WindowManager windowManager) {
		mWindowManager = windowManager;
		mListener = listener;
	}

	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(event.values, 0, mAccelerationValues[mAccelerationIndex++], 0, 3);
			// System.arraycopy(event.values, 0, mAcceleration, 0, 3);
			if (mAccelerationIndex >= STACK_SIZE) {
				mAccelerationIndex = 0;
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			System.arraycopy(event.values, 0, mMagneticValues[mMagneticIndex++], 0, 3);
			// System.arraycopy(event.values, 0, mMagnetic, 0, 3);
			if (mMagneticIndex >= STACK_SIZE) {
				mMagneticIndex = 0;
			}
			break;
		}

		average(mMagneticValues, mMagnetic);
		average(mAccelerationValues, mAcceleration);
		final int rotation = mWindowManager.getDefaultDisplay().getOrientation();
		double azimuth = CompassHelper.CalculateHeading(mMagnetic, mAcceleration, rotation);
		mListener.onDirectionChanged(azimuth);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void average(float[][] values, float[] average) {
		average[0] = 0;
		average[1] = 0;
		average[2] = 0;
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < 3; j++) {
				average[j] += values[i][j];
			}
		}
		average[0] /= values.length;
		average[1] /= values.length;
		average[2] /= values.length;
	}
}
