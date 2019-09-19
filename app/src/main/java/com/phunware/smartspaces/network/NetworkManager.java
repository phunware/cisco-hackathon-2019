package com.phunware.smartspaces.network;

public interface NetworkManager {

    public static final String API_END_POINT_RECEPTION_BOT = "https://phunjoan.herokuapp.com/reception";
    public static final String API_END_POINT_ROOM_DEVICE = "https://phunjoan.herokuapp.com/room";
    public static final String API_END_POINT_JOAN_DIRECTIONS = "";

    void sendArrivalRequestToBot();

    void sendDirectionsRequestToJoan(String screenId, String userEmail, String direction);

    void sendContentToWebExDevice(String content);
}
