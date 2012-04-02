package de.renard.radar.map;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import de.renard.radar.R;

public class LocationPickActivity extends MapActivity {
	public static final String EXTRA_LATITUDE = "lat";
	public static final String EXTRA_LONGITUDE = "long";

	private MapView mMapView;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.location_pick_activity);
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true);

		List<Overlay> overlays = mMapView.getOverlays();
		overlays.add(new LocationOverlay());

		Button okButton = (Button) findViewById(R.id.button_pick_location);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final GeoPoint mapCenter = mMapView.getMapCenter();
				Intent i = new Intent();
				i.putExtra(EXTRA_LATITUDE, mapCenter.getLatitudeE6());
				i.putExtra(EXTRA_LONGITUDE, mapCenter.getLongitudeE6());
				setResult(RESULT_OK, i);
				finish();
			}
		});

		Button cancelButton = (Button) findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
