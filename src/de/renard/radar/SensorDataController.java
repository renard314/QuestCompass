package de.renard.radar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.TextView;
import de.renard.radar.CompassSensorListener.DirectionListener;
import de.renard.radar.ScreenOrienationEventListener.OnScreenOrientationChangeListener;
import de.renard.radar.map.LocationPickActivity;
import de.renard.radar.views.RadarView;
import de.renard.radar.views.RotateView;

/**
 * receives data from device sensors /gps and updates all views
 */
public class SensorDataController implements OnScreenOrientationChangeListener, DirectionListener, LocationListener {
	private final static String DEBUG_TAG = SensorDataController.class.getSimpleName();			
	
	private SensorManager mSensorManager;
	private Sensor mSensorMagnetic;
	private Sensor mSensorAcceleration;
	private OrientationEventListener mOrientationListener;
	private CompassSensorListener mListener;
	private LocationManager mLocationManager;
	private String mLocationProvider;
	private SharedPreferences mSharedPrefs;
	
	private Location mDestination = null;
	private Location mMapCenter;


	private RadarView mRadarView;
	private RotateView mRotateViewDistance;
	private RotateView mRotateViewButtons;
	private TextView mTextViewDistance;
	private final int mRotationOffset;
	
	private final static int LOCATION_MIN_TIME_MS = 15000;
	private final static int LOCATION_MIN_DISTANCE_METERS = 0;

	public SensorDataController(RadarActivity activity) {
		mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
		mSensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mOrientationListener = new ScreenOrienationEventListener(activity, this);
		mListener = new CompassSensorListener(this);
		mSharedPrefs = activity.getSharedPreferences(RadarActivity.class.getSimpleName(), Context.MODE_PRIVATE);
		mRadarView = (RadarView) activity.findViewById(R.id.radarView);
		mRotateViewDistance = (RotateView) activity.findViewById(R.id.rotateView);
		mRotateViewButtons = (RotateView) activity.findViewById(R.id.rotateView_buttons);
		mTextViewDistance = (TextView) activity.findViewById(R.id.textView_distance);

		mRotationOffset = determineNaturalOrientationOffset(activity);
		restoreDestionation();
		getLastKnownLocation();
	}

