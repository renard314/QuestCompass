package de.renard.radar;

public class CompassFilter {

	private static final float SmoothFactorCompass = 0.5f;
	private static final float SmoothThresholdCompass = 30.0f;
	static float sOldCompass = 0.0f;

	public static float filterSensorValue(final float newCompass) {
		if (Math.abs(newCompass - sOldCompass) < 180) {
			if (Math.abs(newCompass - sOldCompass) > SmoothThresholdCompass) {
				sOldCompass = newCompass;
			} else {
				sOldCompass = sOldCompass + SmoothFactorCompass * (newCompass - sOldCompass);
			}
		} else {
			if (360.0 - Math.abs(newCompass - sOldCompass) > SmoothThresholdCompass) {
				sOldCompass = newCompass;
			} else {
				if (sOldCompass > newCompass) {
					sOldCompass = (sOldCompass + SmoothFactorCompass * ((360 + newCompass - sOldCompass) % 360) + 360) % 360;
				} else {
					sOldCompass = (sOldCompass - SmoothFactorCompass * ((360 - newCompass + sOldCompass) % 360) + 360) % 360;
				}
			}
		}
		return sOldCompass;
	}

}
