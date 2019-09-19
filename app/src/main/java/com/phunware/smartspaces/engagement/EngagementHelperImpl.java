package com.phunware.smartspaces.engagement;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import com.phunware.engagement.Engagement;

public class EngagementHelperImpl implements EngagementHelper {

    public void enableEngagementCampaigns(Context context){
        enablePushNotifications(context);
        enableGeofenceAndBeaconCampaigns(context);
    }

    public void enablePushNotifications(Context context){
        Engagement.enablePushNotifications(context);
    }

    public void enableGeofenceAndBeaconCampaigns(Context context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        ) {
            Engagement.locationManager().start();
        }
    }
}
