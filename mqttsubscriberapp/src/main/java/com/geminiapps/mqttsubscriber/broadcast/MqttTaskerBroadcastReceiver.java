package com.geminiapps.mqttsubscriber.broadcast;

import android.content.Context;
import android.content.Intent;

import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;

import org.eclipse.paho.android.service.tasker.TaskerPlugin;

/**
 * Created by jim.stys on 11/1/16.
 */

public class MqttTaskerBroadcastReceiver extends TaskerBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        this.actionRunner = new MqttServiceSender(context);
        super.onReceive(context, intent);

        if(isOrderedBroadcast()){
            // TODO: return pending and asynchronously send actual result
            setResultCode(TaskerPlugin.Setting.RESULT_CODE_OK);
        }
    }
}
