package com.geminiapps.mqttsubscriber.broadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.Status;
import org.eclipse.paho.android.service.TaskerMqttConstants;

/**
 * Created by jim.stys on 10/4/16.
 */

public class MqttServiceReceiver extends BroadcastReceiver {

    private MqttServiceListener listener;

    public MqttServiceReceiver(MqttServiceListener listener) {
        super();

        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            Bundle resultBundle = intent.getExtras();
            if(resultBundle != null){
                String action = resultBundle.getString(MqttServiceConstants.CALLBACK_ACTION);
                if(action != null){
                    switch(action){
                        case TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION:
                            boolean running = intent.getSerializableExtra(MqttServiceConstants.CALLBACK_STATUS) == Status.OK;
                            this.listener.onQueryServiceRunningResponse(running);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public abstract class MqttServiceListener{
        public abstract void onQueryServiceRunningResponse(boolean running);
    }
}
