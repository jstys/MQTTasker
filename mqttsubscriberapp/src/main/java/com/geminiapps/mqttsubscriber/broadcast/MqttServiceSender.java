package com.geminiapps.mqttsubscriber.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;
import com.geminiapps.mqttsubscriber.tasker.ITaskerActionRunner;

import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;
import org.eclipse.paho.android.service.tasker.TaskerMqttService;

/**
 * Created by jim.stys on 10/5/16.
 */

public class MqttServiceSender implements ITaskerActionRunner{
    private static final String TAG = MqttServiceSender.class.getName();

    private Context context;

    public MqttServiceSender(Context context){
        this.context = context;
    }

    private void sendMqttServiceAction(Intent intent){
        this.context.startService(intent);
    }

    public void startMqttService(){
        Log.d(TAG, "Starting MQTT Foreground service");

        Intent startIntent = new Intent(this.context, TaskerMqttService.class);
        startIntent.setAction(TaskerMqttConstants.START_SERVICE_ACTION);

        sendMqttServiceAction(startIntent);
    }

    public void stopMqttService(){
        Log.d(TAG, "Stopping MQTT Foreground service");

        Intent stopIntent = new Intent(this.context, TaskerMqttService.class);
        stopIntent.setAction(TaskerMqttConstants.STOP_SERVICE_ACTION);

        sendMqttServiceAction(stopIntent);
    }

    public void checkService(){
        Log.d(TAG, "Checking MQTT Foreground service");

        Intent checkIntent = new Intent(this.context, TaskerMqttService.class);
        checkIntent.setAction((TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION));

        sendMqttServiceAction(checkIntent);
    }

    public void connectToBroker(String profileName){
        Log.d(TAG, "Connecting with profile name = " + profileName);

        Intent connectIntent = new Intent(this.context, TaskerMqttService.class);
        connectIntent.setAction(TaskerMqttConstants.CONNECT_ACTION);
        connectIntent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);

        sendMqttServiceAction(connectIntent);
    }

    public void disconnectFromBroker(String profileName) {
        Log.d(TAG, "Disconnecting with profile name = " + profileName);

        Intent disconnectIntent = new Intent(this.context, TaskerMqttService.class);
        disconnectIntent.setAction(TaskerMqttConstants.DISCONNECT_ACTION);
        disconnectIntent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);

        sendMqttServiceAction(disconnectIntent);
    }

    @Override
    public void runAction(Context context, Bundle data) {
        String action = data.getString(TaskerMqttConstants.ACTION_EXTRA, "");
        switch(action){
            case TaskerMqttConstants.START_SERVICE_ACTION:
                startMqttService();
                break;
            case TaskerMqttConstants.STOP_SERVICE_ACTION:
                stopMqttService();
                break;
            case TaskerMqttConstants.CONNECT_ACTION:
                connectToBroker(data.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, ""));
                break;
            case TaskerMqttConstants.DISCONNECT_ACTION:
                disconnectFromBroker(data.getString(TaskerMqttConstants.PROFILE_NAME_EXTRA, ""));
                break;
            case TaskerMqttConstants.SUBSCRIBE_ACTION:
            case TaskerMqttConstants.UNSUBSCRIBE_ACTION:
            default:
                Log.d(TAG, "Action not implemented (" + action + ")");
                break;
        }
    }
}
