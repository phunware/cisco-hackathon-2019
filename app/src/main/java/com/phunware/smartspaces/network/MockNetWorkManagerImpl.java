package com.phunware.smartspaces.network;

import android.util.Log;

public class MockNetWorkManagerImpl implements NetworkManager {

    private static final String TAG = MockNetWorkManagerImpl.class.getSimpleName();

    @Override
    public void sendArrivalRequestToBot() {
        Log.d(TAG,"Sent Arrival Request To Reception Bot ");
    }

    @Override
    public void sendDirectionsRequestToJoan(String screenId, String userEmail, String direction) {
        Log.d(TAG,"Sent Directions Request To Joan: "+screenId+" , "+userEmail+", "+direction);
    }

    @Override
    public void sendContentToWebExDevice(String content) {
        Log.d(TAG,"Sent Content to WebEx device: "+content);
    }
}
