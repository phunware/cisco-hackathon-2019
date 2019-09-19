package com.phunware.smartspaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.phunware.engagement.entities.Message;
import com.phunware.engagement.messages.MessageManager;
import com.phunware.smartspaces.engagement.EngagementHelper;
import com.phunware.smartspaces.engagement.EngagementHelperImpl;
import com.phunware.smartspaces.mapping.NavigateToPoiActivity;
import com.phunware.smartspaces.mapping.NavigateToReceptionActivity;
import com.phunware.smartspaces.network.NetworkManager;
import com.phunware.smartspaces.network.NetworkManagerImpl;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String RECEPTION_SPACE_ID = "Y2lzY29zcGFyazovL3VzL1JPT00vOGJiODU1ODAtZGExNi0xMWU5LTk4NzItMWQ0YjJmZjMyYjBk";

    private static final int PERM_REQ = 1000;
    private NetworkManager networkManager = new NetworkManagerImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button navigate = findViewById(R.id.btn_navigate);
        navigate.setOnClickListener((view) -> {
            navigateToRoom();
        });

        checkPermissions();
    }

    private void checkPermissions() {
        if (!hasLocationPermission() || !hasExternalStoragePermission()) {
            requestPermissions();
        } else {
            onPermissionsGranted();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_REQ);
    }

    private void onPermissionsGranted() {

        switch (getIntent().getAction()) {
            case Intent.ACTION_VIEW:
                Message intentMessage = getIntent()
                        .getParcelableExtra(MessageManager.EXTRA_MESSAGE);

                boolean hasExtras = getIntent()
                        .getBooleanExtra(MessageManager.EXTRA_HAS_EXTRAS, false);
                if (hasExtras) {
                    long messageId = intentMessage.campaignId;
                    Log.d("EXPLORE", "Message = " + messageId + " has extras");
                }

                if (intentMessage != null) {
                    networkManager.sendArrivalRequestToBot();
                }
                break;
        }

        EngagementHelper engagementHelper = new EngagementHelperImpl();
        engagementHelper.enableEngagementCampaigns(this);
        engagementHelper.enableGeofenceAndBeaconCampaigns(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean allPermissionsGranted = true;
        if (requestCode == PERM_REQ) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                showToast("Permissions not granted. Cannot continue");
                finish();
            } else {
                onPermissionsGranted();
            }
        }
    }

    private boolean hasLocationPermission() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasExternalStoragePermission() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToRoom() {
        Intent navigationIntent = new Intent(this, NavigateToPoiActivity.class);
        startActivity(navigationIntent);
    }

    private void navigateToReception() {
        Intent navigationIntent = new Intent(this, NavigateToReceptionActivity.class);
        startActivity(navigationIntent);
    }
}
