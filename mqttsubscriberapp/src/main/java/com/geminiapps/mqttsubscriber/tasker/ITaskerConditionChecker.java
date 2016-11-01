package com.geminiapps.mqttsubscriber.tasker;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by jim.stys on 9/23/16.
 */

public interface ITaskerConditionChecker {
    public void checkCondition(Context context, Bundle data);
}
