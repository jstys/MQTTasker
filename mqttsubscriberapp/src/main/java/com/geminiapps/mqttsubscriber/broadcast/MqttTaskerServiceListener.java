package com.geminiapps.mqttsubscriber.broadcast;

/**
 * Created by jim.stys on 11/2/16.
 */

public class MqttTaskerServiceListener extends MqttServiceListener {
    @Override
    protected void onClientConnectResponse(String profileName, String clientId, boolean success, String error) {

    }

    @Override
    protected void onClientDisconnectResponse(String profileName, String clientId, boolean success) {

    }
}
