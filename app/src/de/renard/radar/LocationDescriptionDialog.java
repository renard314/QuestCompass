package de.renard.radar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static android.R.string.ok;

/**
 * Created by renard on 01/01/14.
 */
public class LocationDescriptionDialog extends DialogFragment implements DialogInterface.OnClickListener, TextView.OnEditorActionListener {
    private static final String LAT_LONG = "arg_latlng";
    public static final String TAG = LocationDescriptionDialog.class.getSimpleName();


    private static class GeoCodeAsyncTask extends AsyncTask<Void, Void, String> {

        private final Geocoder mGeocoder;
        private final Context mContext;
        private final LatLng mLocation;
        private final EditText mTextView;
        private final ProgressBar mProgressBar;

        GeoCodeAsyncTask(Context context, LatLng location, EditText textView, ProgressBar progressBar) {
            mContext = context;
            mLocation = location;
            mTextView = textView;
            mProgressBar = progressBar;
            mGeocoder = new Geocoder(context);
        }

        @Override
        protected void onPostExecute(String s) {
            mProgressBar.setVisibility(View.GONE);
            if (s != null) {
                mTextView.setText(s);
            } else {
                Toast.makeText(mContext, R.string.could_not_get_adress, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            final List<Address> fromLocation;
            try {
                fromLocation = mGeocoder.getFromLocation(mLocation.latitude, mLocation.longitude, 1);
            } catch (IOException e) {
                return null;
            } catch (IllegalArgumentException iae) {
                return null;
            }
            // If the reverse geocode returned an address
            if (fromLocation != null && fromLocation.size() > 0) {
                // Get the first address
                Address address = fromLocation.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return null;
            }
        }
    }

    private EditText mDescriptionView;
    private ProgressBar mProgressBar;
    private GeoCodeAsyncTask mGeoCodeTask;


    static LocationDescriptionDialog newInstance(LatLng latlng) {
        Bundle args = new Bundle();
        args.putParcelable(LAT_LONG, latlng);

        final LocationDescriptionDialog locationDescriptionDialog = new LocationDescriptionDialog();
        locationDescriptionDialog.setArguments(args);
        return locationDescriptionDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        RadarActivity radar = (RadarActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(radar);
        builder.setTitle(R.string.enter_location_description);
        View view = radar.getLayoutInflater().inflate(R.layout.location_description, null);
        mDescriptionView = (EditText) view.findViewById(R.id.description);

        mDescriptionView.setOnEditorActionListener(this);
        builder.setView(view);
        builder.setPositiveButton(ok, this);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                propagateResult(DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()));
            }
        });
        LatLng loc = getArguments().getParcelable(LAT_LONG);
        mGeoCodeTask = new GeoCodeAsyncTask(radar,loc,mDescriptionView, (ProgressBar) view.findViewById(R.id.progressBar));
        mGeoCodeTask.execute();
        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cancelGeoCoding();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        cancelGeoCoding();
        propagateResult(mDescriptionView.getText().toString());
    }

    private void cancelGeoCoding() {
        if (mGeoCodeTask!=null){
            mGeoCodeTask.cancel(true);
        }
    }

    private void propagateResult(final String description) {
        RadarActivity radar = (RadarActivity) getActivity();
        if (radar != null) {
            LatLng loc = getArguments().getParcelable(LAT_LONG);
            radar.saveLocation(loc, description);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        cancelGeoCoding();
        return false;
    }
}