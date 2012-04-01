package de.renard.radar;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class LocationPickActivity extends MapActivity{
	
	@SuppressWarnings("unused")
	private MapView mMapView;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.location_pick_activity);
		mMapView = (MapView) findViewById(R.id.map);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
