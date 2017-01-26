package com.geminiapps.mqttsubscriber.broadcast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.geminiapps.mqttsubscriber.tasker.ITaskerConditionChecker;
import com.geminiapps.mqttsubscriber.tasker.TaskerBroadcastReceiver;
import com.geminiapps.mqttsubscriber.views.MainActivity;

import org.eclipse.paho.android.service.MqttServiceConstants;
import org.eclipse.paho.android.service.tasker.TaskerMqttConstants;
import org.eclipse.paho.android.service.tasker.TaskerPlugin;

/**
 * Created by jim.stys on 11/1/16.
 */

public class MqttTaskerBroadcastReceiver extends TaskerBroadcastReceiver implements ITaskerConditionChecker {
    @Override
    public void onReceive(Context context, Intent intent) {
        this.actionRunner = new MqttServiceSender(context);
        this.conditionChecker = this;
        super.onReceive(context, intent);

        if(TaskerMqttConstants.OPEN_APP_INTENT.equals(intent.getAction())){
            Intent appIntent = new Intent(context, MainActivity.class);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        }
    }

    @Override
    public void checkCondition(Context context, Bundle data, int messageId) {
        String action = data.getString(TaskerMqttConstants.ACTION_EXTRA, "");
        switch(action){
            case MqttServiceConstants.MESSAGE_ARRIVED_ACTION:
                onTaskerMessageArrived(context, data);
                break;
            default:
                break;
        }

    }

    private void onTaskerMessageArrived(Context context, Bundle data){
        String topic = data.getString(TaskerMqttConstants.TOPIC_EXTRA);
        String topicFilter = data.getString(TaskerMqttConstants.TOPIC_FILTER_EXTRA);
        String taskerTopicFilter = data.getString(TaskerMqttConstants.TASKER_TOPIC_FILTER);
        String taskerProfileName = data.getString(TaskerMqttConstants.TASKER_PROFILE_NAME);
        String profileName = data.getString(MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT);
        String message = data.getString(TaskerMqttConstants.MESSAGE_EXTRA);
        int qos = data.getInt(TaskerMqttConstants.QOS_EXTRA);

        if((taskerProfileName == null || taskerProfileName.equals(profileName)) && (taskerTopicFilter == null || taskerTopicFilter.equals(topicFilter))){
            setResultCode(TASKER_RESULT_CONDITION_SATISFIED);
        }
        else{
            setResultCode(TASKER_RESULT_CONDITION_UNSATISFIED);
        }
    }
}
