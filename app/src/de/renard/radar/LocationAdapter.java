package de.renard.radar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import de.renard.radar.R;
import de.renard.radar.views.LocationDeleteInterface;
import gen.radar.Location;

/**
 * Created by renard on 31/12/13.
 */
public class LocationAdapter extends BaseAdapter implements LocationDeleteInterface {

    private final LocationDeleteInterface mLocationDeleter;

    private static class ViewHolder implements View.OnClickListener {
        private final TextView mDescription;
        private final TextView mLatLong;
        private final ImageButton mDeleteButton;
        private final LocationDeleteInterface mLocationDeleter;
        private Location mLocation;


        ViewHolder(View view, LocationDeleteInterface locationDeleter) {
            mLocationDeleter = locationDeleter;
            mDescription = (TextView) view.findViewById(R.id.description);
            mLatLong = (TextView) view.findViewById(R.id.latlong);
            mDeleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        }

        void update(Location location) {
            mLocation = location;
            mDescription.setText(location.getDescription());
            final String text = mLatLong.getResources().getString(R.string.latlng_formatted, location.getLat(), location.getLng());
            mLatLong.setText(text);
            mDeleteButton.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            mLocationDeleter.deleteLocation(mLocation);
        }
    }


    private final List<Location> mLocations;
    LayoutInflater mInflater;

    public LocationAdapter(Context context, List<Location> locations, LocationDeleteInterface locationDeleter) {
        mLocationDeleter = locationDeleter;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLocations = locations;
    }

    @Override
    public void deleteLocation(Location location) {
        mLocations.remove(location);
        mLocationDeleter.deleteLocation(location);
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public Object getItem(int position) {
        return mLocations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mLocations.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Location location = (Location) getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.location_list_item, null);
            convertView.setTag(new ViewHolder(convertView, this));
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.update(location);
        return convertView;
    }
}
