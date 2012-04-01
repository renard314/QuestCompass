package de.renard.radar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	private TextView mTextViewDistance;
	private TextView mTextViewBearing;
	private TextView mTextViewSatellites;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		setContentView(R.layout.main);
		mRadarView = (RadarView) findViewById(R.id.radarView);
		mTextViewDistance = (TextView) findViewById(R.id.textView_distance);
		mTextViewBearing = (TextView) findViewById(R.id.textView_bearing);
		mTextViewSatellites = (TextView) findViewById(R.id.textView_satellites);
		ToggleButton toggle = (ToggleButton) findViewById(R.id.button_wake_lock);
		toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
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
	}

	private void initGPS() {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationProvider = mLocationManager.getBestProvider(new Criteria(), false);
		Location location = mLocationManager.getLastKnownLocation(mLocationProvider);
		mRadarView.setMapCenter(location);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_LOCATION:
				// TODO add location to compass view
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
		Log.i("RadarActivity",location.toString());
		mRadarView.setMapCenter(location);
		
		final float meters = mRadarView.getDistanceToDestination();
		final float bearing = mRadarView.getBearingToDestination();
		mTextViewDistance.setText(String.valueOf(meters));
		mTextViewBearing.setText(String.valueOf(bearing));

//		float[] val = new float[2];
//		Location.distanceBetween(location.getLatitude(), location.getLongitude(), dest.getLatitude(), dest.getLongitude(), val);
//		
//		final float distance = val[0];
//		final float bearing = val[1];
//		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (extras!=null){
			final Object sats = extras.get("satellites");
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

}