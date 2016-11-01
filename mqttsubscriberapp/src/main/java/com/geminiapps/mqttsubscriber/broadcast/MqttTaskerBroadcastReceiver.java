package com.geminiapps.mqttsubscriber.broadcast;

import android.content.Context;
import android.content.Intent;

import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;

/**
 * Created by jim.stys on 11/1/16.
 */

public class MqttTaskerBroadcastReceiver extends TaskerBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        this.actionRunner = new MqttServiceSender(context);
        super.onReceive(context, intent);
    }
}
