package com.phunware.smartspaces.network;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkManagerImpl implements NetworkManager {

    private OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    public void sendArrivalRequestToBot() {
        okhttp3.Callback callback = new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("EXPLORE","Posting to Reception Bot failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d("EXPLORE","Posting to Reception Bot : Success");
                }
            }
        };

        String reqJson = "";
        Request request = new Request.Builder()
                .url("https://phunjoan.herokuapp.com/reception")
                .post(RequestBody.create(MediaType.parse("application/json"), reqJson))
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        okHttpClient.newCall(request).enqueue(callback);

        Log.d("EXPLORE","Sent message to reception Bot");
    }

    @Override
    public void sendContentToWebExDevice(String content) {
        MediaType mediaType = MediaType.parse("application/json");

        RequestBody body = RequestBody.create(mediaType, "{\n\t\"content\":\"https://storage.googleapis.com/cisco-hackathon/team10_v2.html\"\n}");
        Request request = new Request.Builder()
                .url("https://phunjoan.herokuapp.com/room")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {

                    }
                });
    }

    @Override
    public void sendDirectionsRequestToJoan(String screenId, String userEmail, String direction) {

        Log.d("EXPLORE","Sending request to Joan");

        MediaType mediaType = MediaType.parse("application/json");
        String json = "{\n\"destination\":\"White room, Neo\"," +
                "\n\"direction\":\"" +
                direction +
                "\"," +
                "\n\"uuid\":\"display-1\"\n}";
        RequestBody body = RequestBody.create(mediaType,json);

        Request request = new Request.Builder()
                .url("https://us-central1-visionect-testing.cloudfunctions.net/function-1/send-location")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        okHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        Log.d("EXPLORE","Posting to Reception Bot failed");
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        Log.d("EXPLORE","Posting to Joan: Success, response code: "+response.code());
                        Log.d("EXPLORE","Posting to Joan: Success, response code: "+response.body());
                    }
                });

        Log.d("EXPLORE","Request sent to Joan");
    }
}
