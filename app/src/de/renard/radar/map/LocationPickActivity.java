package de.renard.radar.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import de.renard.radar.R;
import de.renard.radar.RadarActivity;

public class LocationPickActivity extends FragmentActivity {
	public static final String EXTRA_LATLNG = "latlong";

	private GoogleMap mMap;

	private LocationOverlay overlay;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.location_pick_activity);
		setUpMapIfNeeded();
		overlay = (LocationOverlay) findViewById(R.id.location_overlay);

		Button okButton = (Button) findViewById(R.id.button_pick_location);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CameraPosition cameraPosition = mMap.getCameraPosition();

				Intent i = new Intent();
				i.putExtra(EXTRA_LATLNG, cameraPosition.target);
				setResult(RESULT_OK, i);
				finish();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMap() {
		mMap.setOnCameraChangeListener(new OnCameraChangeListener() {

			@Override
			public void onCameraChange(CameraPosition position) {
				overlay.updateCameraPostion(position);
			}
		});

		mMap.setMyLocationEnabled(true);
		if (getIntent() != null) {
			Location location = getIntent().getParcelableExtra(RadarActivity.CURRENT_LOCATION);
			if (location!=null){
				LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));
			}

		}

	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

}
