package de.renard.radar;

import android.view.Surface;

/**
 * computes correct azimuth independent of device screen rotation
 * source: http://www.littlefluffytoys.com/?p=190
 * @author renard
 *
 */
public class CompassHelper {

	private static class Vector {
		private double x;
		private double y;
		private double z;

		private Vector(double X, double Y, double Z) {
			x = X;
			y = Y;
			z = Z;
		}

		void set(final double X, final double Y, final double Z) {
			x = X;
			y = Y;
			z = Z;
		}

		void normalise() {
			double mag = Math.sqrt(x * x + y * y + z * z);
			if (mag > 0.0) {
				x /= mag;
				y /= mag;
				z /= mag;
			} else {
				x = 1.0;
			}
		}

		static void cross(Vector a, Vector b, Vector out) {
			out.set(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
		}

		static double dot(Vector a, Vector b) {
			return a.x * b.x + a.y * b.y + a.z * b.z;
		}
	}

	private final static Vector mPortraitVector = new Vector(-1.0, 0.0, 0.0);
	private final static Vector mLandscapeLeftVector = new Vector(0.0, 1.0, 0.0);
	private final static Vector mUpsideDownVector = new Vector(1.0, 0.0, 0.0);
	private final static Vector mLandscapeRightVector = new Vector(0.0, -1.0, 0.0);

	private final static Vector mGravityVector = new Vector(0.0, 0.0, 0.0);
	private final static Vector mMagneticVector = new Vector(0.0, 0.0, 0.0);

	private final static Vector mX = new Vector(0.0, 0.0, 0.0);
	private final static Vector mY = new Vector(0.0, 0.0, 0.0);
	private final static Vector mP = new Vector(0.0, 0.0, 0.0);

	// Returns heading of device in radians.
	static double CalculateHeading(float[] geomagnetic, float[] gravity, int orientation) {
		mGravityVector.set(gravity[0], gravity[1], gravity[2]);
		mMagneticVector.set(geomagnetic[0], geomagnetic[1], geomagnetic[2]);
		Vector.cross(mMagneticVector, mGravityVector, mX);
		Vector.cross(mGravityVector, mX, mY);
		mX.normalise();
		mY.normalise();
		mGravityVector.normalise();
		switch (orientation) {
		default:
		case Surface.ROTATION_0: // Portrait
			Vector.cross(mGravityVector, mPortraitVector, mP);
			break;
		case Surface.ROTATION_90: // Landscape left
			Vector.cross(mGravityVector, mLandscapeLeftVector, mP);
			break;
		case Surface.ROTATION_180: // Upside down.
			Vector.cross(mGravityVector, mUpsideDownVector, mP);
			break;
		case Surface.ROTATION_270: // Landscape right
			Vector.cross(mGravityVector, mLandscapeRightVector, mP);
			break;
		}
		double xx = Vector.dot(mP, mX);
		double yy = Vector.dot(mP, mY);
		return -Math.atan2(xx, yy);
	}
}
