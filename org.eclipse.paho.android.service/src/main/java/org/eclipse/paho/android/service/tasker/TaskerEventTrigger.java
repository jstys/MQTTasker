package org.eclipse.paho.android.service.tasker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by jim.stys on 9/23/16.
 */

public class TaskerEventTrigger {
    private static final String EVENT_INTENT =
            "com.twofortyfouram.locale.intent.action.REQUEST_QUERY";

    public static void triggerEvent(Context context, Bundle data)
    {
        Intent eventIntent = new Intent(EVENT_INTENT);
        TaskerPlugin.Event.addPassThroughMessageID(eventIntent);
        TaskerPlugin.Event.addPassThroughData(eventIntent, data);
        //TODO: make the activity name configurable
        eventIntent.putExtra("com.twofortyfouram.locale.intent.extra.ACTIVITY", "com.geminiapps.mqttsubscriber.views.TaskerMessageEventActivity");
        context.sendBroadcast(eventIntent);
    }
}
