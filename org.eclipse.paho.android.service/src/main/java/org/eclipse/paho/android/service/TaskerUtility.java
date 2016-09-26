package org.eclipse.paho.android.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

/**
 * Created by jim.stys on 9/23/16.
 */

public class TaskerUtility {
    private static final String EVENT_INTENT = "com.twofortyfouram.locale.Intent.ACTION_REQUEST_QUERY";
    private TaskerBroadcastReceiver broadcastReceiver;

    public TaskerUtility(ITaskerActionRunner runner, ITaskerConditionChecker checker)
    {
        broadcastReceiver = new TaskerBroadcastReceiver(runner, checker);
    }

    public void triggerTaskerEvent(Context context, Bundle data)
    {
        Intent eventIntent = new Intent(EVENT_INTENT);
        TaskerPlugin.Event.addPassThroughMessageID(eventIntent);
        TaskerPlugin.Event.addPassThroughData(eventIntent, data);
        context.sendBroadcast(eventIntent);
    }

    public void registerBroadcastReceiver(Context context)
    {
        IntentFilter taskerIntents = new IntentFilter();
        taskerIntents.addAction(TaskerBroadcastReceiver.ACTION_INTENT);
        taskerIntents.addAction(TaskerBroadcastReceiver.CONDITION_INTENT);
        context.registerReceiver(broadcastReceiver, taskerIntents);
    }

    public void unregisterBroadcastReceiver(Context context)
    {
        context.unregisterReceiver(broadcastReceiver);
    }
}
