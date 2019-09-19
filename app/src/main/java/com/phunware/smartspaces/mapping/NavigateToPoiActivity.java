package com.phunware.smartspaces.mapping;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.phunware.core.exceptions.PwException;
import com.phunware.location.provider_managed.ManagedProviderFactory;
import com.phunware.location.provider_managed.PwManagedLocationProvider;
import com.phunware.mapping.MapFragment;
import com.phunware.mapping.OnPhunwareMapReadyCallback;
import com.phunware.mapping.PhunwareMap;
import com.phunware.mapping.manager.Callback;
import com.phunware.mapping.manager.Navigator;
import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.manager.Router;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteOptions;
import com.phunware.smartspaces.R;
import com.phunware.smartspaces.network.NetworkManager;
import com.phunware.smartspaces.network.NetworkManagerImpl;
import com.phunware.smartspaces.ui.MessageDialogFragment;

import java.lang.ref.WeakReference;
import java.util.List;

public class NavigateToPoiActivity extends AppCompatActivity implements
        OnPhunwareMapReadyCallback,
        Building.OnFloorChangedListener,
        Navigator.OnManeuverChangedListener {


    interface BlueDotAcquisitionListener {
        void onBlueDotAcquired();
    }

    private static final String TAG = NavigateToPoiActivity.class.getSimpleName();

    private Handler handler = new Handler();

    private long buildingId = 99197;

    private boolean isBuildingLoaded = false;
    private boolean isMapLoaded = false;

    private PhunwareMapManager phunwareMapManager = null;
    private PhunwareMap phunwareMap = null;
    private Navigator navigator = null;
    private View navOverlayContainer;
    private NavigationOverlayView navOverlay;

    private Building currentBuilding = null;
    private FloorOptions initialFloorOptions = null;

    //private NetworkManager networkManager = new MockNetWorkManagerImpl();
    private NetworkManager networkManager = new NetworkManagerImpl();
    private int currentManeuverIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate_to_poi);

        navOverlayContainer = findViewById(R.id.nav_overlay_container);
        navOverlay = findViewById(R.id.nav_overlay);

        // Create map manager
        phunwareMapManager = PhunwareMapManager.create(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getPhunwareMapAsync(this);

        //Load building
        loadBuilding();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (phunwareMapManager != null) {
            phunwareMapManager.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        if (navOverlayContainer.getVisibility() == View.VISIBLE) {
            stopNavigating();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPhunwareMapReady(PhunwareMap phunwareMap) {
        isMapLoaded = true;
        this.phunwareMap = phunwareMap;

        phunwareMap.getGoogleMap().getUiSettings().setMapToolbarEnabled(false);
        phunwareMap.getGoogleMap().setMapStyle(MapStyleOptions.loadRawResourceStyle(
                this, R.raw.map_style));

        attachBuilding();
    }

    private void loadBuilding() {
        phunwareMapManager.loadBuilding(buildingId, new Callback<Building>() {
            @Override
            public void onSuccess(Building building) {
                Log.d(TAG, "Building " + buildingId + " fully loaded");

                isBuildingLoaded = true;
                currentBuilding = building;

                initialFloorOptions = building.initialFloor();
                building.selectFloor(initialFloorOptions.getLevel());

                Log.d(TAG, "Selected floor : " + initialFloorOptions.getLevel());

                attachBuilding();
            }

            @Override
            public void onFailure(final Throwable throwable) {
                Log.d(TAG, "Error loading building : " + throwable.toString());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String error = getString(R.string.building_not_loaded) + " : " + throwable.getMessage();
                        showErrorAndFinish(error);
                    }
                });
            }
        });
    }

    private void attachBuilding() {
        if (!isMapLoaded) {
            Log.d(TAG, "Map has not finished loading");
            return;
        }

        if (!isBuildingLoaded) {
            Log.d(TAG, "Building has not finished loading");
            return;
        }

        try {
            phunwareMapManager.attachBuildingToMap(currentBuilding, phunwareMap);

            // Set building to initial floor value
            FloorOptions initialFloor = currentBuilding.initialFloor();
            currentBuilding.selectFloor(initialFloor.getLevel());

            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLngZoom(currentBuilding.getLocation(), 19);

            phunwareMap.getGoogleMap().animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                    // Initialize location provider
                    setManagedLocationProvider(currentBuilding);

                    // Add a listener to monitor floor switches
                    phunwareMapManager.addFloorChangedListener(NavigateToPoiActivity.this);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            waitToAcquireLocation(new BlueDotAcquisitionListener() {
                                @Override
                                public void onBlueDotAcquired() {

                                    Log.i("EXPLORE","Bluedot accquired ! ");

                                    phunwareMapManager.setMyLocationMode(PhunwareMapManager.MODE_FOLLOW_ME);


                                    RouteOptions route = findRouteFromCurrentLocationToPoi("Webex 2");

                                    // Get routes
                                    if(route != null){
                                        // Start Navigating
                                        runOnUiThread(() -> {
                                            startNavigating(route);
                                        });

                                    } else {
                                        showError("Unable to find a route from current Location to POI");
                                    }
                                }
                            });
                        }
                    }).start();

                }

                @Override
                public void onCancel() {

                }
            });
        } catch (PwException e) {
            Log.e(TAG, "Error attaching building to map: " + e.toString());
            e.printStackTrace();
        }
    }

    private void waitToAcquireLocation(BlueDotAcquisitionListener blueDotAcquisitionListener){
        while(! phunwareMapManager.isBluedotVisibleOnFloor()){
            try {
                Thread.sleep(1000);

                Log.i("EXPLORE","one sec passed. Bluedot not accquired yet");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        blueDotAcquisitionListener.onBlueDotAcquired();
    }

    PointOptions findPoiWithName(String name, FloorOptions floorOptions) {
        PointOptions pointOptions = null;
        //Get all pois
        if (floorOptions != null) {
            List<PointOptions> points = floorOptions.getPoiOptions();

            for (PointOptions tempPoint : points) {
                if (tempPoint.getName().equalsIgnoreCase(name)) {
                    pointOptions = tempPoint;
                    break;
                }
            }
        }
        return pointOptions;
    }

    private RouteOptions findRouteFromCurrentLocationToPoi(String poiName) {
        LatLng currentLatLng = null;
        RouteOptions route = null;
        boolean isAccessible = false;

        if (phunwareMapManager.isMyLocationEnabled() && phunwareMapManager.getCurrentLocation() != null) {
            Location currentLocation = phunwareMapManager.getCurrentLocation();
            currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            PointOptions destinationPoiOptions = findPoiWithName(poiName, initialFloorOptions);
            long destinationId = destinationPoiOptions.getId();

            //find routes
            Router router = phunwareMapManager.findRoutes(currentLatLng, destinationId, initialFloorOptions.getId(), isAccessible);
            if(router != null){
                route = router.shortestRoute();
            }

        } else {
            showErrorAndFinish("Unable to get current location");
        }
        return route;
    }

    private RouteOptions findRouteTBetweenPois(String startPoiName, String endPoiName) {
        RouteOptions route = null;
        boolean isAccessible = false;

        PointOptions startPoiOptions = findPoiWithName(startPoiName, initialFloorOptions);
        PointOptions endPoiOptions = findPoiWithName(endPoiName, initialFloorOptions);
        long startId = startPoiOptions.getId();
        long endId = endPoiOptions.getId();

        //find routes
        Router router = phunwareMapManager.findRoutes(startId, endId, isAccessible);
        if(router != null){
            route = router.shortestRoute();
        } else{
            showErrorAndFinish("No routes between " + startPoiName + " and " + endPoiName);
        }

        return route;
    }

    private void startNavigating(RouteOptions route) {
        if (navigator != null) {
            navigator.stop();
        }
        navigator = phunwareMapManager.navigate(route);
        navigator.addOnManeuverChangedListener(this);

        navOverlay.setNavigator(navigator);
        navOverlayContainer.setVisibility(View.VISIBLE);

        navigator.start();
    }

    private void stopNavigating() {
        if (navigator != null) {
            navigator.stop();
            navigator = null;
        }
        navOverlayContainer.setVisibility(View.GONE);
    }

    private void setManagedLocationProvider(Building building) {
        ManagedProviderFactory.ManagedProviderFactoryBuilder builder =
                new ManagedProviderFactory.ManagedProviderFactoryBuilder();
        builder.application(getApplication())
                .context(new WeakReference<Context>(getApplication()))
                .buildingId(String.valueOf(building.getId()));
        ManagedProviderFactory factory = builder.build();
        PwManagedLocationProvider managedProvider
                = (PwManagedLocationProvider) factory.createLocationProvider();
        phunwareMapManager.setLocationProvider(managedProvider, building);
        phunwareMapManager.setMyLocationEnabled(true);
    }

    private void showErrorAndFinish(String errorMessage) {
        Log.e(TAG, errorMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NavigateToPoiActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        finish();
    }

    private void showError(String errorMessage) {
        Log.e(TAG, errorMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NavigateToPoiActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    // OnFloorChangedListener
    @Override
    public void onFloorChanged(Building building, long l) {

    }



    // Maneuver Changed listener
    @Override
    public void onManeuverChanged(Navigator navigator, int position) {
        this.currentManeuverIndex = position;

        int lastIndex = navigator.getManeuvers().size() - 1;
        if(currentManeuverIndex == lastIndex){

            MessageDialogFragment.newInstance("You have arrived !",
                    "Have a nice day")
                    .show(getSupportFragmentManager(),"message_dialog");
            stopNavigating();

            //Call Room device endpoint
            Log.w("EXPLORE","Waiting for 7 seconds to trigger message to room end point ...");

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Call Room device endpoint
                    networkManager.sendContentToWebExDevice("");

                    Log.w("EXPLORE","7 seconds passed.. message triggered. ...");
                }
            }, 7000);
        }
    }

    @Override
    public void onRouteSnapFailed() {

    }
}
