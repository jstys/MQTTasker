package org.eclipse.paho.android.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by jim.stys on 9/24/16.
 */

public class MqttServiceStarter implements ITaskerActionRunner {
    private static final String SERVICE_NAME = "org.eclipse.paho.android.service.MqttService";

    @Override
    public void runAction(Context context, Bundle data) {
        String action = data.getString(TaskerMqttConstants.ACTION_EXTRA, null);
        switch(action){
            case TaskerMqttConstants.START_SERVICE_ACTION:
                Intent serviceStartIntent = new Intent();
                serviceStartIntent.setClassName(context, SERVICE_NAME);
                context.startService(serviceStartIntent);
                break;
            default:
                break;
        }
        //TODO: Send result to tasker

    }
}
