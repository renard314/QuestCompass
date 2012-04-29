package de.renard.radar;

public class Util {
	
	public static String buildDistanceString(final float distanceMeters) {
		float distance = distanceMeters;
		String format;
		String unit;
		if (distanceMeters >= 1000) {
			distance /= 1000;
			unit = "km";
			format = "%,.2f%s";
		} else {
			unit = "m";
			format = "%,.0f%s";
		}
		return String.format(format, distance, unit);
	}

	public static String buildSpeedString(final float speedMPerSecond) {
		float speed = speedMPerSecond * 3.6f;
		String format = "%.1f\n%s";
		String unit = "km/s";
		return String.format(format, speed, unit);
	}

}
