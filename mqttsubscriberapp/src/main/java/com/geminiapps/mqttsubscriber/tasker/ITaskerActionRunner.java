package com.geminiapps.mqttsubscriber.tasker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public interface ITaskerActionRunner {
    int runAction(Context context, Bundle data, boolean isOrderedBroadcast, Intent settingIntent);
}
