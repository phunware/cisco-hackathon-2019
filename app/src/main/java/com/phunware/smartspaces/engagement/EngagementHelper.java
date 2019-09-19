package com.phunware.smartspaces.engagement;

import android.content.Context;

public interface EngagementHelper {

    void enableEngagementCampaigns(Context context);

    void enablePushNotifications(Context context);

    void enableGeofenceAndBeaconCampaigns(Context context);
}
