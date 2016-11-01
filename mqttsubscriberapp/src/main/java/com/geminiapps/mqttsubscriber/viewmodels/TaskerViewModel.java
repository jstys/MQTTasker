package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.views.TaskerConnectDisconnectActionActivity;

import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;

import static android.app.Activity.RESULT_OK;

/**
 * Created by jim.stys on 10/10/16.
 */

public class TaskerViewModel {
    Activity context;

    public TaskerViewModel(Activity context){
        this.context = context;
    }

    public void saveConnectionActionSettings(){
        Intent resultIntent = new Intent();
        Bundle resultBundle = new Bundle();
        String profileName = ((TaskerConnectDisconnectActionActivity)this.context).getSelectedProfileName();

        if(profileName != null) {
            resultBundle.putString(TaskerMqttConstants.ACTION_EXTRA, TaskerMqttConstants.CONNECT_ACTION);
            resultBundle.putString(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);

            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", profileName);
            this.context.setResult(RESULT_OK, resultIntent);
        }
        Toast.makeText(this.context, "Clicked save", Toast.LENGTH_SHORT).show();
        this.context.finish();
    }
}
