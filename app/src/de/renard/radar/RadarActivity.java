package de.renard.radar;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import java.util.Date;
import java.util.List;

import de.renard.radar.map.LocationPickActivity;
import gen.radar.DaoMaster;
import gen.radar.DaoSession;
import gen.radar.Location;
import gen.radar.LocationDao;

import static gen.radar.DaoMaster.*;

public class RadarActivity extends FragmentActivity {

	public static final String CURRENT_LOCATION = "current_location";
	private final static String DEBUG_TAG = RadarActivity.class.getSimpleName();
	private final static int REQUEST_CODE_LOCATION = 0;

	private SensorDataController mLocationDataManager;
    private LocationDao mLocationDao;
    private LatLng mLastlocation;
    private SharedPreferences mSharedPrefs;

    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mLocationDataManager = new SensorDataController(this);
        mSharedPrefs = getSharedPreferences(RadarActivity.class.getSimpleName(), Context.MODE_PRIVATE);


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
                if(mLocationDao.count()==0){
                    startMapActivity();
                } else {
                    final FragmentManager supportFragmentManager = getSupportFragmentManager();
                    LocationListDialog.newInstance().show(supportFragmentManager,LocationListDialog.TAG);
                }
			}
		});
        initDataAccess();
        restoreLastActiveLocation();
        Log.i(DEBUG_TAG, "onCreate()");
	}

    private void restoreLastActiveLocation() {
        final List<Location> active = mLocationDao.queryBuilder().where(LocationDao.Properties.Active.eq("true")).limit(1).list();
        if (active!=null && active.size()==1){
            final Location location = active.get(0);
            LatLng latLng = new LatLng(location.getLat(),location.getLng());
            mLocationDataManager.setDestination(latLng);
        }
    }

    private void initDataAccess() {
        DevOpenHelper helper = new DevOpenHelper(this, "location-db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        DaoSession daoSession = daoMaster.newSession();
        mLocationDao = daoSession.getLocationDao();

    }

    public void startMapActivity() {
        Intent i = new Intent(this, LocationPickActivity.class);
        android.location.Location currentLocation = mLocationDataManager.getCurrentLocation();
        i.putExtra(CURRENT_LOCATION, currentLocation);
        startActivityForResult(i, REQUEST_CODE_LOCATION);
    }

    /**
     * location is set by the map activity
     * @param requestCode
     * @param resultCode
     * @param data
     */
	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_CODE_LOCATION:
				mLastlocation = data.getParcelableExtra(LocationPickActivity.EXTRA_LATLNG);
				mLocationDataManager.setDestination(mLastlocation);
			}
		}
	}

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mLastlocation!=null){
            askForLocationDescription(mLastlocation);
            mLastlocation = null;
        }
    }

    private void askForLocationDescription(LatLng location) {
        final FragmentManager supportFragmentManager = getSupportFragmentManager();
        LocationDescriptionDialog.newInstance(location).show(supportFragmentManager,LocationDescriptionDialog.TAG);
    }

    /**
     * called by @LocationDescriptionDialog after user has entered the description for a new location
     * @param location
     * @param description
     */
    void saveLocation(LatLng location, String description) {
        //TODO do it asynchronously
        Location loc = new Location();
        loc.setCreated(new Date());
        loc.setDescription(description);
        loc.setLat(location.latitude);
        loc.setLng(location.longitude);
        loc.setActive(true);
        removeLastActiveLocation();
        mLocationDao.insert(loc);
    }

    private void removeLastActiveLocation() {
        final List<Location> list = mLocationDao.queryBuilder().where(LocationDao.Properties.Active.eq(true)).limit(1).list();
        if (list.size()==1){
            list.get(0).setActive(false);
            mLocationDao.update(list.get(0));
        }
    }

    /**
     * @param location picked by user from location list dialog
     */
    public void setLocation(Location location) {
        LatLng l = new LatLng(location.getLat(),location.getLng());
        location.setActive(true);
        removeLastActiveLocation();
        mLocationDao.update(location);
        mLocationDataManager.setDestination(l);
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

    public LocationDao getLocationDao() {
        return mLocationDao;
    }
}