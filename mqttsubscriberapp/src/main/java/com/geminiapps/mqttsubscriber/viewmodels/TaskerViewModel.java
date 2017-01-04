package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.views.TaskerConnectDisconnectActionActivity;
import com.geminiapps.mqttsubscriber.views.TaskerStartStopServiceActivity;

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
        String profileName = ((TaskerConnectDisconnectActionActivity)this.context).getSelectedProfileName();
        boolean connect = ((TaskerConnectDisconnectActionActivity)this.context).isConnectAction();

        if(profileName != null) {
            String action = connect ? TaskerMqttConstants.CONNECT_ACTION : TaskerMqttConstants.DISCONNECT_ACTION;
            resultBundle.putString(TaskerMqttConstants.ACTION_EXTRA, action);
            resultBundle.putString(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);

            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", profileName);
            this.context.setResult(RESULT_OK, resultIntent);
        }
        this.context.finish();
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
        this.context.finish();
    }
}
