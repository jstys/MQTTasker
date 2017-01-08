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

    public void unsubscribeTopic(String profileName, String topic){
        Log.d(TAG, "Unsubscribing from topic = " + topic + " from profile = " + profileName);

        Intent unsubscribeIntent = new Intent(this.context, TaskerMqttService.class);
        unsubscribeIntent.setAction(TaskerMqttConstants.UNSUBSCRIBE_ACTION);
        unsubscribeIntent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);
        unsubscribeIntent.putExtra(TaskerMqttConstants.TOPIC_FILTER_EXTRA, topic);

        sendMqttServiceAction(unsubscribeIntent);
    }

    public void subscribeTopic(String profileName, String topic, int qos){
        Log.d(TAG, "Subscribing to topic = " + topic + " from profile = " + profileName);

        Intent subscribeIntent = new Intent(this.context, TaskerMqttService.class);
        subscribeIntent.setAction(TaskerMqttConstants.SUBSCRIBE_ACTION);
        subscribeIntent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);
        subscribeIntent.putExtra(TaskerMqttConstants.TOPIC_FILTER_EXTRA, topic);
        subscribeIntent.putExtra(TaskerMqttConstants.QOS_EXTRA, qos);

        sendMqttServiceAction(subscribeIntent);
    }

    @Override
    public void runAction(Context context, Bundle data) {
        String action = data.getString(TaskerMqttConstants.ACTION_EXTRA, "");
        if(data.containsKey(TaskerMqttConstants.ACTION_EXTRA)){
            data.remove(TaskerMqttConstants.ACTION_EXTRA);
        }

        Intent serviceIntent = new Intent(this.context, TaskerMqttService.class);
        serviceIntent.setAction(action);
        serviceIntent.putExtras(data);
        sendMqttServiceAction(serviceIntent);
    }
}
