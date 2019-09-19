package com.phunware.smartspaces.mapping;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.phunware.mapping.model.LandmarkManeuverOptions;
import com.phunware.mapping.model.PointOptions;
import com.phunware.mapping.model.RouteManeuverOptions;
import com.phunware.smartspaces.R;

import java.util.Locale;

public class ManeuverDisplayHelper {

    private static final double NUM_FEET_PER_METER = 3.28084;
    private static final String SPACE = " ";

    public String stringForDirection(Context context, RouteManeuverOptions maneuver) {
        if (context == null || maneuver == null || maneuver.getDirection() == null) {
            return "";
        }

        String landmarkStringTurn = "";
        String landmarkStringMain = "";
        if (!maneuver.getLandmarks().isEmpty()) {
            LandmarkManeuverOptions landmark = maneuver.getLandmarks().get(maneuver.getLandmarks().size() - 1);
            if (landmark.getPosition() == LandmarkManeuverOptions.POSITION.AFTER) {
                landmarkStringTurn = SPACE + context.getString(R.string.after) + SPACE;
            } else if (landmark.getPosition() == LandmarkManeuverOptions.POSITION.AT) {
                landmarkStringTurn = SPACE + context.getString(R.string.at) + SPACE;
            }
            landmarkStringTurn += landmark.getName();
            landmarkStringMain += landmark.getName();
        }

        StringBuilder directionString = new StringBuilder();
        switch (maneuver.getDirection()) {
            case FLOOR_CHANGE:
                directionString.append(floorChangeDescriptionForManeuver(context, maneuver));
                break;
            case BEAR_LEFT:
                directionString.append(context.getString(R.string.bear_left));
                directionString.append(landmarkStringTurn);
                break;
            case BEAR_RIGHT:
                directionString.append(context.getString(R.string.bear_right));
                directionString.append(landmarkStringTurn);
                break;
            case LEFT:
                directionString.append(context.getString(R.string.turn_left));
                directionString.append(landmarkStringTurn);
                break;
            case RIGHT:
                directionString.append(context.getString(R.string.turn_right));
                directionString.append(landmarkStringTurn);
                break;
            case STRAIGHT:
                if (TextUtils.isEmpty(landmarkStringMain)) {
                    directionString.append(String.format(Locale.US,
                            context.getString(R.string.continue_straight_distance),
                            getStringDistanceInFeet(maneuver.getDistance())));
                } else {
                    directionString.append(String.format(Locale.US, context.getString(R.string.walk_straight_distance),
                            getStringDistanceInFeet(maneuver.getDistance()), landmarkStringMain));
                }
                break;
            default:
                directionString.append(context.getString(R.string.unknown));
                break;
        }
        return directionString.toString();
    }

    /**
     * Converts the distance from meters to feet
     *
     * @param distance Double distance in meters
     * @return String object containing the converted number of feet (rounded up)
     */
    public String getStringDistanceInFeet(double distance) {
        if (distance < 0) {
            return "";
        }
        double res = distance * NUM_FEET_PER_METER;
        res = Math.ceil(res);
        return String.valueOf((int) res);
    }

    public int getImageResourceForDirection(@NonNull Context context,
                                            RouteManeuverOptions maneuver) {
        int resource = 0;
        switch (maneuver.getDirection()) {
            case STRAIGHT:
                resource = R.drawable.ic_arrow_straight;
                break;
            case LEFT:
                resource = R.drawable.ic_arrow_left;
                break;
            case RIGHT:
                resource = R.drawable.ic_arrow_right;
                break;
            case BEAR_LEFT:
                resource = R.drawable.ic_arrow_bear_left;
                break;
            case BEAR_RIGHT:
                resource = R.drawable.ic_arrow_bear_right;
                break;
            case FLOOR_CHANGE:
                String changeDescription = floorChangeDescriptionForManeuver(context, maneuver);
                if (changeDescription.toLowerCase(Locale.getDefault())
                        .contains(context.getString(R.string.elevator))) {
                    if (changeDescription.toLowerCase(Locale.getDefault())
                            .contains(context.getString(R.string.down))) {
                        resource = R.drawable.ic_elevator_down;
                    } else {
                        resource = R.drawable.ic_elevator_up;
                    }
                } else {
                    if (changeDescription.toLowerCase(Locale.getDefault())
                            .contains(context.getString(R.string.down))) {
                        resource = R.drawable.ic_stairs_down;
                    } else {
                        resource = R.drawable.ic_stairs_up;
                    }
                }
                break;
        }
        return resource;
    }

    private String floorChangeDescriptionForManeuver(@NonNull Context context,
                                                     RouteManeuverOptions maneuver) {
        PointOptions endPoint = maneuver.getPoints().get(maneuver.getPoints().size() - 1);
        String endPointName = endPoint.getName();
        String methodOfChange = "";
        if (endPointName != null && endPointName.toLowerCase(Locale.getDefault())
                .contains(context.getString(R.string.elevator))) {
            methodOfChange = context.getString(R.string.elevator);
        } else if (endPointName != null && endPointName.toLowerCase(Locale.getDefault())
                .contains(context.getString(R.string.stairs))) {
            methodOfChange = context.getString(R.string.stairs);
        }

        FloorChangeDirection direction = directionForManeuver(maneuver);
        String directionMessage;
        if (direction == FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp) {
            directionMessage = context.getString(R.string.up_to_level);
        } else if (direction
                == FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown) {
            directionMessage = context.getString(R.string.down_to_level);
        } else {
            directionMessage = context.getString(R.string.to);
        }
        return String.format(Locale.US, context.getString(R.string.floor_change_message_format),
                methodOfChange, directionMessage, endPoint.getLevel());
    }

    private FloorChangeDirection directionForManeuver(RouteManeuverOptions maneuver) {
        PointOptions startPoint = maneuver.getPoints().get(0);
        PointOptions endPoint = maneuver.getPoints().get(maneuver.getPoints().size() - 1);
        if (startPoint.getLevel() < endPoint.getLevel())
            return FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionUp;
        else if (startPoint.getLevel() > endPoint.getLevel())
            return FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionDown;
        else
            return FloorChangeDirection.PWManeuverDisplayHelperFloorChangeDirectionSameFloor;
    }

    private enum FloorChangeDirection {
        PWManeuverDisplayHelperFloorChangeDirectionSameFloor,
        PWManeuverDisplayHelperFloorChangeDirectionUp,
        PWManeuverDisplayHelperFloorChangeDirectionDown
    }
}

