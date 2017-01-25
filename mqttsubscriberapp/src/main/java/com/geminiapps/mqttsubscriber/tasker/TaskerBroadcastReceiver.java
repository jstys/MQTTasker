package com.geminiapps.mqttsubscriber.tasker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.eclipse.paho.android.service.tasker.TaskerPlugin;

public class TaskerBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_INTENT = "com.twofortyfouram.locale.intent.action.FIRE_SETTING";
    public static final String CONDITION_INTENT = "com.twofortyfouram.locale.intent.action.QUERY_CONDITION";
    public static final String TASKER_DATA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";
    public static final int TASKER_RESULT_CONDITION_SATISFIED = 16;
    public static final int TASKER_RESULT_CONDITION_UNSATISFIED = 17;

    protected ITaskerActionRunner actionRunner;
    protected ITaskerConditionChecker conditionChecker;

    // Default constructor allows instantiation directly from AndroidManifest
    // (used here to start the MqttService since it won't have its own broadcast receiver listening)
    public TaskerBroadcastReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        final Bundle bundle = intent.getBundleExtra(TASKER_DATA_BUNDLE);

        if(ACTION_INTENT.equals(intent.getAction()) && this.actionRunner != null)
        {
            // Tasker action triggered
            this.actionRunner.runAction(context, bundle);
        }
        else if(CONDITION_INTENT.equals(intent.getAction()) && this.conditionChecker != null)
        {
            int messageId = TaskerPlugin.Event.retrievePassThroughMessageID(intent);
            Bundle passthroughData = TaskerPlugin.Event.retrievePassThroughData(intent);
            // Tasker condition check triggered
            this.conditionChecker.checkCondition(context, bundle, messageId);
        }
    }
}
