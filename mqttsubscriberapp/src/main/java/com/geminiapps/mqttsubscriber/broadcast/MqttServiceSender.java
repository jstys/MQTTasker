package com.geminiapps.mqttsubscriber.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.geminiapps.mqttsubscriber.tasker.ITaskerActionRunner;

import org.eclipse.paho.android.service.MqttConnectionProfileRecord;
import org.eclipse.paho.android.service.MqttSubscriptionRecord;
import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;
import org.eclipse.paho.android.service.tasker.TaskerMqttService;
import org.eclipse.paho.android.service.tasker.TaskerPlugin;

public class MqttServiceSender implements ITaskerActionRunner, MqttSubscriptionRecord.ISubscriptionRecordListener, MqttConnectionProfileRecord.IConnectionProfileRecordListener{
    private static final String TAG = MqttServiceSender.class.getName();

    private Context context;

    public MqttServiceSender(Context context){
        this.context = context;
        MqttSubscriptionRecord.setSubscriptionRecordListener(this);
        MqttConnectionProfileRecord.setConnectionProfileRecordListener(this);
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

    public void connectToBroker(String profileName, boolean autoReconnect, boolean cleanSession){
        Log.d(TAG, "Connecting with profile name = " + profileName);

        Intent connectIntent = new Intent(this.context, TaskerMqttService.class);
        connectIntent.setAction(TaskerMqttConstants.CONNECT_ACTION);
        connectIntent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);
        connectIntent.putExtra(TaskerMqttConstants.RECONNECT_EXTRA, autoReconnect);
        connectIntent.putExtra(TaskerMqttConstants.CLEAN_SESSION_EXTRA, cleanSession);

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

    public void queryProfilesConnected(String profileName){
        Intent intent = new Intent(this.context, TaskerMqttService.class);
        intent.setAction(TaskerMqttConstants.QUERY_PROFILE_CONNECTED_ACTION);
        if(profileName != null){
            intent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);
        }

        sendMqttServiceAction(intent);
    }

    private void notifyProfileCreated(MqttConnectionProfileRecord record){
        Intent intent = new Intent(this.context, TaskerMqttService.class);
        intent.setAction(TaskerMqttConstants.PROFILE_CREATED_ACTION);
        intent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, record.profileName);

        sendMqttServiceAction(intent);
    }

    private void notifyProfileDeleted(MqttConnectionProfileRecord record){
        Intent intent = new Intent(this.context, TaskerMqttService.class);
        intent.setAction(TaskerMqttConstants.PROFILE_DELETED_ACTION);
        intent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, record.profileName);

        sendMqttServiceAction(intent);
    }

    private void notifySubscriptionCreated(MqttSubscriptionRecord record){
        Intent intent = new Intent(this.context, TaskerMqttService.class);
        intent.setAction(TaskerMqttConstants.SUBSCRIPTION_CREATED_ACTION);
        intent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, record.profileName);
        intent.putExtra(TaskerMqttConstants.TOPIC_FILTER_EXTRA, record.topic);
        intent.putExtra(TaskerMqttConstants.QOS_EXTRA, record.qos);

        sendMqttServiceAction(intent);
    }

    private void notifySubscriptionDeleted(MqttSubscriptionRecord record){
        Intent intent = new Intent(this.context, TaskerMqttService.class);
        intent.setAction(TaskerMqttConstants.SUBSCRIPTION_DELETED_ACTION);
        intent.putExtra(TaskerMqttConstants.PROFILE_NAME_EXTRA, record.profileName);
        intent.putExtra(TaskerMqttConstants.TOPIC_FILTER_EXTRA, record.topic);
        intent.putExtra(TaskerMqttConstants.QOS_EXTRA, record.qos);

        sendMqttServiceAction(intent);
    }

    @Override
    public int runAction(Context context, Bundle data, boolean isOrderedBroadcast, Intent settingIntent) {
        String action = data.getString(TaskerMqttConstants.ACTION_EXTRA, "");
        if(data.containsKey(TaskerMqttConstants.ACTION_EXTRA)){
            data.remove(TaskerMqttConstants.ACTION_EXTRA);
        }

        String completionExtra = settingIntent.getStringExtra(TaskerPlugin.Setting.EXTRA_PLUGIN_COMPLETION_INTENT);
        Intent clonedIntent = settingIntent.cloneFilter();
        clonedIntent.putExtra(TaskerPlugin.Setting.EXTRA_PLUGIN_COMPLETION_INTENT, completionExtra);

        data.putParcelable(TaskerMqttConstants.TASKER_SAVED_SETTING_INTENT, clonedIntent);

        Intent serviceIntent = new Intent(context, TaskerMqttService.class);
        serviceIntent.setAction(action);
        serviceIntent.putExtras(data);
        sendMqttServiceAction(serviceIntent);

        if(isOrderedBroadcast && action.equals(TaskerMqttConstants.CONNECT_ACTION)){
            return TaskerPlugin.Setting.RESULT_CODE_PENDING;
        }
        return TaskerPlugin.Setting.RESULT_CODE_OK;
    }

    @Override
    public void onSubscriptionCreated(MqttSubscriptionRecord record) {
        Log.d(TAG, "Subscription record created for profile = " + record.profileName + " and topic = " + record.topic);

        notifySubscriptionCreated(record);
    }

    @Override
    public void onSubscriptionUpdated(MqttSubscriptionRecord record) {
    }

    @Override
    public void onSubscriptionDeleted(MqttSubscriptionRecord record) {
        Log.d(TAG, "Subscription record deleted for profile = " + record.profileName + " and topic = " + record.topic);

        notifySubscriptionDeleted(record);
    }

    @Override
    public void onConnectionProfileCreated(MqttConnectionProfileRecord record) {
        Log.d(TAG, "Connection profile created for profile = " + record.profileName);

        notifyProfileCreated(record);
    }

    @Override
    public void onConnectionProfileUpdated(MqttConnectionProfileRecord record) {
    }

    @Override
    public void onConnectionProfileDeleted(MqttConnectionProfileRecord record) {
        Log.d(TAG, "Connection profile record deleted for profile = " + record.profileName);

        notifyProfileDeleted(record);
    }
}
