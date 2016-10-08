package com.geminiapps.mqttsubscriber.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.geminiapps.mqttsubscriber.models.MqttConnectionProfileModel;

import org.eclipse.paho.android.service.TaskerMqttConstants;
import org.eclipse.paho.android.service.TaskerMqttService;

/**
 * Created by jim.stys on 10/5/16.
 */

public class MqttServiceSender {
    private Context context;

    public MqttServiceSender(Context context){
        this.context = context;
    }

    private void sendMqttServiceAction(Intent intent){
        this.context.startService(intent);
    }

    public void startMqttService(){
        Intent startIntent = new Intent(this.context, TaskerMqttService.class);
        startIntent.setAction(TaskerMqttConstants.START_SERVICE_ACTION);

        sendMqttServiceAction(startIntent);
    }

    public void stopMqttService(){
        Intent stopIntent = new Intent(this.context, TaskerMqttService.class);
        stopIntent.setAction(TaskerMqttConstants.STOP_SERVICE_ACTION);

        sendMqttServiceAction(stopIntent);
    }

    public void checkService(){
        Intent checkIntent = new Intent(this.context, TaskerMqttService.class);
        checkIntent.setAction((TaskerMqttConstants.QUERY_SERVICE_RUNNING_ACTION));

        sendMqttServiceAction(checkIntent);
    }

    public void connectToBroker(MqttConnectionProfileModel profile){
        Intent connectIntent = new Intent(this.context, TaskerMqttService.class);
        connectIntent.setAction(TaskerMqttConstants.CONNECT_ACTION);
        connectIntent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profile.getProfileName());

        sendMqttServiceAction(connectIntent);
    }

    public void disconnectFromBroker(MqttConnectionProfileModel profile){
        Intent disconnectIntent = new Intent(this.context, TaskerMqttService.class);
        disconnectIntent.setAction(TaskerMqttConstants.DISCONNECT_ACTION);
        disconnectIntent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profile.getProfileName());

        sendMqttServiceAction(disconnectIntent);
    }
}
