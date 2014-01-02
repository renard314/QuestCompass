package de.renard.radar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.Button;

import java.util.List;

import de.renard.radar.views.LocationDeleteInterface;
import gen.radar.Location;

/**
 * Created by renard on 30/12/13.
 */
public class LocationListDialog extends DialogFragment implements DialogInterface.OnClickListener, LocationDeleteInterface{

    public static final String TAG = LocationListDialog.class.getSimpleName();
    private LocationAdapter mAdapter;

    static LocationListDialog newInstance() {
        return new LocationListDialog();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        RadarActivity radar = (RadarActivity) getActivity();
        final List<Location> locations = radar.getLocationDao().loadAll();
        mAdapter = new LocationAdapter(radar, locations, this);
        AlertDialog.Builder builder = new AlertDialog.Builder(radar);
        builder.setTitle(R.string.choose_or_create_location);
        builder.setAdapter(mAdapter,this);
        builder.setPositiveButton(R.string.create_location, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final RadarActivity radar = (RadarActivity) getActivity();
                if (radar != null) {
                    radar.startMapActivity();
                }
            }
        });
        final AlertDialog alertDialog = builder.create();
        final LayoutAnimationController layoutAnimationController = AnimationUtils.loadLayoutAnimation(radar, R.anim.layout_animation);
        alertDialog.getListView().setLayoutAnimation(layoutAnimationController);
        alertDialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Location location = (Location) mAdapter.getItem(position);
                RadarActivity radar = (RadarActivity) getActivity();
                radar.setLocation(location);
                alertDialog.cancel();
            }
        });
        addIconToPositiveButton(alertDialog);
        return alertDialog;
    }

    private void addIconToPositiveButton(final AlertDialog dialog) {
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                // if you do the following it will be left aligned, doesn't look correct

                Drawable drawable = getActivity().getResources().getDrawable(R.drawable.ic_action_content_new);
                //button.setCompoundDrawablesWithIntrinsicBounds(null,null,drawable,null);

                // set the bounds to place the drawable a bit right
                drawable.setBounds((int) (drawable.getIntrinsicWidth())*2, 0, (int) (drawable.getIntrinsicWidth() * 3), drawable.getIntrinsicHeight());
                button.setCompoundDrawables(drawable, null, null, null);

                // could modify the placement more here if desired
                //  button.setCompoundDrawablePadding();
            }
        });
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
    }

    @Override
    public void deleteLocation(Location location) {
        RadarActivity radar = (RadarActivity) getActivity();
        if (radar != null) {
            radar.getLocationDao().delete(location);
        }
    }

}