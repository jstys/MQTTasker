package com.geminiapps.mqttsubscriber.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.geminiapps.mqttsubscriber.views.TaskerConnectDisconnectActionActivity;
import com.geminiapps.mqttsubscriber.views.TaskerMessageEventActivity;
import com.geminiapps.mqttsubscriber.views.TaskerStartStopServiceActivity;

import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;
import org.eclipse.paho.android.service.tasker.TaskerPlugin;

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
            if(connect){
                if (TaskerPlugin.Setting.hostSupportsSynchronousExecution( this.context.getIntent().getExtras()))
                    //TODO: figure out what the connect timeout should be
                    TaskerPlugin.Setting.requestTimeoutMS( resultIntent, 10000 );
            }
            resultBundle.putString(TaskerMqttConstants.ACTION_EXTRA, action);
            resultBundle.putString(TaskerMqttConstants.PROFILE_NAME_EXTRA, profileName);
            resultBundle.putBoolean(TaskerMqttConstants.RECONNECT_EXTRA, autoReconnect);
            resultBundle.putBoolean(TaskerMqttConstants.CLEAN_SESSION_EXTRA, cleanSession);

            String blurb = buildTaskerBlurb(new String[]{action,
                                                         "Profile = " + profileName});
            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);
            resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", blurb);
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

        TaskerMessageEventActivity activity = (TaskerMessageEventActivity)this.context;
        String selectedProfile = activity.getSelectedProfile();
        String selectedSubscription = activity.getSelectedSubscription();

        resultBundle.putString(TaskerMqttConstants.TASKER_PROFILE_NAME, selectedProfile);
        if(!selectedSubscription.equals(TaskerMessageEventActivity.ANY_SUBSCRIBED_TOPIC)) {
            resultBundle.putString(TaskerMqttConstants.TASKER_TOPIC_FILTER, selectedSubscription);
        }
        resultBundle.putString(TaskerMqttConstants.ACTION_EXTRA, MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", resultBundle);

        String blurb = buildTaskerBlurb(new String[]{"Message Arrived",
                                                     "Profile = " + selectedProfile,
                                                     "Subscription = " + selectedSubscription});
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", blurb);

        if ( TaskerPlugin.hostSupportsRelevantVariables( this.context.getIntent().getExtras() ) )
            TaskerPlugin.addRelevantVariableList( resultIntent, new String [] {
                    "%topic\nTopic\nThe full topic name that the message was published to",
                    "%topic_filter\nTopic Filter\nThe subscription that triggered the arrival of the incoming message",
                    "%profile\nProfile\nThe profile name that the message arrived for",
                    "%message\nMessage\nThe message contents formatted as a string",
                    "%qos\nQOS\nThe qos value of the incoming message (Depends on subscription qos, too)",
                    "%retained\nRetained Flag\nFlag determining if message was retained by the server",
                    "%duplicate\nDuplicate\nServer indicated that the message might be duplicate of one already received"
            } );

        this.context.setResult(RESULT_OK, resultIntent);
    }

    private String buildTaskerBlurb(String[] lines)
    {
        String blurb = "";
        for(String line : lines){
            blurb += line + "\n";
        }
        return blurb;
    }
}
