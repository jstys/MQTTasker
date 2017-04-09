package org.eclipse.paho.android.service.tasker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.eclipse.paho.android.service.MqttServiceConstants;


public class TaskerEventTrigger {
    private static final String EVENT_INTENT =
            "com.twofortyfouram.locale.intent.action.REQUEST_QUERY";

    public static void triggerEvent(Context context, Bundle data, String action)
    {
        Intent eventIntent = new Intent(EVENT_INTENT);
        TaskerPlugin.Event.addPassThroughMessageID(eventIntent);
        TaskerPlugin.Event.addPassThroughData(eventIntent, data);
        if(action.equals(MqttServiceConstants.MESSAGE_ARRIVED_ACTION)) {
            eventIntent.putExtra("com.twofortyfouram.locale.intent.extra.ACTIVITY", "com.geminiapps.mqttsubscriber.views.TaskerMessageEventActivity");
        }
        else if(action.equals(TaskerMqttConstants.CONNECT_ACTION) || action.equals(TaskerMqttConstants.DISCONNECT_ACTION)){
            eventIntent.putExtra("com.twofortyfouram.locale.intent.extra.ACTIVITY", "com.geminiapps.mqttsubscriber.views.TaskerConnectionEventActivity");
        }
        context.sendBroadcast(eventIntent);
    }
}
