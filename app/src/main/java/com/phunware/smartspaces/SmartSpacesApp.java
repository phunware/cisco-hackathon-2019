package com.phunware.smartspaces;

import android.app.Application;

import com.phunware.core.PwLog;
import com.phunware.engagement.EngagementLifecycleCallbacks;
import com.phunware.smartspaces.core.EnvInitializer;
import com.phunware.smartspaces.core.EnvInitializerImpl;
import com.phunware.smartspaces.engagement.EngagementHelper;
import com.phunware.smartspaces.engagement.EngagementHelperImpl;

public class SmartSpacesApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PwLog.setShowLog(true);

        String appId = getString(R.string.app_id);
        String accessKey = getString(R.string.access_key);
        String sigKey = getString(R.string.signature_key);

        EnvInitializer envInitializer = new EnvInitializerImpl();
        envInitializer.initializeCore(this, appId, accessKey, sigKey);

        envInitializer.initializeEngagement(this, appId);
        registerActivityLifecycleCallbacks(new EngagementLifecycleCallbacks());

        //Enable campaigns
        EngagementHelper engagementHelper = new EngagementHelperImpl();
        engagementHelper.enableEngagementCampaigns(this);
    }
}
