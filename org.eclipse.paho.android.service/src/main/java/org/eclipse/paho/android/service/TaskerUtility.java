package org.eclipse.paho.android.service;

import android.content.Context;
import android.content.Intent;
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

    public void TriggerTaskerEvent(Context context, Bundle data)
    {
        Intent eventIntent = new Intent(EVENT_INTENT);
        TaskerPlugin.Event.addPassThroughMessageID(eventIntent);
        TaskerPlugin.Event.addPassThroughData(eventIntent, data);
        context.sendBroadcast(eventIntent);
    }

    public TaskerBroadcastReceiver getBroadcastReceiver()
    {
        return broadcastReceiver;
    }
}
