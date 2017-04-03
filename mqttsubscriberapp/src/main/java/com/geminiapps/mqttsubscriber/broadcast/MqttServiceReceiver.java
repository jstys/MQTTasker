package com.geminiapps.mqttsubscriber.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttService;
import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.Status;
import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.Serializable;

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
        String error = null;

        if(intent != null){
            Bundle resultBundle = intent.getExtras();
            if(resultBundle != null){
                String action = resultBundle.getString(MqttServiceConstants.CALLBACK_ACTION);
                String profileName = resultBundle.getString(MqttServiceConstants.CALLBACK_CLIENT_HANDLE);
                String topicFilter = resultBundle.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA);
                String topic = resultBundle.getString(TaskerMqttConstants.TOPIC_EXTRA);
                String message = resultBundle.getString(TaskerMqttConstants.MESSAGE_EXTRA);
                int qos = resultBundle.getInt(TaskerMqttConstants.QOS_EXTRA);
                boolean reconnect = resultBundle.getBoolean(MqttServiceConstants.CALLBACK_RECONNECT, false);
                if(action != null){
                    boolean status = intent.getSerializableExtra(MqttServiceConstants.CALLBACK_STATUS) == Status.OK;
                    Serializable exception = intent.getSerializableExtra(MqttServiceConstants.CALLBACK_EXCEPTION);
                    if(exception != null){
                        error = ((MqttException)exception).toString();
                    }
                    switch(action){
                        case TaskerMqttConstants.START_SERVICE_ACTION:
                            this.listener.onStartServiceResponse(status);
                            break;
                        case TaskerMqttConstants.STOP_SERVICE_ACTION:
                            this.listener.onStopServiceResponse(status);
                            break;
                        case TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION:
                            this.listener.onQueryServiceRunningResponse(status);
                            break;
                        case MqttServiceConstants.CONNECT_EXTENDED_ACTION:
                            if(reconnect){
                                this.listener.onClientConnectResponse(profileName, null, status, error);
                            }
                            break;
                        case MqttServiceConstants.CONNECT_ACTION:
                            this.listener.onClientConnectResponse(profileName, null, status, error);
                            break;
                        case MqttServiceConstants.ON_CONNECTION_LOST_ACTION:
                        case MqttServiceConstants.DISCONNECT_ACTION:
                            this.listener.onClientDisconnectResponse(profileName, null, status);
                            break;
                        case MqttServiceConstants.SUBSCRIBE_ACTION:
                            this.listener.onClientSubscribeResponse(profileName, topicFilter, status);
                            break;
                        case MqttServiceConstants.MESSAGE_ARRIVED_ACTION:
                            this.listener.onMessageArrived(profileName, topicFilter, topic, message, qos);
                            break;
                        case TaskerMqttConstants.QUERY_PROFILE_CONNECTED_ACTION:
                            this.listener.onQueryProfileConnectedResponse(resultBundle);
                            break;
                        default:
                            Toast.makeText(context, "Received broadcast with unsupported action = " + action, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        }
    }
}
