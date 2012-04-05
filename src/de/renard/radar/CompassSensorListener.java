package de.renard.radar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.WindowManager;

/**
 * accumulates and averages Magnetic and Acceleration SensorEvents and reports back the device heading.
 * @author renard
 *
 */
public class CompassSensorListener implements SensorEventListener {

	/**
	 * receives azimuth values
	 * @author renard
	 *
	 */
	public interface DirectionListener {
		void onDirectionChanged(final double bearing);
		void onRollChanged(final float roll);
		void onPitchChanged(final float pitch);
	}
		
	//number of values which are averaged
	private final int STACK_SIZE = 10;
	//arrays to keep track of the last Sensor Events
	private final float[][] mAccelerationValues = new float[STACK_SIZE][3];
	private final float[][] mMagneticValues = new float[STACK_SIZE][3];
	//current indexes into the arrays
	private int mAccelerationIndex = 0;
	private int mMagneticIndex = 0;
	//holder for temporary values
	private final float[] mMagnetic = new float[3];
	private final float[] mAcceleration = new float[3];
    float[] mMappedValues = new float[3];
    float[] mRotationMatrix = new float[9];
    float[] mMappedRotationMatrix = new float[9];
    
    private final DirectionListener mListener;


	/**
	 * @param Listener receives direction updates
	 * @param windowManager Needed for device screen rotation
	 */
	public CompassSensorListener(final DirectionListener listener, WindowManager windowManager) {
		//mWindowManager = windowManager;
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
		//final int rotation = mWindowManager.getDefaultDisplay().getOrientation();
		//double azimuth = CompassHelper.CalculateHeading(mMagnetic, mAcceleration, rotation);
		float[] values = calculateOrientation();
		mListener.onDirectionChanged(values[0]);
		mListener.onPitchChanged(values[1]);
		mListener.onRollChanged(values[2]);
	}

    private float[] calculateOrientation() {


        SensorManager.getRotationMatrix(mRotationMatrix, null, mAcceleration, mMagnetic);
        SensorManager.remapCoordinateSystem(mRotationMatrix, 
                                            SensorManager.AXIS_X, 
                                            SensorManager.AXIS_Z, 
                                            mMappedRotationMatrix);

        SensorManager.getOrientation(mMappedRotationMatrix, mMappedValues);

        // Convert from Radians to Degrees.
        mMappedValues[0] = (float) Math.toDegrees(mMappedValues[0]);
        mMappedValues[1] = (float) Math.toDegrees(mMappedValues[1]);
        mMappedValues[2] = (float) Math.toDegrees(mMappedValues[2]);

        return mMappedValues;
      }
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
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
