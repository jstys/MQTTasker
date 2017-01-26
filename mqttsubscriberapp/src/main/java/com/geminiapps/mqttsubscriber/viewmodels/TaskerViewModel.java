package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.views.TaskerConnectDisconnectActionActivity;
import com.geminiapps.mqttsubscriber.views.TaskerStartStopServiceActivity;

import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;

import static android.app.Activity.RESULT_OK;

/**
 * Created by jim.stys on 10/10/16.
 */

public class TaskerViewModel {
    private Activity context;

    public TaskerViewModel(Activity context){
        this.context = context;
    }

    public void saveConnectionActionSettings(){
        Intent resultIntent = new Intent();
        Bundle resultBundle = new Bundle();
        TaskerConnectDisconnectActionActivity activity = ((TaskerConnectDisconnectActionActivity)this.context);
        String profileName = activity.getSelectedProfileName();
        boolean connect = activity.isConnectAction();
        boolean autoReconnect = activity.isAutoReconnectEnabled();
        boolean cleanSession = activity.isCleanSessionEnabled();

        if(profileName != null) {
            String action = connect ? TaskerMqttConstants.CONNECT_ACTION : TaskerMqttConstants.DISCONNECT_ACTION;
            resultBundle.putString(TaskerMqttConstants.ACTION_EXTRA, action);
            resultBundle.putString(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);
            resultBundle.putBoolean(TaskerMqttConstants.RECONNECT_EXTRA, autoReconnect);
            resultBundle.putBoolean(TaskerMqttConstants.CLEAN_SESSION_EXTRA, cleanSession);

            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", profileName);
            this.context.setResult(RESULT_OK, resultIntent);
        }
    }

    public void saveServiceActionSettings(){
        Intent resultIntent = new Intent();
        Bundle resultBundle = new Bundle();
        boolean start = ((TaskerStartStopServiceActivity)this.context).isStartAction();

        String action = start ? TaskerMqttConstants.START_SERVICE_ACTION : TaskerMqttConstants.STOP_SERVICE_ACTION;
        String blurb = start ? "Start Service" : "Stop Service";
        resultBundle.putString(TaskerMqttConstants.ACTION_EXTRA, action);

        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", blurb);
        this.context.setResult(RESULT_OK, resultIntent);
    }

    public void saveMessageEventSettings(){
        Intent resultIntent = new Intent();
        Bundle resultBundle = new Bundle();


        //TODO: add the actual profile and topic filter selected
        if(false) {
            resultBundle.putString(TaskerMqttConstants.TASKER_PROFILE_NAME, "test");
        }
        resultBundle.putString(TaskerMqttConstants.ACTION_EXTRA, MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
        resultBundle.putString(TaskerMqttConstants.TASKER_TOPIC_FILTER, "tasker/#");
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", "Message Arrived");

        this.context.setResult(RESULT_OK, resultIntent);
    }
}
