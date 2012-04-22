package de.renard.radar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * accumulates and averages Magnetic and Acceleration SensorEvents and reports
 * back the device heading.
 * 
 * @author renard
 * 
 */
public class CompassSensorListener implements SensorEventListener {

	/**
	 * receives azimuth values
	 * 
	 * @author renard
	 * 
	 */
	public interface DirectionListener {
		void onDirectionChanged(final double bearing);

		void onRollChanged(final float roll);

		void onPitchChanged(final float pitch);
		
		void onAccuracyChanged(Sensor sensor, int accuracy);
	}

	// number of values which are averaged
	private final int STACK_SIZE = 15;
	// arrays to keep track of the last Sensor Events
	private final float[][] mAccelerationValues = new float[STACK_SIZE][3];
	private final float[][] mMagneticValues = new float[STACK_SIZE][3];
	// current indexes into the arrays
	private int mAccelerationIndex = 0;
	private int mMagneticIndex = 0;
	// holder for temporary values
	private final float[] mMagnetic = new float[3];
	private final float[] mAcceleration = new float[3];
	float[] mMappedValues = new float[3];
	float[] mR = new float[9];
	float[] mMappedRotationMatrix = new float[9];

	private final DirectionListener mListener;

	// private int mScreenOrientation;

	/**
	 * @param Listener
	 *            receives direction updates
	 * @param windowManager
	 *            Needed for device screen rotation
	 */
	public CompassSensorListener(final DirectionListener listener) {
		mListener = listener;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(event.values, 0, mAccelerationValues[mAccelerationIndex++], 0, 3);
			if (mAccelerationIndex >= STACK_SIZE) {
				mAccelerationIndex = 0;
			}
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			System.arraycopy(event.values, 0, mMagneticValues[mMagneticIndex++], 0, 3);
			if (mMagneticIndex >= STACK_SIZE) {
				mMagneticIndex = 0;
			}
			break;
		}

		average(mMagneticValues, mMagnetic);
		average(mAccelerationValues, mAcceleration);
		// final int rotation =
		// mWindowManager.getDefaultDisplay().getOrientation();
		// double azimuth = CompassHelper.CalculateHeading(mMagnetic,
		// mAcceleration, mScreenOrientation);
		float[] values = calculateOrientation();
		// mListener.onDirectionChanged(Math.toDegrees(azimuth));
		mListener.onDirectionChanged(values[0]);
		mListener.onPitchChanged(values[1]);
		mListener.onRollChanged(values[2]);
	}

	private float[] calculateOrientation() {

		if (SensorManager.getRotationMatrix(mR, null, mAcceleration, mMagnetic)) {

			// switch (mScreenOrientation) {
			// // portrait - normal
			// case Surface.ROTATION_0:
			// SensorManager.remapCoordinateSystem(mRotationMatrix,
			// SensorManager.AXIS_X, SensorManager.AXIS_Z,
			// mMappedRotationMatrix);
			// break;
			// // rotated left (landscape - keys to bottom)
			// case Surface.ROTATION_90:
			// SensorManager.remapCoordinateSystem(mRotationMatrix,
			// SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X,
			// mMappedRotationMatrix);
			// break;
			// // upside down
			// case Surface.ROTATION_180:
			// SensorManager.remapCoordinateSystem(mRotationMatrix,
			// SensorManager.AXIS_X, SensorManager.AXIS_Z,
			// mMappedRotationMatrix);
			// break;
			// // rotated right
			// case Surface.ROTATION_270:
			// SensorManager.remapCoordinateSystem(mRotationMatrix,
			// SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X,
			// mMappedRotationMatrix);
			// break;
			//
			// default:
			// break;
			// }

			// SensorManager.remapCoordinateSystem(mRotationMatrix,
			// SensorManager.AXIS_X, SensorManager.AXIS_Z,
			// mMappedRotationMatrix);

			SensorManager.getOrientation(mR, mMappedValues);

			// float degrees0to360 =
			// (float)(Math.toDegrees(mMappedValues[0])+180);
			// degrees0to360 = CompassFilter.filterSensorValue(degrees0to360);

			// Convert from Radians to Degrees.
			mMappedValues[0] = (float) Math.toDegrees(mMappedValues[0]);
			mMappedValues[1] = (float) Math.toDegrees(mMappedValues[1]);
			mMappedValues[2] = (float) Math.toDegrees(mMappedValues[2]);
		}
		return mMappedValues;
	}

	public void setScreenOrientation(final int orientation) {
		// mScreenOrientation = orientation;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		mListener.onAccuracyChanged(sensor, accuracy);
	

	}

	public void average(float[][] values, float[] outAverage) {
		outAverage[0] = 0;
		outAverage[1] = 0;
		outAverage[2] = 0;
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < 3; j++) {
				outAverage[j] += values[i][j];
			}
		}
		outAverage[0] /= values.length;
		outAverage[1] /= values.length;
		outAverage[2] /= values.length;
	}
}
