package com.geminiapps.mqttsubscriber.broadcast;

import android.os.Bundle;

/**
 * Created by jim.stys on 10/5/16.
 */

public class MqttServiceListener {
    protected void onQueryServiceRunningResponse(boolean running){ /* Empty default implementation  */}
    protected void onStartServiceResponse(boolean success){ /* Empty default implementation  */ }
    protected void onStopServiceResponse(boolean success){ /* Empty default implementation  */ }
    protected void onClientConnectResponse(String profileName, String clientId, boolean success, String error){ /* Empty default implementation  */ }
    protected void onClientDisconnectResponse(String profileName, String clientId, boolean success){ /* Empty default implementation */ }
    protected void onClientSubscribeResponse(String profileName, String topicFilter, boolean success) { /* Empty default implementation  */ }
    protected void onMessageArrived(String profileName, String topicFilter, String topic, String message, int qos){ /* Empty default implementation */ }
    protected void onQueryProfileConnectedResponse(Bundle profileConnectivityMap){ /* Empty default implementation */ }
}
