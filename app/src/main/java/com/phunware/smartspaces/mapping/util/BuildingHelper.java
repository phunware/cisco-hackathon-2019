package com.phunware.smartspaces.mapping.util;

import com.phunware.mapping.manager.PhunwareMapManager;
import com.phunware.mapping.manager.Router;
import com.phunware.mapping.model.Building;
import com.phunware.mapping.model.FloorOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteOptions;

import java.util.List;

public class BuildingHelper {

    private Building building = null;

    public BuildingHelper(Building building){
        this.building = building;
    }

    public FloorOptions getFloorOptionsFromLevel(int level){
        FloorOptions floorOptions = null;
        List<FloorOptions> allFloorOptions = building.getFloorOptions();
        for(FloorOptions tempOptions: allFloorOptions){
            if(tempOptions.getLevel() == level){
                floorOptions = tempOptions;
                break;
            }
        }
        return floorOptions;
    }

    PointOptions findPoiOptionsWithName(String name, FloorOptions floorOptions) {
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

    private RouteOptions findRouteTBetweenPois(
            PhunwareMapManager phunwareMapManager,
            String startPoiName, String endPoiName,
            FloorOptions floorOptions) {
        RouteOptions route = null;
        boolean isAccessible = false;

        PointOptions startPoiOptions = findPoiOptionsWithName(startPoiName, floorOptions);
        PointOptions endPoiOptions = findPoiOptionsWithName(endPoiName, floorOptions);
        long startId = startPoiOptions.getId();
        long endId = endPoiOptions.getId();

        //find routes
        Router router = phunwareMapManager.findRoutes(startId, endId, isAccessible);
        if(router != null && router.shortestRoute() != null){
            route = router.shortestRoute();
        }
        return route;
    }

    /*public List<PointOptions> getMeetingRoons(){
        return null;
    }*/
}
