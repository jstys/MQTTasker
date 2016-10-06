package com.geminiapps.mqttsubscriber.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.Status;
import org.eclipse.paho.android.service.TaskerMqttConstants;

/**
 * Created by jim.stys on 10/4/16.
 */

public class MqttServiceReceiver extends BroadcastReceiver {

    public static final String TAG = "MqttServiceReceiver";

    private MqttServiceListener listener;
    private Context context;

    public MqttServiceReceiver(MqttServiceListener listener, Context context) {
        super();

        this.listener = listener;
        this.context = context;

        register();
    }

    public void register(){
        this.context.registerReceiver(this, new IntentFilter(MqttServiceConstants.CALLBACK_TO_ACTIVITY));
    }

    public void unregister(){
        this.context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received broadcast from TaskerMqttService");

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
                            Toast.makeText(context, "Received broadcast with action = " + action, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    }
}
