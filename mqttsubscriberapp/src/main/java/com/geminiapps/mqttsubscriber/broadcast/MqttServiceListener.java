package com.geminiapps.mqttsubscriber.broadcast;

/**
 * Created by jim.stys on 10/5/16.
 */

public class MqttServiceListener {
    protected void onQueryServiceRunningResponse(boolean running){ /* Empty default implementation  */}
    protected void onStartServiceResponse(boolean success){ /* Empty default implementation  */ }
    protected void onStopServiceResponse(boolean success){ /* Empty default implementation  */ }
    protected void onClientConnectResponse(){ /* Empty default implementation  */ }
    protected void onClientSubscribeResponse() { /* Empty default implementation  */ }
}
