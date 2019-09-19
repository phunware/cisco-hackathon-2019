package com.phunware.smartspaces.core;

import android.content.Context;
import android.util.Log;

import com.phunware.core.PwCoreSession;
import com.phunware.engagement.Engagement;
import com.phunware.engagement.log.LogLogger;

public class EnvInitializerImpl implements EnvInitializer {

    private static final String TAG = EnvInitializerImpl.class.getSimpleName();

    @Override
    public void initializeCore(Context context, String appID, String accessKey, String signatureKey) {

        Log.d(TAG, "Initializing Core");

        PwCoreSession.getInstance().setEnvironment(PwCoreSession.Environment.PROD);

        Log.d(TAG, "Core Initialized ...");

        PwCoreSession.getInstance().registerKeys(context, appID, accessKey, signatureKey);

        Log.d(TAG, "Keys registered for " + appID + " , " + accessKey + ", " + signatureKey);
    }

    @Override
    public void initializeEngagement(Context context, String appID) {
        Log.d(TAG, "Initializing Engagement");
        new Engagement.Builder(context)
                .appId(Long.valueOf(appID))
                .addLogger(new LogLogger())
                .build();

        Log.d(TAG, "Engagement Initialized...");
    }
}