	private int determineNaturalOrientationOffset(Context c){
		WindowManager wm = (WindowManager)c.getSystemService(Context.WINDOW_SERVICE);
		int rotation = wm.getDefaultDisplay().getRotation();
		switch(rotation){
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return  90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}
	
	private void getLastKnownLocation() {
		mLocationProvider = mLocationManager.getBestProvider(new Criteria(), false);
		Location location = mLocationManager.getLastKnownLocation(mLocationProvider);
		mMapCenter = location;
		calculateDestinationAndBearing();
	}

	/**
	 * load destination from preferences
	 */
	public void restoreDestionation() {
		if (mSharedPrefs.contains(LocationPickActivity.EXTRA_LATITUDE)) {
			final int latitudeE6 = mSharedPrefs.getInt(LocationPickActivity.EXTRA_LATITUDE, 0);
			final int longitudeE6 = mSharedPrefs.getInt(LocationPickActivity.EXTRA_LONGITUDE, 0);
			setDestination(latitudeE6, longitudeE6);
		}
	}

	/**
	 * save current destination to the preferences
	 */
	public void saveDestination() {
		if (null != mDestination) {
			final Editor editor = mSharedPrefs.edit();
			final int latitudeE6 = (int) (mDestination.getLatitude() * 1E6);
			final int longitudeE6 = (int) (mDestination.getLongitude() * 1E6);
			editor.putInt(LocationPickActivity.EXTRA_LATITUDE, latitudeE6);
			editor.putInt(LocationPickActivity.EXTRA_LONGITUDE, longitudeE6);
			editor.commit();
		}

	
	}
	


	/**
	 * stop sensors
	 */
	public void onPause() {
		mSensorManager.unregisterListener(mListener);
		mLocationManager.removeUpdates(this);
		mOrientationListener.disable();
	}

	/**
	 * start sensors
	 */
	public void onResume() {
		mSensorManager.registerListener(mListener, mSensorAcceleration, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_GAME);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME_MS, LOCATION_MIN_DISTANCE_METERS, this);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_MIN_TIME_MS, LOCATION_MIN_DISTANCE_METERS, this);
		mOrientationListener.enable();

	}
	
	public void setDestination(double latitude, double longitude) {
		if (null == mDestination) {
			mDestination = new Location("user");
		}
		mDestination.setLatitude(latitude);
		mDestination.setLongitude(longitude);
		calculateDestinationAndBearing();
	}

	/**
	 * updates the compass to show bearing to destination
	 */
	public void setDestination(final int latitude, final int longitude) {
		setDestination(latitude / 1E6, longitude / 1E6);
	}

	
	
	/**
	 * Device Orientation Callback
	 */
	@Override
	public void onScreenOrientationChanged(int orientation) {

		int rotation = 0;
		
		switch (orientation) {
		case Surface.ROTATION_0:
			rotation = 0;
			break;
		case Surface.ROTATION_90:
			rotation = 270;
			break;
		case Surface.ROTATION_180:
			rotation = 180;
			break;
		case Surface.ROTATION_270:
			rotation = 90;
			break;
		}
		
		rotation += (360-mRotationOffset);
		rotation = rotation%360;
		Log.i("onScreenOrientationChanged","Rotation: " + rotation);
		mRotateViewDistance.startRotateAnimation(rotation);		
		mRotateViewButtons.startRotateAnimation(rotation);
	}
	
	@Override
	public void onScreenRotationChanged(int degrees) {
		mListener.setScreenOrientation(degrees);		
	}



	/*********************************************
	 * Compass Callbacks
	 *********************************************/

	/**
	 * azimuth in degrees
	 */
	@Override
	public void onDirectionChanged(double bearing) {
		float degrees0to360 = (float)(bearing+mRotationOffset);		
		mRadarView.setAzimuth(degrees0to360);
	}

	/**
	 * 
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		final float frac = 255/4f;
		final int a=0xff;
		final int b=0x00;
		int r = 0;
		int g = 0;
		int index = 0;
		switch (accuracy) {
		case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
			Log.i(DEBUG_TAG,"SENSOR_STATUS_ACCURACY_HIGH");
			index = 3;
			break;
		case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
			Log.i(DEBUG_TAG,"SENSOR_STATUS_ACCURACY_MEDIUM");
			index = 2;
			break;
		case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
			Log.i(DEBUG_TAG,"SENSOR_STATUS_ACCURACY_LOW");
			index = 1;
			break;
		case SensorManager.SENSOR_STATUS_UNRELIABLE:
			Log.i(DEBUG_TAG,"SENSOR_STATUS_UNRELIABLE");
			index = 0;
			break;
		}
		r = (int) ((3-index)*frac);
		g = (int) (index*frac);
		mRadarView.setLight(Color.argb(a, r, g, b), 1);

	}
	/**
	 * roll in degrees
	 */
	@Override
	public void onRollChanged(float roll) {
	}

	/**
	 * pitch in degrees
	 */
	@Override
	public void onPitchChanged(float pitch) {

	}
	
	private void calculateDestinationAndBearing(){
		if (null != mDestination && null != mMapCenter) {
			final int distance = (int) mMapCenter.distanceTo(mDestination);
			mRadarView.setBearing(mMapCenter.bearingTo(mDestination));
			mTextViewDistance.setText(Util.buildDistanceString(distance));
		}
	}

	/*********************************************
	 * GPS Callbacks
	 *********************************************/

	
	@Override
	public void onLocationChanged(Location location) {
		Log.i("RadarActivity", location.toString());
		GeomagneticField geoField = new GeomagneticField(Double.valueOf(location.getLatitude()).floatValue(), Double.valueOf(location.getLongitude()).floatValue(), Double.valueOf(location.getAltitude()).floatValue(), System.currentTimeMillis());
		mRadarView.setDelination(geoField.getDeclination());
		mMapCenter = location;
		calculateDestinationAndBearing();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

}
