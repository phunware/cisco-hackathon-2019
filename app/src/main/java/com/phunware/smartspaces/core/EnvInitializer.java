package com.phunware.smartspaces.core;

import android.content.Context;

public interface EnvInitializer {

    void initializeCore(Context context,
                        String appID,
                        String accessKey,
                        String signatureKey);

    void initializeEngagement(Context context,
                        String appID);
}
