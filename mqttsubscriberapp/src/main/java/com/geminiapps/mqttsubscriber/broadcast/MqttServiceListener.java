package com.geminiapps.mqttsubscriber.broadcast;

/**
 * Created by jim.stys on 10/5/16.
 */

public abstract class MqttServiceListener {
    public abstract void onQueryServiceRunningResponse(boolean running);
}
