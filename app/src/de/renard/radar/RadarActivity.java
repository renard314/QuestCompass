package de.renard.radar;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;
import de.renard.radar.map.LocationPickActivity;

public class RadarActivity extends Activity {

	public static final String CURRENT_LOCATION = "current_location";
	private final static String DEBUG_TAG = RadarActivity.class.getSimpleName();
	private final static int REQUEST_CODE_LOCATION = 0;

	private SensorDataController mLocationDataManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLocationDataManager = new SensorDataController(this);

		ToggleButton toggle = (ToggleButton) findViewById(R.id.button_wake_lock);
		toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Toast message = null;
				if (isChecked) {
					message = Toast.makeText(RadarActivity.this, R.string.display_stays_on, Toast.LENGTH_LONG);
					getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					message = Toast.makeText(RadarActivity.this, R.string.display_will_sleep, Toast.LENGTH_LONG);
					getWindow().clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				message.setGravity(Gravity.BOTTOM, 0, 0);
				message.show();

			}
		});
		Button b = (Button) findViewById(R.id.button_pick_location);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(RadarActivity.this, LocationPickActivity.class);
				Location currentLocation = mLocationDataManager.getCurrentLocation();
				i.putExtra(CURRENT_LOCATION, currentLocation);
				startActivityForResult(i, REQUEST_CODE_LOCATION);
			}
		});
		
		Log.i(DEBUG_TAG, "onCreate()");
	}
	

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mLocationDataManager.saveDestination();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_LOCATION:
				LatLng location = data.getParcelableExtra(LocationPickActivity.EXTRA_LATLNG);
				mLocationDataManager.setDestination(location);
			}
		}
	}
	


	@Override
	protected void onResume() {
		super.onResume();
		mLocationDataManager.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocationDataManager.onPause();
	}
	
	/**
	 * remember target destination
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mLocationDataManager.saveDestination();
	}

}