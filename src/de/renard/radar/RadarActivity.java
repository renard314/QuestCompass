package de.renard.radar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.renard.radar.CompassSensorListener.DirectionListener;
import de.renard.radar.map.LocationPickActivity;

public class RadarActivity extends Activity implements DirectionListener, LocationListener {

	private final static int REQUEST_CODE_LOCATION = 0;
	private final static int LOCATION_MIN_TIME_MS = 0;
	private final static int LOCATION_MIN_DISTANCE_METERS = 0;

	private SensorManager mSensorManager;
	private Sensor mSensorMagnetic;
	private Sensor mSensorAcceleration;
	private RadarView mRadarView;
	private SensorEventListener mListener;
	private LocationManager mLocationManager;
	private String mLocationProvider;
	@SuppressWarnings("unused")
	private TextView mTextViewSatellites;
	private SharedPreferences mSharedPrefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		setContentView(R.layout.main);
		mRadarView = (RadarView) findViewById(R.id.radarView);
		mTextViewSatellites = (TextView) findViewById(R.id.textView_satellites);
		ToggleButton toggle = (ToggleButton) findViewById(R.id.button_wake_lock);
		toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
				}

			}
		});

		mListener = new CompassSensorListener(this, (WindowManager) getSystemService(WINDOW_SERVICE));
		Button b = (Button) findViewById(R.id.button_pick_location);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(RadarActivity.this, LocationPickActivity.class);
				startActivityForResult(i, REQUEST_CODE_LOCATION);
			}
		});
		initGPS();

		mSharedPrefs = getSharedPreferences(RadarActivity.class.getSimpleName(), MODE_PRIVATE);

		restoreDestionation();

	}

	private void restoreDestionation() {
		if (mSharedPrefs.contains(LocationPickActivity.EXTRA_LATITUDE)) {
			final int latitudeE6 = mSharedPrefs.getInt(LocationPickActivity.EXTRA_LATITUDE, 0);
			final int longitudeE6 = mSharedPrefs.getInt(LocationPickActivity.EXTRA_LONGITUDE, 0);
			mRadarView.setDestination(latitudeE6, longitudeE6);
		}
	}

	private void saveDestination() {
		if (null != mRadarView.getDestination()) {
			final Editor editor = mSharedPrefs.edit();
			final int latitudeE6 = (int) (mRadarView.getDestination().getLatitude() * 1E6);
			final int longitudeE6 = (int) (mRadarView.getDestination().getLongitude() * 1E6);
			editor.putInt(LocationPickActivity.EXTRA_LATITUDE, latitudeE6);
			editor.putInt(LocationPickActivity.EXTRA_LONGITUDE, longitudeE6);
			editor.commit();
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		saveDestination();
	}

	private void initGPS() {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationProvider = mLocationManager.getBestProvider(new Criteria(), false);
		Location location = mLocationManager.getLastKnownLocation(mLocationProvider);
		if (null != location) {
			mRadarView.setMapCenter(location);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_LOCATION:
				final int latE6 = data.getIntExtra(LocationPickActivity.EXTRA_LATITUDE, -1);
				final int longE6 = data.getIntExtra(LocationPickActivity.EXTRA_LONGITUDE, -1);
				mRadarView.setDestination(latE6, longE6);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSensorManager.registerListener(mListener, mSensorAcceleration, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_GAME);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_MIN_TIME_MS, LOCATION_MIN_DISTANCE_METERS, this);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_MIN_TIME_MS, LOCATION_MIN_DISTANCE_METERS, this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mListener);
		mLocationManager.removeUpdates(this);
	}
	
	@Override
	public void onDirectionChanged(double bearing) {
		mRadarView.updateDirection(bearing);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.i("RadarActivity", location.toString());
		mRadarView.setMapCenter(location);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (extras != null) {
			final Object sats = extras.get("satellites");
			if (sats != null) {
				Log.i("RadarActivity", sats.toString());
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveDestination();
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

}